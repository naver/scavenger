package com.navercorp.scavenger.repository

import com.navercorp.scavenger.repository.sql.EnvironmentSql
import com.navercorp.scavenger.util.getFirstKey
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class EnvironmentDao(
    entityJdbcProvider: EntityJdbcProvider,
    environmentRepository: EnvironmentRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    EnvironmentRepository by environmentRepository {
    private val sql: EnvironmentSql = super.sqls(::EnvironmentSql)

    fun upsert(
        customerId: Long,
        name: String,
        createdAt: Instant,
    ): Long? {
        val keyHolder = GeneratedKeyHolder()
        val processedName = if (name.trim().isEmpty()) {
            DEFAULT_ENVIRONMENT_NAME
        } else {
            name
        }

        update(
            sql.upsert(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("name", processedName)
                .addValue("createdAt", createdAt),
            keyHolder
        )

        return keyHolder.getFirstKey() ?: run {
            findByCustomerIdAndName(customerId, processedName)?.id
        }
    }

    companion object {
        private const val DEFAULT_ENVIRONMENT_NAME = "<default>"
    }
}
