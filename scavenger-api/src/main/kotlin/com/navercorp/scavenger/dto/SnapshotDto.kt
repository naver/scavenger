package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ApplicationRefEntity
import com.navercorp.scavenger.entity.EnvironmentRefEntity
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
        fun from(snapshotEntity: SnapshotEntity): SnapshotDto {
            return snapshotEntity.run {
                SnapshotDto(
                    checkNotNull(id),
                    customerId,
                    name,
                    createdAt,
                    applications.map { obj: ApplicationRefEntity -> obj.applicationId },
                    environments.map { obj: EnvironmentRefEntity -> obj.environmentId },
                    filterInvokedAtMillis,
                    packages
                )
            }
        }
    }
}
