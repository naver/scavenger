package com.navercorp.scavenger.dto

import java.time.Instant

data class McpSnapshotUsageSummaryDto(
    val snapshotId: Long,
    val snapshotName: String,
    val createdAt: Instant,
    val packages: String,
    val totalMethods: Long,
    val usedMethods: Long,
    val unusedMethods: Long,
    val usageRatio: String
)
