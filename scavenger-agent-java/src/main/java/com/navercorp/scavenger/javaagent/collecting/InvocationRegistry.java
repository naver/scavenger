package com.navercorp.scavenger.javaagent.collecting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.InvocationDataPublication;

@Log
public class InvocationRegistry {

    private static class BooleanHolder {
        private volatile boolean value = true;
    }

    private final ConcurrentHashMap<String, BooleanHolder> invocations = new ConcurrentHashMap<>();
    private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

    public void register(String hash) {
        BooleanHolder holder = invocations.computeIfAbsent(hash, k -> new BooleanHolder());
        if (!holder.value) {
            holder.value = true;
        }
    }

    public InvocationDataPublication getPublication(Config config, String codeBaseFingerprint) {
        List<InvocationDataPublication.InvocationDataEntry> dataEntries = new ArrayList<>();
        for (Map.Entry<String, BooleanHolder> entry : invocations.entrySet()) {
            if (entry.getValue().value) {
                entry.getValue().value = false;
                dataEntries.add(InvocationDataPublication.InvocationDataEntry.newBuilder()
                        .setHash(entry.getKey())
                        .build());
            }
        }
        long oldRecordingIntervalStartedAtMillis = recordingIntervalStartedAtMillis;
        recordingIntervalStartedAtMillis = System.currentTimeMillis();
        return InvocationDataPublication.newBuilder()
                .setCommonData(
                    config.buildCommonPublicationData().toBuilder()
                        .setCodeBaseFingerprint(codeBaseFingerprint)
                        .build()
                )
                .addAllEntry(dataEntries)
                .setRecordingIntervalStartedAtMillis(oldRecordingIntervalStartedAtMillis)
                .build();
    }
}
