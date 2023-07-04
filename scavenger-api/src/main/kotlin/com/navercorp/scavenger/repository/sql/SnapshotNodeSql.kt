package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class SnapshotNodeSql : SqlGeneratorSupport() {

    fun selectAllExportSnapshotNode(): String =
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
        """.trimIndent()

    fun insert(): String =
        """
        INSERT INTO
            snapshot_nodes 
            (
                snapshotId,
                signature,
                parent,
                usedCount,
                unusedCount,
                lastInvokedAtMillis,
                type,
                customerId
            )
        VALUES
            (
                :snapshotId,
                :signature,
                :parent,
                :usedCount,
                :unusedCount,
                :lastInvokedAtMillis,
                :type,
                :customerId
            )
        """.trimIndent()

    fun selectAllBySignatureContaining(id: Long?): String =
        """
        SELECT
            id,
            type,
            parent,
            usedCount,
            signature,
            snapshotId,
            customerId,
            unusedCount,
            lastInvokedAtMillis
        FROM
            snapshot_nodes
        WHERE
            customerId = :customerId
            AND snapshotId = :snapshotId
            AND signature LIKE concat('%', :signature, '%')
            ${if (id != null) "AND id > :id" else ""}
        ORDER BY id
        LIMIT 30
        """.trimIndent()
}
