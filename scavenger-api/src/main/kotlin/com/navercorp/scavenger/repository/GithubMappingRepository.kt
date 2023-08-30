package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.GithubMappingEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GithubMappingRepository : DelegatableJdbcRepository<GithubMappingEntity, Long> {
    fun findAllByCustomerId(customerId: Long): List<GithubMappingEntity>

    @Modifying
    @Query("DELETE FROM github_mappings WHERE customerId = :customerId AND id = :id")
    fun deleteByCustomerIdAndId(@Param("customerId") customerId: Long, @Param("id") id: Long): Long

    @Modifying
    @Query("DELETE FROM github_mappings WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
