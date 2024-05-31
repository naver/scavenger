package com.navercorp.scavenger.service

import com.navercorp.scavenger.config.IntervalProperties
import com.navercorp.scavenger.repository.JvmDao
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class IntervalService(
    val intervalProperties: IntervalProperties,
    val jvmDao: JvmDao,
    @Value("\${spring.profiles.active}") val activeProfile: String,
) {

    val logger = KotlinLogging.logger {}

    val batchDailyScheduleFixedRate: Long by lazy {
        (if (activeProfile.contains("test")) {
            10 * 60 * 1000L // 10 min for test
        } else {
            24 * 60 * 60 * 1000L
        }).also {
            logger.info { "daily schedule rate is set as $it ms" }
        }
    }

    val batchHourlyScheduleFixedDelayRate: Long by lazy {
        (if (activeProfile.contains("test")) {
            1 * 60 * 1000L // 1 min for test
        } else {
            60 * 60 * 1000L // 1 hour for rest
        }).also {
            logger.info { "hourly schedule rate is set as $it ms" }
        }
    }

    val batchSweepMarginMilliSecond: Long by lazy {
        (if (activeProfile.contains("test")) {
            (intervalProperties.publishIntervalSeconds * TIMESLOT_WEIGHTS_FOR_TEST.last() + TimeUnit.MINUTES.toSeconds(2)) * 1000L
        } else {
            (intervalProperties.publishIntervalSeconds * TIMESLOT_WEIGHTS.last() + TimeUnit.MINUTES.toSeconds(GC_DEAD_MARGIN_MINUTES)) * 1000L
        }).also {
            logger.info { "batchSweepMarginMilliSecond is set as $it ms" }
        }
    }

    val batchSweepLongMarginMilliSecond: Long by lazy {
        (if (activeProfile.contains("test")) {
            1 * 60 * 60 * 1000L // 1 hour
        } else {
            GC_DEAD_MARGIN_DAYS * 24 * 60 * 60 * 1000L // 1 week
        }).also {
            logger.info { "batchSweepLongMarginMilliSecond is set as $it ms" }
        }
    }

    @Cacheable(INTERVAL_CACHE)
    fun get(customerId: Long, jvmUuid: String): IntervalProperties =
        jvmDao.findByCustomerIdAndUuid(customerId, jvmUuid)?.createdAt.let {
            get(it)
        }

    fun get(createdAt: Instant?): IntervalProperties = with(intervalProperties) {
        val weight = run {
            val timeslot = createdAt?.let {
                val aliveSeconds = Instant.now().epochSecond - it.epochSecond
                (aliveSeconds / TIMESLOT_SECONDS).toInt()
            } ?: 0

            if (activeProfile.contains("test")) {
                TIMESLOT_WEIGHTS_FOR_TEST.getOrElse(timeslot) { TIMESLOT_WEIGHTS_FOR_TEST.last() }
            } else {
                TIMESLOT_WEIGHTS.getOrElse(timeslot) { TIMESLOT_WEIGHTS.last() }
            }
        }

        IntervalProperties(
            pollIntervalSeconds = pollIntervalSeconds * weight,
            publishIntervalSeconds = publishIntervalSeconds * weight,
            retryIntervalSeconds = retryIntervalSeconds
        )
    }

    companion object {
        const val INTERVAL_CACHE = "interval"
        const val TIMESLOT_SECONDS = 600L
        const val GC_DEAD_MARGIN_MINUTES = 30L
        const val GC_DEAD_MARGIN_DAYS = 7L
        val TIMESLOT_WEIGHTS = arrayOf(1, 2, 5, 10)
        val TIMESLOT_WEIGHTS_FOR_TEST = arrayOf(1, 1, 1, 1)
    }
}
