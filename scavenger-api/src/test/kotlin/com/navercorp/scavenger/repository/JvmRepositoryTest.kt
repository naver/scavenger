package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class JvmRepositoryTest {
    @Autowired
    private lateinit var sut: JvmRepository

    @Test
    fun countByCustomerIdAndApplicationId() {
        assertThat(sut.countByCustomerIdAndApplicationId(1, 1)).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun countByCustomerIdAndEnvironmentId() {
        assertThat(sut.countByCustomerIdAndEnvironmentId(1, 1)).isGreaterThanOrEqualTo(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndApplicationId() {
        sut.deleteByCustomerIdAndApplicationId(1, 1)
        assertThat(sut.countByCustomerIdAndApplicationId(1, 1)).isEqualTo(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndEnvironmentId() {
        sut.deleteByCustomerIdAndEnvironmentId(1, 1)
        assertThat(sut.countByCustomerIdAndEnvironmentId(1, 1)).isEqualTo(0)
    }
}
