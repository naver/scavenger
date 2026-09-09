package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.McpCoverageDbRow
import com.navercorp.scavenger.entity.McpScopeCoverageDbRow
import com.navercorp.scavenger.repository.sql.McpCoverageQuerySql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class McpCoverageDao(
    entityJdbcProvider: EntityJdbcProvider,
) : ExtendedJdbcDaoSupport(entityJdbcProvider) {
    private val sql: McpCoverageQuerySql = super.sqls(::McpCoverageQuerySql)

    fun findEnvironmentCoverage(customerId: Long, environmentId: Long?): List<McpCoverageDbRow> {
        return select(
            sql.selectEnvironmentCoverage(hasEnvironmentId = environmentId != null),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("environmentId", environmentId),
            McpCoverageDbRow::class.java
        )
    }

    fun findScopeCoverage(customerId: Long): List<McpScopeCoverageDbRow> {
        return select(
            sql.selectScopeCoverage(),
            mapParameterSource()
                .addValue("customerId", customerId),
            McpScopeCoverageDbRow::class.java
        )
    }
}
