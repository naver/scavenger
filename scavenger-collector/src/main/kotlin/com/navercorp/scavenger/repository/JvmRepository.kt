package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.JvmEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface JvmRepository : DelegatableJdbcRepository<JvmEntity, Long> {
    fun findByCustomerIdAndUuid(customerId: Long, uuid: String): JvmEntity?

    fun findAllByCustomerId(customerId: Long): List<JvmEntity>
}
