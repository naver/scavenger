package com.navercorp.scavenger.dto

data class CoverageScopeDto(
    val application: String,
    val environment: String,
    val environmentEnabled: Boolean,
    val collectingSinceMillis: Long?,
    val agentAliveAtMillis: Long?
)
