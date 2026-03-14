package com.navercorp.scavenger.dto

data class McpSnapshotRefreshResultDto(
    val snapshotId: Long,
    val status: String,
    val message: String
)
