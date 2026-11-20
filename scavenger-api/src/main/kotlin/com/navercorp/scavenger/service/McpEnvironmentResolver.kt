package com.navercorp.scavenger.service

import com.navercorp.scavenger.exception.McpException
import com.navercorp.scavenger.repository.EnvironmentRepository
import org.springframework.stereotype.Component

@Component
class McpEnvironmentResolver(
    private val environmentRepository: EnvironmentRepository
) {
    fun resolveEnvironmentId(customerId: Long, env: String?): Long? {
        if (env.isNullOrBlank()) {
            return null
        }
        val environment = environmentRepository.findByCustomerIdAndName(customerId, env)
            ?: throw McpException.invalidArgument(
                message = "Unknown environment: $env",
                hint = "Call list_scopes to discover valid environment names."
            )
        if (!environment.enabled) {
            throw McpException.invalidArgument(
                message = "Environment is disabled: $env",
                hint = "Call list_scopes to discover enabled environments."
            )
        }
        return environment.id
    }
}
