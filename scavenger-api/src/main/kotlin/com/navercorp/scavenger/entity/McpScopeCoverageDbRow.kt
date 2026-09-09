package com.navercorp.scavenger.entity

import java.time.Instant

data class McpScopeCoverageDbRow(
    val application: String,
    val environment: String,
    val collectingSince: Instant?,
    val agentAliveAt: Instant?,
)
