package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class InvocationSql : SqlGeneratorSupport() {

    fun updateLastSeen(): String =
        """
        UPDATE invocations
        SET
            lastSeenAtMillis = :lastSeenAtMillis
        WHERE
            customerId = :customerId
            AND applicationId = :applicationId
            AND environmentId = :environmentId
            AND signatureHash = :signatureHash
        """

    fun insertWithLastSeen(): String =
        """
        INSERT IGNORE INTO
            invocations
            (
                customerId,
                applicationId,
                environmentId,
                signatureHash,
                status,
                invokedAtMillis,
                lastSeenAtMillis
            )
        VALUES
            (
                :customerId,
                :applicationId,
                :environmentId,
                :signatureHash,
                :status,
                :invokedAtMillis,
                :lastSeenAtMillis
            )
        """.trimIndent()

    fun insert() =
        """
        INSERT IGNORE INTO
        invocations
        (
            customerId,
            applicationId,
            environmentId,
            signatureHash,
            status,
            invokedAtMillis
        )
        VALUES
        (
            :customerId,
            :applicationId,
            :environmentId,
            :signatureHash,
            :status,
            :invokedAtMillis
        )
        """

    fun update(): String =
        """
        UPDATE invocations
        SET
            invokedAtMillis = GREATEST(invokedAtMillis, :invokedAtMillis),
            status = :status
        WHERE
            customerId = :customerId
            AND applicationId = :applicationId
            AND environmentId = :environmentId
            AND signatureHash = :signatureHash
        """

    fun deleteAllInvocations(): String =
        """
        DELETE FROM
            invocations
        WHERE
            customerId = :customerId
            AND signatureHash in ( :signatureHashes );
        """

    fun selectFirstNotInvokedInvocation() =
        """
        SELECT 
            *
        FROM
            invocations
        WHERE
            customerId = :customerId
            AND applicationId = :applicationId
            AND environmentId = :environmentId
            AND status <> 'INVOKED' 
        LIMIT 1
        """
}
