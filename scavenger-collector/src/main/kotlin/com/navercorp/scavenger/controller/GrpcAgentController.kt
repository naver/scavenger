package com.navercorp.scavenger.controller

import com.navercorp.scavenger.model.CodeBasePublication
import com.navercorp.scavenger.model.CommonPublicationData
import com.navercorp.scavenger.model.GetConfigRequest
import com.navercorp.scavenger.model.GetConfigResponse
import com.navercorp.scavenger.model.GrpcAgentServiceGrpcKt
import com.navercorp.scavenger.model.InvocationDataPublication
import com.navercorp.scavenger.model.ProtoPublication
import com.navercorp.scavenger.model.PublicationResponse
import com.navercorp.scavenger.service.AgentService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Controller

@Controller
class GrpcAgentController(
    val agentService: AgentService,
) : GrpcAgentServiceGrpcKt.GrpcAgentServiceCoroutineImplBase() {
    val logger = KotlinLogging.logger {}

    override suspend fun pollConfig(request: GetConfigRequest): GetConfigResponse {
        return agentService.getConfig(request.apiKey, request.jvmUuid)
    }

    override suspend fun sendCodeBasePublication(request: CodeBasePublication): PublicationResponse {
        try {
            validate(request)
            agentService.savePublication(ProtoPublication.from(request))
        } catch (e: Exception) {
            logger.warn(e) { "grpc agent ${request.commonData.jvmUuid} from ${request.commonData.apiKey} codebase import failed: " }
            throw e
        }

        return PublicationResponse.newBuilder()
            .setStatus("OK")
            .build()
    }

    override suspend fun sendInvocationDataPublication(request: InvocationDataPublication): PublicationResponse {
        try {
            validate(request)
            agentService.savePublication(ProtoPublication.from(request))
        } catch (e: Exception) {
            logger.warn(e) { "grpc agent ${request.commonData.jvmUuid} from ${request.commonData.apiKey} invocation import failed: " }
            throw e
        }

        return PublicationResponse.newBuilder()
            .setStatus("OK")
            .build()
    }

    private fun validate(request: CodeBasePublication) {
        if (!request.hasCommonData()) {
            throw IllegalArgumentException("CommonPublicationData is a mandatory field")
        }
        for (entry in request.entryList) {
            if (entry.signatureHash.isEmpty()) {
                throw IllegalArgumentException("hash is a mandatory field")
            }
        }

        validate(request.commonData)
    }

    private fun validate(request: InvocationDataPublication) {
        if (!request.hasCommonData()) {
            throw IllegalArgumentException("CommonPublicationData is a mandatory field")
        }
        for (entry in request.entryList) {
            if (entry.hash.isEmpty()) {
                throw IllegalArgumentException("hash is a mandatory field")
            }
        }

        validate(request.commonData)
    }

    private fun validate(commonData: CommonPublicationData) {
        with(commonData) {
            if (appName.isEmpty()) {
                throw IllegalArgumentException("appName is a mandatory field")
            }

            if (appVersion.isEmpty()) {
                throw IllegalArgumentException("appVersion is a mandatory field")
            }

            if (codeBaseFingerprint.isEmpty()) {
                throw IllegalArgumentException("codeBaseFingerprint is a mandatory field")
            }

            if (hostname.isEmpty()) {
                throw IllegalArgumentException("hostname is a mandatory field")
            }

            if (jvmStartedAtMillis < 1_490_000_000_000L) {
                throw IllegalArgumentException("jvmStartedAtMillis is invalid")
            }

            if (jvmUuid.isEmpty()) {
                throw IllegalArgumentException("jvmUuid is a mandatory field")
            }

            if (publishedAtMillis < 1_490_000_000_000L) {
                throw IllegalArgumentException("publishedAtMillis is invalid")
            }

            if (apiKey.isEmpty()) {
                throw IllegalArgumentException("apiKey is a mandatory field")
            }
        }
    }
}
