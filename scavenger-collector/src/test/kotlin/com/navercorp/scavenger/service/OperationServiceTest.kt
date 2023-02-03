package com.navercorp.scavenger.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OperationServiceTest {

    @Autowired
    lateinit var sut: OperationService

    @Test
    fun dispatch() {
        sut.dispatch()
        sut.dispatch()
    }
}
