package com.navercorp.scavenger.controller

import com.navercorp.scavenger.service.ExportSnapshotMethodService
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RestController
@RequestMapping("/api")
class ExportController(
    private val exportSnapshotMethodService: ExportSnapshotMethodService
) {
    @GetMapping("/customers/{customerId}/export/snapshot/{snapshotId}", produces = ["text/csv"])
    fun exportSnapshotMethod(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
        @RequestParam fn: String
    ): ResponseEntity<Resource> {
        val byteArrayInputStream = ByteArrayOutputStream().use {
            exportSnapshotMethodService.writeSnapshotToTsv(
                stream = it,
                customerId = customerId,
                snapshotId = snapshotId
            )
            ByteArrayInputStream(it.toByteArray())
        }

        val headers = HttpHeaders().apply {
            set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$fn")
            set(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
        }

        return ResponseEntity(
            InputStreamResource(byteArrayInputStream),
            headers,
            HttpStatus.OK
        )
    }
}
