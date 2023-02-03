package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Invocation
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface InvocationRepository : DelegatableJdbcRepository<Invocation, Long> {
    fun findAllByCustomerIdAndSignatureHashIn(customerId: Long, signatureHash: List<String>): List<Invocation>

    fun findAllByCustomerId(customerId: Long): List<Invocation>
}
