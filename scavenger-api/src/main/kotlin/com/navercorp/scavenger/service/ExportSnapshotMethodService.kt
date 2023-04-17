package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.ExportSnapshotMethodDto
import com.navercorp.scavenger.entity.ExportSnapshotMethodEntity
import com.navercorp.scavenger.repository.ExportSnapshotMethodRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.io.Writer

@Service
class ExportSnapshotMethodService(
    var exportSnapshotMethodRepository: ExportSnapshotMethodRepository
) {

    @Transactional(readOnly = true)
    fun writeDtoToTsv(writer: Writer, customerId: Long, snapshotId: Long) {
        val data: List<ExportSnapshotMethodEntity> = exportSnapshotMethodRepository.findSnapshotMethodExport(customerId, snapshotId)

        try {
            val tsvPrinter = CSVPrinter(writer, CSVFormat.MONGODB_TSV)
            tsvPrinter.printRecord(
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

            data.forEach { entity: ExportSnapshotMethodEntity ->
                val dto = ExportSnapshotMethodDto.from(entity)
                tsvPrinter.printRecord(
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
            }
            tsvPrinter.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
