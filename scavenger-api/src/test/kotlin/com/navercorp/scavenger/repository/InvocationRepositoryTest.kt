package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class InvocationRepositoryTest {
    @Autowired
    private lateinit var sut: InvocationRepository

    val customerId: Long = 1
    val applicationId: Long = 1
    val environmentId: Long = 1

    @Test
    fun countByCustomerIdAndApplicationId() {
        assertThat(sut.countByCustomerIdAndApplicationId(customerId, applicationId)).isGreaterThan(0)
    }

    @Test
    fun countByCustomerIdAndEnvironmentId() {
        assertThat(sut.countByCustomerIdAndEnvironmentId(customerId, environmentId)).isGreaterThan(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndApplicationId() {
        sut.deleteByCustomerIdAndApplicationId(customerId, applicationId)
        assertThat(sut.countByCustomerIdAndApplicationId(customerId, applicationId)).isEqualTo(0)
    }

    @Test
    @Transactional
    fun deleteByCustomerIdAndEnvironmentId() {
        sut.deleteByCustomerIdAndEnvironmentId(customerId, environmentId)
        assertThat(sut.countByCustomerIdAndEnvironmentId(customerId, environmentId)).isEqualTo(0)
    }
}
