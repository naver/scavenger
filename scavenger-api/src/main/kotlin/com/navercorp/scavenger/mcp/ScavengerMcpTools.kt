package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpResponse
import com.navercorp.scavenger.exception.McpException
import com.navercorp.scavenger.service.McpMetaService
import com.navercorp.scavenger.service.McpQueryLimits
import com.navercorp.scavenger.service.MethodCallerQueryService
import com.navercorp.scavenger.service.MethodUsageQueryService
import com.navercorp.scavenger.service.ScopeQueryService
import com.navercorp.scavenger.service.StaleMethodSearchService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

// Tools return the `ok/data/error` envelope as a JSON text content block (Spring AI @Tool +
// MethodToolCallbackProvider → ReturnMode.TEXT). Universally parseable by any MCP client; the LLM reads
// the JSON guided by the tool descriptions. Native MCP structuredContent + outputSchema is deferred to v2:
// it requires the @McpTool provider path and concrete (non-wildcard) return types, and adds little for the
// P0 LLM-agent consumer. Revisit when a programmatic (non-LLM) consumer needs schema-validated output.
@Component
class ScavengerMcpTools(
    private val methodUsageQueryService: MethodUsageQueryService,
    private val methodCallerQueryService: MethodCallerQueryService,
    private val staleMethodSearchService: StaleMethodSearchService,
    private val scopeQueryService: ScopeQueryService,
    private val mcpMetaService: McpMetaService,
) {
    @Tool(
        name = "list_scopes",
        description = """
            List the customer's environments and applications (names, ids, enabled). Start here for any
            analysis so you use valid environment/application names instead of guessing "prod" vs "production".
            Use this tool when:
              - Starting any analysis — discover valid environment/application names first
              - An env filter returned INVALID_ARGUMENT
            The response `coverage` metadata gives, per (application, environment), how far back data goes
            (collectingSinceMillis) and when an agent last reported (agentAliveAtMillis) — check it before
            trusting a "not invoked" result.
        """,
    )
    fun listScopes(): McpResponse<*> = mcpCall {
        val customerId = currentMcpCustomerId()
        withMeta(customerId, null, scopeQueryService.listScopes(customerId))
    }

    @Tool(
        name = "is_method_used",
        description = """
            Check whether a single method is used (INVOKED in any application/environment), from Scavenger's
            runtime data. Returns evidence — not a deletion verdict.
            Use this tool when:
              - User asks "is this method dead?" / "이 메서드 진짜 안 쓰여?"
              - Gathering evidence that a method may be safe to delete (combine with get_method_callers)
              - Validating a deprecated method is no longer called, or a new method is being invoked
            Input is the fully-qualified signature as stored by the agent, e.g. com.example.Foo.bar().
            An empty/absent result is NOT proof of death — weigh the `coverage` metadata.
        """,
    )
    fun isMethodUsed(
        @ToolParam(description = "Fully-qualified method signature, e.g. com.example.Foo.bar() with FQCN params")
        signature: String,
        @ToolParam(required = false, description = "Optional environment name (e.g. prod). Omit to aggregate across all.")
        env: String?,
    ): McpResponse<*> = mcpCall {
        val customerId = currentMcpCustomerId()
        withMeta(customerId, env, methodUsageQueryService.isMethodUsed(customerId, signature, env))
    }

    @Tool(
        name = "get_method_callers",
        description = """
            List the runtime direct callers of a method, from call-stack data.
            Use this tool when:
              - User asks "who calls this method?"
              - Cross-checking caller evidence after is_method_used
              - Analyzing the impact of changing a method signature, or planning a deprecation migration
            IMPORTANT: call-stack tracking is off by default. When there is no call-stack data in the requested
            scope (the given env, or the whole workspace) the result carries a typed "tracking disabled or no
            data" state — never read an empty list as "no callers".
            Data covers direct callers only, within instrumented packages and the tracking-enabled window.
        """,
    )
    fun getMethodCallers(
        @ToolParam(description = "Fully-qualified method signature, e.g. com.example.Foo.bar()")
        signature: String,
        @ToolParam(required = false, description = "Optional environment name. Omit to aggregate across all.")
        env: String?,
    ): McpResponse<*> = mcpCall {
        val customerId = currentMcpCustomerId()
        withMeta(customerId, env, methodCallerQueryService.getCallers(customerId, signature, env))
    }

    @Tool(
        name = "get_stale_methods",
        description = """
            Find stale / unused methods. The workhorse for dead-code discovery.
            Use this tool when:
              - "운영에서 안 쓰이는 코드" / "dead code in prod" → env="prod"
              - "이 패키지 안 쓰이는 메서드" → prefix="com.foo.legacy" (left-anchored, package/type prefix)
              - "한 번도 호출 안 된 메서드" → neverInvoked=true
              - "30일 이상 미호출" → idleDays=30
              - Periodic dead-code cleanup, legacy-package removal candidates
            IMPORTANT: without idleDays or neverInvoked=true this returns ALL instrumented methods, used ones
            included (check lastInvokedAtMillis) — for dead-code questions always set at least one of them.
            Omitting idleDays applies no idle cutoff (no hidden default). Results are paginated: pass nextCursor
            back as cursor.
            Results are evidence, not deletion verdicts — weigh the `coverage` metadata.
        """,
    )
    fun getStaleMethods(
        @ToolParam(required = false, description = "Optional environment name (e.g. prod). Omit to aggregate across all.")
        env: String?,
        @ToolParam(required = false, description = "Only methods idle for at least this many days. Omit for no idle filter.")
        idleDays: Int?,
        @ToolParam(required = false, description = "Left-anchored declaringType prefix, e.g. com.foo.legacy")
        prefix: String?,
        @ToolParam(required = false, description = "If true, only methods never invoked at all.")
        neverInvoked: Boolean?,
        @ToolParam(required = false, description = "Page size (1..100, default 50).")
        limit: Int?,
        @ToolParam(required = false, description = "Opaque pagination cursor — pass the previous page's nextCursor as-is.")
        cursor: String?,
    ): McpResponse<*> = mcpCall {
        val customerId = currentMcpCustomerId()
        withMeta(
            customerId,
            env,
            staleMethodSearchService.search(
                customerId = customerId,
                env = env,
                idleDays = idleDays,
                prefix = prefix,
                neverInvoked = neverInvoked ?: false,
                limit = limit ?: McpQueryLimits.DEFAULT_PAGE_SIZE,
                cursor = cursor,
            ),
        )
    }

    @Tool(
        name = "get_pr_impact",
        description = """
            Bulk usage check for many signatures at once (up to 200). Prefer this over N is_method_used calls
            whenever 3 or more methods are involved.
            Use this tool when:
              - Reviewing a PR that deletes/modifies multiple methods
              - Verifying all changed methods in a refactor are safe to remove
              - Auditing a deprecation cycle, or validating a library upgrade (migrated APIs no longer called)
            Returns per-method usage plus the list of signatures that were not found. Not-found is NOT proof of
            death — the method may simply be outside the agent's instrumented packages.
        """,
    )
    fun getPrImpact(
        @ToolParam(description = "Fully-qualified method signatures (max 200).")
        signatures: List<String>,
        @ToolParam(required = false, description = "Optional environment name. Omit to aggregate across all.")
        env: String?,
    ): McpResponse<*> = mcpCall {
        val customerId = currentMcpCustomerId()
        withMeta(customerId, env, methodUsageQueryService.getPrImpact(customerId, signatures, env))
    }

    // Attaches coverage + dataFreshness metadata to a successful data payload. Built only after the data
    // query succeeded, so the env re-resolution inside cannot surface a new error.
    private fun <T> withMeta(customerId: Long, env: String?, data: T): McpResponse<T> {
        val meta = mcpMetaService.build(customerId, env)
        return McpResponse.success(data, meta.coverage, meta.dataFreshness)
    }

    // Maps the service's typed failures to the response envelope. Any non-McpException (incl. the
    // IllegalStateException from currentMcpCustomerId) becomes a retryable INTERNAL_ERROR rather than
    // leaking a framework stack trace to the agent.
    private fun <T> mcpCall(block: () -> McpResponse<T>): McpResponse<T> =
        try {
            block()
        } catch (e: McpException) {
            McpResponse.failure(e.error)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error handling MCP tool call" }
            McpResponse.failure(McpError(McpError.Code.INTERNAL_ERROR, "Internal error.", retryable = true))
        }
}
