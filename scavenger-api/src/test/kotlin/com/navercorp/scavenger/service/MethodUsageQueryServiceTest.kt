package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.dto.McpMethodUsageDto
import com.navercorp.scavenger.exception.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
class MethodUsageQueryServiceTest {
    @Autowired
    private lateinit var sut: MethodUsageQueryService

    @Autowired
    private lateinit var applicationService: ApplicationService

    @Test
    fun `should report used when method is invoked`() {
        val result = sut.isMethodUsed(1, "com.example.demo.MyController.hello()")

        assertThat(result.usage).isEqualTo(McpMethodUsageDto.UsageState.INVOKED)
        assertThat(result.lastInvokedAtMillis).isEqualTo(1646027033946)
        assertThat(result.knownSinceMillis).isNotNull
    }

    @Test
    fun `should report used when method is invoked in any application`() {
        // NOT_INVOKED in app1 but INVOKED in app2 — a per-row check would call this dead
        val result = sut.isMethodUsed(1, "com.example.demo.additional.AdditionalService.get()")

        assertThat(result.usage).isEqualTo(McpMethodUsageDto.UsageState.INVOKED)
    }

    @Test
    fun `should report not used when method has never been invoked`() {
        val result = sut.isMethodUsed(2, "com.example.demo.legacy.LegacyService.oldMethod()")

        assertThat(result.usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_INVOKED)
        assertThat(result.lastInvokedAtMillis).isNull()
    }

    @Test
    fun `should respect env filter for cross-env divergence`() {
        val signature = "com.example.demo.legacy.LegacyService.usedInTestOnly()"

        assertThat(sut.isMethodUsed(2, signature, env = "test").usage).isEqualTo(McpMethodUsageDto.UsageState.INVOKED)
        assertThat(sut.isMethodUsed(2, signature, env = "prod").usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_INVOKED)
    }

    @Test
    fun `should report NOT_OBSERVED_IN_SCOPE when the env holds no record of the method`() {
        // run() is recorded only in env 'test' — prod has no evidence either way, which is not "not invoked"
        val result = sut.isMethodUsed(2, "com.other.App.run()", env = "prod")

        assertThat(result.usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_OBSERVED_IN_SCOPE)
    }

    @Test
    fun `should report when the scope started recording the method`() {
        val recorded = sut.isMethodUsed(2, "com.example.demo.legacy.LegacyService.oldMethod()", env = "prod")
        val notRecorded = sut.isMethodUsed(2, "com.other.App.run()", env = "prod")

        // seed rows are zone-less TIMESTAMPs read in the JVM time zone
        assertThat(recorded.observedInScopeSinceMillis?.let(Instant::ofEpochMilli))
            .isBetween(SEED_RECORDED_AT.minus(1, ChronoUnit.DAYS), SEED_RECORDED_AT.plus(1, ChronoUnit.DAYS))
        assertThat(notRecorded.observedInScopeSinceMillis).isNull()
    }

    @Test
    @Transactional
    fun `should report NOT_OBSERVED_IN_SCOPE after the method's application is deleted`() {
        // deleting an application drops its invocations but keeps the methods rows
        applicationService.deleteApplication(2, 3)

        val result = sut.isMethodUsed(2, "com.other.App.run()")

        assertThat(result.usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_OBSERVED_IN_SCOPE)
    }

    @Test
    fun `should throw METHOD_NOT_FOUND for unknown signature`() {
        assertThatThrownBy { sut.isMethodUsed(1, "com.example.demo.NoSuch.method()") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.METHOD_NOT_FOUND)
    }

    @Test
    fun `should throw METHOD_NOT_FOUND for garbage method`() {
        assertThatThrownBy { sut.isMethodUsed(2, "com.example.demo.legacy.LegacyService.removedMethod()") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.METHOD_NOT_FOUND)
    }

    @Test
    fun `should not see another customer's method`() {
        assertThatThrownBy { sut.isMethodUsed(1, "com.other.App.run()") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.METHOD_NOT_FOUND)

        assertThat(sut.isMethodUsed(2, "com.other.App.run()").usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_INVOKED)
    }

    @Test
    fun `should throw INVALID_ARGUMENT for unknown env`() {
        assertThatThrownBy { sut.isMethodUsed(1, "com.example.demo.MyController.hello()", env = "nope") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `getPrImpact should split found and unknown signatures`() {
        val result = sut.getPrImpact(
            2,
            listOf(
                "com.example.demo.legacy.LegacyService.usedInTestOnly()",
                "com.example.demo.legacy.LegacyService.oldMethod()",
                "com.example.demo.NoSuch.method()"
            )
        )

        assertThat(result.methods).hasSize(2)
        assertThat(result.methods.single { it.signature.endsWith("usedInTestOnly()") }.usage).isEqualTo(McpMethodUsageDto.UsageState.INVOKED)
        assertThat(result.methods.single { it.signature.endsWith("oldMethod()") }.usage).isEqualTo(McpMethodUsageDto.UsageState.NOT_INVOKED)
        assertThat(result.unknownSignatures).containsExactly("com.example.demo.NoSuch.method()")
    }

    @Test
    fun `getPrImpact should keep methods without a record in the env apart from NOT_INVOKED`() {
        val result = sut.getPrImpact(
            2,
            listOf("com.other.App.run()", "com.example.demo.legacy.LegacyService.oldMethod()"),
            env = "prod"
        )

        assertThat(result.methods.single { it.signature == "com.other.App.run()" }.usage)
            .isEqualTo(McpMethodUsageDto.UsageState.NOT_OBSERVED_IN_SCOPE)
        assertThat(result.methods.single { it.signature.endsWith("oldMethod()") }.usage)
            .isEqualTo(McpMethodUsageDto.UsageState.NOT_INVOKED)
        assertThat(result.unknownSignatures).isEmpty()
    }

    @Test
    fun `getPrImpact should reject empty and oversized input`() {
        assertThatThrownBy { sut.getPrImpact(1, emptyList()) }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)

        val tooMany = (1..201).map { "com.example.demo.Foo.bar$it()" }
        assertThatThrownBy { sut.getPrImpact(1, tooMany) }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `should reject blank or oversized signature`() {
        assertThatThrownBy { sut.isMethodUsed(1, " ") }
            .isInstanceOf(McpException::class.java)

        assertThatThrownBy { sut.isMethodUsed(1, "a".repeat(1001)) }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    companion object {
        // test-data-set-1.1.5.sql: customer 2 invocation rows are created at this time
        private val SEED_RECORDED_AT: Instant = Instant.parse("2026-01-05T09:02:00Z")
    }
}
