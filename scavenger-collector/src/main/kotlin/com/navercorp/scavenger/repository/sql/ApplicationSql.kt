package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class ApplicationSql : SqlGeneratorSupport() {
    fun upsert(): String =
        """
        INSERT INTO
            applications
            (
                customerId,
                name,
                createdAt
            )
        VALUES
            (
                :customerId,
                :name,
                :createdAt
            )
        ON DUPLICATE KEY UPDATE
            createdAt = LEAST(createdAt, :createdAt)
        """.trimIndent()
}
