package com.navercorp.scavenger.controller

import com.navercorp.scavenger.service.ExportSnapshotMethodService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class ExportController(
    private val response: HttpServletResponse,
    private val exportSnapshotMethodService: ExportSnapshotMethodService
) {
    @GetMapping("/customers/{customerId}/export/snapshot/{snapshotId}", produces = ["text/csv"])
    fun exportSnapshotMethod(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
        @RequestParam fn: String
    ) {
        response.characterEncoding = "UTF-8"
        response.contentType = "text/csv; charset=UTF-8"

        response.setHeader(
            "Content-disposition",
            "attachment;filename=$fn"
        )
        exportSnapshotMethodService.writeDtoToTsv(response.writer, customerId, snapshotId)
    }
}
