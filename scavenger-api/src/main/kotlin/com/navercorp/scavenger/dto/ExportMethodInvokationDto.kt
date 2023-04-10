package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ExportMethodInvocationEntity

data class ExportMethodInvokationDto(
    val id: Long,
    val modifiers: String,
    val signature: String,
    val createdAt: String,
    val lastSeenAtMillis: String,
    val invokedAtMillis: String,
    val status: String,
    val timestamp: String
) {
    companion object {
        fun from(entity: ExportMethodInvocationEntity): ExportMethodInvokationDto {
            return ExportMethodInvokationDto(
                entity.id,
                entity.modifiers,
                entity.signature,
                entity.createdAt.toString(),
                entity.lastSeenAtMillis.toString(),
                entity.invokedAtMillis.toString(),
                entity.status,
                entity.timestamp.toString()
            )
        }
    }
}
