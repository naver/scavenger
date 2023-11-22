package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.AgentStateEntity
import com.navercorp.scavenger.entity.CodeBaseFingerprintEntity
import com.navercorp.scavenger.entity.JvmEntity
import com.navercorp.scavenger.repository.AgentStateDao
import com.navercorp.scavenger.repository.CodeBaseFingerprintDao
import com.navercorp.scavenger.repository.InvocationDao
import com.navercorp.scavenger.repository.JvmDao
import com.navercorp.scavenger.repository.MethodDao
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
@Nested
@SpringBootTest
@DisplayName("GarbageCollectService class")
class GarbageCollectServiceTest {
    @Autowired
    lateinit var sut: GarbageCollectService

    @Autowired
    lateinit var jvmDao: JvmDao

    @Autowired
    lateinit var agentStateDao: AgentStateDao

    @Autowired
    lateinit var codeBaseFingerprintDao: CodeBaseFingerprintDao

    @Autowired
    lateinit var methodDao: MethodDao

    @Autowired
    lateinit var invocationDao: InvocationDao

    val customerId = 1L

    @Nested
    @DisplayName("if expired agent state exists")
    inner class ExpiredAgentStateAndJvm {
        private lateinit var expiredJvmEntity: JvmEntity
        private lateinit var expiredAgentState: AgentStateEntity
        val now = Instant.now()
        val ago = now.minus(600 + IntervalService.Companion.GC_DEAD_MARGIN_MINUTES * 60, ChronoUnit.SECONDS)

        @BeforeEach
        fun prepareExpiredAgentState() {
            expiredAgentState = agentStateDao.insert(
                AgentStateEntity(
                    customerId = customerId,
                    jvmUuid = "uuid",
                    createdAt = ago,
                    lastPolledAt = ago,
                    nextPollExpectedAt = ago.plusSeconds(60),
                    timestamp = ago,
                    enabled = true,
                )
            )
            expiredJvmEntity = jvmDao.insert(
                JvmEntity(
                    customerId = customerId,
                    applicationId = 1,
                    environmentId = 1,
                    uuid = "uuid",
                    codeBaseFingerprint = null,
                    createdAt = ago,
                    publishedAt = ago,
                    hostname = "hostname",
                )
            )

            assertThat(jvmDao.findById(expiredJvmEntity.id)).isPresent
            assertThat(agentStateDao.findById(expiredAgentState.id)).isPresent
        }

        @Test
        @DisplayName("it removes expired agent state")
        fun sweepAgentStates_removeWhenAgentExpired() {
            sut.sweepAgentStatesAndJvms(customerId, now.plusSeconds(3000))
            assertThat(agentStateDao.findById(expiredAgentState.id)).isEmpty
            assertThat(jvmDao.findById(expiredJvmEntity.id)).isEmpty
        }

        @Test
        @DisplayName("it does not remove not expired agent state")
        fun sweepAgentStates_notRemoveWhenAgentExpired() {
            sut.sweepAgentStatesAndJvms(customerId, now)
            assertThat(agentStateDao.findById(expiredAgentState.id)).isNotEmpty
            assertThat(jvmDao.findById(expiredJvmEntity.id)).isNotEmpty
        }
    }

    @Nested
    @DisplayName("if expired codeBaseFingerprint exists")
    inner class ExpiredCodeBaseFingerprint {
        val now = Instant.now()
        val ago = now.minus(600 + IntervalService.Companion.GC_DEAD_MARGIN_MINUTES * 60, ChronoUnit.SECONDS)

        @BeforeEach
        fun beforeEach() {
            // Clean up first
            codeBaseFingerprintDao.findAllByCustomerId(customerId).forEach {
                codeBaseFingerprintDao.deleteById(it.id)
            }

            val finger = CodeBaseFingerprintEntity(codeBaseFingerprint = "finger1", applicationId = 1, customerId = customerId, publishedAt = ago)
            for (i in 1..3) {
                codeBaseFingerprintDao.insert(finger.copy(codeBaseFingerprint = "finger$i"))
            }

            val jvmEntity = JvmEntity(
                customerId = customerId,
                applicationId = 1,
                environmentId = 1,
                uuid = "jvmUuid",
                codeBaseFingerprint = null,
                createdAt = ago,
                publishedAt = ago,
                hostname = "hostname",
            )

            for (i in 1..3) {
                jvmDao.insert(jvmEntity.copy(uuid = "jvmUuid$i", codeBaseFingerprint = "finger$i"))
            }
        }

        @Test
        @DisplayName("it removes expired codeBaseFingerprint")
        fun sweepCodeBaseFingerprint_removeCodeBaseFingerPrintNotUsing() {
            val jvms = jvmDao.findAllByCustomerId(customerId)
            jvmDao.deleteById(jvms.map { it.id }.first())
            sut.sweepCodeBaseFingerprints(customerId)
            assertThat(codeBaseFingerprintDao.findAllByCustomerId(customerId).map { it.codeBaseFingerprint })
                .satisfies {
                    assertThat(it).isNotEmpty
                    assertThat(it).doesNotContain(jvms.first().codeBaseFingerprint)
                }
        }

        @Test
        @DisplayName("it does not remove not expired codeBaseFingerprint")
        fun sweepCodeBaseFingerprint_notRemoveCodeBaseFingerPrintUsing() {
            sut.sweepAgentStatesAndJvms(customerId, now)
            assertThat(codeBaseFingerprintDao.findAllByCustomerId(customerId))
                .satisfies {
                    assertThat(it).hasSize(3)
                }
        }
    }

    @Nested
    @DisplayName("if expired method exists")
    inner class ExpiredMethod {
        val weekAgo = 7 * 24 * 60 * 60 * 1000L
        var min: Instant? = null

        @BeforeEach
        fun beforeEach() {
            val methods = methodDao.findAllByCustomerId(customerId)
            val allLiveFingerprints = codeBaseFingerprintDao.findAllByCustomerId(customerId)
            min = allLiveFingerprints.minByOrNull { it.createdAt }?.createdAt ?: Instant.now()
            methodDao.update(methods[0].copy(lastSeenAtMillis = min?.minusMillis(weekAgo)?.toEpochMilli()))
            methodDao.update(methods[1].copy(lastSeenAtMillis = min?.minusMillis(weekAgo)?.toEpochMilli()))
            methodDao.update(methods[2].copy(lastSeenAtMillis = min?.toEpochMilli()))
            methodDao.update(methods[3].copy(lastSeenAtMillis = min?.toEpochMilli()))
            methodDao.update(methods[4].copy(lastSeenAtMillis = min?.toEpochMilli()))
        }

        @Test
        @DisplayName("it marks expired methods")
        fun markMethods_markExpiredMethod() {
            assertThat(methodDao.findAllByCustomerId(customerId)).allSatisfy {
                assertThat(it.garbage).isFalse()
            }
            sut.markMethods(customerId)
            assertThat(methodDao.findAllByCustomerId(customerId).filter { it.garbage }).satisfies {
                assertThat(it).hasSize(2)
            }
        }

        @Test
        @DisplayName("it deletes marked methods")
        fun sweepMethods_sweepMarkMethod() {
            sut.markMethods(customerId)
            assertThat(methodDao.findAllByCustomerId(customerId).filter { it.garbage }).hasSize(2)
            val invocations = invocationDao.findAllByCustomerId(customerId)
            assertThat(invocations).hasSizeGreaterThan(0)
            sut.sweepMethods(customerId, min!!)
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("nothing is deleted when the 1 week is not passed since last seen")
                .hasSize(34)
            assertThat(invocationDao.findAllByCustomerId(customerId))
                .describedAs("invocations should be same as before")
                .hasSize(invocations.size)
            sut.sweepMethods(customerId, min!!.plusSeconds(10))
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("marked method is deleted when the more than 1 week is passed after last seen")
                .hasSize(32)
            val invocationsAfter = invocationDao.findAllByCustomerId(customerId)
            assertThat(invocationsAfter)
                .describedAs("invocations should be deleted as well")
                .hasSizeLessThan(invocations.size)
            sut.sweepMethods(customerId, Instant.now())
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("nothing more is deleted when no marking is done")
                .hasSize(32)
        }

        @Test
        @DisplayName("it deletes marked methods")
        fun sweepMethods_sweepMarkMethodWithInvocationUpdate() {
            sut.markMethods(customerId)
            assertThat(methodDao.findAllByCustomerId(customerId).filter { it.garbage }).hasSize(2)
            val invocations = invocationDao.findAllByCustomerId(customerId)
            assertThat(invocations).hasSizeGreaterThan(0)
            sut.sweepMethods(customerId, min!!)
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("nothing is deleted when the 1 week is not passed since last seen")
                .hasSize(34)
            assertThat(invocationDao.findAllByCustomerId(customerId))
                .describedAs("invocations should be same as before")
                .hasSize(invocations.size)
            sut.sweepMethods(customerId, min!!.plusSeconds(10))
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("marked method is deleted when the more than 1 week is passed after last seen")
                .hasSize(32)
            val invocationsAfter = invocationDao.findAllByCustomerId(customerId)
            assertThat(invocationsAfter)
                .describedAs("invocations should be deleted as well")
                .hasSizeLessThan(invocations.size)
            sut.sweepMethods(customerId, Instant.now())
            assertThat(methodDao.findAllByCustomerId(customerId))
                .describedAs("nothing more is deleted when no marking is done")
                .hasSize(32)
        }
    }

    @Nested
    @DisplayName("if without agent state exists")
    inner class WithoutAgentJvms {
        val now = Instant.now()
        val ago = now.minus(600 + IntervalService.Companion.GC_DEAD_MARGIN_MINUTES * 60, ChronoUnit.SECONDS)

        @BeforeEach
        fun beforeEach() {
            // Clean up first
            jvmDao.findAllByCustomerId(customerId).forEach {
                jvmDao.deleteById(it.id)
            }

            jvmDao.insert(
                JvmEntity(
                    customerId = customerId,
                    applicationId = 1,
                    environmentId = 1,
                    uuid = "uuid",
                    codeBaseFingerprint = null,
                    createdAt = ago,
                    publishedAt = ago,
                    hostname = "hostname",
                )
            )

            jvmDao.insert(
                JvmEntity(
                    customerId = customerId,
                    applicationId = 1,
                    environmentId = 1,
                    uuid = "withoutUUID",
                    codeBaseFingerprint = null,
                    createdAt = ago,
                    publishedAt = ago,
                    hostname = "hostname",
                )
            )

            agentStateDao.insert(
                AgentStateEntity(
                    customerId = customerId,
                    jvmUuid = "uuid",
                    createdAt = ago,
                    lastPolledAt = ago,
                    nextPollExpectedAt = ago.plusSeconds(60),
                    timestamp = ago,
                    enabled = true,
                )
            )
        }

        @Test
        fun sweepAgentStatesAndJvms_removeWithoutAgentJvms() {
            sut.sweepAgentStatesAndJvms(customerId, now)
            assertThat(jvmDao.findAllByCustomerId(customerId).size).isEqualTo(1)
        }
    }
}
