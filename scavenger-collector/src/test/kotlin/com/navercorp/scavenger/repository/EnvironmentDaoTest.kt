package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class EnvironmentDaoTest {
    @Autowired
    lateinit var sut: EnvironmentDao

    @Test
    fun makeRepositoryMethodsWork() {
        assertDoesNotThrow {
            sut.findById(1)
            sut.findByCustomerIdAndName(1, "test")
        }
    }

    @Test
    fun upsert() {
        assertThat(sut.upsert(1, "vitess", Instant.now())).isGreaterThanOrEqualTo(0L)
    }

    @Test
    fun findFirstByCustomerIdAndName() {
        assertThat(sut.findByCustomerIdAndName(1, "test")).isNotNull
    }

    @Test
    fun findByCustomerIdAndId() {
        assertThat(sut.findByCustomerIdAndId(1, 1)).isNotNull
    }
}
