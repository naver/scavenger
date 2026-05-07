package com.navercorp.scavenger.mcp

import jakarta.servlet.http.HttpServletRequest

object McpAuthContext {
    const val ATTRIBUTE_CUSTOMER_ID = "scavenger.mcp.customerId"
    const val HEADER_LICENSE_KEY = "X-Scavenger-License-Key"
}

fun HttpServletRequest.requireCustomerId(): Long {
    val attr = getAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID)
        ?: error("MCP customerId not present on request — ApiKeyAuthInterceptor must run first")
    return attr as? Long ?: error("Expected Long for ${McpAuthContext.ATTRIBUTE_CUSTOMER_ID} but got ${attr::class}")
}
