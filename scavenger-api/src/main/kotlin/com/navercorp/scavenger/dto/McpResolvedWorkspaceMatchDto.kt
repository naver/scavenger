package com.navercorp.scavenger.dto

data class McpResolvedWorkspaceMatchDto(
    val customer: CustomerDto,
    val basePackages: List<String>
)
