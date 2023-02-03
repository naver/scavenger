package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MethodRepositoryTest {
    @Autowired
    private lateinit var sut: MethodRepository

    @Test
    fun findMethodInvocations() {
        assertThat(sut.countMethodSignatureHashByCustomerId(1)).isGreaterThan(0)
    }
}
