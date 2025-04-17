package com.navercorp.scavenger.javaagent.collecting;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.CallStackDataPublication;

import lombok.extern.java.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CallStackRegistry {
    private final Map<String, Set<String>> callStacks = new ConcurrentHashMap<>();
    private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

    public void register(String caller, String callee) {
        Set<String> callers = callStacks.computeIfAbsent(callee, k -> new HashSet<>());
        callers.add(caller);
    }

    public CallStackDataPublication getPublication(Config config, String codeBaseFingerprint) {
        Set<CallStackDataPublication.CallStackDataEntry> dataEntries = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : callStacks.entrySet()) {
            String callee = entry.getKey();
            Set<String> callers = entry.getValue();
            if (!callers.isEmpty()) {
                CallStackDataPublication.CallStackDataEntry CallStackDataEntry = CallStackDataPublication.CallStackDataEntry.newBuilder()
                    .setCallee(callee)
                    .addAllCallers(callers)
                    .build();
                dataEntries.add(CallStackDataEntry);
                callers.clear();
            }
        }

        long oldRecordingIntervalStartedAtMillis = recordingIntervalStartedAtMillis;
        recordingIntervalStartedAtMillis = System.currentTimeMillis();
        return CallStackDataPublication.newBuilder()
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
