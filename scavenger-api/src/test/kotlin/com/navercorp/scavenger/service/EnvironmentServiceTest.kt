package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.EnvironmentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class EnvironmentServiceTest {
    @Autowired
    private lateinit var sut: EnvironmentService

    @Autowired
    private lateinit var environmentRepository: EnvironmentRepository

    @Test
    @Transactional
    fun deleteByCustomerIdAndId() {
        sut.deleteEnvironment(1, 1)
        Assertions.assertThrows(EmptyResultDataAccessException::class.java) { environmentRepository.findByCustomerIdAndId(1, 1) }
    }
}
