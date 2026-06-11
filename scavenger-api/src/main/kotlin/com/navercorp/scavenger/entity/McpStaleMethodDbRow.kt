package com.navercorp.scavenger.entity

import java.time.Instant

data class McpStaleMethodDbRow(
    val signature: String,
    val signatureHash: String,
    val lastInvokedAtMillis: Long?,
    val knownSince: Instant?
)
