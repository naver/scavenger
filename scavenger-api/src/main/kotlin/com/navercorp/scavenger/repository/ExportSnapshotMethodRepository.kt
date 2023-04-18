package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExportSnapshotMethodRepository : DelegatableJdbcRepository<ExportSnapshotMethodEntity, String> {
    @Query(
        """
             SELECT
                snapshots.filterInvokedAtMillis,
                snapshots.packages,
                snapshots.status,
                snapshots.excludeAbstract,
                snapshot_nodes.parent,
                snapshot_nodes.signature,
                snapshot_nodes. `type`,
                snapshot_nodes.usedCount,
                snapshot_nodes.unUsedCount,
                snapshot_nodes.lastInvokedAtMillis
            FROM
                snapshots
                INNER JOIN snapshot_nodes ON snapshots.id = snapshot_nodes.snapshotId
            WHERE
                snapshots.id = :snapshotId
                AND snapshots.customerId = :customerId
        """
    )
    fun findSnapshotMethodExport(
        @Param("customerId") customerId: Long,
        @Param("snapshotId") snapshotId: Long,
    ): List<ExportSnapshotMethodEntity>
}
