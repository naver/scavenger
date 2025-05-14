package com.navercorp.scavenger.javaagent.collecting;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.model.CallStackDataPublication;

import com.navercorp.scavenger.util.HashGenerator;

import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CallStackRegistry {
    private final Map<String, List<String>> callTraces = new ConcurrentHashMap<>();
    private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

    public void register(List<String> callTrace) {
        String callTraceJoined = String.join("-", callTrace);
        String callTraceHash = HashGenerator.Md5.from(callTraceJoined);
        callTraces.put(callTraceHash, callTrace);
    }

    public CallStackDataPublication getPublication(Config config, String codeBaseFingerprint) {
        List<CallStackDataPublication.CallStackDataEntry> dataEntries = new ArrayList<>();
        Iterator<Map.Entry<String, List<String>>> iterator = callTraces.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            CallStackDataPublication.CallStackDataEntry callStackDataEntry =
                CallStackDataPublication.CallStackDataEntry.newBuilder()
                    .addAllSignature(entry.getValue())
                    .build();
            dataEntries.add(callStackDataEntry);

            iterator.remove();
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
