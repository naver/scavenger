package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class CodeBaseFingerPrintSql : SqlGeneratorSupport() {
    fun updatePublishedAt(): String =
        """
        UPDATE
            codebase_fingerprints
        SET
            publishedAt = :publishedAt
        WHERE
            customerId = :customerId
            AND id = :id
        """.trimIndent()

    fun deleteAllByCustomerIdAndCodeBaseFingerprintIn(): String =
        """
        DELETE FROM
            codebase_fingerprints
        WHERE
            customerId = :customerId
            AND codeBaseFingerprint IN ( :codeBaseFingerprints )
        """.trimIndent()
}
