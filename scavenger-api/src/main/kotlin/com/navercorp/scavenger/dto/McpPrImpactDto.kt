package com.navercorp.scavenger.dto

data class McpPrImpactDto(
    val methods: List<McpMethodUsageDto>,
    val unknownSignatures: List<String>
)
