package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.ExportMethodInvocationEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExportMethodInvocationRepository : DelegatableJdbcRepository<ExportMethodInvocationEntity, String> {
    @Query(
        """
             SELECT
            	m.id,
            	m.modifiers,
            	m.signature,
            	m.createdAt,
            	m.lastSeenAtMillis,
            	i.invokedAtMillis,
            	i.status,
            	i. `timestamp`
            FROM
            	methods AS m
            	JOIN invocations AS i
            WHERE
            	m.customerId = :customerId
            	AND m.signatureHash = i.signatureHash
        """
    )
    fun findMethodInvocationExports(
        @Param("customerId") customerId: Long,
    ): List<ExportMethodInvocationEntity>
}
