package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpMethodCallerDto
import com.navercorp.scavenger.dto.McpMethodCallersDto
import com.navercorp.scavenger.exception.McpException
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
        val recordedIn = callStackDao.findCallStackScopes(customerId, method.signatureHash, environmentId)
            .map { McpMethodCallersDto.RecordedScope.from(it) }
        // empty caller data must distinguish "tracking off / no data" from "no callers" — a false dead verdict
        // deletes live code. Tracking is per agent, so only data in every pair that records the method vouches for it.
        val trackingState = when {
            recordedIn.isNotEmpty() && recordedIn.all { it.hasCallStackData } -> McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE
            recordedIn.any { it.hasCallStackData } -> McpMethodCallersDto.CallStackTrackingState.PARTIAL
            else -> McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA
        }
        return McpMethodCallersDto(
            signature = signature,
            callers = callers.map { McpMethodCallerDto.from(it) },
            trackingState = trackingState,
            recordedIn = recordedIn
        )
    }
}
