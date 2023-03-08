package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.MethodEntity
import com.navercorp.scavenger.param.MethodUpsertParam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@Transactional
class MethodDaoTest {
    @Autowired
    private lateinit var sut: MethodDao

    private val instant = Instant.now().minusSeconds(60)

    @BeforeEach
    fun setup() {
        val param1 = MethodUpsertParam(
            customerId = 2,
            visibility = "public",
            signature = "com.example.demo.MyController.vitess1()",
            createdAt = instant,
            lastSeenAtMillis = instant.toEpochMilli(),
            declaringType = "com.example.demo.MyController",
            methodName = "additional",
            modifiers = "public",
            garbage = false,
            signatureHash = "signatureHash1"
        )
        val param2 = MethodUpsertParam(
            customerId = 2,
            visibility = "public",
            signature = "com.example.demo.MyController.vitess2()",
            createdAt = instant,
            lastSeenAtMillis = instant.toEpochMilli(),
            declaringType = "com.example.demo.MyController",
            methodName = "additional",
            modifiers = "public",
            garbage = false,
            signatureHash = "signatureHash2"
        )

        sut.batchUpsert(listOf(param1, param2))

        sut.insert(
            MethodEntity(
                customerId = 2,
                createdAt = instant.minus(1, ChronoUnit.HOURS),
                garbage = true,
                signatureHash = "garbageSignatureHash2"
            )
        )
    }

    @Test
    fun batchUpsert() {
        val instant = Instant.now().minusSeconds(30)
        val param = MethodUpsertParam(
            customerId = 2,
            visibility = "public",
            signature = "com.example.demo.MyController.vitess1()",
            createdAt = instant,
            lastSeenAtMillis = instant.toEpochMilli(),
            declaringType = "com.example.demo.MyController",
            methodName = "additional",
            modifiers = "public",
            garbage = false,
            signatureHash = "signatureHash1"
        )

        sut.batchUpsert(listOf(param))

        assertThat(sut.findByCustomerIdAndSignatureHash(2, "signatureHash1")).isNotNull

        sut.batchUpsert(listOf(param))

        assertThat(sut.findByCustomerIdAndSignatureHash(2, "signatureHash1")).isNotNull
    }

    @Test
    fun updateSetGarbageLastSeenBefore() {
        sut.updateSetGarbageLastSeenBefore(2, Instant.now().toEpochMilli())

        val actual = sut.findByCustomerIdAndSignatureHash(2, "signatureHash1")

        assertThat(actual).isNotNull
        assertThat(actual!!.garbage).isTrue
    }

    @Test
    fun findAllByCustomerIdAndSignatureHashIn() {
        assertThat(
            sut.findAllByCustomerIdAndSignatureHashIn(
                2,
                listOf(
                    "signatureHash1",
                    "signatureHash2"
                )
            )
        ).hasSize(2)
    }

    @Test
    fun findByCustomerIdAndSignature() {
        assertThat(sut.findByCustomerIdAndSignatureHash(2, "signatureHash1")).isNotNull
    }
}
