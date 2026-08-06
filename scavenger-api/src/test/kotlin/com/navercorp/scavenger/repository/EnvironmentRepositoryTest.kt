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
        assertThat(sut.findAllByCustomerId(1)).hasSize(3)
    }

    @Test
    fun findByCustomerIdAndId() {
        assertThat(sut.findByCustomerIdAndId(1, 1)).satisfies({
            assertThat(it.id).isEqualTo(1)
        })
    }

    @Test
    fun `findByCustomerIdAndName returns environment when name matches`() {
        val result = sut.findByCustomerIdAndName(1, "prod")

        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(2)
        assertThat(result?.enabled).isTrue
    }

    @Test
    fun `findByCustomerIdAndName returns null when name is unknown`() {
        assertThat(sut.findByCustomerIdAndName(1, "no-such-env")).isNull()
    }

    @Test
    fun `findByCustomerIdAndName scopes the same name to each customer`() {
        assertThat(sut.findByCustomerIdAndName(1, "prod")?.id).isEqualTo(2)
        assertThat(sut.findByCustomerIdAndName(2, "prod")?.id).isEqualTo(5)
        assertThat(sut.findByCustomerIdAndName(2, "no-such")).isNull()
    }
}
