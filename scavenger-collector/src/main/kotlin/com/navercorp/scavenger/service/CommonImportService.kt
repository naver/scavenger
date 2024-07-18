package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CommonImportDto
import com.navercorp.scavenger.dto.CommonImportResultDto
import com.navercorp.scavenger.param.JvmUpsertParam
import com.navercorp.scavenger.repository.ApplicationDao
import com.navercorp.scavenger.repository.EnvironmentDao
import com.navercorp.scavenger.repository.JvmDao
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CommonImportService(
    val applicationDao: ApplicationDao,
    val environmentDao: EnvironmentDao,
    val jvmDao: JvmDao
) {
    val logger = KotlinLogging.logger {}

    fun import(commonImportDto: CommonImportDto): CommonImportResultDto {
        with(commonImportDto) {
            val createdAt = Instant.ofEpochMilli(jvmStartedAtMillis)

            val applicationId = applicationDao.upsert(customerId, appName, createdAt) ?: run {
                throw RuntimeException("error while importing application $appName of customerId $customerId")
            }

            val environmentId = environmentDao.upsert(customerId, environment, createdAt) ?: run {
                throw RuntimeException("error while importing environment $environment of customerId $customerId")
            }

            val jvmId = jvmDao.upsert(
                JvmUpsertParam(
                    customerId = customerId,
                    applicationId = applicationId,
                    applicationVersion = appVersion,
                    environmentId = environmentId,
                    uuid = jvmUuid,
                    codeBaseFingerprint = codeBaseFingerprint,
                    createdAt = createdAt,
                    publishedAt = Instant.ofEpochMilli(publishedAtMillis),
                    hostname = hostname,
                )
            ) ?: run {
                throw RuntimeException("error while importing jvm $jvmUuid of customerId $customerId $appName($environment)")
            }

            return CommonImportResultDto(
                customerId = customerId,
                applicationId = applicationId,
                environmentId = environmentId,
                jvmId = jvmId,
                publishedAtMillis = publishedAtMillis
            ).also {
                logger.info { "common data imported: $commonImportDto" }
            }
        }
    }
}
