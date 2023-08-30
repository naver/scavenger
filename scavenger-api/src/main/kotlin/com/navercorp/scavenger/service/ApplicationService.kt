package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.ApplicationDetailDto
import com.navercorp.scavenger.dto.ApplicationDto
import com.navercorp.scavenger.entity.ApplicationEntity
import com.navercorp.scavenger.repository.ApplicationRepository
import com.navercorp.scavenger.repository.InvocationRepository
import com.navercorp.scavenger.repository.JvmRepository
import com.navercorp.scavenger.repository.SnapshotApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationService(
    val applicationRepository: ApplicationRepository,
    val jvmRepository: JvmRepository,
    val invocationRepository: InvocationRepository,
    val snapshotApplicationRepository: SnapshotApplicationRepository,
    val snapshotService: SnapshotService
) {
    fun getApplications(customerId: Long): List<ApplicationDto> {
        return applicationRepository.findAllByCustomerId(customerId).map { ApplicationDto.from(it) }
    }

    fun getApplicationsDetail(customerId: Long): List<ApplicationDetailDto> {
        val applications = applicationRepository.findAllByCustomerId(customerId)
        return applications
            .map { applicationEntity: ApplicationEntity ->
                val applicationId = applicationEntity.id
                val jvmCount = jvmRepository.countByCustomerIdAndApplicationId(customerId, applicationId)
                val invocationCount = invocationRepository.countByCustomerIdAndApplicationId(customerId, applicationId)
                val snapshotCount = snapshotApplicationRepository.countByCustomerIdAndApplicationId(customerId, applicationId)

                ApplicationDetailDto(
                    id = applicationId,
                    name = applicationEntity.name,
                    jvmCount = jvmCount,
                    invocationCount = invocationCount,
                    snapshotCount = snapshotCount,
                    createdAt = applicationEntity.createdAt
                )
            }
    }

    @Transactional
    fun deleteApplication(customerId: Long, applicationId: Long) {
        checkNotNull(applicationRepository.findByCustomerIdAndId(customerId, applicationId)) { "잘못된 접근" }
        jvmRepository.deleteByCustomerIdAndApplicationId(customerId, applicationId)
        invocationRepository.deleteByCustomerIdAndApplicationId(customerId, applicationId)
        snapshotApplicationRepository.findAllByCustomerIdAndApplicationId(customerId, applicationId)
            .forEach { snapshotId -> snapshotService.deleteSnapshot(customerId, snapshotId) }
        applicationRepository.deleteByCustomerIdAndId(customerId, applicationId)
    }
}
