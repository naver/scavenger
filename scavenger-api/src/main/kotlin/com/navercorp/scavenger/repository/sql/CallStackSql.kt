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
}
