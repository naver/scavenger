package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpMethodCallersDto
import com.navercorp.scavenger.mcp.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MethodCallerQueryServiceTest {
    @Autowired
    private lateinit var sut: MethodCallerQueryService

    @Test
    fun `should return direct callers with last invocation time`() {
        val result = sut.getCallers(1, "com.example.demo.additional.AdditionalService.get()")

        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE)
        assertThat(result.callers.map { it.callerSignature }).containsExactlyInAnyOrder(
            "com.example.demo.controller.MyController.additional()",
            "com.example.demo.controller.MyController\$MyTest.nesting()"
        )
        assertThat(result.callers.single { it.callerSignature.endsWith("additional()") }.lastInvokedAtMillis)
            .isEqualTo(1743148320000)
    }

    @Test
    fun `empty callers with call stack data present should stay DATA_AVAILABLE`() {
        val result = sut.getCallers(1, "com.example.demo.MyController.hello()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE)
    }

    @Test
    fun `customer without any call stack rows should signal DISABLED_OR_NO_DATA`() {
        val result = sut.getCallers(2, "com.other.App.run()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA)
    }

    @Test
    fun `env filter should scope callers to that environment`() {
        // all call_stacks rows are in env 'test' — prod scope must be empty but DATA_AVAILABLE
        val result = sut.getCallers(1, "com.example.demo.additional.AdditionalService.get()", env = "prod")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE)
    }

    @Test
    fun `should throw METHOD_NOT_FOUND for unknown signature`() {
        assertThatThrownBy { sut.getCallers(1, "com.example.demo.NoSuch.method()") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.METHOD_NOT_FOUND)
    }
}
