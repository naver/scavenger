package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.service.McpQueryLimits
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

// The test's mock request carries no customerId (ApiKeyAuthInterceptor never ran), so tenant resolution
// throws inside the tool — the unexpected-exception path.
@SpringBootTest
@ExtendWith(OutputCaptureExtension::class)
class ScavengerMcpToolsTest {
    @Autowired
    private lateinit var sut: ScavengerMcpTools

    @Test
    fun `unexpected failure should be logged with the tool, tenant and arguments but answered generically`(output: CapturedOutput) {
        val response = sut.isMethodUsed("com.example.demo.MyController.hello()", "prod")

        assertThat(response.error).isEqualTo(McpError(McpError.Code.INTERNAL_ERROR, "Internal error.", retryable = true))
        assertThat(output).contains(
            "tool=is_method_used customerId=null signature=\"com.example.demo.MyController.hello()\" env=\"prod\"",
            "MCP customerId not present on request"
        )
    }

    @Test
    fun `client arguments should not forge log lines or fields`(output: CapturedOutput) {
        sut.isMethodUsed("a()\nERROR forged", "prod\" customerId=\"42")

        assertThat(output)
            .doesNotContain("\nERROR forged")
            .contains("signature=\"a()\\u000aERROR forged\" env=\"prod\\u0022 customerId=\\u002242\"")
    }

    @Test
    fun `an oversized argument should be cut in the log`(output: CapturedOutput) {
        // a pre-validation failure (e.g. no DB connection) logs the raw argument before its length is checked
        sut.isMethodUsed("x".repeat(5000), null)

        assertThat(output)
            .contains("...(length=5000)")
            .doesNotContain("x".repeat(McpQueryLimits.MAX_SIGNATURE_LENGTH + 1))
    }
}
