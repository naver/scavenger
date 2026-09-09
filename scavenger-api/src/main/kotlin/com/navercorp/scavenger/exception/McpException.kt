package com.navercorp.scavenger.exception

import com.navercorp.scavenger.dto.McpError

// No @ResponseStatus on purpose: MCP tools map this to the ok:false envelope, not to an HTTP status.
class McpException(val error: McpError) : RuntimeException(error.message) {
    companion object {
        fun invalidArgument(message: String, hint: String? = null): McpException =
            McpException(McpError(McpError.Code.INVALID_ARGUMENT, message, hint))

        fun methodNotFound(message: String, hint: String? = null): McpException =
            McpException(McpError(McpError.Code.METHOD_NOT_FOUND, message, hint))
    }
}
