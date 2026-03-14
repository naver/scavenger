package com.navercorp.scavenger.dto

data class McpMethodsNotInvokedSinceLastDeployResultDto(
    val sinceMillis: Long? = null,
    val sinceDate: String? = null,
    val totalCount: Int = 0,
    val methods: List<McpMethodReferenceDto> = emptyList(),
    val error: Boolean = false,
    val message: String? = null
)
