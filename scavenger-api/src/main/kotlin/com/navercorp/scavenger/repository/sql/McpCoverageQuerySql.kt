package com.navercorp.scavenger.repository.sql

import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class McpCoverageQuerySql : SqlGeneratorSupport() {
    // Per environment: collectingSince = when the oldest still-registered JVM started reporting,
    // agentAliveAt = the most recent poll. Over the small jvms / agent_state tables, not invocations.
    // Environment is the granularity a method payload can be related to (methods carry no application).
    fun selectEnvironmentCoverage(hasEnvironmentId: Boolean): String =
        """
        SELECT
            environments.name AS environment,
            MIN(jvms.createdAt) AS collectingSince,
            MAX(agent_state.lastPolledAt) AS agentAliveAt
        FROM
            jvms
            INNER JOIN environments ON environments.customerId = jvms.customerId
                AND environments.id = jvms.environmentId
            LEFT JOIN agent_state ON agent_state.customerId = jvms.customerId
                AND agent_state.jvmUuid = jvms.uuid
        WHERE
            jvms.customerId = :customerId
            ${if (hasEnvironmentId) "AND jvms.environmentId = :environmentId" else ""}
        GROUP BY environments.name
        """.trimIndent()

    // Per (application, environment): the detailed breakdown, served by list_scopes only.
    fun selectScopeCoverage(): String =
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
        GROUP BY applications.name, environments.name
        """.trimIndent()
}
