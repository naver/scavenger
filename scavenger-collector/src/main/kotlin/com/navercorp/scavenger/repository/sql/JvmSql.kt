package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class JvmSql : SqlGeneratorSupport() {
    fun upsert(): String =
        """
        INSERT INTO
            jvms
            (
                customerId,
                applicationId,
                applicationVersion,
                environmentId,
                uuid,
                codeBaseFingerprint,
                createdAt,
                publishedAt,
                hostname
            )
        VALUES
            (
                :customerId,
                :applicationId,
                :applicationVersion,
                :environmentId,
                :uuid,
                :codeBaseFingerprint,
                :createdAt,
                :publishedAt,
                :hostname
            )
        ON DUPLICATE KEY UPDATE
            codeBaseFingerprint = :codeBaseFingerprint,
            publishedAt = :publishedAt
        """.trimIndent()

    fun deleteAllByCustomerIdAndUuids(): String =
        """
        DELETE FROM
            jvms
        WHERE
            customerId = :customerId
            AND uuid IN ( :uuids )
        """.trimIndent()

    fun selectAllUuidsByWithoutAgent(): String =
        """
        SELECT uuid FROM
             jvms
        WHERE
             customerId = :customerId
             AND uuid NOT IN (
                 SELECT
                     jvmUuid
                 FROM
                     agent_state
             )
        LIMIT 10000
        """.trimIndent()
}
