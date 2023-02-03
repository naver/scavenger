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
import com.navercorp.scavenger.service.LeadershipService
import com.navercorp.scavenger.service.OperationService
import io.codekvast.javaagent.model.v4.GetConfigRequest4
import io.codekvast.javaagent.model.v4.GetConfigResponse4
import io.codekvast.javaagent.model.v4.InitConfigResponse4
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.servlet.http.HttpServletRequest
import kotlin.random.Random

@RestController
class AgentController(
    val agentService: AgentService,
    @Value("\${scavenger.operation-directory:}") operationDirectory: String,
    @Value("\${scavenger.grpc-direct-access-port:}") val grpcDirectAccessPort: String,
    val leadershipService: LeadershipService,
    val operationService: OperationService,
) {
    val logger = KotlinLogging.logger {}

    val diagnosisDirectory: String =
        if (operationDirectory.isNotEmpty()) {
            File("$operationDirectory/diagnosis").mkdirs()
            "$operationDirectory/diagnosis"
        } else {
            ""
        }

    val maintenanceDirectory: String =
        if (operationDirectory.isNotEmpty()) {
            File("$operationDirectory/maintenance").mkdirs()
            "$operationDirectory/maintenance"
        } else {
            ""
        }

    val maintenanceInProgressDirectory: String =
        if (operationDirectory.isNotEmpty()) {
            File("$operationDirectory/maintenance-in-progress").mkdirs()
            "$operationDirectory/maintenance-in-progress"
        } else {
            ""
        }

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
        val splitUrl = request.requestURL.split("/")
        val grpcBaseUrl = splitUrl[2].split(":")[0] + if (grpcDirectAccessPort.isEmpty()) "" else ":$grpcDirectAccessPort"
        return InitConfigResponse.newBuilder()
            .setCollectorUrl(grpcBaseUrl)
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
        if (operationService.operationInfo.maintenance && maintenanceDirectory.isNotEmpty()) {
            val maintenanceFile = File(maintenanceDirectory, "${System.currentTimeMillis()}=$licenseKey=${Random.nextLong()}.upload")
            file.transferTo(maintenanceFile)
            logger.info { "file uploaded to ${maintenanceFile.canonicalFile}" }
            return "OK"
        }

        val inputStream =
            if (operationService.operationInfo.diagnosis && diagnosisDirectory.isNotEmpty()) {
                val diagnosisFile = File(maintenanceDirectory, "${request.remoteAddr}-${System.currentTimeMillis()}.upload")
                diagnosisFile.inputStream()
            } else {
                file.inputStream
            }
        agentService.saveLegacyPublication(licenseKey, inputStream)
        return "OK"
    }

    @Scheduled(fixedDelay = 500L) // every 500 mills delay
    fun processMaintainedFile() {
        if (!operationService.operationInfo.maintenance && maintenanceDirectory.isNotEmpty()) {
            File(maintenanceDirectory).listFiles()?.sorted()?.forEach {
                if (!it.exists()) {
                    return@forEach
                }
                val newFilePath = File(maintenanceInProgressDirectory, it.name)
                try {
                    Files.move(it.toPath(), newFilePath.toPath(), StandardCopyOption.ATOMIC_MOVE)
                    val split = it.name.split("=")
                    if (split.size == 3) {
                        newFilePath.inputStream().use {
                            agentService.saveLegacyPublication(split[1], it)
                        }
                    }
                    newFilePath.delete()
                } catch (ignored: Exception) {
                    // ignore
                }
                Thread.sleep(100L) // sleep enough
            }
        }
    }

    @Scheduled(fixedDelay = 3600000L) // every 1 hours
    fun cleanUpFiles() {
        val hour6ago = Instant.now().minus(6, ChronoUnit.HOURS).toEpochMilli()
        if (diagnosisDirectory.isNotEmpty()) {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            File(diagnosisDirectory).listFiles().forEach {
                if (it.lastModified() < hour6ago) {
                    logger.info { "deleting publications from ${it.name}" }
                    it.delete()
                }
            }
        }
    }
}
