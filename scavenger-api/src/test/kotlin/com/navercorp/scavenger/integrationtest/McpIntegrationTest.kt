package com.navercorp.scavenger.integrationtest

import com.navercorp.scavenger.config.WebConfig
import com.navercorp.scavenger.mcp.ScavengerMcpProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.config.annotation.CorsRegistry

class McpIntegrationTest {
    @Test
    fun `addCorsMappings registers mcp endpoints with configured origins`() {
        val registry = ExposedCorsRegistry()

        WebConfig(ScavengerMcpProperties(allowedOrigins = listOf("http://localhost:5173")))
            .addCorsMappings(registry)

        val configurations = registry.configurations()

        assertThat(configurations.keys).containsExactlyInAnyOrder("/mcp", "/mcp/**")
        assertThat(configurations["/mcp"]?.allowedOriginPatterns).contains("http://localhost:5173")
        assertThat(configurations["/mcp"]?.allowedMethods).contains("GET", "POST", "DELETE", "OPTIONS")
        assertThat(configurations["/mcp"]?.allowedHeaders).contains("Content-Type", "Accept", "Mcp-Session-Id")
        assertThat(configurations["/mcp"]?.exposedHeaders).contains("Mcp-Session-Id")
        assertThat(configurations["/mcp/**"]?.allowedOriginPatterns).contains("http://localhost:5173")
        assertThat(configurations["/mcp/**"]?.allowedMethods).contains("GET", "POST", "DELETE", "OPTIONS")
    }

    private class ExposedCorsRegistry : CorsRegistry() {
        fun configurations() = getCorsConfigurations()
    }
}
