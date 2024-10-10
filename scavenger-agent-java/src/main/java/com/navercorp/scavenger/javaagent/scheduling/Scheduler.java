package com.navercorp.scavenger.javaagent.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.navercorp.scavenger.javaagent.collecting.InvocationRegistry;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner;
import com.navercorp.scavenger.javaagent.collecting.InvocationTracker;
import com.navercorp.scavenger.javaagent.model.CodeBase;
import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.publishing.Publisher;
import com.navercorp.scavenger.model.CodeBasePublication;
import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.InvocationDataPublication;

@Log
public class Scheduler implements Runnable {
    private final InvocationRegistry invocationRegistry;
    private final Config config;
    private final Publisher publisher;
    private final CodeBaseScanner codeBaseScanner;
    private final ScheduledExecutorService executor;

    private final SchedulerState pollState;
    private final SchedulerState codeBasePublisherState;
    private final SchedulerState invocationDataPublisherState;

    private final int forceIntervalSeconds;
    private final int maxMethodsCount;

    private GetConfigResponse dynamicConfig;
    private CodeBasePublication codeBasePublication;
    private boolean isCodeBasePublished = false;
    private InvocationDataPublication invocationDataPublication;
    private String codeBaseFingerprint;

    public Scheduler(InvocationRegistry invocationRegistry, Config config) {
        this(invocationRegistry, config, new Publisher(config), new CodeBaseScanner(config));
    }

    public Scheduler(InvocationRegistry invocationRegistry, Config config, Publisher publisher, CodeBaseScanner codeBaseScanner) {
        this.invocationRegistry = invocationRegistry;
        this.config = config;
        this.codeBaseScanner = codeBaseScanner;
        this.publisher = publisher;
        this.executor = Executors.newScheduledThreadPool(
            1,
            ScavengerThreadFactory.builder().
                name("scheduler").
                relativePriority(-1)
                .build()
        );

        this.forceIntervalSeconds = this.config.getForceIntervalSeconds();
        this.maxMethodsCount = this.config.getMaxMethodsCount();
        // these intervals will be updated when dynamic config is polled (not likely be used)
        int intervalSeconds = 600;
        int retryIntervalSeconds = 600;

        this.pollState = new SchedulerState("config_poll").initialize(intervalSeconds, retryIntervalSeconds);
        this.codeBasePublisherState = new SchedulerState("codebase").initialize(intervalSeconds, retryIntervalSeconds);
        this.invocationDataPublisherState = new SchedulerState("invocation_data").initialize(intervalSeconds, retryIntervalSeconds);

        if (forceIntervalSeconds != 0) {
            this.pollState.updateIntervals(forceIntervalSeconds, forceIntervalSeconds);
            this.codeBasePublisherState.updateIntervals(forceIntervalSeconds, forceIntervalSeconds);
            this.invocationDataPublisherState.updateIntervals(forceIntervalSeconds, forceIntervalSeconds);
        }
    }

    public void start() {
        executor.scheduleAtFixedRate(
            this,
            config.getSchedulerInitialDelayMillis(),
            config.getSchedulerIntervalMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    public void shutdown() {
        synchronized (executor) {
            log.info("[scavenger] shutting scheduler down");
            executor.shutdown();
            try {
                //noinspection ResultOfMethodCallIgnored
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.fine("[scavenger] shutdown interrupted");
                Thread.currentThread().interrupt();
            }

            if (dynamicConfig != null) {
                codeBasePublisherState.scheduleNow();
                publishCodeBaseIfNeeded();

                invocationDataPublisherState.scheduleNow();
                publishInvocationDataIfNeeded();
            }
        }
    }

    @Override
    public void run() {
        synchronized (executor) {
            if (executor.isShutdown()) {
                log.info("[scavenger] scheduler is shutting down");
                return;
            }

            try {
                pollDynamicConfigIfNeeded();
                publishCodeBaseIfNeeded();
                publishInvocationDataIfNeeded();
            } catch (Throwable t) {
                log.severe("[scavenger] scheduler failure: " + t);
            }
        }
    }

    public void pollDynamicConfigIfNeeded() {
        if (pollState.isDueTime()) {
            try {
                dynamicConfig = publisher.pollDynamicConfig();
                if (forceIntervalSeconds == 0) { // if forceIntervalSeconds is disabled
                    pollState.updateIntervals(
                        dynamicConfig.getConfigPollIntervalSeconds(),
                        dynamicConfig.getConfigPollRetryIntervalSeconds()
                    );
                    codeBasePublisherState.updateIntervals(
                        dynamicConfig.getCodeBasePublisherCheckIntervalSeconds(),
                        dynamicConfig.getCodeBasePublisherRetryIntervalSeconds()
                    );
                    invocationDataPublisherState.updateIntervals(
                        dynamicConfig.getInvocationDataPublisherIntervalSeconds(),
                        dynamicConfig.getInvocationDataPublisherRetryIntervalSeconds()
                    );
                }
                pollState.scheduleNext();
            } catch (Exception e) {
                log.log(Level.SEVERE, "[scavenger] poll config failed", e);
                if (e instanceof StatusRuntimeException) {
                    Status.Code code = ((StatusRuntimeException)e).getStatus().getCode();
                    if (code == Status.Code.UNAUTHENTICATED) {
                        log.warning("[scavenger] authentication failed. disabling scavenger...");
                        shutdown();
                    }
                }
                pollState.scheduleRetry();
            }
        }
    }

    public void publishCodeBaseIfNeeded() {
        if (!isCodeBasePublished && codeBasePublisherState.isDueTime() && dynamicConfig != null) {
            try {
                if (this.codeBasePublication == null) {
                    boolean scanSuccessful = scanCodeBase();

                    if (!scanSuccessful) {
                        return;
                    }
                }

                this.publisher.publishCodeBase(codeBasePublication);
                this.isCodeBasePublished = true;
                this.codeBaseFingerprint = codeBasePublication.getCommonData().getCodeBaseFingerprint();
                this.codeBasePublication = null; // remove codebase publication reference to let it garbage collected.
            } catch (Exception e) {
                log.log(Level.SEVERE, "[scavenger] codebase publish failed", e);
                codeBasePublisherState.scheduleRetry();
            }
        }
    }

    public boolean scanCodeBase() {
        CodeBase codeBase;
        try {
            codeBase = codeBaseScanner.scan();
        } catch (Throwable e) {
            log.log(Level.SEVERE, "[scavenger] code scanning is failed. Stop codebase publishing.", e);
            return false;
        }

        if (codeBase.getMethods().isEmpty()) {
            log.severe("[scavenger] no methods are found");
            return false;
        } else if (codeBase.getMethods().size() > maxMethodsCount) {
            log.severe("[scavenger] maximum methods count(" + maxMethodsCount + ") exceed: " + codeBase.getMethods().size());
            return false;
        }
        this.codeBasePublication = codeBase.toPublication(config);
        return true;
    }

    public void publishInvocationDataIfNeeded() {
        if (invocationDataPublisherState.isDueTime() && dynamicConfig != null && isCodeBasePublished) {
            try {
                if (invocationDataPublication == null) {
                    invocationDataPublication = invocationRegistry
                        .getPublication(
                            config,
                            codeBaseFingerprint
                        );
                }
                publisher.publishInvocationData(invocationDataPublication);

                invocationDataPublication = null;
                invocationDataPublisherState.scheduleNext();
            } catch (Exception e) {
                log.log(Level.SEVERE, "[scavenger] invocation data publish failed", e);
                invocationDataPublisherState.scheduleRetry();
            }
        }
    }
}
