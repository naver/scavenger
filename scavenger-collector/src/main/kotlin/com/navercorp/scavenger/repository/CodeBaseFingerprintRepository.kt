package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CodeBaseFingerprint
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface CodeBaseFingerprintRepository : DelegatableJdbcRepository<CodeBaseFingerprint, Long> {
    fun findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
        customerId: Long,
        applicationId: Long,
        codeBaseFingerprint: String
    ): CodeBaseFingerprint?

    fun findAllByCustomerId(customerId: Long): List<CodeBaseFingerprint>
}
