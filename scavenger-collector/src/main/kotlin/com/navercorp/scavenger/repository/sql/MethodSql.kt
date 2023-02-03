package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class MethodSql : SqlGeneratorSupport() {
    fun update(): String =
        """
        UPDATE methods
        SET
            visibility = :visibility,
            createdAt = LEAST(createdAt, :createdAt),
            signature = :signature,
            declaringType = :declaringType,
            methodName = :methodName,
            modifiers = :modifiers,
            garbage = :garbage,
            lastSeenAtMillis = GREATEST(COALESCE(lastSeenAtMillis, 0), :lastSeenAtMillis)
        WHERE
            customerId = :customerId
            AND signatureHash = :signatureHash
        """.trimIndent()

    fun insert(): String =
        """
        INSERT IGNORE INTO
            methods
            (
                customerId,
                visibility,
                signature,
                createdAt,
                declaringType,
                methodName,
                modifiers,
                garbage,
                lastSeenAtMillis,
                signatureHash
            )
        VALUES
            (
                :customerId,
                :visibility,
                :signature,
                :createdAt,
                :declaringType,
                :methodName,
                :modifiers,
                :garbage,
                :lastSeenAtMillis,
                :signatureHash
            )
        """.trimIndent()

    fun updateSetGarbageLastSeenBefore(): String =
        """
        UPDATE
            methods
        SET
            garbage = TRUE
        WHERE
            garbage = FALSE
            AND customerId = :customerId
            AND lastSeenAtMillis < :lastSeenBeforeMillis
        """.trimIndent()

    fun deleteAllMethodsAndInvocations(): String =
        """
        DELETE FROM
            methods
        WHERE
            customerId = :customerId
            AND signatureHash in ( :signatureHashes )
            AND garbage = TRUE
        """

    fun selectAllGarbage() =
        """
        SELECT 
            signatureHash
        FROM 
            methods
        WHERE
            customerId = :customerId
            AND lastSeenAtMillis < :lastSeenAtMillis
            AND garbage = TRUE
        """.trimIndent()
}
