package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.AgentStateEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentStateRepository : DelegatableJdbcRepository<AgentStateEntity, Long> {

    fun findAllByCustomerId(customerId: Long): List<AgentStateEntity>
}
