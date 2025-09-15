package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CallStackSignatureDbRow
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
}
