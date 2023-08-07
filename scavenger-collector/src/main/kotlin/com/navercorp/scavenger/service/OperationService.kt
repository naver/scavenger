package com.navercorp.scavenger.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import javax.annotation.PostConstruct

@Service
class OperationService(
    @Value("\${scavenger.operation-info-url:}") val operationInfoUrl: String,
    val restTemplate: RestTemplate,
) {
    var operationInfo: OperationInfo = OperationInfo(emptyList())
    val logger = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        dispatch()
    }

    @Scheduled(fixedDelay = 60000)
    fun dispatch() {
        if (operationInfoUrl.isEmpty()) {
            return
        }
        try {
            val operationInfo = restTemplate.getForObject(
                operationInfoUrl,
                OperationInfo::class.java
            ).let {
                requireNotNull(it)
            }
            if (this.operationInfo != operationInfo) {
                logger.info { "Operation info is changed to $operationInfo" }
                this.operationInfo = operationInfo
            }
        } catch (ignored: Exception) {
            logger.error(ignored) { "Error occurred while dispatching $operationInfoUrl, operation job is skipped" }
        }
    }

    fun isDisabledCustomerId(customerId: Long): Boolean {
        return customerId in operationInfo.disabledCustomerIds
    }

    data class OperationInfo(
        val disabledCustomerIds: List<Long> = emptyList(),
        val blockGc: Boolean = false,
    )
}
