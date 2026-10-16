package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.McpError
import com.navercorp.scavenger.exception.McpException
import com.navercorp.scavenger.repository.InvocationRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
class StaleMethodSearchServiceTest {
    @Autowired
    private lateinit var sut: StaleMethodSearchService

    @Autowired
    private lateinit var invocationRepository: InvocationRepository

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

        // usedInTestOnly: invoked 2026-01 — stale at 30 days
        val stale30c2 = sut.search(2, idleDays = 30, limit = 100).methods.map { it.signature }
        assertThat(stale30c2).contains(USED_IN_TEST_ONLY)
    }

    @Test
    @Transactional
    fun `idleDays should not report a method invoked after the cutoff`() {
        // recorded in scope since the seed; only its last invocation moves inside the window
        val invocation = invocationRepository.findById(USED_IN_TEST_ONLY_INVOCATION_ID).orElseThrow()
        invocationRepository.save(invocation.copy(invokedAtMillis = Instant.now().minus(5, ChronoUnit.DAYS).toEpochMilli()))

        val stale30 = sut.search(2, idleDays = 30, limit = 100).methods.map { it.signature }

        assertThat(stale30).doesNotContain(USED_IN_TEST_ONLY)
    }

    @Test
    fun `idleDays should not count a method as idle for longer than the scope has recorded it`() {
        // oldMethod has never been invoked; every row in scope was recorded at the seed time
        val daysSinceSeed = ChronoUnit.DAYS.between(SEED_RECORDED_AT, Instant.now()).toInt()

        val longerThanRecorded = sut.search(2, idleDays = daysSinceSeed + 30, limit = 100).methods.map { it.signature }
        val withinRecorded = sut.search(2, idleDays = daysSinceSeed - 1, limit = 100).methods.map { it.signature }

        assertThat(longerThanRecorded).doesNotContain(OLD_METHOD)
        assertThat(withinRecorded).contains(OLD_METHOD)
    }

    @Test
    fun `results should carry when the scope started recording the method`() {
        val result = sut.search(2, env = "prod", neverInvoked = true, limit = 100)

        val observedSince = result.methods.single { it.signature == OLD_METHOD }.observedInScopeSinceMillis?.let(Instant::ofEpochMilli)
        // seed rows are zone-less TIMESTAMPs read in the JVM time zone
        assertThat(observedSince).isBetween(SEED_RECORDED_AT.minus(1, ChronoUnit.DAYS), SEED_RECORDED_AT.plus(1, ChronoUnit.DAYS))
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

    companion object {
        private const val OLD_METHOD = "com.example.demo.legacy.LegacyService.oldMethod()"
        private const val USED_IN_TEST_ONLY = "com.example.demo.legacy.LegacyService.usedInTestOnly()"

        // test-data-set-1.1.5.sql: customer 2 invocation rows are created at this time; id 38 is usedInTestOnly INVOKED in env 'test'
        private val SEED_RECORDED_AT: Instant = Instant.parse("2026-01-05T09:02:00Z")
        private const val USED_IN_TEST_ONLY_INVOCATION_ID = 38L
    }
}
