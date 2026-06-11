package com.navercorp.scavenger.dto

data class McpResponse<out T>(
    val ok: Boolean,
    val data: T? = null,
    val error: McpError? = null,
    val dataFreshness: DataFreshness? = null,
    val coverage: List<CoverageScopeDto>? = null,
    val pagination: Pagination? = null,
    val notice: String? = null
) {
    companion object {
        fun <T> success(data: T): McpResponse<T> = McpResponse(ok = true, data = data)

        fun failure(error: McpError): McpResponse<Nothing> = McpResponse(ok = false, error = error)
    }
}
