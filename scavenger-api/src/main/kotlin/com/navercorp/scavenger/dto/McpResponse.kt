package com.navercorp.scavenger.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class McpResponse<out T>(
    val ok: Boolean,
    val data: T? = null,
    val error: McpError? = null,
    val coverage: List<McpCoverage>? = null,
    val dataFreshness: McpDataFreshness? = null
) {
    companion object {
        fun <T> success(data: T): McpResponse<T> = McpResponse(ok = true, data = data)

        fun <T> success(data: T, coverage: List<McpCoverage>, dataFreshness: McpDataFreshness): McpResponse<T> =
            McpResponse(ok = true, data = data, coverage = coverage, dataFreshness = dataFreshness)

        fun failure(error: McpError): McpResponse<Nothing> = McpResponse(ok = false, error = error)
    }
}
