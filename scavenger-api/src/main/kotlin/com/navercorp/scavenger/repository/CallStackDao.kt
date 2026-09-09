package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CallStackSignatureDbRow
import com.navercorp.scavenger.entity.McpMethodCallerDbRow
import com.navercorp.scavenger.repository.sql.CallStackSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class CallStackDao(
    entityJdbcProvider: EntityJdbcProvider,
) : ExtendedJdbcDaoSupport(entityJdbcProvider) {
    private val sql: CallStackSql = super.sqls(::CallStackSql)

    fun findAllCallStacks(
        customerId: Long,
        applicationIds: List<Long>,
        environmentIds: List<Long>,
        invokedAtMillis: Long?,
    ): List<CallStackSignatureDbRow> {
        return select(
            sql.selectAllCallStacks(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("applicationIds", applicationIds)
                .addValue("environmentIds", environmentIds)
                .addValue("invokedAtMillis", invokedAtMillis),
            CallStackSignatureDbRow::class.java
        )
    }

    fun findCallersBySignatureHash(
        customerId: Long,
        signatureHash: String,
        environmentId: Long?,
    ): List<McpMethodCallerDbRow> {
        return select(
            sql.selectCallersBySignatureHash(hasEnvironmentId = environmentId != null),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("signatureHash", signatureHash)
                .addValue("environmentId", environmentId),
            McpMethodCallerDbRow::class.java
        )
    }

    fun existsAnyCallStack(customerId: Long, environmentId: Long?): Boolean {
        return selectOne(
            sql.selectExistsAnyCallStack(hasEnvironmentId = environmentId != null),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("environmentId", environmentId),
            Long::class.java
        ).orElse(0L) > 0L
    }
}
