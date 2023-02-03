package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Environment
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface EnvironmentRepository : DelegatableJdbcRepository<Environment, Long> {
    fun findByCustomerIdAndName(customerId: Long, name: String): Environment?

    fun findByCustomerIdAndId(customerId: Long, id: Long): Environment?
}
