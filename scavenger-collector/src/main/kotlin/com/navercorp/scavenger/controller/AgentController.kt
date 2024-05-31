package com.navercorp.scavenger.controller

import com.navercorp.scavenger.model.Endpoints.Agent.PARAM_LICENSE_KEY
import com.navercorp.scavenger.model.Endpoints.Agent.PARAM_PUBLICATION_FILE
import com.navercorp.scavenger.model.Endpoints.Agent.V4_INIT_CONFIG
import com.navercorp.scavenger.model.Endpoints.Agent.V4_POLL_CONFIG
import com.navercorp.scavenger.model.Endpoints.Agent.V4_UPLOAD_CODEBASE
import com.navercorp.scavenger.model.Endpoints.Agent.V4_UPLOAD_INVOCATION_DATA
import com.navercorp.scavenger.model.Endpoints.Agent.V5_INIT_CONFIG
import com.navercorp.scavenger.model.InitConfigResponse
import com.navercorp.scavenger.service.AgentService
import io.codekvast.javaagent.model.v4.GetConfigRequest4
import io.codekvast.javaagent.model.v4.GetConfigResponse4
import io.codekvast.javaagent.model.v4.InitConfigResponse4
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import jakarta.servlet.http.HttpServletRequest

@RestController
class AgentController(
    val agentService: AgentService,
) {
    val logger = KotlinLogging.logger {}

    @GetMapping(V4_INIT_CONFIG, produces = [MediaType.APPLICATION_JSON_VALUE])
    fun httpInitConfig(@RequestParam licenseKey: String, request: HttpServletRequest): InitConfigResponse4 {
        logger.info { "init config requested from http client ${request.remoteAddr} with licenseKey: $licenseKey" }
        val splitUrl = request.requestURL.split("/")
        val httpBaseUrl = "${splitUrl[0]}//${splitUrl[2]}"
        return InitConfigResponse4.builder()
            .collectorUrl(httpBaseUrl)
            .build()
    }

    @GetMapping(V5_INIT_CONFIG, produces = [MediaType.APPLICATION_JSON_VALUE])
    fun grpcInitConfig(@RequestParam licenseKey: String, request: HttpServletRequest): InitConfigResponse {
        logger.info { "init config requested from grpc client ${request.remoteAddr} with licenseKey: $licenseKey" }
        return InitConfigResponse.newBuilder()
            .setCollectorUrl(request.requestURL.split("/")[2])
            .build()
    }

    @PostMapping(V4_POLL_CONFIG, produces = [MediaType.APPLICATION_JSON_VALUE])
    fun pollConfig(@RequestBody request: GetConfigRequest4): GetConfigResponse4 {
        return agentService.getLegacyConfig(request.licenseKey, request.jvmUuid)
    }

    @PostMapping(value = [V4_UPLOAD_CODEBASE, V4_UPLOAD_INVOCATION_DATA])
    fun uploadPublication(
        @RequestParam(PARAM_LICENSE_KEY) licenseKey: String,
        @RequestParam(PARAM_PUBLICATION_FILE) file: MultipartFile,
        request: HttpServletRequest,
    ): String {
        logger.info { "loading publications from ${request.remoteAddr}" }
        agentService.saveLegacyPublication(licenseKey, file.inputStream)
        return "OK"
    }
}
