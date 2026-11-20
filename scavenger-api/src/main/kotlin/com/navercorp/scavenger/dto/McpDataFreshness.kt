package com.navercorp.scavenger.dto

// lastInvocationSeenAtMillis (MAX over invocations) is intentionally absent: it is an unindexed shard scan on
// every response and needs a production EXPLAIN gate before it ships. Add the field when it can be populated.
data class McpDataFreshness(
    val queryExecutedAtMillis: Long,
)
