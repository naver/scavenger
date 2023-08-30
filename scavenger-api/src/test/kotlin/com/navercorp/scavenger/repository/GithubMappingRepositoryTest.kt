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
}
