package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.SnapshotExportDbRow
import com.navercorp.scavenger.entity.SnapshotNodeEntity
import com.navercorp.scavenger.repository.sql.SnapshotNodeSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait
import org.springframework.stereotype.Repository

@Repository
class SnapshotNodeDao(
    entityJdbcProvider: EntityJdbcProvider,
    snapshotNodeRepository: SnapshotNodeRepository
) : JdbcDaoSupport(entityJdbcProvider), SingleValueSelectTrait, SnapshotNodeRepository by snapshotNodeRepository {
    private val sql: SnapshotNodeSql = super.sqls(::SnapshotNodeSql)

    fun selectAllExportSnapshotNode(
        customerId: Long,
        snapshotId: Long,
        offset: Long,
        size: Long
    ): List<SnapshotExportDbRow> {
        return select(
            sql.selectAllExportSnapshotNode(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId)
                .addValue("offset", offset)
                .addValue("size", size),
            SnapshotExportDbRow::class.java
        )
    }

    fun countAllExportSnapshotNode(
        customerId: Long,
        snapshotId: Long
    ): Long {
        return selectSingleValue(
            sql.countAllExportSnapshotNode(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId),
            Long::class.java
        )
    }

    fun saveAllSnapshotNodes(entities: List<SnapshotNodeEntity>) {
        jdbcOperations.batchUpdate(
            sql.insert(),
            entities.map { beanParameterSource(it) }.toTypedArray()
        )
    }

    fun selectAllBySignatureContaining(
        customerId: Long,
        snapshotId: Long,
        signature: String,
        id: Long? = null
    ): List<SnapshotNodeEntity> {
        return select(
            sql.selectAllBySignatureContaining(id),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId)
                .addValue("signature", signature)
                .addValue("id", id),
            SnapshotNodeEntity::class.java
        )
    }
}
