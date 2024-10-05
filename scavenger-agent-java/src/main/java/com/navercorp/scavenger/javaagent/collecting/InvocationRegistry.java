package com.navercorp.scavenger.javaagent.collecting;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.InvocationDataPublication;

@Log
public class InvocationRegistry {
    private static final int FRONT_BUFFER_INDEX = 0;
    private static final int BACK_BUFFER_INDEX = 1;

    private final Set<String>[] invocations = new Set[]{ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet()};
    private volatile int currentInvocationIndex = FRONT_BUFFER_INDEX;
    private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

    public void register(String hash) {
        invocations[currentInvocationIndex].add(hash);
    }

    private synchronized void toggleInvocationsIndex() {
        recordingIntervalStartedAtMillis = System.currentTimeMillis();
        currentInvocationIndex = currentInvocationIndex == FRONT_BUFFER_INDEX ? BACK_BUFFER_INDEX : FRONT_BUFFER_INDEX;
    }

    public InvocationDataPublication getPublication(Config config, String codeBaseFingerprint) {
        long oldRecordingIntervalStartedAtMillis = recordingIntervalStartedAtMillis;
        int oldIndex = currentInvocationIndex;

        toggleInvocationsIndex();

        try {
            Thread.sleep(10L);

            return InvocationDataPublication.newBuilder()
                .setCommonData(
                    config.buildCommonPublicationData().toBuilder()
                        .setCodeBaseFingerprint(codeBaseFingerprint)
                        .build()
                )
                .addAllEntry(
                    invocations[oldIndex].stream()
                        .map(it ->
                            InvocationDataPublication.InvocationDataEntry.newBuilder()
                                .setHash(it)
                                .build()
                        ).collect(Collectors.toList())
                )
                .setRecordingIntervalStartedAtMillis(oldRecordingIntervalStartedAtMillis)
                .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            invocations[oldIndex].clear();
        }

        return null;
    }
}
