package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ApplicationEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : DelegatableJdbcRepository<ApplicationEntity, String> {
    fun findAllByCustomerId(customerId: Long): List<ApplicationEntity>

    fun findByCustomerIdAndId(customerId: Long, id: Long): ApplicationEntity

    @Modifying
    @Query("DELETE FROM applications WHERE customerId = :customerId AND id = :id")
    fun deleteByCustomerIdAndId(customerId: Long, id: Long)

    @Modifying
    @Query("DELETE FROM applications WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
