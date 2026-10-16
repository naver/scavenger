package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpMethodCallersDto
import com.navercorp.scavenger.exception.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class MethodCallerQueryServiceTest {
    @Autowired
    private lateinit var sut: MethodCallerQueryService

    @Autowired
    private lateinit var applicationService: ApplicationService

    @Test
    fun `should return direct callers with last invocation time`() {
        // get() is recorded in demo/test (no call-stack data) and demo2/test (tracked) — callers from demo are unknown
        val result = sut.getCallers(1, "com.example.demo.additional.AdditionalService.get()")

        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.PARTIAL)
        assertThat(result.recordedIn).containsExactly(
            McpMethodCallersDto.RecordedScope("demo", "test", hasCallStackData = false),
            McpMethodCallersDto.RecordedScope("demo2", "test", hasCallStackData = true)
        )
        assertThat(result.callers.map { it.callerSignature }).containsExactlyInAnyOrder(
            "com.example.demo.controller.MyController.additional()",
            "com.example.demo.controller.MyController\$MyTest.nesting()"
        )
        assertThat(result.callers.single { it.callerSignature.endsWith("additional()") }.lastInvokedAtMillis)
            .isEqualTo(1743148320000)
    }

    @Test
    fun `empty callers should stay DATA_AVAILABLE when every recording application has call stack data`() {
        // buildModels() is recorded only in demo2/test, which has call-stack rows, and nothing calls it
        val result = sut.getCallers(1, "com.example.demo.extmodel.MyExtensionModel.buildModels()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE)
        assertThat(result.recordedIn).containsExactly(McpMethodCallersDto.RecordedScope("demo2", "test", hasCallStackData = true))
    }

    @Test
    fun `empty callers should signal DISABLED_OR_NO_DATA when the method's application has no call stack data`() {
        // hello() is recorded only in demo/test; the call-stack rows of that env all belong to demo2
        val result = sut.getCallers(1, "com.example.demo.MyController.hello()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA)
        assertThat(result.recordedIn).containsExactly(McpMethodCallersDto.RecordedScope("demo", "test", hasCallStackData = false))
    }

    @Test
    fun `customer without any call stack rows should signal DISABLED_OR_NO_DATA`() {
        val result = sut.getCallers(2, "com.other.App.run()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA)
    }

    @Test
    fun `env filter should scope callers to that environment`() {
        val result = sut.getCallers(1, "com.example.demo.service.AsyncService.asyncJob()", env = "test")

        assertThat(result.callers).isNotEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DATA_AVAILABLE)
    }

    @Test
    fun `env without any call stack rows should signal DISABLED_OR_NO_DATA even if other envs have data`() {
        // all call_stacks rows are in env 'test' — an empty prod result must not be read as "no callers in prod"
        val result = sut.getCallers(1, "com.example.demo.additional.AdditionalService.get()", env = "prod")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA)
        assertThat(result.recordedIn).isEmpty()
    }

    @Test
    @Transactional
    fun `should not report callers left behind by a deleted application`() {
        // demo2 holds every call-stack row; deleting it drops its invocations but leaves the call_stacks rows behind
        applicationService.deleteApplication(1, 2)

        val result = sut.getCallers(1, "com.example.demo.additional.AdditionalService.get()")

        assertThat(result.callers).isEmpty()
        assertThat(result.trackingState).isEqualTo(McpMethodCallersDto.CallStackTrackingState.DISABLED_OR_NO_DATA)
    }

    @Test
    fun `should throw METHOD_NOT_FOUND for unknown signature`() {
        assertThatThrownBy { sut.getCallers(1, "com.example.demo.NoSuch.method()") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.METHOD_NOT_FOUND)
    }
}
