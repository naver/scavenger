package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.CodeBaseFingerprintEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CodeBaseFingerprintRepository : DelegatableJdbcRepository<CodeBaseFingerprintEntity, Long> {

    @Modifying
    @Query("DELETE FROM codebase_fingerprints WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
