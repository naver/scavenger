package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.EnvironmentEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface EnvironmentRepository : DelegatableJdbcRepository<EnvironmentEntity, Long> {
    fun findByCustomerIdAndName(customerId: Long, name: String): EnvironmentEntity?

    fun findByCustomerIdAndId(customerId: Long, id: Long): EnvironmentEntity?
}
