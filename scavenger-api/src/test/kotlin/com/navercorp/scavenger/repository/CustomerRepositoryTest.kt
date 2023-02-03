package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CustomerRepositoryTest {
    @Autowired
    private lateinit var sut: CustomerRepository

    @Test
    fun findById() {
        assertThat(sut.findById(1)).isPresent
    }

    @Test
    fun findByNameAndGroupId() {
        assertThat(sut.findByNameAndGroupId("notExistCustomer", "default-group")).isEmpty
    }
}
