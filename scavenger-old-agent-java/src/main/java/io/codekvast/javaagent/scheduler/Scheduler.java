/*
 * Copyright (c) 2015-2021 Hallin Information Technology AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.codekvast.javaagent.scheduler;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.codekvast.javaagent.CodekvastThreadFactory;
import io.codekvast.javaagent.InvocationRegistry;
import io.codekvast.javaagent.appversion.AppVersionResolver;
import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.model.v4.GetConfigResponse4;
import io.codekvast.javaagent.publishing.CodeBasePublisher;
import io.codekvast.javaagent.publishing.CodeBasePublisherFactory;
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.publishing.InvocationDataPublisherFactory;
import io.codekvast.javaagent.util.LogUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 * Responsible for executing recurring tasks within the agent.
 *
 * @author olle.hallin@crisp.se
 */
@SuppressWarnings("ClassWithTooManyFields")
@Log
public class Scheduler implements Runnable {
    // Collaborators
    private final AgentConfig config;
    private final ConfigPoller configPoller;
    private final CodeBasePublisherFactory codeBasePublisherFactory;
    private final InvocationDataPublisherFactory invocationDataPublisherFactory;
    private final ScheduledExecutorService executor;
    private final SystemClock systemClock;
    private final SchedulerState pollState;
    private final SchedulerState codeBasePublisherState;
    private final SchedulerState invocationDataPublisherState;
    private final Integer forceIntervalSeconds;
    // Mutable state
    private long stopWaitingForResolvedAppVersionAtMillis;
    private GetConfigResponse4 dynamicConfig;
    private CodeBasePublisher codeBasePublisher;
    private InvocationDataPublisher invocationDataPublisher;

    public Scheduler(
        AgentConfig config,
        ConfigPoller configPoller,
        CodeBasePublisherFactory codeBasePublisherFactory,
        InvocationDataPublisherFactory invocationDataPublisherFactory,
        SystemClock systemClock) {
        this.config = config;
        this.configPoller = configPoller;
        this.codeBasePublisherFactory = codeBasePublisherFactory;
        this.invocationDataPublisherFactory = invocationDataPublisherFactory;
        this.systemClock = systemClock;

        this.pollState = new SchedulerState("configPoll", systemClock).initialize(600, 600);

        this.codeBasePublisherState = new SchedulerState("codeBase", systemClock).initialize(600, 600);

        this.invocationDataPublisherState =
            new SchedulerState("invocationData", systemClock).initialize(600, 600);

        this.executor =
            Executors.newScheduledThreadPool(
                1, CodekvastThreadFactory.builder().name("scheduler").relativePriority(-1).build());
        this.forceIntervalSeconds = config.getForceIntervalSeconds();
    }

    /**
     * Starts the scheduler.
     *
     * @return this
     */
    public Scheduler start() {
        stopWaitingForResolvedAppVersionAtMillis = System.currentTimeMillis() + 120_000L;

        executor.scheduleAtFixedRate(
            this,
            config.getSchedulerInitialDelayMillis(),
            config.getSchedulerIntervalMillis(),
            TimeUnit.MILLISECONDS);
        log.info("Scheduler started; pulling dynamic config from " + config.getServerUrl());
        return this;
    }

    /**
     * Shuts down the scheduler. Performs a last publishing before returning.
     */
    public void shutdown() {
        long startedAt = systemClock.currentTimeMillis();
        synchronized (executor) {
            log.fine("Stopping scheduler");
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.fine("Stop interrupted");
                Thread.currentThread().interrupt();
            }

            if (dynamicConfig != null) {
                // We have done at least one successful config poll

                codeBasePublisherState.scheduleNow();
                publishCodeBaseIfNeeded();

                invocationDataPublisherState.scheduleNow();
                publishInvocationDataIfNeeded();
            }
        }
        log.info(
            String.format(
                "Scavenger scheduler stopped in %d ms", systemClock.currentTimeMillis() - startedAt));
    }

    @Override
    public void run() {
        synchronized (executor) {
            if (executor.isShutdown()) {
                log.info("Scavenger scheduler is shutting down");
                return;
            }

            if (!resolvedAndReady()) {
                if (System.currentTimeMillis() < stopWaitingForResolvedAppVersionAtMillis) {
                    return;
                }

                if (stopWaitingForResolvedAppVersionAtMillis > 0L) {
                    log.warning(
                        String.format(
                            "Codekvast is not ready, check codeBase='%s' and appVersion='%s' in %s.",
                            config.getCodeBase(), config.getAppVersion(), "codekvast.conf"));
                    // log warning only once
                    stopWaitingForResolvedAppVersionAtMillis = -1L;
                }
            }

            try {
                pollDynamicConfigIfNeeded();
                publishCodeBaseIfNeeded();
                publishInvocationDataIfNeeded();
            } catch (Throwable t) {
                log.severe("Scavenger scheduler failure: " + t);
            }
        }
    }

    private boolean resolvedAndReady() {
        for (File file : config.getCodeBaseFiles()) {
            if (!file.exists()) {
                log.warning("Codebase file " + file + " does not exist");
                return false;
            }
        }

        if (AppVersionResolver.isUnresolved(config.getResolvedAppVersion())) {
            log.fine(String.format("appVersion='%s' has not resolved", config.getAppVersion()));
            return false;
        }

        return true;
    }

    private void pollDynamicConfigIfNeeded() {
        if (pollState.isDueTime()) {
            try {
                dynamicConfig = configPoller.doPoll();

                configureCodeBasePublisher();
                configureInvocationDataPublisher();

                pollState.updateIntervals(
                    dynamicConfig.getConfigPollIntervalSeconds(),
                    dynamicConfig.getConfigPollRetryIntervalSeconds());
                pollState.scheduleNext();
            } catch (Exception e) {
                LogUtil.logException(log, "Failed to poll " + config.getPollConfigRequestEndpoint(), e);
                pollState.scheduleRetry();
            }
        }
    }

    private void configureCodeBasePublisher() {
        codeBasePublisherState.updateIntervals(
            dynamicConfig.getCodeBasePublisherCheckIntervalSeconds(),
            dynamicConfig.getCodeBasePublisherRetryIntervalSeconds());

        String newName = dynamicConfig.getCodeBasePublisherName();
        if (codeBasePublisher == null || !newName.equals(codeBasePublisher.getName())) {
            codeBasePublisher = codeBasePublisherFactory.create(newName, config);
            codeBasePublisherState.scheduleNow();
        }
        codeBasePublisher.configure(
            dynamicConfig.getCustomerId(), dynamicConfig.getCodeBasePublisherConfig());
    }

    private void configureInvocationDataPublisher() {
        invocationDataPublisherState.updateIntervals(
            forceIntervalSeconds != null ? forceIntervalSeconds : dynamicConfig.getInvocationDataPublisherIntervalSeconds(),
            forceIntervalSeconds != null ? forceIntervalSeconds : dynamicConfig.getInvocationDataPublisherRetryIntervalSeconds());

        String newName = dynamicConfig.getInvocationDataPublisherName();
        if (invocationDataPublisher == null || !newName.equals(invocationDataPublisher.getName())) {
            invocationDataPublisher = invocationDataPublisherFactory.create(newName, config);
            invocationDataPublisherState.scheduleNow();
        }

        if (codeBasePublisher != null) {
            invocationDataPublisher.setCodeBaseFingerprint(codeBasePublisher.getCodeBaseFingerprint());
            if (codeBasePublisher.getSequenceNumber() == 1
                && invocationDataPublisher.getSequenceNumber() == 0) {
                invocationDataPublisherState.scheduleNow();
            }
        }

        invocationDataPublisher.configure(
            dynamicConfig.getCustomerId(), dynamicConfig.getInvocationDataPublisherConfig());
    }

    private void publishCodeBaseIfNeeded() {

        if (codeBasePublisherState.isDueTime() && dynamicConfig != null) {
            log.finer("Checking if code base needs to be published...");

            try {
                codeBasePublisher.publishCodeBase();
                codeBasePublisherState.scheduleNext();
            } catch (Exception e) {
                LogUtil.logException(log, "Failed to publish code base", e);
                codeBasePublisherState.scheduleRetry();
            }
        }
    }

    private void publishInvocationDataIfNeeded() {
        if (invocationDataPublisherState.isDueTime() && dynamicConfig != null) {
            log.finer("Checking if invocation data needs to be published...");
            if (codeBasePublisher.getCodeBaseFingerprint() != null
                && invocationDataPublisher.getCodeBaseFingerprint() == null) {
                log.finer("Enabled a fast first invocation data publishing");
                invocationDataPublisher.setCodeBaseFingerprint(codeBasePublisher.getCodeBaseFingerprint());
            }

            try {
                InvocationRegistry.publishInvocationData(invocationDataPublisher);
                invocationDataPublisherState.scheduleNext();
            } catch (Exception e) {
                LogUtil.logException(log, "Failed to publish invocation data", e);
                invocationDataPublisherState.scheduleRetry();
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    @Log
    static class SchedulerState {
        private final String name;
        private final SystemClock systemClock;

        private long nextEventAtMillis;
        private int intervalSeconds;
        private int retryIntervalSeconds;
        private int retryIntervalFactor;
        private int numFailures;

        SchedulerState initialize(int intervalSeconds, int retryIntervalSeconds) {
            this.intervalSeconds = intervalSeconds;
            this.retryIntervalSeconds = retryIntervalSeconds;
            this.nextEventAtMillis = 0L;
            resetRetryCounter();
            return this;
        }

        private void resetRetryCounter() {
            this.numFailures = 0;
            this.retryIntervalFactor = 1;
        }

        void updateIntervals(int intervalSeconds, int retryIntervalSeconds) {
            this.intervalSeconds = intervalSeconds;
            this.retryIntervalSeconds = retryIntervalSeconds;
        }

        void scheduleNext() {
            nextEventAtMillis = systemClock.currentTimeMillis() + intervalSeconds * 1000L;
            if (numFailures > 0) {
                log.fine(name + " is exiting failure state after " + numFailures + " failures");
            }
            resetRetryCounter();
            log.finer(name + " will execute next at " + new Date(nextEventAtMillis));
        }

        void scheduleNow() {
            nextEventAtMillis = 0L;
            log.fine(name + " will execute now");
        }

        void scheduleRetry() {
            int backOffLimit = 5;

            if (numFailures < backOffLimit) {
                retryIntervalFactor = 1;
            } else {
                retryIntervalFactor = (int)Math.pow(2, Math.min(numFailures - backOffLimit + 1, 4));
            }
            nextEventAtMillis =
                systemClock.currentTimeMillis() + retryIntervalSeconds * retryIntervalFactor * 1000L;
            numFailures += 1;

            log.fine(
                name
                    + " has failed "
                    + numFailures
                    + " times, will retry at "
                    + new Date(nextEventAtMillis));
        }

        boolean isDueTime() {
            return systemClock.currentTimeMillis() >= nextEventAtMillis;
        }
    }
}
