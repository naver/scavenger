package com.navercorp.scavenger.dto

import java.time.Instant

data class DataFreshness(
    val queryExecutedAt: Instant,
    val lastInvocationSeenAtMillis: Long?
)
