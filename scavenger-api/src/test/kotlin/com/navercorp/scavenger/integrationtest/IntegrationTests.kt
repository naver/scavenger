package com.navercorp.scavenger.integrationtest

import com.navercorp.scavenger.controller.SnapshotController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import kotlin.random.Random

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests {

    @LocalServerPort
    var port: Int? = null
    var customerId = "1"
    var snapshotId = "1"
    var parent = "com.example.demo"

    private fun getNewName(): String {
        return Random.nextInt(0, Int.MAX_VALUE).toString()
    }

    private fun `Assert GET status code`(vararg urls: String) {
        urls.forEach {
            val entity: ResponseEntity<String> = TestRestTemplate().exchange(
                "http://localhost:$port/scavenger$it",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String::class.java
            )
            assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    private fun `Assert DELETE status code`(vararg urls: String) {
        urls.forEach {
            val entity: ResponseEntity<String> = TestRestTemplate().exchange(
                "http://localhost:$port/scavenger$it",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String::class.java
            )
            assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    private fun `Assert POST status code`(url: String, body: Any?) {
        val entity: ResponseEntity<String> = TestRestTemplate().exchange(
            "http://localhost:$port/scavenger$url",
            HttpMethod.POST,
            HttpEntity<Any>(body, LinkedMultiValueMap()),
            String::class.java
        )
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Assert AgentController, status code`() = `Assert GET status code`("/api/customers/$customerId/agents")

    @Test
    fun `Assert ApplicationController, status code`() =
        `Assert GET status code`("/api/customers/$customerId/applications")

    @Test
    fun `Assert EnvironmentController, status code`() =
        `Assert GET status code`("/api/customers/$customerId/environments")

    @Test
    fun `Assert SnapshotController, status code`() = run {

        `Assert GET status code`(
            "/api/customers/$customerId/snapshots", "/api/customers/$customerId/snapshots/$snapshotId?parent=$parent"
        )
        `Assert POST status code`(
            "/api/customers/$customerId/snapshots",
            SnapshotController.CreateSnapshotRequestParams(
                name = getNewName(),
                applicationIdList = listOf(1L),
                environmentIdList = listOf(1L),
                filterInvokedAtMillis = 0,
                "**.*Controller.**"
            )
        )
        `Assert POST status code`("/api/customers/$customerId/snapshots/$snapshotId/refresh", null)
        `Assert DELETE status code`("/api/customers/$customerId/snapshots/3")
    }

    @Test
    fun `Assert SummaryController, status code`() = `Assert GET status code`("/api/customers/$customerId/summary")
}
