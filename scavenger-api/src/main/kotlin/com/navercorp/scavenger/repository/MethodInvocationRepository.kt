package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.MethodInvocationEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MethodInvocationRepository : DelegatableJdbcRepository<MethodInvocationEntity, String> {
    @Query(
        """
            SELECT
                methods.signature,
                max(invocations.invokedAtMillis) as invokedAtMillis,
                max(methods.methodName) as methodName
            FROM
                methods 
                INNER JOIN invocations ON methods.customerId = invocations.customerId AND methods.signatureHash = invocations.signatureHash
            WHERE
                methods.declaringType IS NOT NULL
                AND methods.modifiers NOT IN ('public abstract', 'public abstract transient')
                AND methods.garbage = FALSE
                AND invocations.status IN ('NOT_INVOKED', 'INVOKED')
                AND invocations.customerId = :customerId
                AND invocations.applicationId IN (:applicationIdList)
                AND invocations.environmentId IN (:environmentIdList)
            GROUP BY methods.signature
        """
    )
    fun findAllMethodInvocations(
        @Param("customerId") customerId: Long,
        @Param("applicationIdList") applicationIdList: List<Long>,
        @Param("environmentIdList") environmentIdList: List<Long>
    ): List<MethodInvocationEntity>
}
