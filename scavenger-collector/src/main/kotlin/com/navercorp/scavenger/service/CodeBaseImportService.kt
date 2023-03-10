package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CodeBaseImportDto
import com.navercorp.scavenger.entity.CodeBaseFingerprintEntity
import com.navercorp.scavenger.param.InvocationUpsertParam
import com.navercorp.scavenger.param.MethodUpsertParam
import com.navercorp.scavenger.repository.CodeBaseFingerprintDao
import com.navercorp.scavenger.repository.InvocationDao
import com.navercorp.scavenger.repository.MethodDao
import io.codekvast.javaagent.model.v4.SignatureStatus4
import mu.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class CodeBaseImportService(
    val codeBaseFingerprintDao: CodeBaseFingerprintDao,
    val methodDao: MethodDao,
    val invocationDao: InvocationDao,
) {
    val logger = KotlinLogging.logger {}

    fun import(codeBaseImportDto: CodeBaseImportDto) {
        with(codeBaseImportDto) {
            logger.info { "[${codeBaseImportDto.customerId}] trying to import ${entries.size} methods: $codeBaseImportDto" }

            val publishedAt = Instant.ofEpochMilli(publishedAtMillis).truncatedTo(ChronoUnit.MINUTES)
            val truncatedPublishedAtMillis = publishedAt.toEpochMilli()

            // codebase fingerprint should be imported even if it is an empty publication
            val newCodeBaseImported =
                importCodeBaseFingerprint(customerId, applicationId, publishedAt, codeBaseFingerprint)

            if (codeBaseImportDto.entries.isEmpty()) {
                logger.warn { "[$customerId] skipping empty codebase import: $codeBaseImportDto" }
                return
            }

            if (newCodeBaseImported) {
                try {
                    // insert new methods or update last seen
                    upsertMethods(customerId, publishedAt, truncatedPublishedAtMillis, entries)
                    // insert new invocations or update last seen
                    ensureInitialInvocations(codeBaseImportDto, truncatedPublishedAtMillis)

                    logger.info {
                        "[$customerId] importing codebase is completed. $codeBaseImportDto"
                    }
                } catch (e: Exception) {
                    logger.error(e) {
                        "[$customerId] codebase import error occurred. " +
                            "applicationId=$applicationId, environmentId=$environmentId, fingerprint=$codeBaseFingerprint"
                    }
                }
            } else {
                val normal =
                    invocationDao.hasNotInvokedInvocation(customerId, applicationId, environmentId)

                if (!normal) {
                    ensureInitialInvocations(codeBaseImportDto, truncatedPublishedAtMillis)
                    logger.info {
                        "[$customerId] codebase imported due to no NOT_INVOKED invocation" +
                            "applicationId=$applicationId, environmentId=$environmentId, fingerprint=$codeBaseFingerprint"
                    }
                }
            }
        }
    }

    fun importCodeBaseFingerprint(
        customerId: Long,
        applicationId: Long,
        publishedAt: Instant,
        codeBaseFingerprint: String
    ): Boolean {
        val existingCodeBaseFingerprint =
            codeBaseFingerprintDao.findByCustomerIdAndApplicationIdAndCodeBaseFingerprint(customerId, applicationId, codeBaseFingerprint)

        if (existingCodeBaseFingerprint != null) {
            codeBaseFingerprintDao.updatePublishedAt(existingCodeBaseFingerprint.copy(publishedAt = publishedAt))
            logger.info { "codebase fingerprint $codeBaseFingerprint already exists. publishedAt updated to $publishedAt" }
            return false
        } else {
            try {
                codeBaseFingerprintDao.insert(
                    CodeBaseFingerprintEntity(
                        customerId = customerId,
                        applicationId = applicationId,
                        codeBaseFingerprint = codeBaseFingerprint,
                        publishedAt = publishedAt,
                        createdAt = publishedAt
                    )
                )
            } catch (e: DbActionExecutionException) {
                if (e.cause is DuplicateKeyException) {
                    logger.warn(e.cause) { "codebase fingerprint import is failed. it's ok if it's uniq violation" }
                    // treat uniq violation as existing code base fingerprint case
                    return false
                }
                throw e
            }
            return true
        }
    }

    fun upsertMethods(
        customerId: Long,
        publishedAt: Instant,
        lastSeenAtMillis: Long,
        entries: List<CodeBaseImportDto.CodeBaseEntry>
    ) {
        entries.map { entry ->
            MethodUpsertParam(
                customerId = customerId,
                signature = entry.signature,
                visibility = entry.visibility,
                declaringType = entry.declaringType,
                methodName = entry.methodName,
                modifiers = entry.modifiers,
                createdAt = publishedAt,
                lastSeenAtMillis = lastSeenAtMillis,
                garbage = false,
                signatureHash = entry.signatureHash
            )
        }.chunked(BATCH_CHUNK_SIZE).forEach(methodDao::batchUpsert)
    }

    fun ensureInitialInvocations(
        codeBaseImportDto: CodeBaseImportDto,
        lastSeenAtMillis: Long
    ) {
        codeBaseImportDto.entries.chunked(BATCH_CHUNK_SIZE).forEach { entries ->
            entries.map { entry ->
                with(codeBaseImportDto) {
                    InvocationUpsertParam(
                        customerId = customerId,
                        applicationId = applicationId,
                        environmentId = environmentId,
                        signatureHash = entry.signatureHash,
                        status = SignatureStatus4.NOT_INVOKED.name,
                        invokedAtMillis = 0,
                        lastSeenAtMillis = lastSeenAtMillis
                    )
                }
            }.run(invocationDao::batchUpsertLastSeen)
        }
    }

    companion object {
        private const val BATCH_CHUNK_SIZE = 500
    }
}
