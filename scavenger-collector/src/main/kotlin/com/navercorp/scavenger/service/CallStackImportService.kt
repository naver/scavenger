package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.CallStackImportDto
import com.navercorp.scavenger.param.CallStackUpsertParam
import com.navercorp.scavenger.repository.CallStackDao
import io.github.oshai.kotlinlogging.KotlinLogging
 import org.springframework.dao.DeadlockLoserDataAccessException
import org.springframework.stereotype.Service
 import java.time.Instant
 import java.time.temporal.ChronoUnit
 import kotlin.random.Random

@Service
class CallStackImportService(
    val callStackDao: CallStackDao
) {
    val logger = KotlinLogging.logger {}

    fun import(callStackImportDto: CallStackImportDto) {
         with(callStackImportDto) {
             var importedCallStackCount = 0
             val instantAtMinutes = Instant.ofEpochMilli(invokedAtMillis).truncatedTo(ChronoUnit.MINUTES)
             logger.info { "trying to import ${callTraces.size} callstack: $callStackImportDto" }
             callTraces.chunked(BATCH_CHUNK_SIZE).forEach { chunked ->
                 for (index in 0 until MAX_RETRY_COUNT) {
                     try {
                         callStackDao.batchUpsert(
                             chunked.map {
                                 CallStackUpsertParam(
                                     customerId = customerId,
                                     applicationId = applicationId,
                                     environmentId = environmentId,
                                     signatureHash = it.callee,
                                     callerSignatureHash = it.caller,
                                     invokedAtMillis = instantAtMinutes.toEpochMilli()
                                 )
                             }
                         )
                             .also { importedCallStackCount += it.count { rowCount -> rowCount > 0 } }
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
                             logger.error(e) { "[$customerId] error occurred while updating call stack chunk" }
                             throw e
                         }
                     }
                 }
             }
             logger.info { "$importedCallStackCount callstack updated" }
         }
    }

    companion object {
        private const val BATCH_CHUNK_SIZE = 500
        private const val MAX_RETRY_COUNT = 3
    }
}
