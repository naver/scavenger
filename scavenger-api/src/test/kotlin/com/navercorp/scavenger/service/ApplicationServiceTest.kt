package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.ApplicationRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class ApplicationServiceTest {
    @Autowired
    private lateinit var sut: ApplicationService

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Test
    @Transactional
    fun deleteApplication() {
        sut.deleteApplication(1, 1)

        assertThrows(EmptyResultDataAccessException::class.java) { applicationRepository.findByCustomerIdAndId(1, 1) }
    }
}
