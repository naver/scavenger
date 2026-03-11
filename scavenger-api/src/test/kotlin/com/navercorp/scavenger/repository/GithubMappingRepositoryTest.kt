package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class GithubMappingRepositoryTest {
    @Autowired
    private lateinit var sut: GithubMappingRepository

    @Test
    fun findAllByCustomerId() {
        assertThat(sut.findAllByCustomerId(1)).hasSize(1)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndId() {
        sut.deleteByCustomerIdAndId(1, 1)

        assertThat(sut.findAllByCustomerId(1)).hasSize(0)
    }

    @Test
    fun `findAllByUrlIn returns mapping for known URL`() {
        val url = "https://github_url/tree/develop/scavenger-demo/src/main/kotlin/com/example/demo"
        val result = sut.findAllByUrlIn(listOf(url))
        assertThat(result).hasSize(1)
        assertThat(result.first().url).isEqualTo(url)
    }

    @Test
    fun `findAllByUrlIn returns empty for unknown URL`() {
        assertThat(sut.findAllByUrlIn(listOf("https://unknown-url/repo"))).isEmpty()
    }
}
