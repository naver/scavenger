package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Environment
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EnvironmentRepository : DelegatableJdbcRepository<Environment, String> {
    fun findByCustomerId(customerId: Long): List<Environment>

    fun findByCustomerIdAndId(customerId: Long, id: Long): Environment

    @Modifying
    @Query("DELETE FROM environments WHERE customerId = :customerId AND id = :id")
    fun deleteByCustomerIdAndId(customerId: Long, id: Long)

    @Modifying
    @Query("DELETE FROM environments WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
