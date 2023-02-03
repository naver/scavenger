package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Jvm
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface JvmRepository : DelegatableJdbcRepository<Jvm, Long> {
    fun findByCustomerIdAndUuid(customerId: Long, uuid: String): Jvm?

    fun findAllByCustomerId(customerId: Long): List<Jvm>
}
