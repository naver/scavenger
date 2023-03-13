package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.JvmEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JvmRepository : DelegatableJdbcRepository<JvmEntity, String> {
    fun countByCustomerIdAndApplicationId(customerId: Long, applicationId: Long): Long
    fun countByCustomerIdAndEnvironmentId(customerId: Long, environmentId: Long): Long

    @Modifying
    @Query("DELETE FROM jvms WHERE customerId = :customerId AND applicationId = :applicationId")
    fun deleteByCustomerIdAndApplicationId(customerId: Long, applicationId: Long): Long

    @Modifying
    @Query("DELETE FROM jvms WHERE customerId = :customerId AND environmentId = :environmentId")
    fun deleteByCustomerIdAndEnvironmentId(customerId: Long, environmentId: Long): Long

    @Modifying
    @Query("DELETE FROM jvms WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long?
}
