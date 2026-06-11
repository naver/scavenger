package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.dto.McpError

class McpException(val error: McpError) : RuntimeException(error.message) {
    companion object {
        fun invalidArgument(message: String, hint: String? = null): McpException =
            McpException(McpError(McpError.Code.INVALID_ARGUMENT, message, hint))

        fun methodNotFound(message: String, hint: String? = null): McpException =
            McpException(McpError(McpError.Code.METHOD_NOT_FOUND, message, hint))

        fun internalError(message: String, retryable: Boolean = false): McpException =
            McpException(McpError(McpError.Code.INTERNAL_ERROR, message, retryable = retryable))
    }
}
