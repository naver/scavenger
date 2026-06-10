package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@SpringBootTest
class ApiKeyAuthInterceptorTest {
    @Autowired
    private lateinit var sut: ApiKeyAuthInterceptor

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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

    private fun assertErrorCode(response: MockHttpServletResponse, expected: McpError.Code) {
        val body = objectMapper.readValue(response.contentAsString, McpResponse::class.java)
        assertThat(body.ok).isFalse
        assertThat(body.error?.code).isEqualTo(expected)
    }
}
