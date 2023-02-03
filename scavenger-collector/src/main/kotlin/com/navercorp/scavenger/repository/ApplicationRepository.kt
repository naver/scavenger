package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Application
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : DelegatableJdbcRepository<Application, Long> {
    fun findByCustomerIdAndName(customerId: Long, name: String): Application?
}
