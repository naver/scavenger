package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

// End-to-end over the real Spring AI MCP transport (STATELESS/WebMVC). Uses the seeded `local` profile
// data: customer 1 (licenseKey 4c94e0dd...) and customer 2 `demo-second` (11e8e9f2...).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    private val rest = TestRestTemplate()

    private val objectMapper = ObjectMapper()

    // The envelope travels as a JSON string inside result.content[0].text
    private fun envelope(response: ResponseEntity<String>): JsonNode {
        val text = objectMapper.readTree(response.body).path("result").path("content").path(0).path("text").asText()
        return objectMapper.readTree(text)
    }

    private fun mcp(body: String, licenseKey: String? = CUSTOMER_1_KEY): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
            licenseKey?.let { set("X-Scavenger-License-Key", it) }
        }
        return rest.exchange(
            "http://localhost:$port/scavenger/mcp",
            HttpMethod.POST,
            HttpEntity(body, headers),
            String::class.java,
        )
    }

    private fun callTool(name: String, arguments: String, licenseKey: String? = CUSTOMER_1_KEY): ResponseEntity<String> =
        mcp(
            """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"$name","arguments":$arguments}}""",
            licenseKey,
        )

    @Test
    fun `request without a license key is rejected with AUTH_MISSING`() {
        val response = mcp("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""", licenseKey = null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body).contains("AUTH_MISSING")
    }

    @Test
    fun `tools list exposes all five tools`() {
        val response = mcp("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body)
            .contains("list_scopes", "is_method_used", "get_method_callers", "get_stale_methods", "get_pr_impact")
    }

    @Test
    fun `CORS preflight is allowed without a license key`() {
        val headers = HttpHeaders().apply {
            set(HttpHeaders.ORIGIN, "https://inspector.example")
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, McpAuthContext.HEADER_LICENSE_KEY)
        }
        val response = rest.exchange(
            "http://localhost:$port/scavenger/mcp",
            HttpMethod.OPTIONS,
            HttpEntity<Void>(headers),
            String::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.accessControlAllowOrigin).isNotNull()
        assertThat(response.headers.accessControlAllowHeaders).contains(McpAuthContext.HEADER_LICENSE_KEY)
    }

    @Test
    fun `env filter propagates through the transport into the query`() {
        val inTest = envelope(callTool("is_method_used", """{"signature":"$CROSS_ENV_METHOD","env":"test"}""", CUSTOMER_2_KEY))
        val inProd = envelope(callTool("is_method_used", """{"signature":"$CROSS_ENV_METHOD","env":"prod"}""", CUSTOMER_2_KEY))

        assertThat(inTest.path("data").path("used").asBoolean()).isTrue
        assertThat(inProd.path("data").path("used").asBoolean()).isFalse
    }

    @Test
    fun `env filter scopes coverage metadata to that environment`() {
        val body = envelope(callTool("is_method_used", """{"signature":"$SEEDED_METHOD","env":"prod"}"""))

        val coverage = body.path("coverage")
        assertThat(coverage.size()).isEqualTo(1)
        assertThat(coverage[0].path("application").asText()).isEqualTo("demo")
        assertThat(coverage[0].path("environment").asText()).isEqualTo("prod")
    }

    @Test
    fun `envelope omits error on success and data on failure`() {
        val success = envelope(callTool("is_method_used", """{"signature":"$SEEDED_METHOD"}"""))
        val failure = envelope(callTool("is_method_used", """{"signature":"com.nonexistent.Foo.bar()"}"""))

        assertThat(success.path("ok").asBoolean()).isTrue
        assertThat(success.has("error")).isFalse
        assertThat(success.path("dataFreshness").has("queryExecutedAtMillis")).isTrue
        assertThat(failure.path("ok").asBoolean()).isFalse
        assertThat(failure.path("error").path("code").asText()).isEqualTo("METHOD_NOT_FOUND")
        assertThat(failure.has("data")).isFalse
        assertThat(failure.has("coverage")).isFalse
    }

    @Test
    fun `is_method_used resolves a seeded method (auth propagates into the tool)`() {
        val response = callTool("is_method_used", """{"signature":"$SEEDED_METHOD"}""")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains(SEEDED_METHOD).doesNotContain("METHOD_NOT_FOUND")
    }

    @Test
    fun `unknown signature maps to METHOD_NOT_FOUND`() {
        val response = callTool("is_method_used", """{"signature":"com.nonexistent.Foo.bar()"}""")

        assertThat(response.body).contains("METHOD_NOT_FOUND")
    }

    @Test
    fun `list_scopes returns the customer's environments and applications`() {
        val response = callTool("list_scopes", "{}")

        assertThat(response.body).contains("test", "demo")
    }

    @Test
    fun `responses carry coverage and dataFreshness metadata`() {
        val response = callTool("is_method_used", """{"signature":"$SEEDED_METHOD"}""")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body)
            .contains("coverage", "collectingSinceMillis", "agentAliveAtMillis")
            .contains("dataFreshness", "queryExecutedAtMillis")
    }

    @Test
    fun `tenant isolation - customer 2 cannot see customer 1's method`() {
        val response = callTool("is_method_used", """{"signature":"$SEEDED_METHOD"}""", licenseKey = CUSTOMER_2_KEY)

        assertThat(response.body).contains("METHOD_NOT_FOUND")
    }

    companion object {
        private const val CUSTOMER_1_KEY = "4c94e0dd-ad04-4b17-9238-f46bba75c684"
        private const val CUSTOMER_2_KEY = "11e8e9f2-7a5b-4f00-bd1e-1f2a3c4d5e6f"
        private const val SEEDED_METHOD = "com.example.demo.MyController.additional()"

        // customer 2: INVOKED in env 'test', NOT_INVOKED in env 'prod' (test-data-set-1.1.5.sql)
        private const val CROSS_ENV_METHOD = "com.example.demo.legacy.LegacyService.usedInTestOnly()"
    }
}
