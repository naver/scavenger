package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.exception.McpException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StaleMethodSearchServiceTest {
    @Autowired
    private lateinit var sut: StaleMethodSearchService

    @Test
    fun `neverInvoked with prefix should exclude abstract, garbage and invoked methods`() {
        val result = sut.search(2, prefix = "com.example.demo.legacy", neverInvoked = true)

        assertThat(result.methods.map { it.signature })
            .containsExactly("com.example.demo.legacy.LegacyService.oldMethod()")
        // usedInTestOnly: INVOKED in test env → not neverInvoked
        // mustImplement: public abstract → excluded
        // removedMethod: garbage → excluded
    }

    @Test
    fun `should not report method invoked in another application as never invoked`() {
        // 1b3fdd...: NOT_INVOKED in app1 but INVOKED in app2 — must not appear
        val result = sut.search(1, neverInvoked = true, limit = 100)

        assertThat(result.methods.map { it.signature })
            .doesNotContain("com.example.demo.additional.AdditionalService.get()")
    }

    @Test
    fun `env filter should surface methods dead only in that environment`() {
        val result = sut.search(2, env = "prod", neverInvoked = true, limit = 100)

        assertThat(result.methods.map { it.signature }).containsExactlyInAnyOrder(
            "com.example.demo.legacy.LegacyService.oldMethod()",
            "com.example.demo.legacy.LegacyService.usedInTestOnly()"
        )
    }

    @Test
    fun `should throw INVALID_ARGUMENT for unknown or disabled env`() {
        assertThatThrownBy { sut.search(1, env = "no-such-env") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)

        assertThatThrownBy { sut.search(1, env = "staging") }
            .isInstanceOf(McpException::class.java)
            .extracting { (it as McpException).error.code }
            .isEqualTo(McpError.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `idleDays should filter on last invocation across the join scope`() {
        // hello(): invoked 2022 — stale at idleDays=30
        val stale30 = sut.search(1, idleDays = 30, limit = 100).methods.map { it.signature }
        assertThat(stale30).contains("com.example.demo.MyController.hello()")

        // usedInTestOnly: invoked 2026-01 — stale at 30 days, not stale at 365
        val stale30c2 = sut.search(2, idleDays = 30, limit = 100).methods.map { it.signature }
        assertThat(stale30c2).contains("com.example.demo.legacy.LegacyService.usedInTestOnly()")

        val stale365c2 = sut.search(2, idleDays = 365, limit = 100).methods.map { it.signature }
        assertThat(stale365c2).doesNotContain("com.example.demo.legacy.LegacyService.usedInTestOnly()")
    }

    @Test
    fun `cursor should page without duplicates or loss`() {
        val all = sut.search(2, env = "prod", neverInvoked = true, limit = 100).methods.map { it.signature }
        assertThat(all).hasSize(2)

        val seen = mutableListOf<String>()
        var cursor: String? = null
        var pages = 0
        do {
            val page = sut.search(2, env = "prod", neverInvoked = true, limit = 1, cursor = cursor)
            seen += page.methods.map { it.signature }
            cursor = page.nextCursor
            pages++
        } while (cursor != null && pages < 10)

        assertThat(seen).containsExactlyElementsOf(all)
        assertThat(seen).doesNotHaveDuplicates()
    }

    @Test
    fun `terminal page should return null cursor`() {
        val lastPage = sut.search(2, env = "prod", neverInvoked = true, limit = 100)

        assertThat(lastPage.methods.size).isLessThan(100)
        assertThat(lastPage.nextCursor).isNull()
    }

    @Test
    fun `prefix wildcards should be treated as literals`() {
        val result = sut.search(2, prefix = "com.example.demo.legacy%", neverInvoked = true)

        assertThat(result.methods).isEmpty()

        val underscore = sut.search(2, prefix = "com.example.demo.legac_", neverInvoked = true)
        assertThat(underscore.methods).isEmpty()
    }

    @Test
    fun `should reject invalid arguments`() {
        assertThatThrownBy { sut.search(1, idleDays = -1) }
            .isInstanceOf(McpException::class.java)

        assertThatThrownBy { sut.search(1, prefix = "a".repeat(257)) }
            .isInstanceOf(McpException::class.java)

        assertThatThrownBy { sut.search(1, limit = 0) }
            .isInstanceOf(McpException::class.java)

        assertThatThrownBy { sut.search(1, limit = 101) }
            .isInstanceOf(McpException::class.java)

        assertThatThrownBy { sut.search(1, cursor = "a".repeat(65)) }
            .isInstanceOf(McpException::class.java)
    }

    @Test
    fun `should not leak other customers' methods`() {
        val customer1 = sut.search(1, neverInvoked = true, limit = 100).methods.map { it.signature }

        assertThat(customer1).doesNotContain(
            "com.other.App.run()",
            "com.example.demo.legacy.LegacyService.oldMethod()"
        )
    }
}
