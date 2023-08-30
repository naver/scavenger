package com.navercorp.scavenger.repository.sql

import com.navercorp.scavenger.entity.AgentStateEntity
import com.navercorp.spring.data.jdbc.plus.sql.support.SqlGeneratorSupport

class AgentStateSql : SqlGeneratorSupport() {
    fun updateTimestampsAndEnabled(): String =
        """
        UPDATE
            agent_state
        SET
            lastPolledAt = :lastPolledAt,
            nextPollExpectedAt = :nextPollExpectedAt,
            enabled = :enabled
        WHERE
            customerId = :customerId
            AND jvmUuid = :jvmUuid
        """.trimIndent()

    fun selectAllGarbageLastPolledAtBefore(): String =
        """
        SELECT
           ${sql.columns(AgentStateEntity::class.java)}
        FROM
            agent_state
        WHERE
            customerId = :customerId
            AND lastPolledAt < :lastPolledAt
        LIMIT 10000
        """.trimIndent()

    fun deleteAllByCustomerIdAndIds(): String =
        """
        DELETE FROM
            agent_state
        WHERE
            customerId = :customerId
            AND id IN (:ids)            
        """.trimIndent()
}
