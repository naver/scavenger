package com.navercorp.scavenger.config

import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@EnableWebMvc
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(MdcLoggingInterceptor())
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(0, ProtobufJsonFormatHttpMessageConverter())
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
