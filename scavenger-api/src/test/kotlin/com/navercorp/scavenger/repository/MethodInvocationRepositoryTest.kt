package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MethodInvocationRepositoryTest {
    @Autowired
    private lateinit var sut: MethodInvocationRepository

    @Test
    fun findAllMethodInvocations() {
        assertThat(sut.findAllMethodInvocations(1, listOf(1), listOf(1))).hasSizeGreaterThanOrEqualTo(0)
    }
}
