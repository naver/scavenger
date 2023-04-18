package com.navercorp.scavenger.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.navercorp.scavenger.dto.ExportSnapshotMethodDto
import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity
import com.navercorp.scavenger.repository.ExportSnapshotMethodRepository
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.OutputStream

@Service
class ExportSnapshotMethodService(
    var exportSnapshotMethodRepository: ExportSnapshotMethodRepository
) {

    fun writeDtoToTsv(stream: OutputStream, customerId: Long, snapshotId: Long) {
        val data: List<ExportSnapshotMethodEntity> = exportSnapshotMethodRepository.findSnapshotMethodExport(customerId, snapshotId)

        lateinit var rows: MutableList<List<String>>
        try {
            rows = mutableListOf(
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
                        dto.filterInvokedAtMillis?.toString().orEmpty(),
                        dto.packages,
                        dto.status,
                        dto.excludeAbstract?.toString().orEmpty(),
                        dto.parent,
                        dto.signature,
                        dto.type,
                        dto.usedCount.toString(),
                        dto.unusedCount.toString(),
                        dto.lastInvokedAtMillis?.toString().orEmpty()
                    )
                )
            }

            csvWriter {
                delimiter = '\t'
            }.writeAll(
                rows = rows,
                ops = stream
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            rows.clear()
        }
    }
}
