package com.navercorp.scavenger.mcp

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

// Kill switch + CORS default-off: the endpoint and its auth stay, only the tools (and CORS) go away.
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["scavenger.mcp.enabled=false", "scavenger.mcp.cors.enabled=false"],
)
class McpDisabledIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    private val rest = TestRestTemplate()

    private fun mcp(body: String, licenseKey: String? = CUSTOMER_1_KEY): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
            licenseKey?.let { set(McpAuthContext.HEADER_LICENSE_KEY, it) }
        }
        return rest.exchange(
            "http://localhost:$port/scavenger/mcp",
            HttpMethod.POST,
            HttpEntity(body, headers),
            String::class.java,
        )
    }

    @Test
    fun `tools list is empty when the kill switch is off`() {
        val response = mcp("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains(""""tools":[]""").doesNotContain("is_method_used")
    }

    @Test
    fun `tool calls fail at the protocol level instead of executing`() {
        val call = """{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"is_method_used","arguments":{"signature":"x"}}}"""

        val response = mcp(call)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("Tool not found").doesNotContain(""""ok":""")
    }

    @Test
    fun `license key is still required`() {
        val response = mcp("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""", licenseKey = null)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.body).contains("AUTH_MISSING")
    }

    @Test
    fun `CORS preflight is rejected when CORS is not enabled`() {
        val headers = HttpHeaders().apply {
            set(HttpHeaders.ORIGIN, "https://inspector.example")
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        }
        val response = rest.exchange(
            "http://localhost:$port/scavenger/mcp",
            HttpMethod.OPTIONS,
            HttpEntity<Void>(headers),
            String::class.java,
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.headers.accessControlAllowOrigin).isNull()
    }

    companion object {
        private const val CUSTOMER_1_KEY = "4c94e0dd-ad04-4b17-9238-f46bba75c684"
    }
}
