package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerRepository : DelegatableJdbcRepository<CustomerEntity, Long> {
    fun findAllByGroupId(groupId: String): List<CustomerEntity>

    fun findByNameAndGroupId(name: String, groupId: String): Optional<CustomerEntity>
}
