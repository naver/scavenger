package com.navercorp.scavenger.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "scavenger")
data class IntervalProperties(
    val pollIntervalSeconds: Int,
    val publishIntervalSeconds: Int,
    val retryIntervalSeconds: Int,
)
