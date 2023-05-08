package com.navercorp.scavenger.controller

import com.navercorp.scavenger.dto.SnapshotDto
import com.navercorp.scavenger.dto.SnapshotExportDto
import com.navercorp.scavenger.entity.SnapshotNodeEntity
import com.navercorp.scavenger.service.SnapshotNodeService
import com.navercorp.scavenger.service.SnapshotService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SnapshotController(
    private val snapshotService: SnapshotService,
    private val snapshotNodeService: SnapshotNodeService
) {
    @GetMapping("/customers/{customerId}/snapshots")
    fun listSnapshots(@PathVariable customerId: Long): List<SnapshotDto> {
        return snapshotService.listSnapshots(customerId)
    }

    @PostMapping("/customers/{customerId}/snapshots")
    fun createSnapshot(
        @PathVariable customerId: Long,
        @RequestBody createSnapshotRequestParams: CreateSnapshotRequestParams
    ): SnapshotDto {
        return snapshotService.createSnapshot(
            customerId = customerId,
            name = createSnapshotRequestParams.name,
            applicationIdList = createSnapshotRequestParams.applicationIdList,
            environmentIdList = createSnapshotRequestParams.environmentIdList,
            filterInvokedAtMillis = createSnapshotRequestParams.filterInvokedAtMillis,
            packages = createSnapshotRequestParams.packages
        )
    }

    @GetMapping("/customers/{customerId}/snapshots/{snapshotId}")
    fun readSnapshot(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
        @RequestParam parent: String
    ): List<SnapshotNodeEntity> {
        return snapshotNodeService.readSnapshotNode(customerId, snapshotId, parent)
    }

    @PutMapping("/customers/{customerId}/snapshots/{snapshotId}")
    fun updateSnapshot(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
        @RequestBody createSnapshotRequestParams: CreateSnapshotRequestParams
    ): SnapshotDto {
        return createSnapshotRequestParams.run {
            snapshotService.updateSnapshot(
                customerId = customerId,
                snapshotId = snapshotId,
                name = name,
                applicationIdList = applicationIdList,
                environmentIdList = environmentIdList,
                filterInvokedAtMillis = filterInvokedAtMillis,
                packages = packages
            )
        }
    }

    @PostMapping("/customers/{customerId}/snapshots/{snapshotId}/refresh")
    fun refreshSnapshot(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long
    ) {
        snapshotService.refreshSnapshot(customerId, snapshotId)
    }

    @DeleteMapping("/customers/{customerId}/snapshots/{snapshotId}")
    fun deleteSnapshot(@PathVariable customerId: Long, @PathVariable snapshotId: Long) {
        snapshotService.deleteSnapshot(customerId, snapshotId)
    }

    @GetMapping("/customers/{customerId}/snapshots/{snapshotId}/nodes")
    fun getSnapshotNodesBySignatureContaining(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
        @RequestParam signature: String,
        @RequestParam(required = false) snapshotNodeId: Long?
    ): List<SnapshotNodeEntity> {
        return snapshotNodeService.getSnapshotNodesBySignatureContaining(
            customerId,
            snapshotId,
            signature,
            snapshotNodeId
        )
    }

    @GetMapping("/customers/{customerId}/snapshot/{snapshotId}/export")
    fun exportSnapshot(
        @PathVariable customerId: Long,
        @PathVariable snapshotId: Long,
    ): List<SnapshotExportDto> {
        return snapshotNodeService.getAllExportSnapshotNode(customerId, snapshotId)
    }

    data class CreateSnapshotRequestParams(
        val name: String,
        val applicationIdList: List<Long>,
        val environmentIdList: List<Long>,
        val filterInvokedAtMillis: Long,
        val packages: String
    )
}
