package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class EnvironmentSql : SqlGeneratorSupport() {
    fun upsert(): String =
        """
        INSERT INTO
            environments
            (
                customerId,
                name,
                createdAt,
                enabled
            )
        VALUES
            (
                :customerId,
                :name,
                :createdAt,
                TRUE
            )
        ON DUPLICATE KEY UPDATE
            createdAt = LEAST(createdAt, :createdAt)
        """.trimIndent()
}
