package com.navercorp.scavenger.mcp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ScavengerMcpToolsTest {
    @Autowired
    private lateinit var sut: ScavengerMcpTools

    val customerName = "demo"
    val snapshotIdAllDead: Long = 1
    val snapshotIdMixed: Long = 2

    @Test
    fun listCustomers() {
        val result = sut.listCustomers()
        assertThat(result).contains("demo")
    }

    @Test
    fun listApplications() {
        val result = sut.listApplications(customerName)
        assertThat(result).contains("name")
    }

    @Test
    fun listEnvironments() {
        val result = sut.listEnvironments(customerName)
        assertThat(result).contains("id")
    }

    @Test
    fun listSnapshots() {
        val result = sut.listSnapshots(customerName)
        assertThat(result).contains("\"id\"").isNotEqualTo("[]")
    }

    @Test
    fun getSnapshotTree() {
        val result = sut.getSnapshotTree(customerName, snapshotIdAllDead, "")
        assertThat(result).contains("PACKAGE")
    }

    @Test
    fun searchMethods() {
        val result = sut.searchMethods(customerName, snapshotIdAllDead, "MyController")
        assertThat(result).contains("MyController")
    }

    @Test
    fun isMethodUsed() {
        val result = sut.isMethodUsed(customerName, snapshotIdMixed, "hello")
        assertThat(result).contains("isUsed")
    }

    @Test
    fun getDeadCode() {
        val result = sut.getDeadCode(customerName, snapshotIdAllDead, null, null)
        assertThat(result).contains("METHOD")
    }

    @Test
    fun getClassUsageSummary() {
        val result = sut.getClassUsageSummary(customerName, snapshotIdMixed, "com.example.demo.MyController")
        assertThat(result).contains("totalMethods")
    }

    @Test
    fun getPackageUsageSummary() {
        val result = sut.getPackageUsageSummary(customerName, snapshotIdMixed, "com.example.demo")
        assertThat(result).contains("usageRatio")
    }

    @Test
    fun analyzeCleanupCandidates() {
        val result = sut.analyzeCleanupCandidates(customerName, snapshotIdAllDead, null, null)
        assertThat(result).contains("confidence")
    }

    @Test
    fun getUsageOverview() {
        val result = sut.getUsageOverview(customerName)
        assertThat(result).contains("totalMethodsTracked")
    }

    @Test
    fun getSnapshotUsageSummary() {
        val result = sut.getSnapshotUsageSummary(customerName)
        assertThat(result).contains("snapshotId")
    }

    @Test
    fun getMethodsNotInvokedSinceLastDeploy() {
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, null)
        assertThat(result).contains("totalCount")
    }

    @Test
    fun resolveWorkspace() {
        val result = sut.resolveWorkspace("https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo")
        assertThat(result).contains("found")
    }

    @Test
    fun createSnapshot() {
        // Note: not transactional — uses @Transactional in ScavengerMcpService.createSnapshot
        val result = sut.createSnapshot(customerName, "tools-test-snapshot", listOf(1L), listOf(1L), 0L, "")
        assertThat(result).contains("tools-test-snapshot")
    }

    @Test
    fun refreshSnapshot() {
        val result = sut.refreshSnapshot(customerName, snapshotIdAllDead)
        assertThat(result).contains("refreshed")
    }
}
