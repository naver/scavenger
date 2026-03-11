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

    val customerName = "demo"
    val snapshotIdAllDead: Long = 1 // 5 METHOD nodes, all usedCount=0
    val snapshotIdMixed: Long = 2 // hello(used), getMyService(used), additional/get/doSth(unused)

    @Test
    fun listCustomers() {
        val result = sut.listCustomers()
        assertThat(result).isNotEmpty
        assertThat(result.map { it["name"] }).contains(customerName)
    }

    @Test
    fun listApplications() {
        val result = sut.listApplications(customerName)
        assertThat(result).isNotEmpty
        assertThat(result.first()).containsKeys("id", "name", "jvmCount", "invocationCount", "createdAt")
    }

    @Test
    fun listEnvironments() {
        val result = sut.listEnvironments(customerName)
        assertThat(result).isNotEmpty
        assertThat(result.first()).containsKeys("id", "name")
    }

    @Test
    fun listSnapshots() {
        val result = sut.listSnapshots(customerName)
        assertThat(result).hasSizeGreaterThanOrEqualTo(2)
        assertThat(result.first()).containsKeys("id", "name", "createdAt", "filterInvokedAtMillis")
    }

    @Test
    fun `getSnapshotTree returns root packages when parent is empty`() {
        val result = sut.getSnapshotTree(customerName, snapshotIdAllDead, "")
        assertThat(result).isNotEmpty
        assertThat(result.map { it["type"] }).contains("PACKAGE")
    }

    @Test
    fun `getSnapshotTree returns children for given parent`() {
        val result = sut.getSnapshotTree(customerName, snapshotIdAllDead, "com.example.demo")
        assertThat(result).isNotEmpty
        assertThat(result.map { it["parent"] }).allMatch { it == "com.example.demo" }
    }

    @Test
    fun searchMethods() {
        val result = sut.searchMethods(customerName, snapshotIdAllDead, "MyController")
        assertThat(result).isNotEmpty
        assertThat(result.map { it["signature"] as String }).allMatch { it.contains("MyController") }
    }

    @Test
    fun `isMethodUsed returns used status for each matched method`() {
        val result = sut.isMethodUsed(customerName, snapshotIdMixed, "hello")
        assertThat(result).isNotEmpty
        assertThat(result.filter { it["isUsed"] == true }).isNotEmpty
        assertThat(result.first()).containsKeys("signature", "isUsed", "lastInvokedAtMillis", "lastInvokedDate")
    }

    @Test
    fun `getDeadCode returns dead methods in snapshot`() {
        // snapshot 1 may be refreshed by IntegrationTests — verify non-empty and correct type only
        val result = sut.getDeadCode(customerName, snapshotIdAllDead, null, 100)
        assertThat(result).isNotEmpty
        assertThat(result.map { it["type"] }).allMatch { it == "METHOD" }
    }

    @Test
    fun `getDeadCode returns only unused methods in mixed snapshot`() {
        // snapshotId=2: hello and getMyService are used, the other 3 are dead
        val result = sut.getDeadCode(customerName, snapshotIdMixed, null, 100)
        assertThat(result).hasSize(3)
    }

    @Test
    fun `getDeadCode filters by packagePrefix`() {
        val result = sut.getDeadCode(customerName, snapshotIdAllDead, "com.example.demo.additional", 100)
        assertThat(result).isNotEmpty
        assertThat(result.map { it["signature"] as String }).allMatch { it.startsWith("com.example.demo.additional") }
    }

    @Test
    fun `getClassUsageSummary returns mixed usage for MyController in snapshot 2`() {
        val result = sut.getClassUsageSummary(customerName, snapshotIdMixed, "com.example.demo.MyController")
        assertThat(result["className"]).isEqualTo("com.example.demo.MyController")
        assertThat(result["totalMethods"] as Int).isEqualTo(3)
        assertThat(result["usedMethods"] as Int).isEqualTo(2)
        assertThat(result["unusedMethods"] as Int).isEqualTo(1)
    }

    @Test
    fun `getPackageUsageSummary returns usage ratio and children`() {
        // snapshot 2: 2 used methods in com.example.demo.MyController → non-zero ratio
        val result = sut.getPackageUsageSummary(customerName, snapshotIdMixed, "com.example.demo")
        assertThat(result["packageName"]).isEqualTo("com.example.demo")
        assertThat(result["usageRatio"]).isNotEqualTo("N/A")
        assertThat(result["children"] as List<*>).isNotEmpty
    }

    @Test
    fun `analyzeCleanupCandidates returns candidates with LOW confidence for public methods`() {
        val result = sut.analyzeCleanupCandidates(customerName, snapshotIdAllDead, null, 100)
        assertThat(result).isNotEmpty
        assertThat(result.map { it["confidence"] }).allMatch { it == "LOW" }
        assertThat(result.first()).containsKeys("signature", "className", "visibility", "confidence", "reason")
    }

    @Test
    fun getSnapshotUsageSummary() {
        val result = sut.getSnapshotUsageSummary(customerName)
        assertThat(result).isNotEmpty
        val snapshot1 = result.first { it["snapshotId"] == snapshotIdAllDead }
        assertThat(snapshot1["totalMethods"] as Long).isGreaterThan(0)
        assertThat(result.first()).containsKeys("snapshotId", "snapshotName", "totalMethods", "usedMethods", "unusedMethods", "usageRatio")
    }

    @Test
    fun getUsageOverview() {
        val result = sut.getUsageOverview(customerName)
        assertThat(result["customerName"]).isEqualTo(customerName)
        assertThat(result["totalMethodsTracked"] as Int).isGreaterThan(0)
        assertThat(result["applications"] as List<*>).isNotEmpty
        assertThat(result["environments"] as List<*>).isNotEmpty
    }

    @Test
    fun `getMethodsNotInvokedSinceLastDeploy auto-detects deploy time and returns methods`() {
        // JVM publishedAt ≈ 1646027053948ms; hello and getMyService lastInvokedAt=1646027014967 < publishedAt
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, 100)
        assertThat(result["error"]).isNull()
        assertThat(result["totalCount"] as Int).isEqualTo(2)
        assertThat(result).containsKeys("sinceMillis", "sinceDate", "totalCount", "methods")
    }

    @Test
    fun `getMethodsNotInvokedSinceLastDeploy uses manual sinceMillis when provided`() {
        // sinceMillis set to very small value — no method has lastInvokedAt < 0
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, 0L, 100)
        assertThat(result["totalCount"] as Int).isEqualTo(0)
    }

    @Test
    fun `resolveWorkspaceByGitUrl finds workspace for known GitHub URL`() {
        val url = "https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo"
        val result = sut.resolveWorkspaceByGitUrl(url)
        assertThat(result["found"]).isEqualTo(true)
        assertThat(result["workspaces"] as List<*>).isNotEmpty
    }

    @Test
    fun `resolveWorkspaceByGitUrl returns not found for unknown URL`() {
        val result = sut.resolveWorkspaceByGitUrl("https://unknown-url/repo")
        assertThat(result["found"]).isEqualTo(false)
    }

    @Test
    @Transactional
    fun `createSnapshot creates a new snapshot and returns its metadata`() {
        val result = sut.createSnapshot(customerName, "mcp-test-snapshot", listOf(1L), listOf(1L), 0L, "")
        assertThat(result["name"]).isEqualTo("mcp-test-snapshot")
        assertThat(result["id"]).isNotNull
        assertThat(result).containsKeys("id", "name", "createdAt", "packages")
    }

    @Test
    @Transactional
    fun `refreshSnapshot returns success status`() {
        val result = sut.refreshSnapshot(customerName, snapshotIdAllDead)
        assertThat(result["snapshotId"]).isEqualTo(snapshotIdAllDead)
        assertThat(result["status"]).isEqualTo("refreshed")
    }

    @Test
    @Transactional
    fun `getMethodsNotInvokedSinceLastDeploy returns error map when no JVM data exists`() {
        jvmRepository.deleteByCustomerId(1L)
        val result = sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, 100)
        assertThat(result["error"]).isEqualTo(true)
        assertThat(result["message"] as String).contains("sinceMillis")
    }

    @Test
    @Transactional
    fun `getSnapshotUsageSummary returns NA for snapshot with no method nodes`() {
        snapshotNodeDao.deleteAllByCustomerIdAndSnapshotId(1L, snapshotIdMixed)
        val result = sut.getSnapshotUsageSummary(customerName)
        val emptySnapshot = result.first { it["snapshotId"] == snapshotIdMixed }
        assertThat(emptySnapshot["usageRatio"]).isEqualTo("N/A")
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
        assertThat(result["found"]).isEqualTo(false)
        assertThat(result["message"] as String).contains("default-group")
    }

    @Test
    fun `listApplications throws IllegalArgumentException for nonexistent customer`() {
        assertThatThrownBy { sut.listApplications("nonexistent") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("nonexistent")
    }
}
