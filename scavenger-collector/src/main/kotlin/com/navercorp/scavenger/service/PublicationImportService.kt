package com.navercorp.scavenger.service

import com.navercorp.scavenger.model.CodeBaseImportable
import com.navercorp.scavenger.model.InvocationImportable
import com.navercorp.scavenger.model.Publication
import com.navercorp.scavenger.util.withCustomerIdMdc
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PublicationImportService(
    val commonImportService: CommonImportService,
    val codeBaseImportService: CodeBaseImportService,
    val invocationImportService: InvocationImportService
) {
    val logger = KotlinLogging.logger {}

    @Transactional
    fun import(customerId: Long, pub: Publication): Boolean {
        withCustomerIdMdc(customerId) {
            val startedAt = System.currentTimeMillis()
            try {
                val commonImportResultDto = commonImportService.import(pub.getCommonImportDto(customerId))

                when (pub) {
                    is CodeBaseImportable -> codeBaseImportService.import(pub.getCodeBaseImportDto(commonImportResultDto))
                    is InvocationImportable -> invocationImportService.import(pub.getInvocationImportDto(commonImportResultDto))
                }
            } catch (t: Throwable) {
                logger.error(t) { "[$customerId] error occurred while importing pub" }
                throw t
            } finally {
                logger.info { "save publication took ${System.currentTimeMillis() - startedAt} ms" }
            }
        }

        return true
    }
}
