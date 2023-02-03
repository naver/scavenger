package com.navercorp.scavenger.param

import java.time.Instant

class MethodUpsertParam(
    val customerId: Long,
    val visibility: String,
    val signature: String,
    val createdAt: Instant,
    val lastSeenAtMillis: Long,
    val declaringType: String,
    val methodName: String,
    val modifiers: String,
    val garbage: Boolean,
    val signatureHash: String?
)
