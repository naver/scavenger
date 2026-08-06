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

    @Test
    fun `findByLicenseKey returns customer when key matches`() {
        val result = sut.findByLicenseKey("4c94e0dd-ad04-4b17-9238-f46bba75c684")

        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(1)
        assertThat(result.get().name).isEqualTo("demo")
    }

    @Test
    fun `findByLicenseKey returns empty when key is unknown`() {
        assertThat(sut.findByLicenseKey("unknown-key")).isEmpty
    }
}
