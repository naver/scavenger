package com.navercorp.scavenger.controller

import com.navercorp.scavenger.model.CodeBasePublication
import com.navercorp.scavenger.model.CommonPublicationData
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc.GrpcAgentServiceBlockingStub
import com.navercorp.scavenger.model.InvocationDataPublication
import com.navercorp.scavenger.service.AgentService
import com.navercorp.scavenger.support.AbstractMockMvcApiTest
import com.navercorp.scavenger.support.GrpcCleanupExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class GrpcAgentControllerTest : AbstractMockMvcApiTest() {
    @JvmField
    @RegisterExtension
    final val grpcCleanupExtension = GrpcCleanupExtension()

    @Autowired
    lateinit var agentService: AgentService

    lateinit var stub: GrpcAgentServiceBlockingStub

    @BeforeEach
    fun setUp() {
        val grpcAgentController = GrpcAgentController(agentService)
        stub = GrpcAgentServiceGrpc.newBlockingStub(grpcCleanupExtension.addService(grpcAgentController))
    }

    @Test
    fun sendCodeBasePublication_should_send_correctly() {
        // given
        val request = CodeBasePublication.newBuilder()
            .setCommonData(
                CommonPublicationData.newBuilder()
                    .setAppName("appName")
                    .setAppVersion("appVersion")
                    .setEnvironment("environment")
                    .setHostname("hostname")
                    .setJvmUuid("d0dfa3c2-809c-428f-b501-7419197d91c5")
                    .setCodeBaseFingerprint("codeBaseFingerprint")
                    .setJvmStartedAtMillis(Instant.now().toEpochMilli())
                    .setPublishedAtMillis(Instant.now().toEpochMilli())
                    .setApiKey("4c94e0dd-ad04-4b17-9238-f46bba75c684")
            )
            .addEntry(
                CodeBasePublication.CodeBaseEntry.newBuilder()
                    .setDeclaringType("com.example.demo.additional.AdditionalService")
                    .setVisibility("protected")
                    .setSignature("com.example.demo.additional.AdditionalService.get()")
                    .setMethodName("get")
                    .setModifiers("public final")
                    .setPackageName("com.example.demo.additional")
                    .setParameterTypes("")
                    .setSignatureHash("hash")
            )
            .build()

        // when
        val response = stub.sendCodeBasePublication(request)

        // then
        assertThat(response.status).isEqualTo("OK")
    }

    @Test
    fun sendInvocationDataPublication_should_send_correctly() {
        // given
        val request = InvocationDataPublication.newBuilder()
            .setCommonData(
                CommonPublicationData.newBuilder()
                    .setAppName("appName")
                    .setAppVersion("appVersion")
                    .setEnvironment("environment")
                    .setHostname("hostname")
                    .setJvmUuid("d0dfa3c2-809c-428f-b501-7419197d91c5")
                    .setCodeBaseFingerprint("codeBaseFingerprint")
                    .setJvmStartedAtMillis(Instant.now().toEpochMilli())
                    .setPublishedAtMillis(Instant.now().toEpochMilli())
                    .setApiKey("4c94e0dd-ad04-4b17-9238-f46bba75c684")
            )
            .addEntry(
                InvocationDataPublication.InvocationDataEntry.newBuilder()
                    .setHash("hash")
            )
            .build()

        // when
        val response = stub.sendInvocationDataPublication(request)

        // then
        assertThat(response.status).isEqualTo("OK")
    }
}
