package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.ExportMethodInvokationDto
import com.navercorp.scavenger.entity.ExportMethodInvocationEntity
import com.navercorp.scavenger.repository.ExportMethodInvocationRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.io.Writer

@Service
class ExportMethodInvocationService(
    var exportMethodInvocationRepository: ExportMethodInvocationRepository
) {

    @Transactional(readOnly = true)
    fun writeDtoToTsv(writer: Writer, customerId: Long) {
        val data: List<ExportMethodInvocationEntity> = exportMethodInvocationRepository.findMethodInvocationExports(customerId)

        try {
            val tsvPrinter = CSVPrinter(writer, CSVFormat.MONGODB_TSV)
            tsvPrinter.printRecord(
                "id",
                "Modifiers",
                "Signature",
                "CreatedAt",
                "LastSeenAtMills",
                "InvokedAtMiils",
                "Status",
                "Timestamp"
            )

            data.forEach { entity: ExportMethodInvocationEntity ->
                val dto = ExportMethodInvokationDto.from(entity)
                tsvPrinter.printRecord(
                    dto.id,
                    dto.modifiers,
                    dto.signature,
                    dto.createdAt,
                    dto.lastSeenAtMillis,
                    dto.invokedAtMillis,
                    dto.status,
                    dto.timestamp
                )
            }
            tsvPrinter.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
