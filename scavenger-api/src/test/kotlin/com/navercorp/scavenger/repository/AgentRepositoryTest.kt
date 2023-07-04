package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AgentRepositoryTest {
    @Autowired
    private lateinit var sut: AgentRepository

    @Test
    fun findAllAgentsByCustomerId() {
        assertThat(sut.findAllAgentsByCustomerId(1)).isNotNull
    }
}
