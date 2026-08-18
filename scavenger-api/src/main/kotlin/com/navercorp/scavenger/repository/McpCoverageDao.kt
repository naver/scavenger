package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.McpCoverageDbRow
import com.navercorp.scavenger.repository.sql.McpCoverageQuerySql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository

@Repository
class McpCoverageDao(
    entityJdbcProvider: EntityJdbcProvider,
) : ExtendedJdbcDaoSupport(entityJdbcProvider) {
    private val sql: McpCoverageQuerySql = super.sqls(::McpCoverageQuerySql)

    fun findCoverage(customerId: Long, environmentId: Long?): List<McpCoverageDbRow> {
        return select(
            sql.selectCoverage(hasEnvironmentId = environmentId != null),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("environmentId", environmentId),
            McpCoverageDbRow::class.java
        )
    }
}
