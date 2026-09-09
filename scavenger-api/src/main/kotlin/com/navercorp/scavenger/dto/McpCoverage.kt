package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.McpCoverageDbRow

// Anti-false-dead metadata per environment: how far back the currently registered JVMs go (collectingSince)
// and whether an agent is still reporting (agentAliveAt). Without it, "no invocations" can be misread as
// "dead" when collection just started or the agent stopped reporting. Per environment because a method
// payload cannot be tied to one application; list_scopes carries the per-application breakdown.
data class McpCoverage(
    val environment: String,
    val collectingSinceMillis: Long?,
    val agentAliveAtMillis: Long?,
) {
    companion object {
        fun from(row: McpCoverageDbRow): McpCoverage =
            McpCoverage(
                environment = row.environment,
                collectingSinceMillis = row.collectingSince?.toEpochMilli(),
                agentAliveAtMillis = row.agentAliveAt?.toEpochMilli(),
            )
    }
}
