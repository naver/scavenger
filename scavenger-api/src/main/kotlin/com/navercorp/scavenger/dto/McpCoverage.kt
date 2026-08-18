package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpCoverageDbRow

// Anti-false-dead metadata: how far back data goes (collectingSince) and whether an agent is still
// reporting (agentAliveAt) for each (application, environment). Without it, "no invocations" can be
// misread as "dead" when collection just started or the agent stopped reporting.
data class McpCoverage(
    val application: String,
    val environment: String,
    val collectingSinceMillis: Long?,
    val agentAliveAtMillis: Long?,
) {
    companion object {
        fun from(row: McpCoverageDbRow): McpCoverage =
            McpCoverage(
                application = row.application,
                environment = row.environment,
                collectingSinceMillis = row.collectingSince?.toEpochMilli(),
                agentAliveAtMillis = row.agentAliveAt?.toEpochMilli(),
            )
    }
}
