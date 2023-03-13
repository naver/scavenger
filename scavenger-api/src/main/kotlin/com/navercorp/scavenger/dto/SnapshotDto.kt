package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.SnapshotEntity
import java.time.Instant

data class SnapshotDto(
    val id: Long,
    val customerId: Long,
    val name: String,
    val createdAt: Instant,
    val applications: List<Long>,
    val environments: List<Long>,
    val filterInvokedAtMillis: Long,
    val packages: String
) {

    companion object {
        fun from(entity: SnapshotEntity): SnapshotDto {
            return SnapshotDto(
                entity.id,
                entity.customerId,
                entity.name,
                entity.createdAt,
                entity.applications.map { it.applicationId },
                entity.environments.map { it.environmentId },
                entity.filterInvokedAtMillis,
                entity.packages
            )
        }
    }
}
