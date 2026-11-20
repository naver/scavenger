package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.exception.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class McpEnvironmentResolverTest {
    @Autowired
    private lateinit var sut: McpEnvironmentResolver

    @Test
    fun `should resolve enabled environment name to id`() {
        assertThat(sut.resolveEnvironmentId(1, "prod")).isEqualTo(2)
    }

    @Test
    fun `should return null when env is null or blank`() {
        assertThat(sut.resolveEnvironmentId(1, null)).isNull()
        assertThat(sut.resolveEnvironmentId(1, " ")).isNull()
    }

    @Test
    fun `should throw INVALID_ARGUMENT when environment is unknown`() {
        assertThatThrownBy { sut.resolveEnvironmentId(1, "no-such-env") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `should throw INVALID_ARGUMENT when environment is disabled`() {
        assertThatThrownBy { sut.resolveEnvironmentId(1, "staging") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `should resolve the same name within each customer's own scope`() {
        assertThat(sut.resolveEnvironmentId(1, "prod")).isEqualTo(2)
        assertThat(sut.resolveEnvironmentId(2, "prod")).isEqualTo(5)
    }
}
