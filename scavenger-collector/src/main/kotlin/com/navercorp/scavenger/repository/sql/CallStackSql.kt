package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class CallStackSql : SqlGeneratorSupport() {
    fun insert() =
        """
        INSERT IGNORE INTO
        call_stacks
        (
            customerId,
            applicationId,
            environmentId,
            signatureHash,
            callerSignatureHash,
            invokedAtMillis
        )
        VALUES
        (
            :customerId,
            :applicationId,
            :environmentId,
            :signatureHash,
            :callerSignatureHash,
            :invokedAtMillis
        )
        """

    fun update(): String =
        """
        UPDATE call_stacks
        SET
            invokedAtMillis = GREATEST(invokedAtMillis, :invokedAtMillis)
        WHERE
            customerId = :customerId
            AND applicationId = :applicationId
            AND environmentId = :environmentId
            AND signatureHash = :signatureHash
            AND callerSignatureHash = :callerSignatureHash
        """
}
