package com.navercorp.scavenger.dto

data class InvocationImportDto(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,

    val invocations: List<String> = emptyList(),
    val invokedAtMillis: Long,
) {
    override fun toString(): String =
        "InvocationImportDto(customerId=$customerId, applicationId=$applicationId, " +
            "environmentId=$environmentId)"
}
