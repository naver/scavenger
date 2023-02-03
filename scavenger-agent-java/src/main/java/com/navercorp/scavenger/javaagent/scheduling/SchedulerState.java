package com.navercorp.scavenger.javaagent.scheduling;

import java.time.Clock;
import java.util.Date;
import java.util.logging.Level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Getter
@RequiredArgsConstructor
@Log
public class SchedulerState {
    private final String name;

    private Clock clock;
    private long nextEventAtMillis;
    private int intervalSeconds;
    private int retryIntervalSeconds;
    private int retryIntervalFactor;
    private int numFailures;

    SchedulerState initialize(int intervalSeconds, int retryIntervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        this.retryIntervalSeconds = retryIntervalSeconds;
        this.nextEventAtMillis = 0L;
        this.clock = Clock.systemUTC();
        resetRetryCounter();
        return this;
    }

    private void resetRetryCounter() {
        this.numFailures = 0;
        this.retryIntervalFactor = 1;
    }

    public void updateIntervals(int intervalSeconds, int retryIntervalSeconds) {
        if (nextEventAtMillis != 0) {
            if (intervalSeconds < this.intervalSeconds && retryIntervalFactor == 1) {
                nextEventAtMillis = clock.millis() + intervalSeconds * 1000L;
            } else if (retryIntervalSeconds < this.retryIntervalSeconds && retryIntervalFactor > 1) {
                nextEventAtMillis = clock.millis() + retryIntervalSeconds * retryIntervalFactor * 1000L;
            }
        }

        this.intervalSeconds = intervalSeconds;
        this.retryIntervalSeconds = retryIntervalSeconds;
    }

    public void scheduleNext() {
        nextEventAtMillis = clock.millis() + intervalSeconds * 1000L;
        if (numFailures > 0 && log.isLoggable(Level.FINE)) {
            log.fine("[scavenger] " + name + " is exiting failure state after " + numFailures + " failures");
        }
        resetRetryCounter();
        if (log.isLoggable(Level.FINER)) {
            log.finer("[scavenger] " + name + " will execute next at " + new Date(nextEventAtMillis));
        }
    }

    public void scheduleNow() {
        nextEventAtMillis = 0L;
        if (log.isLoggable(Level.FINE)) {
            log.fine("[scavenger] " + name + " will execute now");
        }
    }

    public void scheduleRetry() {
        int backOffLimit = 5;

        if (numFailures < backOffLimit) {
            retryIntervalFactor = 1;
        } else {
            retryIntervalFactor = (int)Math.pow(2, Math.min(numFailures - backOffLimit + 1, 4));
        }
        nextEventAtMillis =
            clock.millis() + retryIntervalSeconds * retryIntervalFactor * 1000L;
        numFailures += 1;

        if (log.isLoggable(Level.FINE)) {
            log.fine(
                "[scavenger] " + name
                    + " has failed "
                    + numFailures
                    + " times, will retry at "
                    + new Date(nextEventAtMillis));
        }
    }

    public boolean isDueTime() {
        return clock.millis() >= nextEventAtMillis;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
