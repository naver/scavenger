package com.navercorp.scavenger.config

import com.navercorp.scavenger.mcp.ApiKeyAuthInterceptor
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class WebConfigTest {
    @Autowired
    private lateinit var apiKeyAuthInterceptor: ApiKeyAuthInterceptor

    private fun webConfig(mcpEndpoint: String) = WebConfig(
        apiKeyAuthInterceptor = apiKeyAuthInterceptor,
        mcpServerEnabled = true,
        mcpProtocol = "STATELESS",
        mcpType = "SYNC",
        mcpEndpoint = mcpEndpoint,
        mcpCorsEnabled = false,
        mcpCorsAllowedOriginPatterns = listOf("*"),
    )

    @Test
    fun `should refuse an MCP endpoint without a leading slash`() {
        // the MCP route normalizes "mcp" to "/mcp" but the interceptor pattern does not, leaving the route unguarded
        assertThatThrownBy { webConfig("mcp") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("mcp-endpoint")
    }

    @Test
    fun `should accept an MCP endpoint with a leading slash`() {
        assertThatCode { webConfig("/mcp") }.doesNotThrowAnyException()
    }
}
