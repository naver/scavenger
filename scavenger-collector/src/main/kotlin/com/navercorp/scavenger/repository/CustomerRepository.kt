package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Customer
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : DelegatableJdbcRepository<Customer, Long> {
    fun findByLicenseKey(licenseKey: String): Customer?
}
