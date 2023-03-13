package com.navercorp.scavenger.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CodeBaseFingerprintDaoTest {
    @Autowired
    private lateinit var sut: CodeBaseFingerprintDao

    @Test
    fun findFirstByCustomerIdAndApplicationIdAndCodeBaseFingerprint() {
        val fingerprint = sut.findAllByCustomerId(1).first()
        assertThat(
            sut.findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(
                customerId = 1,
                applicationId = fingerprint.applicationId,
                codeBaseFingerprint = fingerprint.codeBaseFingerprint
            )
        ).isNotNull
    }
}
