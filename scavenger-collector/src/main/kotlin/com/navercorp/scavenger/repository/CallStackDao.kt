package com.navercorp.scavenger.repository

import com.navercorp.scavenger.param.CallStackUpsertParam
import com.navercorp.scavenger.repository.sql.CallStackSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class CallStackDao(
    entityJdbcProvider: EntityJdbcProvider,
    callStackRepository: CallStackRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    CallStackRepository by callStackRepository {
    private val sql: CallStackSql = super.sqls(::CallStackSql)

    fun batchUpsert(params: List<CallStackUpsertParam>): IntArray {
        if (params.isEmpty()) {
            return IntArray(0)
        }
        val result = batchUpdate(
            sql.update(),
            params.map { beanParameterSource(it) }.toTypedArray()
        )

        val insertParams = params.filterIndexed { index, _ ->
            result[index] == 0
        }
        if (insertParams.isNotEmpty()) {
            batchUpdate(
                sql.insert(),
                insertParams.map { beanParameterSource(it) }.toTypedArray()
            )
        }
        return result
    }
}
