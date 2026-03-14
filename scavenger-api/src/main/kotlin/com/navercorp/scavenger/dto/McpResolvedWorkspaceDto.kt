package com.navercorp.scavenger.dto

data class McpResolvedWorkspaceDto(
    val found: Boolean,
    val gitUrl: String,
    val workspaces: List<McpResolvedWorkspaceMatchDto> = emptyList(),
    val message: String? = null
)
