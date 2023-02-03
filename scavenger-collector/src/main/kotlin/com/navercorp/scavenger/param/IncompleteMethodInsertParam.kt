package com.navercorp.scavenger.param

import java.time.Instant

class IncompleteMethodInsertParam(
    val customerId: Long,
    val createdAt: Instant,
    val hash: String,
    val signatureHash: String,
)
