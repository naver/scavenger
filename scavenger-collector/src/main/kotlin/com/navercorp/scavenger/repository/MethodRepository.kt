package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.MethodEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface MethodRepository : DelegatableJdbcRepository<MethodEntity, Long> {
    fun findAllByCustomerIdAndSignatureHashIn(customerId: Long, signatureHash: List<String>): List<MethodEntity>

    fun findByCustomerIdAndSignatureHash(customerId: Long, signatureHash: String): MethodEntity?

    fun findAllByCustomerId(customerId: Long): List<MethodEntity>
}
