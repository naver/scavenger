package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.InvocationEntity
import com.navercorp.scavenger.param.InvocationUpsertParam
import com.navercorp.scavenger.repository.sql.InvocationSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait
import org.springframework.stereotype.Repository

@Repository
class InvocationDao(
    entityJdbcProvider: EntityJdbcProvider,
    invocationRepository: InvocationRepository
) :
    SingleValueSelectTrait,
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    InvocationRepository by invocationRepository {
    private val sql: InvocationSql = super.sqls(::InvocationSql)

    fun batchUpsertLastSeen(params: List<InvocationUpsertParam>): IntArray {
        if (params.isEmpty()) {
            return IntArray(0)
        }
        val result = batchUpdate(
            sql.updateLastSeen(),
            params.map { beanParameterSource(it) }.toTypedArray()
        )

        val insertParams = params.filterIndexed { index, _ ->
            result[index] == 0
        }
        if (insertParams.isNotEmpty()) {
            batchUpdate(
                sql.insertWithLastSeen(),
                insertParams.map { beanParameterSource(it) }.toTypedArray()
            )
        }
        return result
    }

    fun batchUpsert(params: List<InvocationUpsertParam>): IntArray {
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

    fun deleteAllInvocations(customerId: Long, signatureHashes: List<String>): Int {
        if (signatureHashes.isEmpty()) {
            return 0
        }
        return update(
            sql.deleteAllInvocations(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("signatureHashes", signatureHashes)
        )
    }

    fun hasNotInvokedInvocation(customerId: Long, applicationId: Long, environmentId: Long?): Boolean {
        return selectOne(
            sql.selectFirstNotInvokedInvocation(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("applicationId", applicationId)
                .addValue("environmentId", environmentId),
            InvocationEntity::class.java
        ).isPresent
    }
}
