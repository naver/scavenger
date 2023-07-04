package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.EnvironmentRefEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SnapshotEnvironmentRepository : DelegatableJdbcRepository<EnvironmentRefEntity, Long> {
    fun countByCustomerIdAndEnvironmentId(customerId: Long, environmentId: Long): Long

    @Query("SELECT snapshotId FROM snapshot_environment WHERE customerId = :customerId AND environmentId = :environmentId")
    fun findAllByCustomerIdAndEnvironmentId(@Param("customerId") customerId: Long, @Param("environmentId") environmentId: Long): List<Long>

    @Modifying
    @Query("DELETE FROM snapshot_environment WHERE customerId = :customerId AND snapshotId = :snapshotId")
    fun deleteByCustomerIdAndSnapshotId(customerId: Long, snapshotId: Long)

    @Query("SELECT snapshotId FROM snapshot_environment WHERE customerId = :customerId AND snapshotId = :snapshotId")
    fun findAllByCustomerIdAndSnapshotId(@Param("customerId") customerId: Long, @Param("snapshotId") snapshotId: Long): List<Long>
}
