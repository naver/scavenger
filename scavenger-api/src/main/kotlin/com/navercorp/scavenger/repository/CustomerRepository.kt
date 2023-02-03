package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Customer
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerRepository : DelegatableJdbcRepository<Customer, Long> {
    fun findAllByGroupId(groupId: String): List<Customer>

    fun findByNameAndGroupId(name: String, groupId: String): Optional<Customer>
}
