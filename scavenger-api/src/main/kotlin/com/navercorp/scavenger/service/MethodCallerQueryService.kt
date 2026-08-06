package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpMethodCallerDto
import com.navercorp.scavenger.dto.McpMethodCallersDto
import com.navercorp.scavenger.mcp.McpEnvironmentResolver
import com.navercorp.scavenger.mcp.McpException
import com.navercorp.scavenger.mcp.McpQueryLimits
import com.navercorp.scavenger.repository.CallStackDao
import com.navercorp.scavenger.repository.McpMethodQueryDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MethodCallerQueryService(
    private val callStackDao: CallStackDao,
    private val mcpMethodQueryDao: McpMethodQueryDao,
    private val mcpEnvironmentResolver: McpEnvironmentResolver,
) {
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun getCallers(customerId: Long, signature: String, env: String? = null): McpMethodCallersDto {
        McpQueryLimits.validateSignature(signature)
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)

        val method = mcpMethodQueryDao.findMethodUsage(customerId, listOf(signature), null).firstOrNull()
            ?: throw McpException.methodNotFound(
                message = "Method not found.",
                hint = McpQueryLimits.METHOD_NOT_FOUND_HINT
            )

        val callers = callStackDao.findCallersBySignatureHash(customerId, method.signatureHash, environmentId)
        // empty caller data must distinguish "tracking off" from "no callers" — a false dead verdict deletes live code
        val trackingState = if (callers.isEmpty() && !callStackDao.existsAnyCallStack(customerId)) {
            McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA
        } else {
            McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE
        }
        return McpMethodCallersDto(
            signature = signature,
            callers = callers.map { McpMethodCallerDto.from(it) },
            trackingState = trackingState
        )
    }
}
