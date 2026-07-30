package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpStaleMethodDbRow

data class McpStaleMethodsDto(
    val methods: List<McpStaleMethodDto>,
    val nextCursor: String?,
    val limit: Int
)

data class McpStaleMethodDto(
    val signature: String,
    val lastInvokedAtMillis: Long?,
    val knownSinceMillis: Long?
) {
    companion object {
        fun from(row: McpStaleMethodDbRow): McpStaleMethodDto {
            return McpStaleMethodDto(
                signature = row.signature,
                lastInvokedAtMillis = row.lastInvokedAtMillis?.takeIf { it > 0 },
                knownSinceMillis = row.knownSince?.toEpochMilli()
            )
        }
    }
}
