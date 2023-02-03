package com.navercorp.scavenger.javaagent.scheduling;

public class ShutdownHook extends Thread {
    private final Scheduler scheduler;

    public ShutdownHook(Scheduler scheduler) {
        setName("scavenger shutdown hook");
        setContextClassLoader(null);
        setUncaughtExceptionHandler(
            (t, e) -> {
                System.err.println("[scavenger] uncaught exception in " + t.getName());
                e.printStackTrace(System.err);
            }
        );

        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        System.err.println("[scavenger] shutting down...");
        scheduler.shutdown();
    }
}
