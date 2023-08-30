package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.AgentStateEntity
import com.navercorp.scavenger.repository.sql.AgentStateSql
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class AgentStateDao(
    entityJdbcProvider: EntityJdbcProvider,
    agentStateRepository: AgentStateRepository
) :
    ExtendedJdbcDaoSupport(entityJdbcProvider),
    AgentStateRepository by agentStateRepository {
    private val sql: AgentStateSql = super.sqls(::AgentStateSql)

    fun findAllGarbageLastPolledAtBefore(customerId: Long, lastPolledAt: Instant): List<AgentStateEntity> {
        return select(
            sql.selectAllGarbageLastPolledAtBefore(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("lastPolledAt", lastPolledAt),
            AgentStateEntity::class.java
        )
    }

    @Transactional
    fun updateTimestampsAndEnabled(
        customerId: Long,
        jvmUuid: String,
        thisPollAt: Instant,
        nextExpectedPollAt: Instant,
        agentEnabled: Boolean
    ) {
        val updatedRow = update(
            sql.updateTimestampsAndEnabled(),
            mapParameterSource()
                .addValue("lastPolledAt", thisPollAt)
                .addValue("nextPollExpectedAt", nextExpectedPollAt)
                .addValue("enabled", agentEnabled)
                .addValue("customerId", customerId)
                .addValue("jvmUuid", jvmUuid)
        )

        if (updatedRow == 0) {
            save(
                AgentStateEntity(
                    customerId = customerId,
                    jvmUuid = jvmUuid,
                    lastPolledAt = thisPollAt,
                    nextPollExpectedAt = nextExpectedPollAt,
                    enabled = true
                )
            )
        }
    }

    fun deleteAllByCustomerIdAndIds(customerId: Long, ids: List<Long>): Int {
        if (ids.isEmpty()) {
            return 0
        }
        return update(
            sql.deleteAllByCustomerIdAndIds(),
            mapParameterSource()
                .addValue("customerId", customerId)
                .addValue("ids", ids)
        )
    }
}
