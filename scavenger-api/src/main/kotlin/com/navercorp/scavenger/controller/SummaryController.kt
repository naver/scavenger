package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.SummaryDto
import com.navercorp.scavenger.service.SummaryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SummaryController(private val summaryService: SummaryService) {
    @GetMapping("/customers/{customerId}/summary")
    fun getSummary(@PathVariable customerId: Long): SummaryDto {
        return summaryService.getSummaryByCustomerId(customerId)
    }
}
