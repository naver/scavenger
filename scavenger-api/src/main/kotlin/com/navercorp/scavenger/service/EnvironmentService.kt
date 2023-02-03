package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.EnvironmentDetailDto
import com.navercorp.scavenger.dto.EnvironmentDto
import com.navercorp.scavenger.entity.Environment
import com.navercorp.scavenger.repository.EnvironmentRepository
import com.navercorp.scavenger.repository.InvocationRepository
import com.navercorp.scavenger.repository.JvmRepository
import com.navercorp.scavenger.repository.SnapshotEnvironmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EnvironmentService(
    val environmentRepository: EnvironmentRepository,
    val jvmRepository: JvmRepository,
    val invocationRepository: InvocationRepository,
    val snapshotEnvironmentRepository: SnapshotEnvironmentRepository,
    val snapshotService: SnapshotService
) {
    fun getEnvironments(customerId: Long): List<EnvironmentDto> {
        return environmentRepository.findByCustomerId(customerId).map { EnvironmentDto.from(it) }
    }

    fun getEnvironmentsDetail(customerId: Long): List<EnvironmentDetailDto> {
        val environments = environmentRepository.findByCustomerId(customerId)
        return environments
            .map { environment: Environment ->
                val environmentId = environment.id
                val jvmCount = jvmRepository.countByCustomerIdAndEnvironmentId(customerId, environmentId)
                val invocationCount = invocationRepository.countByCustomerIdAndEnvironmentId(customerId, environmentId)
                val snapshotCount = snapshotEnvironmentRepository.countByCustomerIdAndEnvironmentId(customerId, environmentId)

                EnvironmentDetailDto(
                    id = environmentId,
                    name = environment.name,
                    jvmCount = jvmCount,
                    invocationCount = invocationCount,
                    snapshotCount = snapshotCount,
                    createdAt = environment.createdAt
                )
            }
    }

    @Transactional
    fun deleteEnvironment(customerId: Long, environmentId: Long) {
        checkNotNull(environmentRepository.findByCustomerIdAndId(customerId, environmentId)) { "잘못된 접근" }
        jvmRepository.deleteByCustomerIdAndEnvironmentId(customerId, environmentId)
        invocationRepository.deleteByCustomerIdAndEnvironmentId(customerId, environmentId)
        snapshotEnvironmentRepository.findByCustomerIdAndEnvironmentId(customerId, environmentId)
            .forEach { snapshotId -> snapshotService.deleteSnapshot(customerId, snapshotId) }
        environmentRepository.deleteByCustomerIdAndId(customerId, environmentId)
    }
}
