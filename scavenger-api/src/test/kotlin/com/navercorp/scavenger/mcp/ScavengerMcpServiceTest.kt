package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.entity.GithubMappingEntity
import com.navercorp.scavenger.repository.CustomerRepository
import com.navercorp.scavenger.repository.GithubMappingRepository
import com.navercorp.scavenger.repository.JvmRepository
import com.navercorp.scavenger.repository.SnapshotNodeDao
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class ScavengerMcpServiceTest {
    @Autowired
    private lateinit var sut: ScavengerMcpService

    @Autowired
    private lateinit var jvmRepository: JvmRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var githubMappingRepository: GithubMappingRepository

    @Autowired
    private lateinit var snapshotNodeDao: SnapshotNodeDao

    private val customerName = "demo"
    private val snapshotIdAllDead = 1L
    private val snapshotIdMixed = 2L

    @Test
    fun listCustomers() {
        val result = sut.listCustomers()

        assertThat(result).isNotEmpty
        assertThat(result.map { it.name }).contains(customerName)
    }

    @Test
    fun listApplications() {
        val result = sut.listApplications(customerName)

        assertThat(result).isNotEmpty
        assertThat(result.first().name).isNotBlank()
        assertThat(result.first().jvmCount).isNotNegative
        assertThat(result.first().snapshotCount).isNotNegative
    }

    @Test
    fun listEnvironments() {
        val result = sut.listEnvironments(customerName)

        assertThat(result).isNotEmpty
        assertThat(result.first().name).isNotBlank()
        assertThat(result.first().snapshotCount).isNotNegative
    }

    @Test
    fun listSnapshots() {
        val result = sut.listSnapshots(customerName)

        assertThat(result).hasSizeGreaterThanOrEqualTo(2)
        assertThat(result.first().name).isNotBlank()
        assertThat(result.first().applications).isNotNull()
    }

    @Test
    fun `getSnapshotTree returns root packages when parent is empty`() {
        val result = sut.getSnapshotTree(customerName, snapshotIdAllDead, "")

        assertThat(result).isNotEmpty
        assertThat(result.map { it.type }).contains("PACKAGE")
    }

    @Test
    fun `getSnapshotTree returns children for given parent`() {
        val result = sut.getSnapshotTree(customerName, snapshotIdAllDead, "com.example.demo")

        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.parent == "com.example.demo" }
    }

    @Test
    fun searchMethods() {
        val result = sut.searchMethods(customerName, snapshotIdAllDead, "MyController")

        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.signature.contains("MyController") }
    }

    @Test
    fun `isMethodUsed returns used status for each matched method`() {
        val result = sut.isMethodUsed(customerName, snapshotIdMixed, "hello")

        assertThat(result).isNotEmpty
        assertThat(result).anyMatch { it.isUsed }
        assertThat(result.first().lastInvokedDate).isNotBlank()
    }

    @Test
    fun `getDeadCode returns dead methods in snapshot`() {
        val result = sut.getDeadCode(customerName, snapshotIdAllDead, null, 100)

        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.type == "METHOD" }
    }

    @Test
    fun `getDeadCode returns only unused methods in mixed snapshot`() {
        val result = sut.getDeadCode(customerName, snapshotIdMixed, null, 100)

        assertThat(result).hasSize(3)
    }

    @Test
    fun `getDeadCode filters by packagePrefix`() {
        val result = sut.getDeadCode(customerName, snapshotIdAllDead, "com.example.demo.additional", 100)

        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.signature.startsWith("com.example.demo.additional") }
    }

    @Test
    fun `getClassUsageSummary returns mixed usage for MyController in snapshot 2`() {
        val result = sut.getClassUsageSummary(customerName, snapshotIdMixed, "com.example.demo.MyController")

        assertThat(result.className).isEqualTo("com.example.demo.MyController")
        assertThat(result.totalMethods).isEqualTo(3)
        assertThat(result.usedMethods).isEqualTo(2)
        assertThat(result.unusedMethods).isEqualTo(1)
        assertThat(result.methods).hasSize(3)
    }

    @Test
    fun `getPackageUsageSummary returns usage ratio and children`() {
        val result = sut.getPackageUsageSummary(customerName, snapshotIdMixed, "com.example.demo")

        assertThat(result.packageName).isEqualTo("com.example.demo")
        assertThat(result.usageRatio).isNotEqualTo("N/A")
        assertThat(result.children).isNotEmpty
    }

    @Test
    fun `analyzeCleanupCandidates returns candidates with LOW confidence for public methods`() {
        val result = sut.analyzeCleanupCandidates(customerName, snapshotIdAllDead, null, 100)

        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it.confidence == "LOW" }
        assertThat(result.first().signature).isNotBlank()
        assertThat(result.first().reason).isNotBlank()
    }

    @Test
    fun getSnapshotUsageSummary() {
        val result = sut.getSnapshotUsageSummary(customerName)
        val snapshot1 = result.first { it.snapshotId == snapshotIdAllDead }

        assertThat(result).isNotEmpty
        assertThat(snapshot1.totalMethods).isGreaterThan(0)
        assertThat(snapshot1.snapshotName).isNotBlank()
    }

    @Test
    fun getUsageOverview() {
        val result = sut.getUsageOverview(customerName)

        assertThat(result.customerName).isEqualTo(customerName)
        assertThat(result.summary.methodCount).isGreaterThan(0)
        assertThat(result.applications).isNotEmpty
        assertThat(result.environments).isNotEmpty
    }

    @Test
    fun `getMethodsNotInvokedSinceLastDeploy auto-detects deploy time and returns methods`() {
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, 100)

        assertThat(result.totalCount).isEqualTo(2)
        assertThat(result.sinceMillis).isNotNull()
        assertThat(result.methods).hasSize(2)
    }

    @Test
    fun `getMethodsNotInvokedSinceLastDeploy uses manual sinceMillis when provided`() {
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, 0L, 100)

        assertThat(result.totalCount).isEqualTo(0)
    }

    @Test
    fun `resolveWorkspaceByGitUrl finds workspace for known GitHub URL`() {
        val result = sut.resolveWorkspaceByGitUrl("https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo")

        assertThat(result.found).isTrue()
        assertThat(result.workspaces).isNotEmpty
        assertThat(result.workspaces.first().customer.name).isEqualTo(customerName)
    }

    @Test
    fun `resolveWorkspaceByGitUrl returns not found for unknown URL`() {
        val result = sut.resolveWorkspaceByGitUrl("https://unknown-url/repo")

        assertThat(result.found).isFalse()
    }

    @Test
    @Transactional
    fun `createSnapshot creates a new snapshot and returns its metadata`() {
        val result = sut.createSnapshot(customerName, "mcp-test-snapshot", listOf(1L), listOf(1L), 0L, "")

        assertThat(result.name).isEqualTo("mcp-test-snapshot")
        assertThat(result.id).isNotNull()
        assertThat(result.applications).contains(1L)
    }

    @Test
    @Transactional
    fun `refreshSnapshot returns success status`() {
        val result = sut.refreshSnapshot(customerName, snapshotIdAllDead)

        assertThat(result.snapshotId).isEqualTo(snapshotIdAllDead)
        assertThat(result.status).isEqualTo("refreshed")
    }

    @Test
    @Transactional
    fun `getMethodsNotInvokedSinceLastDeploy throws when no JVM data exists`() {
        jvmRepository.deleteByCustomerId(1L)

        assertThatThrownBy { sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, 100) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("sinceMillis")
    }

    @Test
    @Transactional
    fun `getSnapshotUsageSummary returns NA for snapshot with no method nodes`() {
        snapshotNodeDao.deleteAllByCustomerIdAndSnapshotId(1L, snapshotIdMixed)

        val result = sut.getSnapshotUsageSummary(customerName)
        val emptySnapshot = result.first { it.snapshotId == snapshotIdMixed }

        assertThat(emptySnapshot.usageRatio).isEqualTo("N/A")
    }

    @Test
    @Transactional
    fun `resolveWorkspaceByGitUrl returns not found when mapping exists but customer is in different group`() {
        val otherCustomer = customerRepository.save(
            CustomerEntity(name = "other-customer", licenseKey = "test-key", groupId = "other-group")
        )
        githubMappingRepository.save(
            GithubMappingEntity(customerId = otherCustomer.id, basePackage = "com.other", url = "https://other-group-url/repo")
        )

        val result = sut.resolveWorkspaceByGitUrl("https://other-group-url/repo")

        assertThat(result.found).isFalse()
        assertThat(result.message).contains("default-group")
    }

    @Test
    fun `listApplications throws IllegalArgumentException for nonexistent customer`() {
        assertThatThrownBy { sut.listApplications("nonexistent") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("nonexistent")
    }
}
