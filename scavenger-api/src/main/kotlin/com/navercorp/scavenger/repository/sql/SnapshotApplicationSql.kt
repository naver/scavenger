package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class SnapshotApplicationSql : SqlGeneratorSupport() {
    fun insert(): String =
        """
        INSERT INTO
            snapshot_application
            (
                customerId,
                applicationId,
                snapshotId
            )
        VALUES
            (
                :customerId,
                :applicationId,
                :snapshotId
            )
        """.trimIndent()
}
