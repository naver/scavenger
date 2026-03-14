package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.SnapshotNodeEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class McpMethodReferenceDto(
    val signature: String,
    val className: String,
    val lastInvokedAtMillis: Long?,
    val lastInvokedDate: String?
) {
    companion object {
        fun from(entity: SnapshotNodeEntity): McpMethodReferenceDto {
            return McpMethodReferenceDto(
                signature = entity.signature,
                className = entity.parent,
                lastInvokedAtMillis = entity.lastInvokedAtMillis,
                lastInvokedDate = entity.lastInvokedAtMillis?.toDateString()
            )
        }

        private fun Long.toDateString(): String {
            return Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}
