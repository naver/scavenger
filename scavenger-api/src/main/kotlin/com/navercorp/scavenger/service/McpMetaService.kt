package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpCoverage
import com.navercorp.scavenger.dto.McpDataFreshness
import com.navercorp.scavenger.dto.McpMeta
import com.navercorp.scavenger.repository.McpCoverageDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class McpMetaService(
    private val mcpCoverageDao: McpCoverageDao,
    private val mcpEnvironmentResolver: McpEnvironmentResolver,
) {
    // Runs after the data query succeeded, in its own transaction; only a concurrent env disable can make the
    // env re-resolution fail here (surfaces as INVALID_ARGUMENT via mcpCall).
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun build(customerId: Long, env: String?): McpMeta {
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)
        val coverage = mcpCoverageDao.findEnvironmentCoverage(customerId, environmentId).map { McpCoverage.from(it) }
        return McpMeta(
            coverage = coverage,
            dataFreshness = McpDataFreshness(queryExecutedAtMillis = System.currentTimeMillis()),
        )
    }
}
