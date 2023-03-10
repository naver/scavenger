package com.navercorp.scavenger.repository

import com.navercorp.scavenger.param.JvmUpsertParam
import com.navercorp.scavenger.repository.sql.JvmSql
import com.navercorp.scavenger.util.getFirstKey
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JvmDao(
    entityJdbcProvider: EntityJdbcProvider,
    jvmRepository: JvmRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    JvmRepository by jvmRepository {
    private val sql: JvmSql = super.sqls(::JvmSql)

    fun upsert(param: JvmUpsertParam): Long? {
        val keyHolder = GeneratedKeyHolder()

        update(
            sql.upsert(),
            beanParameterSource(param),
            keyHolder
        )

        return keyHolder.getFirstKey() ?: run {
            findByCustomerIdAndUuid(param.customerId, param.uuid)?.id
        }
    }

    fun deleteGarbagePublishedBefore(customerId: Long, publishedBefore: Instant): Int {
        return update(
            sql.deleteGarbagePublishedBefore(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("publishedAt", publishedBefore)
        )
    }

    fun deleteAllByCustomerIdAndUuids(customerId: Long, uuids: List<String>): Int {
        if (uuids.isEmpty()) {
            return 0
        }
        return update(
            sql.deleteAllByCustomerIdAndUuids(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("uuids", uuids)
        )
    }
}
