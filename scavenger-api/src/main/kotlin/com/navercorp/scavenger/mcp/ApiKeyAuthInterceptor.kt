package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpResponse
import com.navercorp.scavenger.repository.CustomerRepository
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
    private val customerRepository: CustomerRepository,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor, Ordered {

    private val logger = KotlinLogging.logger {}

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 100

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val licenseKey = request.getHeader(McpAuthContext.HEADER_LICENSE_KEY)
        if (licenseKey.isNullOrBlank()) {
            return reject(response, MISSING_KEY_ERROR)
        }

        val customer = customerRepository.findByLicenseKey(licenseKey).orElse(null)
        if (customer == null) {
            logger.warn { "MCP auth rejected: unknown licenseKey suffix=${licenseKey.takeLast(4)}" }
            return reject(response, INVALID_KEY_ERROR)
        }

        request.setAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID, customer.id)
        return true
    }

    private fun reject(response: HttpServletResponse, error: McpError): Boolean {
        response.status = HttpStatus.UNAUTHORIZED.value()
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
        private val INVALID_KEY_ERROR = McpError(
            code = McpError.Code.AUTH_INVALID,
            message = "Invalid licenseKey.",
            hint = "Verify the licenseKey value against the Scavenger workspace.",
        )
    }
}
