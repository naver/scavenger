package com.navercorp.scavenger.dto

import com.navercorp.scavenger.entity.ApplicationEntity
import com.navercorp.scavenger.entity.EnvironmentEntity

data class McpScopesDto(
    val environments: List<Environment>,
    val applications: List<Application>,
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
}
