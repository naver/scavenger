package com.navercorp.scavenger.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.navercorp.scavenger.dto.SnapshotExportDto
import com.navercorp.scavenger.repository.SnapshotNodeDao
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.OutputStream

@Service
class SnapshotExportService(
    private val snapshotNodeDao: SnapshotNodeDao
) {
    fun writeSnapshotToTsv(stream: OutputStream, customerId: Long, snapshotId: Long) {
        try {
            csvWriter {
                delimiter = '\t'
            }.open(stream) {
                writeRow(
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
                snapshotNodeDao.findAllExportSnapshotNode(
                    customerId = customerId,
                    snapshotId = snapshotId
                ).map { writeRow(SnapshotExportDto.from(it).toList()) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
