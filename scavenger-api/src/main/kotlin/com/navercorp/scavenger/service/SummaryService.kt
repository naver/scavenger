package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.SummaryDto
import com.navercorp.scavenger.repository.CustomerRepository
import com.navercorp.scavenger.repository.MethodRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SummaryService(
    val methodRepository: MethodRepository,
    val customerRepository: CustomerRepository,
    @Value("\${scavenger.collector-server-url:}") val collectorServerUrl: String
) {

    fun getSummaryByCustomerId(customerId: Long): SummaryDto {
        val customer = customerRepository.findById(customerId).orElseThrow()
        val methodCount = methodRepository.countMethodSignatureHashByCustomerId(customerId)

        return SummaryDto(
            methodCount = methodCount,
            licenseKey = customer.licenseKey,
            snapshotLimit = SnapshotService.SNAPSHOT_LIMIT,
            collectorServerUrl = collectorServerUrl
        )
    }
}
