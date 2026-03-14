package com.navercorp.scavenger.mcp

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScavengerMcpConfiguration {
    @Bean
    fun scavengerToolCallbackProvider(mcpTools: ScavengerMcpTools): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(mcpTools)
            .build()
    }
}
