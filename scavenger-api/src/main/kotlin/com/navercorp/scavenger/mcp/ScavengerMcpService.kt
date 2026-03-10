package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.entity.SnapshotNodeEntity
import com.navercorp.scavenger.repository.CustomerRepository
import com.navercorp.scavenger.repository.GithubMappingRepository
import com.navercorp.scavenger.repository.JvmRepository
import com.navercorp.scavenger.repository.MethodRepository
import com.navercorp.scavenger.repository.SnapshotNodeDao
import com.navercorp.scavenger.service.ApplicationService
import com.navercorp.scavenger.service.CustomerService
import com.navercorp.scavenger.service.EnvironmentService
import com.navercorp.scavenger.service.SnapshotNodeService
import com.navercorp.scavenger.service.SnapshotService
import com.navercorp.scavenger.service.SummaryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class ScavengerMcpService(
    private val customerResolver: McpCustomerResolver,
    private val customerService: CustomerService,
    private val applicationService: ApplicationService,
    private val environmentService: EnvironmentService,
    private val snapshotService: SnapshotService,
    private val snapshotNodeService: SnapshotNodeService,
    private val snapshotNodeDao: SnapshotNodeDao,
    private val summaryService: SummaryService,
    private val methodRepository: MethodRepository,
    private val githubMappingRepository: GithubMappingRepository,
    private val customerRepository: CustomerRepository,
    private val jvmRepository: JvmRepository,
) {
    fun listCustomers(): List<Map<String, Any>> {
        return customerService.getCustomers(customerResolver.getGroupId()).map {
            mapOf("id" to it.id, "name" to it.name)
        }
    }

    fun listApplications(customerName: String): List<Map<String, Any>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return applicationService.getApplicationsDetail(customerId).map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "jvmCount" to it.jvmCount,
                "invocationCount" to it.invocationCount,
                "createdAt" to it.createdAt.toString()
            )
        }
    }

    fun listEnvironments(customerName: String): List<Map<String, Any>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return environmentService.getEnvironmentsDetail(customerId).map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "jvmCount" to it.jvmCount,
                "invocationCount" to it.invocationCount,
                "createdAt" to it.createdAt.toString()
            )
        }
    }

    fun listSnapshots(customerName: String): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return snapshotService.listSnapshots(customerId).map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "createdAt" to it.createdAt.toString(),
                "filterInvokedAtMillis" to it.filterInvokedAtMillis,
                "packages" to it.packages,
                "applicationIds" to it.applications,
                "environmentIds" to it.environments
            )
        }
    }

    fun getSnapshotTree(customerName: String, snapshotId: Long, parent: String): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return snapshotNodeService.readSnapshotNode(customerId, snapshotId, parent).map { it.toMap() }
    }

    fun searchMethods(customerName: String, snapshotId: Long, signature: String): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return snapshotNodeService.getSnapshotNodesBySignatureContaining(customerId, snapshotId, signature).map { it.toMap() }
    }

    fun getDeadCode(customerName: String, snapshotId: Long, packagePrefix: String?, limit: Int): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val cappedLimit = limit.coerceIn(1, 500)
        return snapshotNodeDao.findAllUnusedMethodNodes(customerId, snapshotId, packagePrefix, cappedLimit).map { it.toMap() }
    }

    fun getClassUsageSummary(customerName: String, snapshotId: Long, className: String): Map<String, Any?> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val methods = snapshotNodeService.readSnapshotNode(customerId, snapshotId, className)
            .filter { it.type == "METHOD" }

        val usedMethods = methods.filter { it.usedCount > 0 }
        val unusedMethods = methods.filter { it.usedCount == 0 }

        return mapOf(
            "className" to className,
            "totalMethods" to methods.size,
            "usedMethods" to usedMethods.size,
            "unusedMethods" to unusedMethods.size,
            "methods" to methods.map {
                mapOf(
                    "signature" to it.signature,
                    "isUsed" to (it.usedCount > 0),
                    "lastInvokedAtMillis" to it.lastInvokedAtMillis,
                    "lastInvokedDate" to it.lastInvokedAtMillis?.toDateString()
                )
            }
        )
    }

    fun getPackageUsageSummary(customerName: String, snapshotId: Long, packageName: String): Map<String, Any?> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val children = snapshotNodeService.readSnapshotNode(customerId, snapshotId, packageName)

        val totalUsed = children.sumOf { it.usedCount }
        val totalUnused = children.sumOf { it.unusedCount }

        return mapOf(
            "packageName" to packageName,
            "totalUsed" to totalUsed,
            "totalUnused" to totalUnused,
            "usageRatio" to if (totalUsed + totalUnused > 0) "%.1f%%".format(totalUsed.toDouble() / (totalUsed + totalUnused) * 100) else "N/A",
            "children" to children.map { it.toMap() }
        )
    }

    fun isMethodUsed(customerName: String, snapshotId: Long, methodSignature: String): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        return snapshotNodeService.getSnapshotNodesBySignatureContaining(customerId, snapshotId, methodSignature)
            .filter { it.type == "METHOD" }
            .map {
                mapOf(
                    "signature" to it.signature,
                    "isUsed" to (it.usedCount > 0),
                    "lastInvokedAtMillis" to it.lastInvokedAtMillis,
                    "lastInvokedDate" to it.lastInvokedAtMillis?.toDateString()
                )
            }
    }

    fun analyzeCleanupCandidates(customerName: String, snapshotId: Long, packagePrefix: String?, limit: Int): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val cappedLimit = limit.coerceIn(1, 500)
        val unusedNodes = snapshotNodeDao.findAllUnusedMethodNodes(customerId, snapshotId, packagePrefix, cappedLimit)

        if (unusedNodes.isEmpty()) return emptyList()

        val signatures = unusedNodes.map { it.signature }
        val methodDetails = methodRepository.findAllByCustomerIdAndSignatureIn(customerId, signatures)
            .associateBy { it.signature }

        return unusedNodes.map { node ->
            val method = methodDetails[node.signature]
            val visibility = method?.visibility ?: "unknown"
            val confidence = when {
                visibility == "private" -> "HIGH"
                visibility == "package-private" || visibility == "protected" -> "MEDIUM"
                else -> "LOW"
            }
            val reason = when (confidence) {
                "HIGH" -> "Private method with no runtime invocations. Safe to remove."
                "MEDIUM" -> "$visibility method with no runtime invocations. Check for subclass/package usage."
                else -> "Public method with no runtime invocations. May be called by external consumers."
            }

            mapOf(
                "signature" to node.signature,
                "className" to node.parent,
                "visibility" to visibility,
                "confidence" to confidence,
                "reason" to reason,
                "lastInvokedAtMillis" to node.lastInvokedAtMillis,
                "lastInvokedDate" to node.lastInvokedAtMillis?.toDateString()
            )
        }.sortedByDescending { CONFIDENCE_ORDER[it["confidence"]] }
    }

    @Transactional
    fun createSnapshot(
        customerName: String,
        name: String,
        applicationIds: List<Long>,
        environmentIds: List<Long>,
        filterInvokedAtMillis: Long,
        packages: String
    ): Map<String, Any?> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val snapshot = snapshotService.createSnapshot(customerId, name, applicationIds, environmentIds, filterInvokedAtMillis, packages)
        return mapOf(
            "id" to snapshot.id,
            "name" to snapshot.name,
            "createdAt" to snapshot.createdAt.toString(),
            "packages" to snapshot.packages
        )
    }

    @Transactional
    fun refreshSnapshot(customerName: String, snapshotId: Long): Map<String, Any> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        snapshotService.refreshSnapshot(customerId, snapshotId)
        return mapOf(
            "snapshotId" to snapshotId,
            "status" to "refreshed",
            "message" to "Snapshot $snapshotId has been refreshed with the latest data."
        )
    }

    fun getSnapshotUsageSummary(customerName: String): List<Map<String, Any?>> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val snapshots = snapshotService.listSnapshots(customerId)
        if (snapshots.isEmpty()) return emptyList()

        val snapshotIds = snapshots.map { it.id }
        val usageSummaries = snapshotNodeDao.countMethodUsageSummary(customerId, snapshotIds)
            .associateBy { it.snapshotId }

        return snapshots.map { snapshot ->
            val usage = usageSummaries[snapshot.id]
            mapOf(
                "snapshotId" to snapshot.id,
                "snapshotName" to snapshot.name,
                "createdAt" to snapshot.createdAt.toString(),
                "packages" to snapshot.packages,
                "totalMethods" to (usage?.totalMethodCount ?: 0),
                "usedMethods" to (usage?.usedMethodCount ?: 0),
                "unusedMethods" to (usage?.unusedMethodCount ?: 0),
                "usageRatio" to if (usage != null && usage.totalMethodCount > 0)
                    "%.1f%%".format(usage.usedMethodCount.toDouble() / usage.totalMethodCount * 100)
                else "N/A"
            )
        }
    }

    fun getMethodsNotInvokedSinceLastDeploy(
        customerName: String,
        snapshotId: Long,
        sinceMillis: Long?,
        limit: Int
    ): Map<String, Any?> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val cappedLimit = limit.coerceIn(1, 500)

        val deployTime = if (sinceMillis != null) {
            sinceMillis
        } else {
            val latestPublishedAt = jvmRepository.findLatestPublishedAt(customerId)
                ?: return mapOf(
                    "error" to true,
                    "message" to "No JVM deployment data found. Specify sinceMillis manually."
                )
            latestPublishedAt.toEpochMilli()
        }

        val methods = snapshotNodeDao.findMethodsNotInvokedSince(customerId, snapshotId, deployTime, cappedLimit)

        return mapOf(
            "sinceMillis" to deployTime,
            "sinceDate" to deployTime.toDateString(),
            "totalCount" to methods.size,
            "methods" to methods.map {
                mapOf(
                    "signature" to it.signature,
                    "className" to it.parent,
                    "lastInvokedAtMillis" to it.lastInvokedAtMillis,
                    "lastInvokedDate" to it.lastInvokedAtMillis?.toDateString()
                )
            }
        )
    }

    fun getUsageOverview(customerName: String): Map<String, Any?> {
        val customerId = customerResolver.resolveCustomerId(customerName)
        val summary = summaryService.getSummaryByCustomerId(customerId)
        val apps = applicationService.getApplicationsDetail(customerId)
        val envs = environmentService.getEnvironmentsDetail(customerId)
        val snapshots = snapshotService.listSnapshots(customerId)

        return mapOf(
            "customerName" to customerName,
            "totalMethodsTracked" to summary.methodCount,
            "snapshotCount" to snapshots.size,
            "snapshotLimit" to summary.snapshotLimit,
            "applications" to apps.map { mapOf("name" to it.name, "invocationCount" to it.invocationCount) },
            "environments" to envs.map { mapOf("name" to it.name) }
        )
    }

    private fun SnapshotNodeEntity.toMap(): Map<String, Any?> = mapOf(
        "signature" to signature,
        "type" to type,
        "parent" to parent,
        "usedCount" to usedCount,
        "unusedCount" to unusedCount,
        "lastInvokedAtMillis" to lastInvokedAtMillis,
        "lastInvokedDate" to lastInvokedAtMillis?.toDateString()
    )

    private fun Long.toDateString(): String =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    fun resolveWorkspaceByGitUrl(gitUrl: String): Map<String, Any?> {
        val base = gitUrl.trimEnd('/').removeSuffix(".git")
        val candidates = listOf(base, "$base.git")
        val mappings = githubMappingRepository.findAllByUrlIn(candidates)

        if (mappings.isEmpty()) {
            return mapOf(
                "found" to false,
                "gitUrl" to gitUrl,
                "message" to "No workspace found for git URL '$gitUrl'. Register a GitHub mapping in the Scavenger UI first."
            )
        }

        val groupId = customerResolver.getGroupId()
        val customerIds = mappings.map { it.customerId }.distinct()
        val customers = customerIds.mapNotNull { customerId ->
            customerRepository.findById(customerId)
                .filter { it.groupId == groupId }
                .orElse(null)
        }

        if (customers.isEmpty()) {
            return mapOf(
                "found" to false,
                "gitUrl" to gitUrl,
                "message" to "GitHub mapping exists but no matching workspace found in group '$groupId'."
            )
        }

        return mapOf(
            "found" to true,
            "gitUrl" to gitUrl,
            "workspaces" to customers.map { customer ->
                val customerMappings = mappings.filter { it.customerId == customer.id }
                mapOf(
                    "customerName" to customer.name,
                    "customerId" to customer.id,
                    "basePackages" to customerMappings.map { it.basePackage }
                )
            }
        )
    }

    companion object {
        private val CONFIDENCE_ORDER = mapOf("HIGH" to 3, "MEDIUM" to 2, "LOW" to 1)
    }
}
