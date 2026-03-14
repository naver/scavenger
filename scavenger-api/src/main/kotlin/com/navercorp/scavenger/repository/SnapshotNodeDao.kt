package com.navercorp.scavenger.repository

import com.navercorp.scavenger.dto.SnapshotMethodUsageSummaryDto
import com.navercorp.scavenger.entity.SnapshotExportDbRow
import com.navercorp.scavenger.entity.SnapshotNodeEntity
import com.navercorp.scavenger.repository.sql.SnapshotNodeSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcDaoSupport
import org.springframework.stereotype.Repository

@Repository
class SnapshotNodeDao(
    entityJdbcProvider: EntityJdbcProvider,
    snapshotNodeRepository: SnapshotNodeRepository
) : JdbcDaoSupport(entityJdbcProvider), SnapshotNodeRepository by snapshotNodeRepository {
    private val sql: SnapshotNodeSql = super.sqls(::SnapshotNodeSql)

    fun findAllExportSnapshotNode(
        customerId: Long,
        snapshotId: Long
    ): List<SnapshotExportDbRow> {
        return select(
            sql.selectAllExportSnapshotNode(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId),
            SnapshotExportDbRow::class.java
        )
    }

    fun saveAllSnapshotNodes(entities: List<SnapshotNodeEntity>) {
        jdbcOperations.batchUpdate(
            sql.insert(),
            entities.map { beanParameterSource(it) }.toTypedArray()
        )
    }

    fun findAllUnusedMethodNodes(
        customerId: Long,
        snapshotId: Long,
        signaturePrefix: String? = null,
        limit: Int = 100
    ): List<SnapshotNodeEntity> {
        return select(
            sql.selectAllUnusedMethodNodes(signaturePrefix),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId)
                .addValue("signaturePrefix", signaturePrefix)
                .addValue("limit", limit),
            SnapshotNodeEntity::class.java
        )
    }

    fun countMethodUsageSummary(
        customerId: Long,
        snapshotIds: List<Long>
    ): List<SnapshotMethodUsageSummaryDto> {
        return select(
            sql.countMethodUsageSummary(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotIds", snapshotIds),
            SnapshotMethodUsageSummaryDto::class.java
        )
    }

    fun findMethodsNotInvokedSince(
        customerId: Long,
        snapshotId: Long,
        sinceMillis: Long,
        limit: Int = 100
    ): List<SnapshotNodeEntity> {
        return select(
            sql.selectMethodsNotInvokedSince(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("snapshotId", snapshotId)
                .addValue("sinceMillis", sinceMillis)
                .addValue("limit", limit),
            SnapshotNodeEntity::class.java
        )
    }

    fun findAllBySignatureContaining(
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
