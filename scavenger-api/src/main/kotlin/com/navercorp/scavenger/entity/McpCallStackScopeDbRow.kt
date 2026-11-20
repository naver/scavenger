package com.navercorp.scavenger.entity

data class McpCallStackScopeDbRow(
    val application: String,
    val environment: String,
    val callStackFlag: Int
)
