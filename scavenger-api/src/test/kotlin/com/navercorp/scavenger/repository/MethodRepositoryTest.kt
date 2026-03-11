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

    @Test
    fun `findAllByCustomerIdAndSignatureIn returns matched methods`() {
        val signatures = listOf(
            "com.example.demo.MyController.hello()",
            "com.example.demo.MyController.additional()"
        )
        val result = sut.findAllByCustomerIdAndSignatureIn(1, signatures)
        assertThat(result).hasSize(2)
        assertThat(result.map { it.signature }).containsExactlyInAnyOrderElementsOf(signatures)
    }

    @Test
    fun `findAllByCustomerIdAndSignatureIn ignores nonexistent signatures`() {
        val result = sut.findAllByCustomerIdAndSignatureIn(1, listOf("com.example.demo.MyController.hello()", "nonexistent()"))
        assertThat(result).hasSize(1)
    }
}
