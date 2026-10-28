package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpMethodUsageDbRow

data class McpMethodUsageDto(
    val signature: String,
    val usage: UsageState,
    val lastInvokedAtMillis: Long?,
    val lastSeenAtMillis: Long?,
    val observedInScopeSinceMillis: Long?,
    val knownSinceMillis: Long?
) {
    enum class UsageState {
        INVOKED,
        NOT_INVOKED,
        NOT_OBSERVED_IN_SCOPE
    }

    companion object {
        fun from(row: McpMethodUsageDbRow): McpMethodUsageDto {
            return McpMethodUsageDto(
                signature = row.signature,
                usage = when {
                    row.observedRows == 0L -> UsageState.NOT_OBSERVED_IN_SCOPE
                    row.invokedFlag == 1 -> UsageState.INVOKED
                    else -> UsageState.NOT_INVOKED
                },
                lastInvokedAtMillis = row.lastInvokedAtMillis?.takeIf { it > 0 },
                lastSeenAtMillis = row.lastSeenAtMillis,
                observedInScopeSinceMillis = row.observedInScopeSince?.toEpochMilli(),
                knownSinceMillis = row.knownSince?.toEpochMilli()
            )
        }
    }
}
