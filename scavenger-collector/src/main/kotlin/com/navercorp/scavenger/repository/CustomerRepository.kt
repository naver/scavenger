package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : DelegatableJdbcRepository<CustomerEntity, Long> {
    fun findByLicenseKey(licenseKey: String): CustomerEntity?
}
