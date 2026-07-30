package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpMethodCallerDbRow

data class McpMethodCallersDto(
    val signature: String,
    val callers: List<McpMethodCallerDto>,
    val trackingState: CallStackTrackingState
) {
    enum class CallStackTrackingState {
        DATA_AVAILABLE,
        DISABLED_OR_NO_DATA
    }
}

data class McpMethodCallerDto(
    val callerSignature: String,
    val lastInvokedAtMillis: Long
) {
    companion object {
        fun from(row: McpMethodCallerDbRow): McpMethodCallerDto {
            return McpMethodCallerDto(
                callerSignature = row.callerSignature,
                lastInvokedAtMillis = row.lastInvokedAtMillis
            )
        }
    }
}
