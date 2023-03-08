package com.navercorp.scavenger.repository

import com.navercorp.scavenger.entity.InvocationEntity
import com.navercorp.scavenger.param.InvocationUpsertParam
import io.codekvast.javaagent.model.v4.SignatureStatus4
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@SpringBootTest
class InvocationDaoTest {
    @Autowired
    private lateinit var sut: InvocationDao

    private val customerId: Long = 2
    private val applicationId: Long = 11000016928
    private val environmentId: Long = 11000023250
    private val signatureHash: List<String> = listOf("5c9649fa5159f5af439d86f6008e3960", "4a257820e03cc0876387a224ae026dea")

    private val params = signatureHash.map {
        InvocationUpsertParam(
            customerId = customerId,
            applicationId = applicationId,
            environmentId = environmentId,
            status = SignatureStatus4.INVOKED.name,
            invokedAtMillis = 0,
            lastSeenAtMillis = 0,
            signatureHash = it,
        )
    }

    @Test
    fun batchUpsertCodeBase() {
        sut.batchUpsertLastSeen(params)

        assertThat(sut.findAllByCustomerIdAndSignatureHashIn(customerId, signatureHash)).isNotEmpty

        sut.batchUpsertLastSeen(params)

        assertThat(sut.findAllByCustomerIdAndSignatureHashIn(customerId, signatureHash)).isNotEmpty
    }

    @Test
    fun batchUpsert() {
        sut.batchUpsert(params)
        sut.batchUpsert(params)

        assertThat(sut.findAllByCustomerIdAndSignatureHashIn(customerId, signatureHash)).isNotEmpty
    }

    @Test
    fun countInvocationsByCustomerIdAndApplicationIdAndEnvironmentId() {
        assertThat(sut.hasNotInvokedInvocation(customerId, applicationId, environmentId)).isFalse
        sut.insert(
            InvocationEntity(
                id = 10000L,
                customerId = customerId,
                applicationId = applicationId,
                environmentId = environmentId,
                signatureHash = "111",
                invokedAtMillis = 1,
                createdAt = Instant.now(),
                status = SignatureStatus4.NOT_INVOKED.name,
                lastSeenAtMillis = 10,
                timestamp = Instant.now()
            )
        )
        assertThat(sut.hasNotInvokedInvocation(customerId, applicationId, environmentId)).isTrue
    }
}
