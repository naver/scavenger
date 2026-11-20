package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ApplicationEntity
import com.navercorp.scavenger.entity.EnvironmentEntity
import com.navercorp.scavenger.entity.McpScopeCoverageDbRow

data class McpScopesDto(
    val environments: List<Environment>,
    val applications: List<Application>,
    val coverage: List<Coverage>,
) {
    data class Environment(
        val id: Long,
        val name: String,
        val enabled: Boolean,
        val registeredAtMillis: Long,
    ) {
        companion object {
            fun from(entity: EnvironmentEntity): Environment =
                Environment(entity.id, entity.name, entity.enabled, entity.createdAt.toEpochMilli())
        }
    }

    data class Application(
        val id: Long,
        val name: String,
        val registeredAtMillis: Long,
    ) {
        companion object {
            fun from(entity: ApplicationEntity): Application =
                Application(entity.id, entity.name, entity.createdAt.toEpochMilli())
        }
    }

    data class Coverage(
        val application: String,
        val environment: String,
        val collectingSinceMillis: Long?,
        val agentAliveAtMillis: Long?,
    ) {
        companion object {
            fun from(row: McpScopeCoverageDbRow): Coverage =
                Coverage(
                    application = row.application,
                    environment = row.environment,
                    collectingSinceMillis = row.collectingSince?.toEpochMilli(),
                    agentAliveAtMillis = row.agentAliveAt?.toEpochMilli(),
                )
        }
    }
}
