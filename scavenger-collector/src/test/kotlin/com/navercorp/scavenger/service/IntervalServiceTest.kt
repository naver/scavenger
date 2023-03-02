package com.navercorp.scavenger.service

import com.navercorp.scavenger.config.IntervalProperties
import com.navercorp.scavenger.service.IntervalService.Companion.TIMESLOT_SECONDS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@Nested
@SpringBootTest
@DisplayName("IntervalService class")
class IntervalServiceTest {
    @Autowired
    lateinit var sut: IntervalService

    @Autowired
    lateinit var baseInterval: IntervalProperties

    private fun weightedInterval(weight: Int) =
        IntervalProperties(
            baseInterval.pollIntervalSeconds * weight,
            baseInterval.publishIntervalSeconds * weight,
            baseInterval.retryIntervalSeconds
        )

    @Nested
    @DisplayName("get method")
    inner class GetMethod {

        @Nested
        @DisplayName("if createdAt is 0")
        inner class CreatedAt0 {

            @Test
            @DisplayName("it returns interval with last weight")
            fun get_returnLastWeightIntervalWhenCreatedAt0() {
                assertThat(sut.get(Instant.ofEpochMilli(0)))
                    .isEqualTo(weightedInterval(TIMESLOT_WEIGHTS.last()))
            }
        }

        @Nested
        @DisplayName("if createdAt is max long value")
        inner class CreatedAtMax {

            @Test
            @DisplayName("it returns interval with last weight")
            fun get_returnLastWeightIntervalWhenCreatedAtMax() {
                assertThat(sut.get(Instant.ofEpochMilli(Long.MAX_VALUE)))
                    .isEqualTo(weightedInterval(TIMESLOT_WEIGHTS.last()))
            }
        }

        @Nested
        @DisplayName("if createdAt is future")
        inner class FutureTimestamp {

            @Test
            @DisplayName("it returns base interval")
            fun get_returnBaseIntervalWhenFutureTimestamp() {
                assertThat(sut.get(Instant.now().plusSeconds(100)))
                    .isEqualTo(baseInterval)
            }
        }

        @Nested
        @DisplayName("if createdAt is within each timeslot")
        inner class EachTimeslot {

            @Test
            @DisplayName("it returns weighted interval for each timeslot")
            fun get_returnWeightedIntervalWhenEachTimeslot() {
                val createdAts = (0..3).map { Instant.now().minusSeconds(TIMESLOT_SECONDS * it) }

                assertThat(createdAts.map(sut::get))
                    .isEqualTo(TIMESLOT_WEIGHTS.map(::weightedInterval))
            }

            @Test
            @DisplayName("it returns same value for retryIntervalSeconds")
            fun get_returnSameRetryIntervalWhenEachTimeslot() {
                val retryIntervals = run {
                    val createdAts = (-5..5).map { Instant.now().minusSeconds(TIMESLOT_SECONDS * it) }

                    createdAts.map(sut::get).map { it.retryIntervalSeconds }
                }

                assertThat(retryIntervals)
                    .containsOnly(retryIntervals.first())
            }
        }
    }

    companion object {
        val TIMESLOT_WEIGHTS = arrayOf(1, 2, 5, 10)
    }
}
