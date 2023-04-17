package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity

data class ExportSnapshotMethodDto(
    val filterInvokedAtMillis: String,
    val packages: String,
    val status: String,
    val excludeAbstract: String,
    val parent: String,
    val signature: String,
    val type: String,
    val usedCount: String,
    val unusedCount: String,
    val lastInvokedAtMillis: String
) {
    companion object {
        fun from(entity: ExportSnapshotMethodEntity): ExportSnapshotMethodDto {
            return ExportSnapshotMethodDto(
                entity.filterInvokedAtMillis.toString(),
                entity.packages ?: "",
                entity.status ?: "",
                entity.excludeAbstract ?: "",
                entity.parent ?: "",
                entity.signature,
                entity.type ?: "",
                entity.usedCount.toString(),
                entity.unusedCount.toString(),
                entity.lastInvokedAtMillis.toString()
            )
        }
    }
}
