package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.ApplicationDetailDto
import com.navercorp.scavenger.dto.ApplicationDto
import com.navercorp.scavenger.service.ApplicationService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ApplicationController(private val applicationService: ApplicationService) {
    @GetMapping("/customers/{customerId}/applications")
    fun getMethodInvocations(@PathVariable customerId: Long): List<ApplicationDto> {
        return applicationService.getApplications(customerId)
    }

    @GetMapping("/customers/{customerId}/applications/_detail")
    fun getApplicationsDetail(@PathVariable customerId: Long): List<ApplicationDetailDto> {
        return applicationService.getApplicationsDetail(customerId)
    }

    @DeleteMapping("/customers/{customerId}/applications/{applicationId}")
    fun deleteApplication(
        @PathVariable customerId: Long,
        @PathVariable applicationId: Long
    ) {
        applicationService.deleteApplication(customerId, applicationId)
    }
}
