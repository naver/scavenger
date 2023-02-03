package com.navercorp.scavenger.util

import com.navercorp.scavenger.model.CodeBasePublication
import com.navercorp.scavenger.model.CommonPublicationData
import com.navercorp.scavenger.model.InvocationDataPublication
import com.navercorp.scavenger.model.LegacyPublication
import com.navercorp.scavenger.model.ProtoPublication
import io.codekvast.javaagent.model.v4.CodeBasePublication4
import io.codekvast.javaagent.model.v4.CommonPublicationData4
import io.codekvast.javaagent.model.v4.InvocationDataPublication4

object SamplePublications {
    val commonPublicationData: CommonPublicationData = CommonPublicationData.newBuilder()
        .setApiKey("apiKey")
        .setAppName("appName")
        .setAppVersion("appVersion")
        .setCodeBaseFingerprint("codeBaseFingerprint")
        .setEnvironment("environment")
        .setHostname("hostname")
        .setJvmStartedAtMillis(0)
        .setJvmUuid("jvmUuid")
        .setPublishedAtMillis(0)
        .build()

    val protoCodeBasePublication = ProtoPublication.CodeBase(
        CodeBasePublication.newBuilder()
            .setCommonData(commonPublicationData)
            .build()
    )

    val legacyCodeBasePublication = LegacyPublication.CodeBase(
        CodeBasePublication4.builder()
            .commonData(CommonPublicationData4.sampleCommonPublicationData())
            .entries(emptyList())
            .build()
    )

    val protoInvocationPublication = ProtoPublication.InvocationData(
        InvocationDataPublication.newBuilder()
            .setCommonData(commonPublicationData)
            .addEntry(
                InvocationDataPublication.InvocationDataEntry.newBuilder()
                    .setHash("hash")
            )
            .setRecordingIntervalStartedAtMillis(0)
            .build()
    )

    val legacyInvocationPublication = LegacyPublication.InvocationData(
        InvocationDataPublication4.builder()
            .commonData(CommonPublicationData4.sampleCommonPublicationData())
            .invocations(setOf("invocation()"))
            .recordingIntervalStartedAtMillis(0)
            .build()
    )
}
