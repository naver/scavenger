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
}
