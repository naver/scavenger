package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.EnvironmentRefEntity
import com.navercorp.scavenger.repository.sql.SnapshotEnvironmentSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.stereotype.Repository

@Repository
class SnapshotEnvironmentDao(
    entityJdbcProvider: EntityJdbcProvider,
    snapshotEnvironmentRepository: SnapshotEnvironmentRepository
) : JdbcDaoSupport(entityJdbcProvider), SnapshotEnvironmentRepository by snapshotEnvironmentRepository {
    private val sql: SnapshotEnvironmentSql = super.sqls(::SnapshotEnvironmentSql)

    fun insertAll(params: Set<EnvironmentRefEntity>): IntArray {
        return jdbcOperations.batchUpdate(
            sql.insert(),
            params.map { beanParameterSource(it) }.toTypedArray()
        )
    }
}
