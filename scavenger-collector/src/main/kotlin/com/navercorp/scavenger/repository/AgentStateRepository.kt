package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.AgentState
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentStateRepository : DelegatableJdbcRepository<AgentState, Long> {

    fun findAllByCustomerId(customerId: Long): List<AgentState>
}
