package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.SnapshotEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SnapshotRepository : DelegatableJdbcRepository<SnapshotEntity, Long> {
    fun findAllByCustomerId(customerId: Long): List<SnapshotEntity>

    fun findByCustomerIdAndId(customerId: Long, id: Long): Optional<SnapshotEntity>

    @Query("SELECT * FROM snapshots WHERE customerId = :customerId FOR UPDATE")
    fun findAllByCustomerIdForUpdate(@Param("customerId") customerId: Long): List<SnapshotEntity>

    @Modifying
    @Query("DELETE FROM snapshots WHERE customerId = :customerId AND id = :id")
    fun deleteByCustomerIdAndId(customerId: Long, id: Long)
}
