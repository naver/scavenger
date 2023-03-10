package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.MethodEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MethodRepository : DelegatableJdbcRepository<MethodEntity, String> {
    @Query("SELECT count(DISTINCT signatureHash) FROM methods WHERE customerId = :customerId AND garbage = FALSE")
    fun countMethodSignatureHashByCustomerId(@Param("customerId") customerId: Long): Int

    @Modifying
    @Query("DELETE FROM methods WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
