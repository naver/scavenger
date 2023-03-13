package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CodeBaseFingerprintEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface CodeBaseFingerprintRepository : DelegatableJdbcRepository<CodeBaseFingerprintEntity, Long> {
    fun findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
        customerId: Long,
        applicationId: Long,
        codeBaseFingerprint: String
    ): CodeBaseFingerprintEntity?

    fun findAllByCustomerId(customerId: Long): List<CodeBaseFingerprintEntity>
}
