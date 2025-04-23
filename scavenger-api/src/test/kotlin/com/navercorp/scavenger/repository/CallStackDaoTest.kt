package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CallStackDaoTest {
    @Autowired
    private lateinit var sut: CallStackDao

    @Test
    fun testFindCallerSignatures() {
        val customerId = 1L
        val applicationIds = listOf(2L)
        val environmentIds = listOf(1L)
        val signature = "com.example.demo.additional.AdditionalService.get()"
        val filterInvokedAtMillis = null

        val result = sut.findCallerSignatures(customerId, applicationIds, environmentIds, signature, filterInvokedAtMillis)

        assertThat(result).containsExactlyInAnyOrder(
            "com.example.demo.controller.MyController.additional()",
            "com.example.demo.controller.MyController\$MyTest.nesting()"
        )
    }
 }
