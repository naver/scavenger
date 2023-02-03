package com.navercorp.scavenger.param

class InvocationUpsertParam(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,
    val signatureHash: String,
    val status: String,
    val invokedAtMillis: Long,
    val lastSeenAtMillis: Long? = null
)
