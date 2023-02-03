package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ApplicationRef
import com.navercorp.scavenger.entity.EnvironmentRef
import com.navercorp.scavenger.entity.Snapshot
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
        fun from(snapshot: Snapshot): SnapshotDto {
            return snapshot.run {
                SnapshotDto(
                    checkNotNull(id),
                    customerId,
                    name,
                    createdAt,
                    applications.map { obj: ApplicationRef -> obj.applicationId },
                    environments.map { obj: EnvironmentRef -> obj.environmentId },
                    filterInvokedAtMillis,
                    packages
                )
            }
        }
    }
}
