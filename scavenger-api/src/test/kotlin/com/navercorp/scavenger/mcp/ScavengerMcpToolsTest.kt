package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class ScavengerMcpToolsTest {
    @Autowired
    private lateinit var sut: ScavengerMcpTools

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val customerName = "demo"
    private val snapshotIdAllDead = 1L
    private val snapshotIdMixed = 2L

    @Test
    fun listCustomers() {
        val result = parseJson(sut.listCustomers())

        assertThat(result).anyMatch { it["name"].asText() == customerName }
    }

    @Test
    fun listApplications() {
        val result = parseJson(sut.listApplications(customerName))

        assertThat(result).isNotEmpty
        assertThat(result.first()["name"].asText()).isNotBlank()
        assertThat(result.first()["snapshotCount"].canConvertToLong()).isTrue()
    }

    @Test
    fun listEnvironments() {
        val result = parseJson(sut.listEnvironments(customerName))

        assertThat(result).isNotEmpty
        assertThat(result.first()["id"].canConvertToLong()).isTrue()
    }

    @Test
    fun listSnapshots() {
        val result = parseJson(sut.listSnapshots(customerName))

        assertThat(result).isNotEmpty
        assertThat(result.first()["id"].canConvertToLong()).isTrue()
        assertThat(result.first()["applications"].isArray).isTrue()
    }

    @Test
    fun getSnapshotTree() {
        val result = parseJson(sut.getSnapshotTree(customerName, snapshotIdAllDead, ""))

        assertThat(result).anyMatch { it["type"].asText() == "PACKAGE" }
    }

    @Test
    fun searchMethods() {
        val result = parseJson(sut.searchMethods(customerName, snapshotIdAllDead, "MyController"))

        assertThat(result).allMatch { it["signature"].asText().contains("MyController") }
    }

    @Test
    fun isMethodUsed() {
        val result = parseJson(sut.isMethodUsed(customerName, snapshotIdMixed, "hello"))

        assertThat(result).anyMatch { it["isUsed"].isBoolean }
    }

    @Test
    fun getDeadCode() {
        val result = parseJson(sut.getDeadCode(customerName, snapshotIdAllDead, null, null))

        assertThat(result).allMatch { it["type"].asText() == "METHOD" }
    }

    @Test
    fun getClassUsageSummary() {
        val result = parseObject(sut.getClassUsageSummary(customerName, snapshotIdMixed, "com.example.demo.MyController"))

        assertThat(result["totalMethods"].canConvertToInt()).isTrue()
        assertThat(result["methods"].isArray).isTrue()
    }

    @Test
    fun getPackageUsageSummary() {
        val result = parseObject(sut.getPackageUsageSummary(customerName, snapshotIdMixed, "com.example.demo"))

        assertThat(result["usageRatio"].asText()).isNotBlank()
        assertThat(result["children"].isArray).isTrue()
    }

    @Test
    fun analyzeCleanupCandidates() {
        val result = parseJson(sut.analyzeCleanupCandidates(customerName, snapshotIdAllDead, null, null))

        assertThat(result).isNotEmpty
        assertThat(result.first()["confidence"].asText()).isNotBlank()
    }

    @Test
    fun getUsageOverview() {
        val result = parseObject(sut.getUsageOverview(customerName))

        assertThat(result["summary"]["methodCount"].canConvertToInt()).isTrue()
        assertThat(result["applications"].isArray).isTrue()
    }

    @Test
    fun getSnapshotUsageSummary() {
        val result = parseJson(sut.getSnapshotUsageSummary(customerName))

        assertThat(result).isNotEmpty
        assertThat(result.first()["snapshotId"].canConvertToLong()).isTrue()
    }

    @Test
    fun getMethodsNotInvokedSinceLastDeploy() {
        val result = parseObject(sut.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotIdMixed, null, null))

        assertThat(result["totalCount"].canConvertToInt()).isTrue()
        assertThat(result["methods"].isArray).isTrue()
    }

    @Test
    fun resolveWorkspace() {
        val result = parseObject(sut.resolveWorkspace("https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo"))

        assertThat(result["found"].isBoolean).isTrue()
        assertThat(result["workspaces"].isArray).isTrue()
    }

    @Test
    @Transactional
    fun createSnapshot() {
        val result = parseObject(sut.createSnapshot(customerName, "tools-test-snapshot", listOf(1L), listOf(1L), 0L, ""))

        assertThat(result["name"].asText()).isEqualTo("tools-test-snapshot")
        assertThat(result["applications"].isArray).isTrue()
    }

    @Test
    fun refreshSnapshot() {
        val result = parseObject(sut.refreshSnapshot(customerName, snapshotIdAllDead))

        assertThat(result["status"].asText()).isEqualTo("refreshed")
    }

    private fun parseJson(json: String): List<JsonNode> {
        return objectMapper.readTree(json).toList()
    }

    private fun parseObject(json: String): JsonNode {
        return objectMapper.readTree(json)
    }
}
