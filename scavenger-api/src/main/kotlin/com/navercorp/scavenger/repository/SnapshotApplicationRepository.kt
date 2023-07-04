package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ApplicationRefEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SnapshotApplicationRepository : DelegatableJdbcRepository<ApplicationRefEntity, Long> {
    fun countByCustomerIdAndApplicationId(customerId: Long, applicationId: Long): Long

    @Query("SELECT snapshotId FROM snapshot_application WHERE customerId = :customerId AND applicationId = :applicationId")
    fun findAllByCustomerIdAndApplicationId(@Param("customerId") customerId: Long, @Param("applicationId") applicationId: Long): List<Long>

    @Modifying
    @Query("DELETE FROM snapshot_application WHERE customerId = :customerId AND snapshotId = :snapshotId")
    fun deleteByCustomerIdAndSnapshotId(customerId: Long, snapshotId: Long)

    @Query("SELECT snapshotId FROM snapshot_application WHERE customerId = :customerId AND snapshotId = :snapshotId")
    fun findAllByCustomerIdAndSnapshotId(@Param("customerId") customerId: Long, @Param("snapshotId") snapshotId: Long): List<Long>
}
