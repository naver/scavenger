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
            -- only pairs that still record the method (selectCallStackScopes): application/environment deletion
            -- drops invocations but leaves call_stacks behind
            AND EXISTS (
                SELECT 1 FROM invocations
                WHERE invocations.customerId = :customerId
                    AND invocations.applicationId = call_stacks.applicationId
                    AND invocations.environmentId = call_stacks.environmentId
                    AND invocations.signatureHash = call_stacks.signatureHash
            )
        GROUP BY caller_methods.signature
        ORDER BY caller_methods.signature
        """.trimIndent()

    // One row per (application, environment) that records the method, flagged by whether that pair has any
    // call-stack row: callStackTraceMode is a per-agent switch, so an env-wide check would vouch for an
    // untracked application. The EXISTS probes the ix_call_stacks_identity prefix (customerId, applicationId, environmentId).
    fun selectCallStackScopes(hasEnvironmentId: Boolean): String =
        """
        SELECT
            applications.name AS application,
            environments.name AS environment,
            CASE WHEN EXISTS (
                SELECT 1 FROM call_stacks
                WHERE call_stacks.customerId = :customerId
                    AND call_stacks.applicationId = invocations.applicationId
                    AND call_stacks.environmentId = invocations.environmentId
            ) THEN 1 ELSE 0 END AS callStackFlag
        FROM
            invocations
            INNER JOIN applications ON applications.customerId = invocations.customerId
                AND applications.id = invocations.applicationId
            INNER JOIN environments ON environments.customerId = invocations.customerId
                AND environments.id = invocations.environmentId
        WHERE
            invocations.customerId = :customerId
            AND invocations.signatureHash = :signatureHash
            ${if (hasEnvironmentId) "AND invocations.environmentId = :environmentId" else ""}
        ORDER BY applications.name, environments.name
        """.trimIndent()
}
