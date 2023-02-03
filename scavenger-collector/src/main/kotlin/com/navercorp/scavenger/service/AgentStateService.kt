package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.AgentStateDao
import com.navercorp.scavenger.repository.EnvironmentDao
import com.navercorp.scavenger.repository.JvmDao
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AgentStateService(
    val agentStateDao: AgentStateDao,
    val environmentDao: EnvironmentDao,
    val jvmDao: JvmDao
) {
    @Async
    fun update(customerId: Long, jvmUuid: String, pollIntervalSeconds: Int) {
        val now = Instant.now()
        agentStateDao.updateTimestampsAndEnabled(
            customerId = customerId,
            jvmUuid = jvmUuid,
            thisPollAt = now,
            nextExpectedPollAt = now.plusSeconds(pollIntervalSeconds.toLong()),
            agentEnabled = isEnvironmentEnabled(customerId, jvmUuid)
        )
    }

    private fun isEnvironmentEnabled(customerId: Long, jvmUuid: String): Boolean {
        val jvm = jvmDao.findByCustomerIdAndUuid(customerId, jvmUuid) ?: run { return true }
        val environment = environmentDao.findByCustomerIdAndId(customerId, jvm.environmentId) ?: run { return true }
        return if (environment.customerId == customerId) {
            environment.enabled
        } else {
            true
        }
    }
}
