package com.navercorp.scavenger.entity

import java.time.Instant

data class McpCoverageDbRow(
    val application: String,
    val environment: String,
    val collectingSince: Instant?,
    val agentAliveAt: Instant?,
)
