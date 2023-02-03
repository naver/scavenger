package com.navercorp.scavenger.param

import java.time.Instant

data class JvmUpsertParam(
    val customerId: Long,
    val applicationId: Long,
    val applicationVersion: String,
    val environmentId: Long,
    val uuid: String,
    val codeBaseFingerprint: String,
    val createdAt: Instant,
    val publishedAt: Instant,
    val hostname: String,
)
