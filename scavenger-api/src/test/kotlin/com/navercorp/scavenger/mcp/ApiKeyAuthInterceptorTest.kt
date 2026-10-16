package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpResponse
import com.navercorp.scavenger.entity.CustomerEntity
import com.navercorp.scavenger.repository.CustomerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.Optional

@SpringBootTest
class ApiKeyAuthInterceptorTest {
    @Autowired
    private lateinit var sut: ApiKeyAuthInterceptor

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Test
    fun `preHandle injects customerId when licenseKey is valid`() {
        val request = MockHttpServletRequest().apply {
            addHeader(McpAuthContext.HEADER_LICENSE_KEY, "4c94e0dd-ad04-4b17-9238-f46bba75c684")
        }
        val response = MockHttpServletResponse()

        val result = sut.preHandle(request, response, Any())

        assertThat(result).isTrue
        assertThat(request.getAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID)).isEqualTo(1L)
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `preHandle rejects with AUTH_MISSING when header is absent`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        val result = sut.preHandle(request, response, Any())

        assertThat(result).isFalse
        assertThat(response.status).isEqualTo(401)
        assertErrorCode(response, McpError.Code.AUTH_MISSING)
    }

    @Test
    fun `preHandle rejects with AUTH_MISSING when header is blank`() {
        val request = MockHttpServletRequest().apply {
            addHeader(McpAuthContext.HEADER_LICENSE_KEY, "   ")
        }
        val response = MockHttpServletResponse()

        val result = sut.preHandle(request, response, Any())

        assertThat(result).isFalse
        assertThat(response.status).isEqualTo(401)
        assertErrorCode(response, McpError.Code.AUTH_MISSING)
    }

    @Test
    fun `preHandle rejects with AUTH_INVALID when licenseKey is unknown`() {
        val request = MockHttpServletRequest().apply {
            addHeader(McpAuthContext.HEADER_LICENSE_KEY, "unknown-license-key")
        }
        val response = MockHttpServletResponse()

        val result = sut.preHandle(request, response, Any())

        assertThat(result).isFalse
        assertThat(response.status).isEqualTo(401)
        assertErrorCode(response, McpError.Code.AUTH_INVALID)
    }

    @Test
    fun `preHandle allows CORS preflight without a licenseKey`() {
        val request = MockHttpServletRequest().apply {
            method = "OPTIONS"
            addHeader(HttpHeaders.ORIGIN, "https://inspector.example")
            addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        }
        val response = MockHttpServletResponse()

        val result = sut.preHandle(request, response, Any())

        assertThat(result).isTrue
        assertThat(response.status).isEqualTo(200)
        assertThat(request.getAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID)).isNull()
    }

    @Test
    fun `preHandle answers a license lookup failure with a retryable INTERNAL_ERROR envelope`() {
        // a DB outage must still reach the MCP client as the JSON envelope, not Spring's error page
        val failingRepository = object : CustomerRepository by customerRepository {
            override fun findByLicenseKey(licenseKey: String): Optional<CustomerEntity> =
                throw DataAccessResourceFailureException("connection refused")
        }
        val request = MockHttpServletRequest().apply {
            addHeader(McpAuthContext.HEADER_LICENSE_KEY, "4c94e0dd-ad04-4b17-9238-f46bba75c684")
        }
        val response = MockHttpServletResponse()

        val result = ApiKeyAuthInterceptor(failingRepository, objectMapper).preHandle(request, response, Any())

        assertThat(result).isFalse
        assertThat(response.status).isEqualTo(500)
        assertThat(request.getAttribute(McpAuthContext.ATTRIBUTE_CUSTOMER_ID)).isNull()
        assertThat(objectMapper.readValue(response.contentAsString, McpResponse::class.java).error)
            .isEqualTo(McpError(McpError.Code.INTERNAL_ERROR, "Authentication service unavailable.", retryable = true))
    }

    private fun assertErrorCode(response: MockHttpServletResponse, expected: McpError.Code) {
        val body = objectMapper.readValue(response.contentAsString, McpResponse::class.java)
        assertThat(body.ok).isFalse
        assertThat(body.error?.code).isEqualTo(expected)
    }
}
