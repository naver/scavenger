package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity

data class ExportSnapshotMethodDto(
    val filterInvokedAtMillis: Long,
    val packages: String?,
    val status: String?,
    val excludeAbstract: String?,
    val parent: String?,
    val signature: String,
    val type: String?,
    val usedCount: Int,
    val unusedCount: Int,
    val lastInvokedAtMillis: Long?
) {
    companion object {
        fun from(entity: ExportSnapshotMethodEntity): ExportSnapshotMethodDto {
            return ExportSnapshotMethodDto(
                entity.filterInvokedAtMillis,
                entity.packages,
                entity.status,
                entity.excludeAbstract,
                entity.parent,
                entity.signature,
                entity.type,
                entity.usedCount,
                entity.unusedCount,
                entity.lastInvokedAtMillis
            )
        }
    }
}
