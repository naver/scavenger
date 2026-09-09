package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class CallStackSql : SqlGeneratorSupport() {

    fun selectAllCallStacks(): String =
        """
        SELECT
            callee_methods.signature AS calleeSignature,
            caller_methods.signature AS callerSignature
        FROM
            call_stacks
            INNER JOIN methods callee_methods ON call_stacks.customerId = callee_methods.customerId AND call_stacks.signatureHash = callee_methods.signatureHash
            INNER JOIN methods caller_methods ON call_stacks.customerId = caller_methods.customerId AND call_stacks.callerSignatureHash = caller_methods.signatureHash 
        WHERE
            call_stacks.customerId = :customerId
            AND call_stacks.applicationId IN (:applicationIds)
            AND call_stacks.environmentId IN (:environmentIds)
            AND (:invokedAtMillis IS NULL OR call_stacks.invokedAtMillis >= :invokedAtMillis)
        """.trimIndent()

    fun selectCallersBySignatureHash(hasEnvironmentId: Boolean): String =
        """
        SELECT
            caller_methods.signature AS callerSignature,
            MAX(call_stacks.invokedAtMillis) AS lastInvokedAtMillis
        FROM
            call_stacks
            INNER JOIN methods caller_methods ON call_stacks.customerId = caller_methods.customerId AND call_stacks.callerSignatureHash = caller_methods.signatureHash
        WHERE
            call_stacks.customerId = :customerId
            AND call_stacks.signatureHash = :signatureHash
            ${if (hasEnvironmentId) "AND call_stacks.environmentId = :environmentId" else ""}
        GROUP BY caller_methods.signature
        ORDER BY caller_methods.signature
        """.trimIndent()

    // With an environment the negative case scans the customer's ix_call_stacks_identity entries (covering);
    // call_stacks holds distinct caller→callee edges, not invocations, so that stays bounded.
    fun selectExistsAnyCallStack(hasEnvironmentId: Boolean): String =
        """
        SELECT count(1) FROM (
            SELECT 1 FROM call_stacks
            WHERE customerId = :customerId
            ${if (hasEnvironmentId) "AND environmentId = :environmentId" else ""}
            LIMIT 1
        ) first_row
        """.trimIndent()
}
