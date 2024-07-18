package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.InvocationImportDto
import com.navercorp.scavenger.param.InvocationUpsertParam
import com.navercorp.scavenger.repository.InvocationDao
import io.codekvast.javaagent.model.v4.SignatureStatus4
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DeadlockLoserDataAccessException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@Service
class InvocationImportService(
    val invocationDao: InvocationDao
) {
    val logger = KotlinLogging.logger {}

    fun import(invocationImportDto: InvocationImportDto) {
        with(invocationImportDto) {
            var importedInvocationCount = 0
            val instantAtMinutes = Instant.ofEpochMilli(invokedAtMillis).truncatedTo(ChronoUnit.MINUTES)
            logger.info { "trying to import ${invocations.size} invocations: $invocationImportDto" }
            invocations.chunked(BATCH_CHUNK_SIZE).forEach { chunked ->
                for (index in 0 until MAX_RETRY_COUNT) {
                    try {
                        invocationDao.batchUpsert(
                            chunked.map {
                                InvocationUpsertParam(
                                    customerId = customerId,
                                    applicationId = applicationId,
                                    environmentId = environmentId,
                                    signatureHash = it,
                                    status = SignatureStatus4.INVOKED.name,
                                    invokedAtMillis = instantAtMinutes.toEpochMilli()
                                )
                            }
                        )
                            .also { importedInvocationCount += it.count { rowCount -> rowCount > 0 } }
                        break
                    } catch (e: Exception) {
                        if (e is DeadlockLoserDataAccessException) {
                            if (index < (MAX_RETRY_COUNT - 1)) {
                                logger.info(e) { "[$customerId] deadlock detected. retry" }
                                Thread.sleep(Random.nextLong(1000L))
                            } else {
                                logger.warn(e) { "[$customerId] deadlock detected. but skipped due to retry failure" }
                            }
                        } else {
                            logger.error(e) { "[$customerId] error occurred while updating invocation chunk" }
                            throw e
                        }
                    }
                }
            }
            logger.info { "$importedInvocationCount invocations updated" }
        }
    }

    companion object {
        private const val BATCH_CHUNK_SIZE = 500
        private const val MAX_RETRY_COUNT = 3
    }
}
