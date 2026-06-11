package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpStaleMethodDto
import com.navercorp.scavenger.dto.McpStaleMethodsDto
import com.navercorp.scavenger.mcp.McpEnvironmentResolver
import com.navercorp.scavenger.mcp.McpException
import com.navercorp.scavenger.mcp.McpQueryLimits
import com.navercorp.scavenger.repository.McpMethodQueryDao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class StaleMethodSearchService(
    private val mcpMethodQueryDao: McpMethodQueryDao,
    private val mcpEnvironmentResolver: McpEnvironmentResolver,
) {
    // in idleDays results, lastInvokedAtMillis == null means "never invoked", not "idle since unknown"
    @Transactional(readOnly = true, timeout = McpQueryLimits.QUERY_TIMEOUT_SECONDS)
    fun search(
        customerId: Long,
        env: String? = null,
        idleDays: Int? = null,
        prefix: String? = null,
        neverInvoked: Boolean = false,
        limit: Int = McpQueryLimits.DEFAULT_PAGE_SIZE,
        cursor: String? = null,
    ): McpStaleMethodsDto {
        validate(idleDays, prefix, limit, cursor)
        val environmentId = mcpEnvironmentResolver.resolveEnvironmentId(customerId, env)
        val idleCutoffMillis = idleDays?.let { System.currentTimeMillis() - TimeUnit.DAYS.toMillis(it.toLong()) }

        val rows = mcpMethodQueryDao.findStaleMethods(
            customerId = customerId,
            environmentId = environmentId,
            prefix = prefix?.let { escapeLikePrefix(it) },
            cursor = cursor,
            idleCutoffMillis = idleCutoffMillis,
            neverInvokedOnly = neverInvoked,
            limit = limit
        )
        return McpStaleMethodsDto(
            methods = rows.map { McpStaleMethodDto.from(it) },
            nextCursor = if (rows.size == limit) rows.last().signatureHash else null,
            limit = limit
        )
    }

    private fun validate(idleDays: Int?, prefix: String?, limit: Int, cursor: String?) {
        if (idleDays != null && idleDays < 0) {
            throw McpException.invalidArgument("idleDays must be >= 0.")
        }
        if (prefix != null && prefix.length > McpQueryLimits.MAX_PREFIX_LENGTH) {
            throw McpException.invalidArgument(
                message = "prefix length ${prefix.length} exceeds the maximum of ${McpQueryLimits.MAX_PREFIX_LENGTH}.",
                hint = "declaringType is at most ${McpQueryLimits.MAX_PREFIX_LENGTH} characters — a longer prefix can never match."
            )
        }
        if (limit !in 1..McpQueryLimits.MAX_PAGE_SIZE) {
            throw McpException.invalidArgument("limit must be between 1 and ${McpQueryLimits.MAX_PAGE_SIZE}.")
        }
        if (cursor != null && (cursor.length > MAX_CURSOR_LENGTH || cursor.isBlank())) {
            throw McpException.invalidArgument("Invalid cursor.", "Pass the nextCursor value from the previous page as-is.")
        }
    }

    // left-anchored only so the (customerId, declaringType) index applies; user % / _ are literals
    private fun escapeLikePrefix(prefix: String): String {
        val escaped = prefix
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
        return "$escaped%"
    }

    companion object {
        private const val MAX_CURSOR_LENGTH = 64
    }
}
