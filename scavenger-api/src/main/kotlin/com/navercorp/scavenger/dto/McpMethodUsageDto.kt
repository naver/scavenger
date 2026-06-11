package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpMethodUsageDbRow

data class McpMethodUsageDto(
    val signature: String,
    val used: Boolean,
    val lastInvokedAtMillis: Long?,
    val lastSeenAtMillis: Long?,
    val knownSinceMillis: Long?
) {
    companion object {
        fun from(row: McpMethodUsageDbRow): McpMethodUsageDto {
            return McpMethodUsageDto(
                signature = row.signature,
                used = row.invokedFlag == 1,
                lastInvokedAtMillis = row.lastInvokedAtMillis?.takeIf { it > 0 },
                lastSeenAtMillis = row.lastSeenAtMillis,
                knownSinceMillis = row.knownSince?.toEpochMilli()
            )
        }
    }
}
