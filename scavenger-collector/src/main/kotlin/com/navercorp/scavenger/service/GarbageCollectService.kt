package com.navercorp.scavenger.service

import com.navercorp.scavenger.leader.EventListener
import com.navercorp.scavenger.leader.LeadershipContext
import com.navercorp.scavenger.repository.AgentStateDao
import com.navercorp.scavenger.repository.CodeBaseFingerprintDao
import com.navercorp.scavenger.repository.CustomerDao
import com.navercorp.scavenger.repository.InvocationDao
import com.navercorp.scavenger.repository.JvmDao
import com.navercorp.scavenger.repository.MethodDao
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct
import kotlin.system.measureTimeMillis

@Service
class GarbageCollectService(
    val jvmDao: JvmDao,
    val customerDao: CustomerDao,
    val codeBaseFingerprintDao: CodeBaseFingerprintDao,
    val agentStateDao: AgentStateDao,
    val methodDao: MethodDao,
    val invocationDao: InvocationDao,
    val intervalService: IntervalService,
    val leadershipService: LeadershipService,
    val operationService: OperationService,
    val transactionTemplate: TransactionTemplate,
) {
    val logger = KotlinLogging.logger {}

    var leader: Boolean = false

    @PostConstruct
    fun init() {
        leadershipService.addListener(object : EventListener<LeadershipContext> {
            override fun onEvent(payload: LeadershipContext) {
                if (leader != payload.isLeader) {
                    logger.info { "Set this node leadership to ${payload.isLeader}" }
                    logger.info { "Current leader is ${payload.leader}" }
                }
                leader = payload.isLeader
            }
        })
    }

    /**
     * perform hourly clean up with random initial delay which let the hourly() run in each installation not overlapped.
     */
    @Scheduled(
        fixedDelayString = "#{@intervalService.batchHourlyScheduleFixedDelayRate}",
    )
    fun hourly() {
        if (!leader || operationService.operationInfo.blockGc) {
            return
        }

        logger.info { "run hourly batch" }
        val now = Instant.now()

        customerDao.findAll().map { it.id }
            .forEach {
                val millis = measureTimeMillis {
                    sweepAgentStatesAndJvms(it, now)
                    sweepCodeBaseFingerprints(it)
                    markMethods(it)
                }
                logger.info { "[$it] hourly batch took $millis ms" }
            }
    }

    /**
     * perform daily clean up with random initial delay which let the daily() run in each installation not overlapped.
     */
    @Scheduled(
        fixedDelayString = "#{@intervalService.batchDailyScheduleFixedRate}",
    )
    fun daily() {
        if (!leader || operationService.operationInfo.blockGc) {
            return
        }

        logger.info { "run daily batch" }
        val now = Instant.now()
        customerDao.findAll().map { it.id }
            .forEach {
                sweepMethods(it, now)
            }
    }

    /**
     * Remove all agent states which is not active for last expected polling time plus margin
     */
    fun sweepAgentStatesAndJvms(customerId: Long, baseDateTime: Instant) {
        try {
            do {
                val agentStates = agentStateDao.findAllGarbageLastPolledAtBefore(
                    customerId,
                    baseDateTime.minusMillis(intervalService.batchSweepMarginMilliSecond)
                )

                jvmDao.deleteAllByCustomerIdAndUuids(customerId, agentStates.map { it.jvmUuid })
                    .also { logger.info { "[$customerId] $it jvm is swiped. " } }
                agentStateDao.deleteAllByCustomerIdAndIds(customerId, agentStates.map { it.id })
                    .also { logger.info { "[$customerId] $it agent state is swiped. " } }
            } while (agentStates.isNotEmpty())

            do {
                val uuidsWithoutAgent = jvmDao.findAllUuidsByWithoutAgent(customerId)
                jvmDao.deleteAllByCustomerIdAndUuids(customerId, uuidsWithoutAgent)
                    .also { logger.info { "[$customerId] $it jvm without agent is swiped." } }
            } while (uuidsWithoutAgent.isNotEmpty())
        } catch (e: Exception) {
            logger.warn(e) { "[$customerId] error occurred while sweepAgentStates, but ignored. " }
        }
    }

    fun sweepCodeBaseFingerprints(customerId: Long) {
        try {
            val fingerPrintUsed = jvmDao.findAllByCustomerId(customerId).mapNotNull { it.codeBaseFingerprint }.toSet()
            val fingerPrintRegistered = codeBaseFingerprintDao.findAllByCustomerId(customerId).map { it.codeBaseFingerprint }.toSet()
            val sweepSubjects = fingerPrintRegistered - fingerPrintUsed
            codeBaseFingerprintDao.deleteAllByCustomerIdAndCodeBaseFingerprintIn(customerId, sweepSubjects)
            logger.info { "[$customerId] sweep $sweepSubjects codebaseFingerprint" }
        } catch (e: Exception) {
            logger.warn(e) { "[$customerId] error occurred while sweepCodeBaseFingerprints, but ignore. " }
        }
    }

    fun markMethods(customerId: Long) {
        try {
            val garbageDeadline = run {
                val jvms = jvmDao.findAllByCustomerId(customerId)

                if (jvms.isEmpty()) {
                    logger.info { "[$customerId] no jvm exists. skip markMethods" }
                    return
                }

                codeBaseFingerprintDao.findAllByCustomerId(customerId).minOfOrNull { it.createdAt }?.toEpochMilli()
            } ?: return

            methodDao.updateSetGarbageLastSeenBefore(customerId, garbageDeadline - 1)
                .also { updatedCount ->
                    logger.info { "[$customerId] $updatedCount methods as marked as garbage " }
                }
        } catch (e: Exception) {
            logger.warn(e) { "[$customerId] error occurred while markMethods, but ignore. " }
        }
    }

    fun sweepMethods(customerId: Long, now: Instant) {
        try {
            val jvms = jvmDao.findAllByCustomerId(customerId)
            if (jvms.isEmpty()) {
                logger.info { "[$customerId] no jvm exists. skip sweepMethods" }
                return
            }

            val baseDateTime = now.minus(intervalService.batchSweepLongMarginMilliSecond, ChronoUnit.MILLIS)

            methodDao.findAllGarbage(customerId, baseDateTime).chunked(100).forEach { signatureHashes ->
                transactionTemplate.execute {
                    methodDao.deleteAllMethods(customerId, signatureHashes).also {
                        logger.info { "[$customerId] $signatureHashes garbage methods are deleted" }
                    }
                    invocationDao.deleteAllInvocations(customerId, signatureHashes).also {
                        logger.info { "[$customerId] $signatureHashes garbage invocations are deleted" }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "[$customerId] error occurred while sweepMethods, but ignore. " }
        }
    }
}
