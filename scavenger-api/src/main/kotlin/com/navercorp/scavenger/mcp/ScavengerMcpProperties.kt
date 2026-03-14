package com.navercorp.scavenger.mcp

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scavenger.mcp")
data class ScavengerMcpProperties(
    val defaultGroupId: String = "default-group",
    val allowedOrigins: List<String> = listOf("*")
)
