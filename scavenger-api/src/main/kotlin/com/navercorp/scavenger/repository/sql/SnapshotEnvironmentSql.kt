package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class SnapshotEnvironmentSql : SqlGeneratorSupport() {
    fun insert(): String =
        """
        INSERT INTO
            snapshot_environment
            (
                customerId,
                environmentId,
                snapshotId
            )
        VALUES
            (
                :customerId,
                :environmentId,
                :snapshotId
            )
        """.trimIndent()
}
