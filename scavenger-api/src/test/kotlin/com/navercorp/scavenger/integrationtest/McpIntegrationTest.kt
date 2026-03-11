package com.navercorp.scavenger.integrationtest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpIntegrationTest {

    @LocalServerPort
    var port: Int? = null

    private fun mcpUrl() = "http://localhost:$port/scavenger/mcp"

    @Test
    fun `OPTIONS request to mcp endpoint returns 200 with CORS headers`() {
        val headers = HttpHeaders()
        headers.set("Origin", "http://localhost:5173")
        headers.set("Access-Control-Request-Method", "POST")

        val response = TestRestTemplate().exchange(
            mcpUrl(),
            HttpMethod.OPTIONS,
            HttpEntity<Void>(headers),
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers["Access-Control-Allow-Origin"]).contains("*")
        assertThat(response.headers["Access-Control-Allow-Methods"]?.first()).contains("POST")
    }

    @Test
    fun `POST request to mcp endpoint includes CORS headers in response`() {
        val headers = HttpHeaders()
        headers.set("Origin", "http://localhost:5173")
        headers.set("Content-Type", "application/json")

        val response = TestRestTemplate().exchange(
            mcpUrl(),
            HttpMethod.POST,
            HttpEntity("{}", headers),
            String::class.java
        )

        assertThat(response.headers["Access-Control-Allow-Origin"]).contains("*")
    }
}
