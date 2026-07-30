package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.McpMethodUsageDbRow
import com.navercorp.scavenger.entity.McpStaleMethodDbRow
import com.navercorp.scavenger.repository.sql.McpMethodQuerySql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class McpMethodQueryDao(
    entityJdbcProvider: EntityJdbcProvider,
) : ExtendedJdbcDaoSupport(entityJdbcProvider) {
    private val sql: McpMethodQuerySql = super.sqls(::McpMethodQuerySql)

    fun findMethodUsage(
        customerId: Long,
        signatures: List<String>,
        environmentId: Long?,
    ): List<McpMethodUsageDbRow> {
        return select(
            sql.selectMethodUsage(hasEnvironmentId = environmentId != null),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("signatures", signatures)
                .addValue("environmentId", environmentId),
            McpMethodUsageDbRow::class.java
        )
    }

    fun findStaleMethods(
        customerId: Long,
        environmentId: Long?,
        prefix: String?,
        cursor: String?,
        idleCutoffMillis: Long?,
        neverInvokedOnly: Boolean,
        limit: Int,
    ): List<McpStaleMethodDbRow> {
        return select(
            sql.selectStaleMethods(
                hasEnvironmentId = environmentId != null,
                hasPrefix = prefix != null,
                hasCursor = cursor != null,
                hasIdleCutoff = idleCutoffMillis != null,
                neverInvokedOnly = neverInvokedOnly
            ),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("environmentId", environmentId)
                .addValue("prefix", prefix)
                .addValue("cursor", cursor)
                .addValue("idleCutoffMillis", idleCutoffMillis)
                .addValue("limit", limit),
            McpStaleMethodDbRow::class.java
        )
    }
}
