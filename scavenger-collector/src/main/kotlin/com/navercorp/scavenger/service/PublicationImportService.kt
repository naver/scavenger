package com.navercorp.scavenger.service

import com.navercorp.scavenger.model.CallStackImportable
import com.navercorp.scavenger.model.CodeBaseImportable
import com.navercorp.scavenger.model.InvocationImportable
import com.navercorp.scavenger.model.Publication
import com.navercorp.scavenger.util.withCustomerIdMdc
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class PublicationImportService(
    val commonImportService: CommonImportService,
    val codeBaseImportService: CodeBaseImportService,
    val invocationImportService: InvocationImportService,
    val callStackImportService: CallStackImportService
) {
    val logger = KotlinLogging.logger {}

    @Async
    fun import(customerId: Long, pub: Publication): CompletableFuture<Boolean> {
        withCustomerIdMdc(customerId) {
            val startedAt = System.currentTimeMillis()
            try {
                val commonImportResultDto = commonImportService.import(pub.getCommonImportDto(customerId))

                when (pub) {
                    is CodeBaseImportable -> codeBaseImportService.import(pub.getCodeBaseImportDto(commonImportResultDto))
                    is InvocationImportable -> invocationImportService.import(pub.getInvocationImportDto(commonImportResultDto))
                    is CallStackImportable -> callStackImportService.import(pub.getCallStackImportDto(commonImportResultDto))
                }
            } catch (t: Throwable) {
                logger.error(t) { "[$customerId] error occurred while importing pub" }
                throw t
            } finally {
                logger.info { "save publication took ${System.currentTimeMillis() - startedAt} ms" }
            }
        }

        return CompletableFuture.completedFuture(true)
    }
}
