package com.navercorp.scavenger.javaagent.scheduling;

import static org.assertj.core.api.Assertions.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Nested
@DisplayName("SchedulerState class")
public class SchedulerStateTest {
    SchedulerState sut;
    Clock clock;

    void verifyDueInSeconds(int seconds) {
        clock = sut.getClock();
        IntStream.range(0, seconds).forEach(elapsedSeconds -> {
            sut.setClock(Clock.offset(clock, Duration.ofSeconds(elapsedSeconds)));
            assertThat(sut.isDueTime()).isFalse();
        });

        clock = Clock.offset(clock, Duration.ofSeconds(seconds));
        sut.setClock(clock);
        assertThat(sut.isDueTime()).isTrue();
    }

    @BeforeEach
    public void setUp() {
        sut = new SchedulerState("test").initialize(10, 10);
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

        sut.setClock(clock);
    }

    @Nested
    @DisplayName("isDueTime method")
    class IsDueTimeMethod {

        @Nested
        @DisplayName("if nothing is done")
        class NothingDone {

            @Test
            @DisplayName("it return true")
            void dueTimeWhenNothingDone() {
                assertThat(sut.isDueTime()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("scheduleNext method")
    class ScheduleNextMethodTest {

        @Nested
        @DisplayName("if scheduled next")
        class ScheduleNextTest {

            @BeforeEach
            public void scheduleNext() {
                sut.scheduleRetry();
                sut.scheduleRetry();
                sut.scheduleRetry();
                sut.scheduleRetry();
                sut.scheduleRetry();
                sut.scheduleRetry();
                sut.scheduleRetry();
                assertThat(sut.getRetryIntervalFactor()).isNotEqualTo(1);
                sut.scheduleNext();
            }

            @Test
            @DisplayName("it makes due after 10 seconds")
            void dueAfter10Seconds() {
                verifyDueInSeconds(10);
            }

            @Test
            @DisplayName("it resets retryIntervalFactor")
            void resetRetryFactor() {
                assertThat(sut.getRetryIntervalFactor()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("scheduleNow method")
    class ScheduleNowMethodTest {

        @Nested
        @DisplayName("if scheduled now")
        class ScheduleNowTest {

            @BeforeEach
            public void scheduleNow() {
                sut.scheduleNext();
                assertThat(sut.isDueTime()).isFalse();
                sut.scheduleNow();
            }

            @Test
            @DisplayName("it makes due now")
            void dueNow() {
                assertThat(sut.isDueTime()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("scheduleRetry method")
    class ScheduleRetryMethodTest {

        @Nested
        @DisplayName("if several retry happens")
        class RetryTest {

            @Test
            @DisplayName("it schedules retry with given interval factors")
            void intervalFactors() {
                List<Integer> retryIntervalFactors = Arrays.asList(
                    1, 1, 1, 1, 1, 2, 4, 8, 16, 16
                );
                for (int retryIntervalFactor : retryIntervalFactors) {
                    sut.scheduleRetry();
                    assertThat(sut.getRetryIntervalFactor()).isEqualTo(retryIntervalFactor);
                }
            }
        }
    }

    @Nested
    @DisplayName("updateIntervals method")
    class UpdateIntervalsMethodTest {

        @Nested
        @DisplayName("if interval is set to 20 seconds")
        class UpdateIntervalTest {

            @BeforeEach
            public void updateInterval() {
                sut.scheduleNext();
                sut.updateIntervals(20, 20);
            }

            @Test
            @DisplayName("it does not change ongoing due")
            void onGoingDue() {
                verifyDueInSeconds(10);
            }

            @Test
            @DisplayName("it changes upcoming due to 20 seconds")
            void upcomingDue() {
                sut.scheduleNext();
                verifyDueInSeconds(20);
            }
        }

        @Nested
        @DisplayName("if shorter interval is set")
        class UpdateToShorterIntervalTest {

            @Test
            @DisplayName("it changes ongoing due")
            void onGoingDue() {
                sut.scheduleNext();
                sut.updateIntervals(5, 5);
                verifyDueInSeconds(5);
            }

            @Test
            @DisplayName("it should start immediately when it is first time")
            void firstTime() {
                sut.updateIntervals(5, 5);
                verifyDueInSeconds(0);
            }
        }
    }
}
