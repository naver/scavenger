package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.Snapshot
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SnapshotRepository : DelegatableJdbcRepository<Snapshot, Long> {
    fun findByCustomerId(customerId: Long): List<Snapshot>

    fun findByCustomerIdAndId(customerId: Long, id: Long): Optional<Snapshot>

    @Query("SELECT * FROM snapshots WHERE customerId = :customerId FOR UPDATE")
    fun findAllByCustomerIdForUpdate(@Param("customerId") customerId: Long): List<Snapshot>

    @Modifying
    @Query("DELETE FROM snapshots WHERE customerId = :customerId AND id = :id")
    fun deleteByCustomerIdAndId(customerId: Long, id: Long)
}
