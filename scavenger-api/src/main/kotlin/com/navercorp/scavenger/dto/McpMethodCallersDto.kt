package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpCallStackScopeDbRow
import com.navercorp.scavenger.entity.McpMethodCallerDbRow

data class McpMethodCallersDto(
    val signature: String,
    val callers: List<McpMethodCallerDto>,
    val trackingState: CallStackTrackingState,
    val recordedIn: List<RecordedScope>
) {
    enum class CallStackTrackingState {
        DATA_AVAILABLE,
        PARTIAL,
        DISABLED_OR_NO_DATA
    }

    data class RecordedScope(
        val application: String,
        val environment: String,
        val hasCallStackData: Boolean
    ) {
        companion object {
            fun from(row: McpCallStackScopeDbRow): RecordedScope =
                RecordedScope(row.application, row.environment, row.callStackFlag == 1)
        }
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
