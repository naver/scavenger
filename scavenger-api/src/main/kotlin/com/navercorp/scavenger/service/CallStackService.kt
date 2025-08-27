package com.navercorp.scavenger.service

import com.navercorp.scavenger.entity.CallStackSignatureDbRow
import com.navercorp.scavenger.entity.SnapshotEntity
import com.navercorp.scavenger.repository.CallStackDao
import org.springframework.stereotype.Service

@Service
class CallStackService(
    private val callStackDao: CallStackDao,
) {
    fun getCallStackMap(
        customerId: Long,
        snapshotId: Long,
        snapshot: SnapshotEntity
    ): Map<String, List<String>> {
        val callStacks = callStackDao.findAllCallStacks(
            customerId,
            snapshot.applications.map { it.applicationId },
            snapshot.environments.map { it.environmentId },
            snapshot.filterInvokedAtMillis
        )

        return buildCallStackMap(callStacks)
    }

    private fun buildCallStackMap(callStacks: List<CallStackSignatureDbRow>): Map<String, List<String>> {
        val callStackMap = mutableMapOf<String, MutableList<String>>()
        callStacks.forEach { callStack ->
            callStackMap.getOrPut(callStack.calleeSignature) { mutableListOf() }
                .add(callStack.callerSignature)
        }
        return callStackMap
    }
}
