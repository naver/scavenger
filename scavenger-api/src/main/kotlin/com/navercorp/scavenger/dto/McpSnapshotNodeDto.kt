package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.SnapshotNodeEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class McpSnapshotNodeDto(
    val signature: String,
    val type: String?,
    val parent: String,
    val usedCount: Int,
    val unusedCount: Int,
    val lastInvokedAtMillis: Long?,
    val lastInvokedDate: String?
) {
    companion object {
        fun from(entity: SnapshotNodeEntity): McpSnapshotNodeDto {
            return McpSnapshotNodeDto(
                signature = entity.signature,
                type = entity.type,
                parent = entity.parent,
                usedCount = entity.usedCount,
                unusedCount = entity.unusedCount,
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
