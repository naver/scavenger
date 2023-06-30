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

    /**
     * publishedAt in jvm is same as lastSeenAt
     */
    fun deleteGarbagePublishedBefore(): String =
        """
        DELETE FROM
            jvms
        WHERE
            customerId = :customerId
            AND publishedAt < :publishedAt
        """.trimIndent()

    fun deleteAllByCustomerIdAndUuids(): String =
        """
        DELETE FROM
            jvms
        WHERE
            customerId = :customerId
            AND uuid IN ( :uuids )
        """.trimIndent()

    fun deleteAllByWithoutAgent(): String =
        """
            DELETE FROM
                jvms
            WHERE
                uuid NOT IN (
                    SELECT
                        jvmUuid
                    FROM
                        agent_state
                )
        """.trimIndent()
}
