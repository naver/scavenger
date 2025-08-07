package com.navercorp.scavenger.javaagent.collecting;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.CallStackDataPublication;

import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CallStackRegistry {
    private final Map<String, Set<String>> callStacks = new ConcurrentHashMap<>();
    private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

    public void register(String caller, String callee) {
        Set<String> callers = callStacks.computeIfAbsent(callee, k -> ConcurrentHashMap.newKeySet());
        callers.add(caller);
    }

    public CallStackDataPublication getPublication(Config config, String codeBaseFingerprint) {
        Set<CallStackDataPublication.CallStackDataEntry> dataEntries = new HashSet<>();
        callStacks.forEach((callee, callers) -> {
            if (!callers.isEmpty()) {
                CallStackDataPublication.CallStackDataEntry callStackDataEntry = CallStackDataPublication.CallStackDataEntry.newBuilder()
                    .setCallee(callee)
                    .addAllCallers(callers)
                    .build();
                dataEntries.add(callStackDataEntry);
                callers.clear();
            }
        });

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
