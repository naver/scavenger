package com.navercorp.scavenger.dto

// lastInvocationSeenAtMillis (MAX over invocations) is deferred: it is an unindexed shard scan on every
// response and needs a production EXPLAIN gate before it ships. queryExecutedAtMillis is always populated.
data class McpDataFreshness(
    val queryExecutedAtMillis: Long,
    val lastInvocationSeenAtMillis: Long? = null,
)
