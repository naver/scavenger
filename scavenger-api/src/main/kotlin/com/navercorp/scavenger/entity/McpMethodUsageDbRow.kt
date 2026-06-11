package com.navercorp.scavenger.entity

import java.time.Instant

data class McpMethodUsageDbRow(
    val signature: String,
    val signatureHash: String,
    val invokedFlag: Int?,
    val lastInvokedAtMillis: Long?,
    val lastSeenAtMillis: Long?,
    val knownSince: Instant?
)
