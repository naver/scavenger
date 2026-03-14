package com.navercorp.scavenger.dto

data class McpMethodsNotInvokedSinceLastDeployResultDto(
    val sinceMillis: Long,
    val sinceDate: String,
    val totalCount: Int,
    val methods: List<McpMethodReferenceDto>
)
