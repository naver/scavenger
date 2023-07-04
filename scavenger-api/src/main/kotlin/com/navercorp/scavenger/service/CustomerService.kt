package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CustomerDto
import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.repository.AgentRepository
import com.navercorp.scavenger.repository.ApplicationRepository
import com.navercorp.scavenger.repository.CodeBaseFingerprintRepository
import com.navercorp.scavenger.repository.CustomerRepository
import com.navercorp.scavenger.repository.EnvironmentRepository
import com.navercorp.scavenger.repository.GithubMappingRepository
import com.navercorp.scavenger.repository.InvocationRepository
import com.navercorp.scavenger.repository.JvmRepository
import com.navercorp.scavenger.repository.MethodRepository
import com.navercorp.scavenger.repository.SnapshotApplicationRepository
import com.navercorp.scavenger.repository.SnapshotEnvironmentRepository
import com.navercorp.scavenger.repository.SnapshotNodeRepository
import com.navercorp.scavenger.repository.SnapshotRepository
import mu.KotlinLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class CustomerService(
    val customerRepository: CustomerRepository,
    val jvmRepository: JvmRepository,
    val applicationRepository: ApplicationRepository,
    val environmentRepository: EnvironmentRepository,
    val agentRepository: AgentRepository,
    val methodRepository: MethodRepository,
    val invocationRepository: InvocationRepository,
    val snapshotRepository: SnapshotRepository,
    val snapshotNodeRepository: SnapshotNodeRepository,
    val snapshotApplicationRepository: SnapshotApplicationRepository,
    val snapshotEnvironmentRepository: SnapshotEnvironmentRepository,
    val githubMappingRepository: GithubMappingRepository,
    val codeBaseFingerprintRepository: CodeBaseFingerprintRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun getCustomers(groupId: String): List<CustomerDto> {
        return customerRepository.findAllByGroupId(groupId).map { CustomerDto.from(it) }
    }

    fun getCustomerByName(groupId: String, customerName: String): CustomerDto {
        return customerRepository.findByNameAndGroupId(customerName, groupId)
            .orElseThrow { EmptyResultDataAccessException(1) }
            .let {
                CustomerDto.from(it)
            }
    }

    fun createCustomer(groupId: String, userId: String, customerName: String): CustomerDto {
        customerRepository.findByNameAndGroupId(customerName, groupId)
            .ifPresent { throw ResponseStatusException(HttpStatus.CONFLICT, "[$customerName] customer name is duplicated") }

        return customerRepository.save(CustomerEntity(name = customerName, licenseKey = UUID.randomUUID().toString(), groupId = groupId))
            .let {
                logger.info { "[${it.id}] ${it.name} customer is created by $userId" }
                CustomerDto.from(it)
            }
    }

    @Transactional
    fun deleteCustomer(groupId: String, userId: String, customerId: Long, remainCustomer: Boolean = true) {
        val customer = customerRepository.findById(customerId).orElseThrow()
        if (customer.groupId == groupId) {
            snapshotRepository.findAllByCustomerId(customerId).forEach {
                snapshotApplicationRepository.deleteByCustomerIdAndSnapshotId(customerId, requireNotNull(it.id))
                snapshotEnvironmentRepository.deleteByCustomerIdAndSnapshotId(customerId, it.id)
                snapshotNodeRepository.deleteAllByCustomerIdAndSnapshotId(customerId, it.id)
                snapshotRepository.deleteByCustomerIdAndId(customerId, it.id)
            }

            jvmRepository.deleteByCustomerId(customerId)
            applicationRepository.deleteByCustomerId(customerId)
            environmentRepository.deleteByCustomerId(customerId)
            agentRepository.deleteByCustomerId(customerId)
            invocationRepository.deleteByCustomerId(customerId)
            githubMappingRepository.deleteByCustomerId(customerId)

            if (!remainCustomer) {
                codeBaseFingerprintRepository.deleteByCustomerId(customerId)
                methodRepository.deleteByCustomerId(customerId)
                customerRepository.deleteById(customerId)

                logger.info { "[${customer.id}] ${customer.name} customer is deleted by $userId" }
            }
        }
    }
}
