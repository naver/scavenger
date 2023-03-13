package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.InvocationEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface InvocationRepository : DelegatableJdbcRepository<InvocationEntity, Long> {
    fun findAllByCustomerIdAndSignatureHashIn(customerId: Long, signatureHash: List<String>): List<InvocationEntity>

    fun findAllByCustomerId(customerId: Long): List<InvocationEntity>
}
