package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.exception.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MethodUsageQueryServiceTest {
    @Autowired
    private lateinit var sut: MethodUsageQueryService

    @Test
    fun `should report used when method is invoked`() {
        val result = sut.isMethodUsed(1, "com.example.demo.MyController.hello()")

        assertThat(result.used).isTrue
        assertThat(result.lastInvokedAtMillis).isEqualTo(1646027033946)
        assertThat(result.knownSinceMillis).isNotNull
    }

    @Test
    fun `should report used when method is invoked in any application`() {
        // NOT_INVOKED in app1 but INVOKED in app2 — a per-row check would call this dead
        val result = sut.isMethodUsed(1, "com.example.demo.additional.AdditionalService.get()")

        assertThat(result.used).isTrue
    }

    @Test
    fun `should report not used when method has never been invoked`() {
        val result = sut.isMethodUsed(2, "com.example.demo.legacy.LegacyService.oldMethod()")

        assertThat(result.used).isFalse
        assertThat(result.lastInvokedAtMillis).isNull()
    }

    @Test
    fun `should respect env filter for cross-env divergence`() {
        val signature = "com.example.demo.legacy.LegacyService.usedInTestOnly()"

        assertThat(sut.isMethodUsed(2, signature, env = "test").used).isTrue
        assertThat(sut.isMethodUsed(2, signature, env = "prod").used).isFalse
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

        assertThat(sut.isMethodUsed(2, "com.other.App.run()").used).isFalse
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
        assertThat(result.methods.single { it.signature.endsWith("usedInTestOnly()") }.used).isTrue
        assertThat(result.methods.single { it.signature.endsWith("oldMethod()") }.used).isFalse
        assertThat(result.unknownSignatures).containsExactly("com.example.demo.NoSuch.method()")
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
}
