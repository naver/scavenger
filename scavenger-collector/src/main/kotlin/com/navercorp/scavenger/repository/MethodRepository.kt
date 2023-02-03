package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Method
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface MethodRepository : DelegatableJdbcRepository<Method, Long> {
    fun findAllByCustomerIdAndSignatureHashIn(customerId: Long, signatureHash: List<String>): List<Method>

    fun findByCustomerIdAndSignatureHash(customerId: Long, signatureHash: String): Method?

    fun findAllByCustomerId(customerId: Long): List<Method>
}
