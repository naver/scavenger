package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpMethodUsageDto
import com.navercorp.scavenger.dto.McpPrImpactDto
import com.navercorp.scavenger.mcp.McpEnvironmentResolver
import com.navercorp.scavenger.mcp.McpException
import com.navercorp.scavenger.mcp.McpQueryLimits
import com.navercorp.scavenger.repository.McpMethodQueryDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MethodUsageQueryService(
    private val mcpMethodQueryDao: McpMethodQueryDao,
    private val mcpEnvironmentResolver: McpEnvironmentResolver,
) {
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun isMethodUsed(customerId: Long, signature: String, env: String? = null): McpMethodUsageDto {
        McpQueryLimits.validateSignature(signature)
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)

        val row = mcpMethodQueryDao.findMethodUsage(customerId, listOf(signature), environmentId).firstOrNull()
            ?: throw McpException.methodNotFound(
                message = "Method not found.",
                hint = McpQueryLimits.METHOD_NOT_FOUND_HINT
            )
        return McpMethodUsageDto.from(row)
    }

    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun getPrImpact(customerId: Long, signatures: List<String>, env: String? = null): McpPrImpactDto {
        if (signatures.isEmpty()) {
            throw McpException.invalidArgument("signatures must not be empty.")
        }
        if (signatures.size > McpQueryLimits.MAX_BULK_SIGNATURES) {
            throw McpException.invalidArgument(
                message = "signatures size ${signatures.size} exceeds the maximum of ${McpQueryLimits.MAX_BULK_SIGNATURES}.",
                hint = "Split the request into batches of at most ${McpQueryLimits.MAX_BULK_SIGNATURES} signatures."
            )
        }
        signatures.forEach { McpQueryLimits.validateSignature(it) }
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)

        val distinctSignatures = signatures.distinct()
        val rows = mcpMethodQueryDao.findMethodUsage(customerId, distinctSignatures, environmentId)
        val foundSignatures = rows.mapTo(mutableSetOf()) { it.signature }
        return McpPrImpactDto(
            methods = rows.map { McpMethodUsageDto.from(it) },
            unknownSignatures = distinctSignatures.filterNot { it in foundSignatures }
        )
    }
}
