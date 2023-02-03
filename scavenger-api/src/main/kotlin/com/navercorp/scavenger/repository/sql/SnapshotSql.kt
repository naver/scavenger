package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class SnapshotSql : SqlGeneratorSupport() {
    fun update(): String =
        """
        UPDATE
            snapshots
        SET
            name = :name,
            filterInvokedAtMillis = :filterInvokedAtMillis,
            packages = :packages,
            createdAt = :createdAt
        WHERE
            customerId = :customerId
            AND id = :id
        """.trimIndent()
}
