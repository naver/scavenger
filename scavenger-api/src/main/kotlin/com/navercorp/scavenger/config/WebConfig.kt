package com.navercorp.scavenger.config

import com.navercorp.scavenger.mcp.ApiKeyAuthInterceptor
import org.slf4j.MDC
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
    private val apiKeyAuthInterceptor: ApiKeyAuthInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(MdcLoggingInterceptor())
        registry.addInterceptor(apiKeyAuthInterceptor)
            .addPathPatterns("/mcp", "/mcp/**")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        // Browser-based MCP clients (e.g. MCP Inspector) need CORS on the MCP endpoint. CLI clients don't.
        registry.addMapping("/mcp/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
            .allowedHeaders("*")
        registry.addMapping("/mcp")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
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
