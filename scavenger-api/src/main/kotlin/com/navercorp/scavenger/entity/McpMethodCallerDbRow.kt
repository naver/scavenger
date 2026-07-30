package com.navercorp.scavenger.entity

data class McpMethodCallerDbRow(
    val callerSignature: String,
    val lastInvokedAtMillis: Long
)
