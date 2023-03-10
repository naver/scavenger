package com.navercorp.scavenger.controller

import com.navercorp.scavenger.entity.AgentEntity
import com.navercorp.scavenger.service.AgentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AgentController(private val agentService: AgentService) {
    @GetMapping("/customers/{customerId}/agents")
    fun getAgents(@PathVariable customerId: Long): List<AgentEntity> {
        return agentService.getAgents(customerId)
    }
}
