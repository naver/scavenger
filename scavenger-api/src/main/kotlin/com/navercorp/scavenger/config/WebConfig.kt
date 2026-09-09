package com.navercorp.scavenger.config

import com.navercorp.scavenger.mcp.ApiKeyAuthInterceptor
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@EnableWebMvc
@Configuration
class WebConfig(
    private val apiKeyAuthInterceptor: ApiKeyAuthInterceptor,
    @Value("\${spring.ai.mcp.server.enabled:true}") private val mcpServerEnabled: Boolean,
    @Value("\${spring.ai.mcp.server.protocol:sse}") private val mcpProtocol: String,
    @Value("\${spring.ai.mcp.server.type:sync}") private val mcpType: String,
    @Value("\${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}") private val mcpEndpoint: String,
    @Value("\${scavenger.mcp.cors.enabled:false}") private val mcpCorsEnabled: Boolean,
    @Value("\${scavenger.mcp.cors.allowed-origin-patterns:*}") private val mcpCorsAllowedOriginPatterns: List<String>,
) : WebMvcConfigurer {
    init {
        // Fail closed. SSE/STREAMABLE register endpoints (/sse, /mcp/message) outside the auth interceptor,
        // and ASYNC moves tool execution off the request thread that McpAuthContext reads the tenant from.
        if (mcpServerEnabled) {
            check(mcpProtocol.equals("STATELESS", ignoreCase = true)) {
                "spring.ai.mcp.server.protocol must be STATELESS for the MCP auth model (was '$mcpProtocol')"
            }
            check(mcpType.equals("SYNC", ignoreCase = true)) {
                "spring.ai.mcp.server.type must be SYNC for the MCP auth model (was '$mcpType')"
            }
        }
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(MdcLoggingInterceptor())
        // Derived from the Spring AI endpoint property so a config change cannot leave the route unguarded.
        registry.addInterceptor(apiKeyAuthInterceptor)
            .addPathPatterns(mcpEndpoint, "$mcpEndpoint/**")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        if (!mcpCorsEnabled) {
            return
        }
        // Browser-based MCP clients (e.g. MCP Inspector) need CORS on the MCP endpoint. CLI clients don't.
        // "$mcpEndpoint/**" also matches the bare endpoint; the STATELESS transport serves POST only.
        registry.addMapping("$mcpEndpoint/**")
            .allowedOriginPatterns(*mcpCorsAllowedOriginPatterns.toTypedArray())
            .allowedMethods("POST", "OPTIONS")
            .allowedHeaders("*")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/assets/*.js",
            "/assets/*.png",
            "/assets/*.jpg",
            "/assets/*.swf",
            "/assets/*.csv",
            "/assets/*.css",
            "/assets/*.html",
            "/assets/*.gif",
            "/assets/*.ico",
            "/assets/*.woff2",
            "/assets/*.woff",
            "/assets/*.ttf"
        ).addResourceLocations("classpath:/static/assets/")
    }

    class MdcLoggingInterceptor : HandlerInterceptor, Ordered {

        @Throws(Exception::class)
        override fun preHandle(
            request: HttpServletRequest,
            response: HttpServletResponse,
            handler: Any
        ): Boolean {
            MDC.put(RequestMetaNames.REMOTE_ADDR.key, request.remoteAddr)
            MDC.put(RequestMetaNames.REQUEST_METHOD.key, request.method)
            MDC.put(RequestMetaNames.REQUEST_URI.key, request.requestURI)
            MDC.put(RequestMetaNames.REQUEST_PARAMS.key, parameterMapToReadableString(request.parameterMap))
            MDC.put(HttpHeaders.HOST, request.getHeader(HttpHeaders.HOST))
            MDC.put(HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT))
            MDC.put(HttpHeaders.REFERER, request.getHeader(HttpHeaders.REFERER))
            return super.preHandle(request, response, handler)
        }

        override fun getOrder(): Int {
            return Ordered.HIGHEST_PRECEDENCE
        }

        private fun parameterMapToReadableString(parameterMap: Map<String, Array<String>>): String {
            return parameterMap.entries
                .flatMap { entry ->
                    entry.value.map { value ->
                        "${entry.key}=$value"
                    }
                }.joinToString("&")
        }

        enum class RequestMetaNames(val key: String) {
            REMOTE_ADDR("requestAddr"),
            REQUEST_METHOD("requestMethod"),
            REQUEST_URI("requestUri"),
            REQUEST_PARAMS("requestParams")
        }
    }
}
