package com.navercorp.scavenger.mcp

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class McpCorsFilterConfig {
    @Bean
    fun mcpCorsFilter(): FilterRegistrationBean<McpCorsFilter> {
        val registration = FilterRegistrationBean<McpCorsFilter>()
        registration.filter = McpCorsFilter()
        registration.addUrlPatterns("/mcp", "/mcp/*")
        registration.order = 0
        return registration
    }
}

class McpCorsFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        res.setHeader("Access-Control-Allow-Origin", "*")
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, Mcp-Session-Id")
        res.setHeader("Access-Control-Expose-Headers", "Mcp-Session-Id")

        if (req.method == "OPTIONS") {
            res.status = 200
            return
        }

        chain.doFilter(request, response)
    }
}
