package com.navercorp.scavenger.dto

data class McpMeta(
    val coverage: List<McpCoverage>,
    val dataFreshness: McpDataFreshness,
)
