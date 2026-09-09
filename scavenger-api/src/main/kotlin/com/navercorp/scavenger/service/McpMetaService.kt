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
    // Built after the data query already succeeded, so env re-resolution here can't fail with a new error.
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun build(customerId: Long, env: String?): McpMeta {
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)
        val coverage = mcpCoverageDao.findCoverage(customerId, environmentId).map { McpCoverage.from(it) }
        return McpMeta(
            coverage = coverage,
            dataFreshness = McpDataFreshness(queryExecutedAtMillis = System.currentTimeMillis()),
        )
    }
}
