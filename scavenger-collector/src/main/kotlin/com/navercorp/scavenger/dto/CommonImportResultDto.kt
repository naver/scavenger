package com.navercorp.scavenger.dto

data class CommonImportResultDto(
    val customerId: Long,
    val applicationId: Long,
    val environmentId: Long,
    val jvmId: Long,
    val publishedAtMillis: Long
)
