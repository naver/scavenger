package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.AgentEntity
import com.navercorp.scavenger.spring.DelegatableJdbcRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AgentRepository : DelegatableJdbcRepository<AgentEntity, String> {
    @Query(
        """
            SELECT
                agent_state.id,
                agent_state.jvmUuid,
                agent_state.createdAt,
                agent_state.lastPolledAt,
                agent_state.nextPollExpectedAt,
                jvms.hostname,
                jvms.applicationVersion,
                applications.name AS applicationName,
                environments.name AS environmentName
            FROM
                agent_state
                INNER JOIN jvms ON agent_state.customerId = jvms.customerId AND agent_state.jvmUuid = jvms.uuid
                INNER JOIN applications ON jvms.customerId = applications.customerId AND jvms.applicationId = applications.id
                INNER JOIN environments ON jvms.customerId = environments.customerId AND jvms.environmentId = environments.id
            WHERE
                agent_state.customerId = :customerId
        """
    )
    fun findAllAgentsByCustomerId(@Param("customerId") customerId: Long): List<AgentEntity>

    @Modifying
    @Query("DELETE FROM agent_state WHERE customerId = :customerId")
    fun deleteByCustomerId(@Param("customerId") customerId: Long): Long
}
