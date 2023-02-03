package com.navercorp.scavenger.repository

import com.navercorp.scavenger.repository.sql.ApplicationSql
import com.navercorp.scavenger.util.getFirstKey
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ApplicationDao(
    entityJdbcProvider: EntityJdbcProvider,
    applicationRepository: ApplicationRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    ApplicationRepository by applicationRepository {
    private val sql: ApplicationSql = super.sqls(::ApplicationSql)

    fun upsert(customerId: Long, name: String, createdAt: Instant): Long? {
        val keyHolder = GeneratedKeyHolder()

        update(
            sql.upsert(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("name", name)
                .addValue("createdAt", createdAt),
            keyHolder
        )

        return keyHolder.getFirstKey() ?: run {
            findByCustomerIdAndName(customerId, name)?.id
        }
    }
}
