package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class McpCoverageQuerySql : SqlGeneratorSupport() {
    // Per (application, environment): collectingSince = when the first agent started reporting,
    // agentAliveAt = the most recent poll. Over the small jvms / agent_state tables, not invocations.
    fun selectCoverage(hasEnvironmentId: Boolean): String =
        """
        SELECT
            applications.name AS application,
            environments.name AS environment,
            MIN(jvms.createdAt) AS collectingSince,
            MAX(agent_state.lastPolledAt) AS agentAliveAt
        FROM
            jvms
            INNER JOIN applications ON applications.customerId = jvms.customerId
                AND applications.id = jvms.applicationId
            INNER JOIN environments ON environments.customerId = jvms.customerId
                AND environments.id = jvms.environmentId
            LEFT JOIN agent_state ON agent_state.customerId = jvms.customerId
                AND agent_state.jvmUuid = jvms.uuid
        WHERE
            jvms.customerId = :customerId
            ${if (hasEnvironmentId) "AND jvms.environmentId = :environmentId" else ""}
        GROUP BY applications.name, environments.name
        """.trimIndent()
}
