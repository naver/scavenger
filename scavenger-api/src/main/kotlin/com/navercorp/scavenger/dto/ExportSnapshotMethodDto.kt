package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity
import java.time.Instant

data class ExportSnapshotMethodDto(
    val filterInvokedAtMillis: Instant?,
    val packages: String,
    val status: String,
    val excludeAbstract: Int?,
    val parent: String,
    val signature: String,
    val type: String,
    val usedCount: Int,
    val unusedCount: Int,
    val lastInvokedAtMillis: Instant?
) {
    companion object {
        fun from(entity: ExportSnapshotMethodEntity): ExportSnapshotMethodDto {
            return ExportSnapshotMethodDto(
                filterInvokedAtMillis = entity.filterInvokedAtMillis?.let { Instant.ofEpochMilli(it) },
                packages = entity.packages.orEmpty(),
                status = entity.status.orEmpty(),
                excludeAbstract = entity.excludeAbstract,
                parent = entity.parent,
                signature = entity.signature,
                type = entity.type.orEmpty(),
                usedCount = entity.usedCount,
                unusedCount = entity.unusedCount,
                lastInvokedAtMillis = entity.lastInvokedAtMillis?.let { Instant.ofEpochMilli(it) }
            )
        }
    }

    fun toList(): List<String> {
        return listOf(
            this.filterInvokedAtMillis?.toString().orEmpty(),
            this.packages,
            this.status,
            this.excludeAbstract?.toString().orEmpty(),
            this.parent,
            this.signature,
            this.type,
            this.usedCount.toString(),
            this.unusedCount.toString(),
            this.lastInvokedAtMillis?.toString().orEmpty()
        )
    }
}
