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
                filterInvokedAtMillis = entity.filterInvokedAtMillis.toString(),
                packages = entity.packages ?: "",
                status = entity.status ?: "",
                excludeAbstract = entity.excludeAbstract ?: "",
                parent = entity.parent ?: "",
                signature = entity.signature,
                type = entity.type ?: "",
                usedCount = entity.usedCount.toString(),
                unusedCount = entity.unusedCount.toString(),
                lastInvokedAtMillis = entity.lastInvokedAtMillis.toString()
            )
        }
    }
}
