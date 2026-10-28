package com.navercorp.scavenger.config

import com.navercorp.scavenger.mcp.ScavengerMcpTools
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// Ops kill switch: scavenger.mcp.enabled=false removes the tool registration (endpoint stays but exposes
// no tools). Defaults to enabled when the property is absent.
@Configuration
@ConditionalOnProperty(prefix = "scavenger.mcp", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class McpServerConfig {
    @Bean
    fun scavengerMcpToolCallbackProvider(scavengerMcpTools: ScavengerMcpTools): ToolCallbackProvider =
        MethodToolCallbackProvider.builder()
            .toolObjects(scavengerMcpTools)
            .build()
}
