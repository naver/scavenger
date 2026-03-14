package com.navercorp.scavenger.dto

data class McpCleanupCandidateDto(
    val signature: String,
    val className: String,
    val visibility: String,
    val confidence: String,
    val reason: String,
    val lastInvokedAtMillis: Long?,
    val lastInvokedDate: String?
)
