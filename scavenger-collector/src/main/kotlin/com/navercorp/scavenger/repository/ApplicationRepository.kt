package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ApplicationEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : DelegatableJdbcRepository<ApplicationEntity, Long> {
    fun findByCustomerIdAndName(customerId: Long, name: String): ApplicationEntity?
}
