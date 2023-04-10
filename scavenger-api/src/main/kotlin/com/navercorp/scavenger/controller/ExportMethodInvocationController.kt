package com.navercorp.scavenger.controller

import com.navercorp.scavenger.service.ExportMethodInvocationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class ExportMethodInvocationController(
    private val response: HttpServletResponse,
    private val exportMethodInvocationService: ExportMethodInvocationService
) {
    @GetMapping("/customers/{customerId}/export/method-invocation", produces = ["text/csv"])
    fun listMethodInvocations(
        @PathVariable customerId: Long,
        @RequestParam fn: String
    ) {
        response.characterEncoding = "UTF-8"
        response.contentType = "text/csv; charset=UTF-8"

        response.setHeader(
            "Content-disposition",
            "attachment;filename=$fn"
        )
        exportMethodInvocationService.writeDtoToTsv(response.writer, customerId)
    }
}
