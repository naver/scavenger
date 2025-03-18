package com.navercorp.scavenger.dto

data class CallStackImportDto(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,

    val callTraces: List<CallTrace> = emptyList(),
    val invokedAtMillis: Long,
) {
    data class CallTrace(
        val callee: String,
        val caller: String,
    )
    override fun toString(): String =
        "CallStackImportDto(customerId=$customerId, applicationId=$applicationId, " +
            "environmentId=$environmentId)"
}
