package com.navercorp.scavenger.dto

data class SnapshotMethodUsageSummaryDto(
    val snapshotId: Long,
    val usedMethodCount: Long,
    val unusedMethodCount: Long,
    val totalMethodCount: Long
)
