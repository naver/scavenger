package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class McpMethodQuerySql : SqlGeneratorSupport() {

    fun selectMethodUsage(hasEnvironmentId: Boolean): String =
        """
        SELECT
            methods.signature AS signature,
            methods.signatureHash AS signatureHash,
            MAX(CASE WHEN invocations.status = 'INVOKED' THEN 1 ELSE 0 END) AS invokedFlag,
            MAX(invocations.invokedAtMillis) AS lastInvokedAtMillis,
            MAX(invocations.lastSeenAtMillis) AS lastSeenAtMillis,
            methods.createdAt AS knownSince
        FROM
            methods
            LEFT JOIN invocations ON methods.customerId = invocations.customerId
                AND methods.signatureHash = invocations.signatureHash
                ${if (hasEnvironmentId) "AND invocations.environmentId = :environmentId" else ""}
        WHERE
            methods.customerId = :customerId
            AND methods.signature IN (:signatures)
            AND methods.garbage = FALSE
        GROUP BY methods.signature, methods.signatureHash, methods.createdAt
        """.trimIndent()

    fun selectStaleMethods(
        hasEnvironmentId: Boolean,
        hasPrefix: Boolean,
        hasCursor: Boolean,
        hasIdleCutoff: Boolean,
        neverInvokedOnly: Boolean
    ): String =
        """
        SELECT
            methods.signature AS signature,
            methods.signatureHash AS signatureHash,
            MAX(invocations.invokedAtMillis) AS lastInvokedAtMillis,
            methods.createdAt AS knownSince
        FROM
            methods
            -- INNER JOIN mirrors the UI query: methods with no invocation record at all
            -- (e.g. leftovers of a deleted application) are out of staleness scope —
            -- they drop out (conservative), never get misreported as dead
            INNER JOIN invocations ON methods.customerId = invocations.customerId
                AND methods.signatureHash = invocations.signatureHash
        WHERE
            methods.customerId = :customerId
            AND methods.garbage = FALSE
            AND methods.modifiers NOT IN ('public abstract', 'public abstract transient')
            AND methods.declaringType IS NOT NULL
            ${if (hasEnvironmentId) "AND invocations.environmentId = :environmentId" else ""}
            ${if (hasPrefix) "AND methods.declaringType LIKE :prefix ESCAPE '!'" else ""}
            ${if (hasCursor) "AND methods.signatureHash > :cursor" else ""}
        GROUP BY methods.signature, methods.signatureHash, methods.createdAt
        ${havingClause(hasIdleCutoff, neverInvokedOnly)}
        ORDER BY methods.signatureHash
        LIMIT :limit
        """.trimIndent()

    private fun havingClause(hasIdleCutoff: Boolean, neverInvokedOnly: Boolean): String {
        val conditions = buildList {
            if (hasIdleCutoff) {
                add("MAX(invocations.invokedAtMillis) < :idleCutoffMillis")
            }
            if (neverInvokedOnly) {
                add("MAX(CASE WHEN invocations.status = 'INVOKED' THEN 1 ELSE 0 END) = 0")
            }
        }
        return if (conditions.isEmpty()) "" else "HAVING ${conditions.joinToString(" AND ")}"
    }
}
