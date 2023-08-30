package com.navercorp.scavenger.repository

import com.navercorp.scavenger.param.JvmUpsertParam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JvmDaoTest {
    @Autowired
    private lateinit var sut: JvmDao

    private val applicationId: Long = 11000016928
    private val environmentId: Long = 11000023250

    @BeforeEach
    fun setup() {
        val instant = Instant.now().minusSeconds(60)
        val param = JvmUpsertParam(
            customerId = 2,
            applicationId = applicationId,
            applicationVersion = "unspecified",
            environmentId = environmentId,
            uuid = "d0dfa3c2-809c-428f-b501-7419196d91c5",
            codeBaseFingerprint = "CodeBaseFingerprint(numClassFiles=0, numJarFiles=1, " +
                "sha256=b90e15678202f78cee45fa05cef8cba7c070114e37e81ff9131858c1d9c488c7)",
            publishedAt = instant,
            createdAt = instant,
            hostname = "AL01978856.local",
        )

        val paramWithoutAgent = JvmUpsertParam(
            customerId = 3,
            applicationId = applicationId,
            applicationVersion = "unspecified",
            environmentId = environmentId,
            uuid = "9ec4624c-5b81-44fa-82c8-74233095b120",
            codeBaseFingerprint = "CodeBaseFingerprint(numClassFiles=0, numJarFiles=1, " +
                "sha256=b90e15678202f78cee45fa05cef8cba7c070114e37e81ff9131858c1d9c488c7)",
            publishedAt = instant,
            createdAt = instant,
            hostname = "AL01978856.local",
        )

        sut.upsert(param)
        sut.upsert(paramWithoutAgent)
    }

    @Test
    @Transactional
    fun upsert() {
        val param = JvmUpsertParam(
            customerId = 2,
            applicationId = applicationId,
            applicationVersion = "unspecified",
            environmentId = environmentId,
            uuid = "d0dfa3c2-809c-428f-b501-7419196d91c5",
            codeBaseFingerprint = "CodeBaseFingerprint(numClassFiles=0, numJarFiles=1, " +
                "sha256=b90e15678202f78cee45fa05cef8cba7c070114e37e81ff9131858c1d9c488c7)",
            publishedAt = Instant.now(),
            createdAt = Instant.now(),
            hostname = "AL01978856.local",
        )

        sut.upsert(param)

        assertThat(sut.findByCustomerIdAndUuid(2, "d0dfa3c2-809c-428f-b501-7419196d91c5")).isNotNull
    }

    @Test
    fun findAllByCustomerId() {
        assertThat(sut.findAllByCustomerId(2)).isNotEmpty
    }

    @Test
    fun selectUuidsByWithoutAgent() {
        val jvmUuids = sut.findAllUuidsByWithoutAgent(3)
        assertThat(jvmUuids).isNotEmpty
        assertThat(jvmUuids).contains("9ec4624c-5b81-44fa-82c8-74233095b120")
    }
}
