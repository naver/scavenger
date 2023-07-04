package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class EnvironmentRepositoryTest {
    @Autowired
    private lateinit var sut: EnvironmentRepository

    @Test
    fun findAllByCustomerId() {
        assertThat(sut.findAllByCustomerId(1)).hasSize(1)
    }

    @Test
    fun findByCustomerIdAndId() {
        assertThat(sut.findByCustomerIdAndId(1, 1)).satisfies {
            assertThat(it.id).isEqualTo(1)
        }
    }
}
