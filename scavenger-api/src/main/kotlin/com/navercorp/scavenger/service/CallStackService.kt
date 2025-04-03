package com.navercorp.scavenger.service

import com.navercorp.scavenger.repository.CallStackDao
import org.springframework.stereotype.Service

@Service
class CallStackService(
    private val snapshotService: SnapshotService,
    private val callStackDao: CallStackDao,
) {

    fun getCallerSignatures(
        customerId: Long,
        snapshotId: Long,
        signature: String,
    ): List<String> {
        val snapshot = snapshotService.getSnapshot(snapshotId)

        return callStackDao.findCallerSignatures(customerId,
            snapshot.applications.map { it.applicationId },
            snapshot.environments.map { it.environmentId },
            signature,
            snapshot.filterInvokedAtMillis
        )
    }
}
