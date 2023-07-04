package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SnapshotRepositoryTest {
    @Autowired
    private lateinit var sut: SnapshotDao

    @Test
    fun findAllByCustomerId() {
        assertThat(sut.findAllByCustomerId(1)).hasSizeGreaterThan(0)
    }

    @Test
    fun findByCustomerIdAndId() {
        assertThat(sut.findByCustomerIdAndId(1, 1)).isPresent
    }

    @Test
    fun findAllByCustomerIdForUpdate() {
        assertThat(sut.findAllByCustomerIdForUpdate(1)).hasSizeGreaterThan(0)
    }
}
