package com.navercorp.scavenger.dto

data class McpError(
    val code: Code,
    val message: String,
    val hint: String? = null,
    val retryable: Boolean = false
) {
    enum class Code {
        AUTH_MISSING,
        AUTH_INVALID,
        METHOD_NOT_FOUND,
        INVALID_ARGUMENT,
        INTERNAL_ERROR
    }
}
