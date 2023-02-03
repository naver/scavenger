package com.navercorp.scavenger.dto

data class SummaryDto(
    val methodCount: Int,
    val licenseKey: String,
    val snapshotLimit: Int,
    val collectorServerUrl: String
)
