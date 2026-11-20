package com.navercorp.scavenger.mcp

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object McpAuthContext {
    const val ATTRIBUTE_CUSTOMER_ID = "scavenger.mcp.customerId"
    const val HEADER_LICENSE_KEY = "X-Scavenger-License-Key"
}

fun HttpServletRequest.requireCustomerId(): Long {
    val attr = getAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID)
        ?: error("MCP customerId not present on request — ApiKeyAuthInterceptor must run first")
    return attr as? Long ?: error("Expected Long for ${McpAuthContext.ATTRIBUTE_CUSTOMER_ID} but got ${attr::class}")
}

// WHY: @Tool methods receive no HttpServletRequest parameter, so tenant identity is pulled from the
// request-bound thread-local. Load-bearing for tenant isolation — depends on the tool executing in the
// HTTP request thread, which Spring AI guarantees for SYNC tools in a servlet environment. WebConfig
// refuses to start with any other protocol/type; McpIntegrationTest covers the propagation end to end.
fun currentMcpCustomerId(): Long {
    val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        ?: error("No servlet request bound to the current thread — MCP tool must run within an HTTP request")
    return attributes.request.requireCustomerId()
}
