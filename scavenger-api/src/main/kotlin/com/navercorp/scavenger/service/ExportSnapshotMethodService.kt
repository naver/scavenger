package com.navercorp.scavenger.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.navercorp.scavenger.dto.ExportSnapshotMethodDto
import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity
import com.navercorp.scavenger.repository.ExportSnapshotMethodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.io.OutputStream

@Service
class ExportSnapshotMethodService(
    var exportSnapshotMethodRepository: ExportSnapshotMethodRepository
) {

    @Transactional(readOnly = true)
    fun writeDtoToTsv(stream: OutputStream, customerId: Long, snapshotId: Long) {
        val data: List<ExportSnapshotMethodEntity> = exportSnapshotMethodRepository.findSnapshotMethodExport(customerId, snapshotId)

        try {
            val rows = mutableListOf(
                listOf(
                    "filterInvokedAtMillis",
                    "packages",
                    "status",
                    "excludeAbstract",
                    "parent",
                    "signature",
                    "type",
                    "usedCount",
                    "unusedCount",
                    "lastInvokedAtMillis"
                )
            )

            data.forEach { entity: ExportSnapshotMethodEntity ->
                val dto = ExportSnapshotMethodDto.from(entity)
                rows.add(
                    listOf(
                        dto.filterInvokedAtMillis,
                        dto.packages,
                        dto.status,
                        dto.excludeAbstract,
                        dto.parent,
                        dto.signature,
                        dto.type,
                        dto.usedCount,
                        dto.unusedCount,
                        dto.lastInvokedAtMillis
                    )
                )
            }

            csvWriter {
                delimiter = '\t'
            }.writeAll(rows, stream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
