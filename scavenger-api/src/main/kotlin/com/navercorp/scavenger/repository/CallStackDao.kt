package com.navercorp.scavenger.repository

import com.navercorp.scavenger.repository.sql.CallStackSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class CallStackDao(
    entityJdbcProvider: EntityJdbcProvider,
) : ExtendedJdbcDaoSupport(entityJdbcProvider) {
    private val sql: CallStackSql = super.sqls(::CallStackSql)

    fun findCallerSignatures(
        customerId: Long,
        applicationIds: List<Long>,
        environmentIds: List<Long>,
        signature: String,
        invokedAtMillis: Long?,
    ): List<String> {
        return select(
            sql.selectCallerSignatures(
                customerId,
                applicationIds,
                environmentIds,
                signature,
                invokedAtMillis
            ),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("applicationIds", applicationIds)
                .addValue("environmentIds", environmentIds)
                .addValue("signature", signature)
                .addValue("invokedAtMillis", invokedAtMillis),
            String::class.java
        )
    }
}
