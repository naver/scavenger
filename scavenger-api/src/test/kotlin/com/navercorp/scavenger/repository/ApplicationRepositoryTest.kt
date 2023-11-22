package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationRepositoryTest {
    @Autowired
    private lateinit var sut: ApplicationRepository

    @Test
    fun findAllByCustomerId() {
        assertThat(sut.findAllByCustomerId(1)).hasSize(2)
    }

    @Test
    fun findByCustomerIdAndId() {
        assertThat(sut.findByCustomerIdAndId(1, 1)).satisfies {
            assertThat(it.id).isEqualTo(1)
        }
    }
}
