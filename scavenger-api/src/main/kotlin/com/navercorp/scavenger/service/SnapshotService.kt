package com.navercorp.scavenger.service

import com.navercorp.scavenger.dto.SnapshotDto
import com.navercorp.scavenger.entity.ApplicationRefEntity
import com.navercorp.scavenger.entity.EnvironmentRefEntity
import com.navercorp.scavenger.entity.SnapshotEntity
import com.navercorp.scavenger.exception.SnapshotCountExceeded
import com.navercorp.scavenger.repository.MethodInvocationRepository
import com.navercorp.scavenger.repository.SnapshotApplicationDao
import com.navercorp.scavenger.repository.SnapshotDao
import com.navercorp.scavenger.repository.SnapshotEnvironmentDao
import com.navercorp.scavenger.util.proxy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SnapshotService(
    private val methodInvocationRepository: MethodInvocationRepository,
    private val snapshotDao: SnapshotDao,
    private val snapshotApplicationDao: SnapshotApplicationDao,
    private val snapshotEnvironmentDao: SnapshotEnvironmentDao,
    private val snapshotNodeService: SnapshotNodeService,
) {
    fun createSnapshot(
        customerId: Long,
        name: String,
        applicationIdList: List<Long>,
        environmentIdList: List<Long>,
        filterInvokedAtMillis: Long,
        packages: String
    ): SnapshotDto {
        val methodInvocations = methodInvocationRepository.findAllMethodInvocations(customerId, applicationIdList, environmentIdList)
        val snapshotEntity = SnapshotEntity(
            name = name,
            customerId = customerId,
            createdAt = Instant.now(),
            filterInvokedAtMillis = filterInvokedAtMillis,
            packages = packages,
            applications = applicationIdList.map { ApplicationRefEntity(it, customerId, 0) }.toSet(),
            environments = environmentIdList.map { EnvironmentRefEntity(it, customerId, 0) }.toSet(),
        )
        return proxy(this).saveSnapshotWithLimit(snapshotEntity).let {
            snapshotNodeService.createAndSaveSnapshotNodes(it, methodInvocations)
            SnapshotDto.from(it)
        }
    }

    @Transactional
    fun updateSnapshot(
        customerId: Long,
        snapshotId: Long,
        name: String,
        applicationIdList: List<Long>,
        environmentIdList: List<Long>,
        filterInvokedAtMillis: Long,
        packages: String
    ): SnapshotDto {
        val existing = snapshotDao.findByCustomerIdAndId(customerId, snapshotId).orElseThrow()
        val methodInvocations = methodInvocationRepository.findAllMethodInvocations(existing.customerId, applicationIdList, environmentIdList)
        val snapshot = existing.copy(packages = packages, name = name, filterInvokedAtMillis = filterInvokedAtMillis)

        snapshotDao.updateSnapshot(snapshot)

        snapshotApplicationDao.deleteByCustomerIdAndSnapshotId(existing.customerId, requireNotNull(existing.id))
        applicationIdList.map { ApplicationRefEntity(applicationId = it, customerId = existing.customerId, snapshotId = existing.id) }.toSet()
            .let {
                snapshotApplicationDao.insertAll(it)
            }

        snapshotEnvironmentDao.deleteByCustomerIdAndSnapshotId(existing.customerId, requireNotNull(existing.id))
        environmentIdList.map { EnvironmentRefEntity(environmentId = it, customerId = existing.customerId, snapshotId = existing.id) }.toSet()
            .let {
                snapshotEnvironmentDao.insertAll(it)
            }

        snapshotNodeService.deleteSnapshotNode(existing.customerId, snapshotId)
        snapshotNodeService.createAndSaveSnapshotNodes(snapshot, methodInvocations)

        return SnapshotDto.from(snapshot)
    }

    @Transactional
    fun refreshSnapshot(customerId: Long, snapshotId: Long) {
        val snapshot = snapshotDao.findByCustomerIdAndId(customerId, snapshotId).orElseThrow()
        val applicationIdList: List<Long> = snapshot.applications.map { it.applicationId }
        val environmentIdList: List<Long> = snapshot.environments.map { it.environmentId }
        val methodInvocations = methodInvocationRepository.findAllMethodInvocations(snapshot.customerId, applicationIdList, environmentIdList)

        snapshotNodeService.deleteSnapshotNode(snapshot.customerId, snapshotId)
        snapshotNodeService.createAndSaveSnapshotNodes(snapshot, methodInvocations)
        snapshotDao.updateSnapshot(snapshot.copy(createdAt = Instant.now()))
    }

    @Transactional
    fun deleteSnapshot(customerId: Long, snapshotId: Long) {
        snapshotNodeService.deleteSnapshotNode(customerId, snapshotId)
        snapshotApplicationDao.deleteByCustomerIdAndSnapshotId(customerId, snapshotId)
        snapshotEnvironmentDao.deleteByCustomerIdAndSnapshotId(customerId, snapshotId)
        snapshotDao.deleteByCustomerIdAndId(customerId, snapshotId)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveSnapshotWithLimit(snapshotEntity: SnapshotEntity): SnapshotEntity {
        val snapshots = snapshotDao.findAllByCustomerIdForUpdate(snapshotEntity.customerId)
        if (snapshots.size >= SNAPSHOT_LIMIT) {
            throw SnapshotCountExceeded()
        }
        return snapshotDao.insert(snapshotEntity)
    }

    fun listSnapshots(customerId: Long): List<SnapshotDto> {
        return snapshotDao.findAllByCustomerId(customerId).map { SnapshotDto.from(it) }
    }

    companion object {
        const val SNAPSHOT_LIMIT = 20
    }
}
