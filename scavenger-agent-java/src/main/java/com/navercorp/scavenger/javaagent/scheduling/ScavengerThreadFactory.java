package com.navercorp.scavenger.javaagent.scheduling;

import java.util.concurrent.ThreadFactory;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
public class ScavengerThreadFactory implements ThreadFactory {
    private final String name;
    private final int relativePriority;

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("scavenger " + name);
        thread.setPriority(thread.getPriority() + relativePriority);
        thread.setDaemon(true);
        return thread;
    }
}
