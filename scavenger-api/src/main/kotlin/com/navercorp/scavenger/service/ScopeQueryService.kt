package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpScopesDto
import com.navercorp.scavenger.repository.ApplicationRepository
import com.navercorp.scavenger.repository.EnvironmentRepository
import com.navercorp.scavenger.repository.McpCoverageDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScopeQueryService(
    private val environmentRepository: EnvironmentRepository,
    private val applicationRepository: ApplicationRepository,
    private val mcpCoverageDao: McpCoverageDao,
) {
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun listScopes(customerId: Long): McpScopesDto =
        McpScopesDto(
            environments = environmentRepository.findAllByCustomerId(customerId)
                .map { McpScopesDto.Environment.from(it) },
            applications = applicationRepository.findAllByCustomerId(customerId)
                .map { McpScopesDto.Application.from(it) },
            coverage = mcpCoverageDao.findScopeCoverage(customerId)
                .map { McpScopesDto.Coverage.from(it) },
        )
}
