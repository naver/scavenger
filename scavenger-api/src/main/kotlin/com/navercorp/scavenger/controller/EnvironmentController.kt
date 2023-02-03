package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.EnvironmentDetailDto
import com.navercorp.scavenger.dto.EnvironmentDto
import com.navercorp.scavenger.service.EnvironmentService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class EnvironmentController(private val environmentService: EnvironmentService) {
    @GetMapping("/customers/{customerId}/environments")
    fun getMethodInvocations(@PathVariable customerId: Long): List<EnvironmentDto> {
        return environmentService.getEnvironments(customerId)
    }

    @GetMapping("/customers/{customerId}/environments/_detail")
    fun getEnvironmentsDetail(@PathVariable customerId: Long): List<EnvironmentDetailDto> {
        return environmentService.getEnvironmentsDetail(customerId)
    }

    @DeleteMapping("/customers/{customerId}/environments/{environmentId}")
    fun deleteEnvironments(
        @PathVariable customerId: Long,
        @PathVariable environmentId: Long
    ) {
        environmentService.deleteEnvironment(customerId, environmentId)
    }
}
