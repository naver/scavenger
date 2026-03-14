package com.navercorp.scavenger.dto

data class McpClassUsageSummaryDto(
    val className: String,
    val totalMethods: Int,
    val usedMethods: Int,
    val unusedMethods: Int,
    val methods: List<McpMethodUsageDto>
)
