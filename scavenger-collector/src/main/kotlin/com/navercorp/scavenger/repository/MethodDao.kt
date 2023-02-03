package com.navercorp.scavenger.repository

import com.navercorp.scavenger.param.MethodUpsertParam
import com.navercorp.scavenger.repository.sql.MethodSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MethodDao(
    entityJdbcProvider: EntityJdbcProvider,
    methodRepository: MethodRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    MethodRepository by methodRepository {
    private val sql: MethodSql = super.sqls(::MethodSql)

    fun batchUpsert(params: List<MethodUpsertParam>): IntArray {
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

    fun updateSetGarbageLastSeenBefore(customerId: Long, lastSeenBeforeMillis: Long): Int =
        update(
            sql.updateSetGarbageLastSeenBefore(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("lastSeenBeforeMillis", lastSeenBeforeMillis)
        )

    fun findAllGarbage(customerId: Long, lastSeenAtMillis: Instant): List<String> {
        return select(
            sql.selectAllGarbage(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("lastSeenAtMillis", lastSeenAtMillis.toEpochMilli()),
            String::class.java
        )
    }

    fun deleteAllMethods(customerId: Long, signatureHashes: List<String>): Int {
        if (signatureHashes.isEmpty()) {
            return 0
        }
        return update(
            sql.deleteAllMethodsAndInvocations(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("signatureHashes", signatureHashes)
        )
    }
}
