package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.AgentEntity
import com.navercorp.scavenger.repository.AgentRepository
import org.springframework.stereotype.Service

@Service
class AgentService(val agentRepository: AgentRepository) {
    fun getAgents(customerId: Long): List<AgentEntity> {
        return agentRepository.findAllAgentsByCustomerId(customerId)
    }
}
