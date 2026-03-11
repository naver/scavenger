package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.SnapshotNodeEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class SnapshotNodeRepositoryTest {
    @Autowired
    private lateinit var sut: SnapshotNodeDao

    val customerId: Long = 1
    val snapshotId: Long = 1
    val parent = "com.example.demo"

    @Test
    fun findAllByCustomerIdAndSnapshotIdAndParent() {
        assertThat(sut.findAllByCustomerIdAndSnapshotIdAndParent(customerId, snapshotId, parent)).hasSizeGreaterThan(0)
    }

    @Test
    @Transactional
    fun deleteAllByCustomerIdAndSnapshotId() {
        sut.deleteAllByCustomerIdAndSnapshotId(customerId, snapshotId)
        assertThat(sut.findAllByCustomerIdAndSnapshotId(customerId, snapshotId)).hasSize(0)
    }

    @Test
    @Transactional
    fun saveAllSnapshotNodes() {
        val snapshotNodeEntities = listOf(
            SnapshotNodeEntity(
                snapshotId = snapshotId,
                signature = "com.example.demo.additional.AdditionalService.get()",
                usedCount = 33,
                unusedCount = 410,
                parent = "com.example.demo.additional.AdditionalService",
                customerId = customerId,
                lastInvokedAtMillis = null,
                type = "METHOD"
            ),
            SnapshotNodeEntity(
                snapshotId = snapshotId,
                signature = "com.example.demo.additional.AdditionalService.WOW.doSth()",
                usedCount = 33,
                unusedCount = 410,
                parent = "com.example.demo.additional.AdditionalService.WOW",
                customerId = customerId,
                lastInvokedAtMillis = null,
                type = "METHOD"
            )
        )
        sut.saveAllSnapshotNodes(snapshotNodeEntities)

        val actual =
            sut.findAllByCustomerIdAndSnapshotId(customerId, snapshotId).let { it.subList(it.size - 2, it.size) }
        assertThat(actual).extracting("signature").containsExactlyElementsOf(
            listOf(
                "com.example.demo.additional.AdditionalService.get()",
                "com.example.demo.additional.AdditionalService.WOW.doSth()"
            )
        )
    }

    @Test
    fun findAllBySignatureContaining() {
        val result = sut.findAllBySignatureContaining(customerId, snapshotId, parent)
        assertThat(result).hasSizeGreaterThan(0)

        assertThat(sut.findAllBySignatureContaining(customerId, snapshotId, parent, result.last().id)).hasSize(0)

        assertThat(sut.findAllBySignatureContaining(customerId, snapshotId, "hello")).hasSize(1)
    }

    @Test
    fun `findAllUnusedMethodNodes returns dead methods in snapshot`() {
        // snapshot 1 may be refreshed by other tests (IntegrationTests), so we only verify non-empty and correct type
        val result = sut.findAllUnusedMethodNodes(customerId, snapshotId = 1, signaturePrefix = null, limit = 100)
        assertThat(result).isNotEmpty
        assertThat(result.map { it.type }).allMatch { it == "METHOD" }
    }

    @Test
    fun `findAllUnusedMethodNodes returns only unused methods in mixed snapshot`() {
        // snapshotId=2: hello and getMyService are used, 3 methods are unused
        assertThat(sut.findAllUnusedMethodNodes(customerId, snapshotId = 2, signaturePrefix = null, limit = 100)).hasSize(3)
    }

    @Test
    fun `findAllUnusedMethodNodes filters by signaturePrefix`() {
        val result = sut.findAllUnusedMethodNodes(customerId, snapshotId = 2, signaturePrefix = "com.example.demo.additional", limit = 100)
        assertThat(result).isNotEmpty
        assertThat(result.map { it.signature }).allMatch { it.startsWith("com.example.demo.additional") }
    }

    @Test
    fun `countMethodUsageSummary returns per-snapshot usage counts`() {
        val result = sut.countMethodUsageSummary(customerId, listOf(1L, 2L))
        assertThat(result).hasSizeGreaterThanOrEqualTo(2)
        // snapshot 1 may be modified by IntegrationTests refresh — only verify structure
        val summary1 = result.first { it.snapshotId == 1L }
        assertThat(summary1.totalMethodCount).isGreaterThan(0)
        // snapshot 2 is never modified — verify exact counts
        val summary2 = result.first { it.snapshotId == 2L }
        assertThat(summary2.usedMethodCount).isEqualTo(2)
        assertThat(summary2.unusedMethodCount).isEqualTo(3)
        assertThat(summary2.totalMethodCount).isEqualTo(5)
    }

    @Test
    fun `findMethodsNotInvokedSince returns methods invoked before given timestamp`() {
        // hello and getMyService have lastInvokedAt=1646027014967 < JVM publishedAt≈1646027053000
        val result = sut.findMethodsNotInvokedSince(customerId, snapshotId = 2, sinceMillis = 1646027053000L, limit = 100)
        assertThat(result).hasSize(2)
        assertThat(result.map { it.lastInvokedAtMillis }).allMatch { it != null && it < 1646027053000L }
    }

    @Test
    fun `findMethodsNotInvokedSince returns empty when sinceMillis is zero`() {
        assertThat(sut.findMethodsNotInvokedSince(customerId, snapshotId = 2, sinceMillis = 0L, limit = 100)).isEmpty()
    }
}
