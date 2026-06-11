package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiKeyAuthInterceptor(
    private val mcpLicenseService: McpLicenseService,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor, Ordered {

    private val logger = KotlinLogging.logger {}

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 100

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val licenseKey = request.getHeader(McpAuthContext.HEADER_LICENSE_KEY)
        if (licenseKey.isNullOrBlank()) {
            return reject(response, MISSING_KEY_ERROR)
        }

        val customerId = try {
            mcpLicenseService.resolveCustomerId(licenseKey)
        } catch (e: McpException) {
            logger.warn { "MCP auth rejected: unknown licenseKey suffix=${licenseKey.takeLast(4)}" }
            return reject(response, e.error)
        } catch (e: Exception) {
            // MCP clients must always receive the JSON envelope, never Spring's default error page
            logger.error(e) { "Unexpected error during MCP license resolution" }
            return reject(response, LOOKUP_FAILED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        request.setAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID, customerId)
        return true
    }

    private fun reject(
        response: HttpServletResponse,
        error: McpError,
        status: HttpStatus = HttpStatus.UNAUTHORIZED,
    ): Boolean {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        objectMapper.writeValue(response.writer, McpResponse.failure(error))
        return false
    }

    companion object {
        private val MISSING_KEY_ERROR = McpError(
            code = McpError.Code.AUTH_MISSING,
            message = "${McpAuthContext.HEADER_LICENSE_KEY} header is required.",
            hint = "Set the header to your Scavenger licenseKey in your MCP client config.",
        )
        private val LOOKUP_FAILED_ERROR = McpError(
            code = McpError.Code.INTERNAL_ERROR,
            message = "Authentication service unavailable.",
            retryable = true,
        )
    }
}
