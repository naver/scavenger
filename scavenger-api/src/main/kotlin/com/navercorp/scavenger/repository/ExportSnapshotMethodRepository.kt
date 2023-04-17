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
                s.filterInvokedAtMillis,
                s.packages,
                s.status,
                s.excludeAbstract,
                sn.parent,
                sn.signature,
                sn. `type`,
                sn.usedCount,
                sn.unUsedCount,
                sn.lastInvokedAtMillis
            FROM
                snapshots AS s
                JOIN snapshot_nodes AS sn
            WHERE
                s.id = :snapshotId
                AND s.customerId = :customerId
                AND s.id = sn.snapshotId
        """
    )
    fun findSnapshotMethodExport(
        @Param("customerId") customerId: Long,
        @Param("snapshotId") snapshotId: Long,
    ): List<ExportSnapshotMethodEntity>
}
