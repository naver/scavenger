package com.navercorp.scavenger.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CallStackServiceTest {
     @Autowired
     private lateinit var sut: CallStackService

     @Test
     fun getCallerSignatures() {
         val customerId = 1L
         val snapshotId = 4L
         val signature = "com.example.demo.additional.AdditionalService.get()"

         val result = sut.getCallerSignatures(customerId, snapshotId, signature)

         assertThat(result).containsExactlyInAnyOrder(
             "com.example.demo.controller.MyController.additional()",
             "com.example.demo.controller.MyController\$MyTest.nesting()"
         )
     }
 }
