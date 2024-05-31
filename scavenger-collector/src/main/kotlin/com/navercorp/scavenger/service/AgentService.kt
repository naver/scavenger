package com.navercorp.scavenger.service

import com.navercorp.scavenger.exception.DisabledCustomerException
import com.navercorp.scavenger.exception.LicenseKeyMismatchException
import com.navercorp.scavenger.model.GetConfigResponse
import com.navercorp.scavenger.model.ProtoPublication
import com.navercorp.scavenger.model.toLegacyPublication
import io.codekvast.javaagent.model.v4.GetConfigResponse4
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.io.InputStream
import kotlin.random.Random

@Service
class AgentService(
    val licenseService: LicenseService,
    val publicationImportService: PublicationImportService,
    val intervalService: IntervalService,
    val agentStateService: AgentStateService,
    val operationService: OperationService
) {
    val logger = KotlinLogging.logger {}

    fun saveLegacyPublication(apiKey: String, inputStream: InputStream) {
        val customer = licenseService.check(apiKey)

        if (operationService.isDisabledCustomerId(customer.id)) {
            logger.info { "[${customer.id}] legacy publication of customer ${customer.id} is ignored" }
            throw DisabledCustomerException("[${customer.id}] publication of customer is ignored")
        }

        val pub = inputStream.toLegacyPublication()

        if (customer.id == pub.commonData.customerId) {
            publicationImportService.import(customer.id, pub)
        } else {
            throw LicenseKeyMismatchException(
                "provided licenseKey for customer ${customer.id}" +
                    " is not same with publication customerId ${pub.commonData.customerId}"
            )
        }
    }

    fun savePublication(pub: ProtoPublication) {
        val customer = licenseService.check(pub.commonData.apiKey)

        if (operationService.isDisabledCustomerId(customer.id)) {
            logger.info { "[${customer.id}] publication of customer is ignored" }
            throw DisabledCustomerException("[${customer.id}] publication of customer is ignored")
        }

        publicationImportService.import(customer.id, pub)
    }

    fun getLegacyConfig(licenseKey: String, jvmUuid: String): GetConfigResponse4 {
        val customer = licenseService.check(licenseKey)

        with(intervalService.get(customer.id, jvmUuid)) {
            agentStateService.update(customer.id, jvmUuid, pollIntervalSeconds)

            val response = GetConfigResponse4.builder()
                .customerId(customer.id)
                .configPollIntervalSeconds(pollIntervalSeconds)
                .configPollRetryIntervalSeconds(retryIntervalSeconds)
                .codeBasePublisherName("http")
                .codeBasePublisherConfig("enabled=true")
                .invocationDataPublisherName("http")
                .invocationDataPublisherConfig("enabled=true")

            if (operationService.isDisabledCustomerId(customer.id)) {
                val delayedIntervalSeconds = 1 * 60 * 60 // 1 hrs

                logger.info { "[${customer.id}] publishing of customer ${customer.id} is delayed" }

                return response
                    .codeBasePublisherCheckIntervalSeconds(delayedIntervalSeconds)
                    .codeBasePublisherRetryIntervalSeconds(delayedIntervalSeconds)
                    .invocationDataPublisherIntervalSeconds(delayedIntervalSeconds)
                    .invocationDataPublisherRetryIntervalSeconds(delayedIntervalSeconds)
                    .build()
            } else {

                return response
                    .codeBasePublisherCheckIntervalSeconds(publishIntervalSeconds + Random.nextInt(10))
                    .codeBasePublisherRetryIntervalSeconds(retryIntervalSeconds)
                    .invocationDataPublisherIntervalSeconds(publishIntervalSeconds + Random.nextInt(10))
                    .invocationDataPublisherRetryIntervalSeconds(retryIntervalSeconds)
                    .build()
            }
        }
    }

    fun getConfig(licenseKey: String, jvmUuid: String): GetConfigResponse {
        return with(getLegacyConfig(licenseKey, jvmUuid)) {
            GetConfigResponse.newBuilder()
                .setConfigPollIntervalSeconds(configPollIntervalSeconds)
                .setConfigPollRetryIntervalSeconds(configPollRetryIntervalSeconds)
                .setCodeBasePublisherCheckIntervalSeconds(codeBasePublisherCheckIntervalSeconds)
                .setCodeBasePublisherRetryIntervalSeconds(codeBasePublisherRetryIntervalSeconds)
                .setInvocationDataPublisherIntervalSeconds(invocationDataPublisherIntervalSeconds)
                .setInvocationDataPublisherRetryIntervalSeconds(invocationDataPublisherRetryIntervalSeconds)
                .build()
        }
    }
}
