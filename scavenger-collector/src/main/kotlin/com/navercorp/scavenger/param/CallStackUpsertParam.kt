package com.navercorp.scavenger.param

data class CallStackUpsertParam(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,
    val signatureHash: String,
    val callerSignatureHash: String,
    val invokedAtMillis: Long,
)
