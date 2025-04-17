package com.navercorp.scavenger.repository

import com.navercorp.scavenger.param.CallStackUpsertParam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class CallStackDaoTest {
    @Autowired
    private lateinit var sut: CallStackDao

    private val customerId: Long = 2
    private val applicationId: Long = 11000016928
    private val environmentId: Long = 11000023250
    private val callTraces: List<Pair<String, String>> = listOf("callee1" to "caller1", "callee2" to "caller2")

    private val param = callTraces.map {
        CallStackUpsertParam(
            customerId = customerId,
            applicationId = applicationId,
            environmentId = environmentId,
            signatureHash = it.first,
            callerSignatureHash = it.second,
            invokedAtMillis = 0
        )
    }

    @Test
    fun batchUpsert() {
        assertThat(sut.findAll()).isEmpty()

        sut.batchUpsert(param)

        assertThat(sut.findAll()).isNotEmpty
    }

    @Test
    fun batchUpsert_twice() {
        sut.batchUpsert(param)
        sut.batchUpsert(param)

        assertThat(sut.findAll()).size().isEqualTo(2)
    }
}
