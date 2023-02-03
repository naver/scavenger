package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.SnapshotNode
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SnapshotNodeRepository : DelegatableJdbcRepository<SnapshotNode, Long> {
    fun findAllByCustomerIdAndSnapshotIdAndParent(
        customerId: Long,
        snapshotId: Long,
        parent: String
    ): List<SnapshotNode>

    @Modifying
    @Query("DELETE FROM snapshot_nodes WHERE customerId = :customerId AND snapshotId = :snapshotId")
    fun deleteAllByCustomerIdAndSnapshotId(@Param("customerId") customerId: Long, @Param("snapshotId") snapshotId: Long)

    fun findAllByCustomerIdAndSnapshotId(customerId: Long, snapshotId: Long): List<SnapshotNode>
}
