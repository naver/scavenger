package com.navercorp.scavenger.dto

data class McpPackageUsageSummaryDto(
    val packageName: String,
    val totalUsed: Int,
    val totalUnused: Int,
    val usageRatio: String,
    val children: List<McpSnapshotNodeDto>
)
