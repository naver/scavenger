package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ApplicationRefEntity
import com.navercorp.scavenger.repository.sql.SnapshotApplicationSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.stereotype.Repository

@Repository
class SnapshotApplicationDao(
    entityJdbcProvider: EntityJdbcProvider,
    snapshotApplicationRepository: SnapshotApplicationRepository
) : JdbcDaoSupport(entityJdbcProvider), SnapshotApplicationRepository by snapshotApplicationRepository {
    private val sql: SnapshotApplicationSql = super.sqls(::SnapshotApplicationSql)

    fun insertAll(params: Set<ApplicationRefEntity>): IntArray {
        return jdbcOperations.batchUpdate(
            sql.insert(),
            params.map { beanParameterSource(it) }.toTypedArray()
        )
    }
}
