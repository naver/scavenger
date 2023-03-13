package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.SnapshotEntity
import com.navercorp.scavenger.repository.sql.SnapshotSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.stereotype.Repository

@Repository
class SnapshotDao(
    entityJdbcProvider: EntityJdbcProvider,
    snapshotRepository: SnapshotRepository
) : JdbcDaoSupport(entityJdbcProvider), SnapshotRepository by snapshotRepository {
    private val sql: SnapshotSql = super.sqls(::SnapshotSql)

    fun updateSnapshot(snapshotEntity: SnapshotEntity) {
        jdbcOperations.update(
            sql.update(),
            beanParameterSource(snapshotEntity)
        )
    }
}
