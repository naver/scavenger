package com.navercorp.scavenger.mcp

import com.navercorp.scavenger.dto.ApplicationDetailDto
import com.navercorp.scavenger.dto.CustomerDto
import com.navercorp.scavenger.dto.EnvironmentDetailDto
import com.navercorp.scavenger.dto.McpClassUsageSummaryDto
import com.navercorp.scavenger.dto.McpCleanupCandidateDto
import com.navercorp.scavenger.dto.McpMethodReferenceDto
import com.navercorp.scavenger.dto.McpMethodUsageDto
import com.navercorp.scavenger.dto.McpMethodsNotInvokedSinceLastDeployResultDto
import com.navercorp.scavenger.dto.McpPackageUsageSummaryDto
import com.navercorp.scavenger.dto.McpResolvedWorkspaceDto
import com.navercorp.scavenger.dto.McpResolvedWorkspaceMatchDto
import com.navercorp.scavenger.dto.McpSnapshotNodeDto
import com.navercorp.scavenger.dto.McpSnapshotRefreshResultDto
import com.navercorp.scavenger.dto.McpSnapshotUsageSummaryDto
import com.navercorp.scavenger.dto.McpUsageOverviewDto
import com.navercorp.scavenger.dto.SnapshotDto
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
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class ScavengerMcpService(
    private val mcpProperties: ScavengerMcpProperties,
    private val customerService: CustomerService,
    private val applicationService: ApplicationService,
    private val environmentService: EnvironmentService,
    private val snapshotService: SnapshotService,
    private val snapshotNodeService: SnapshotNodeService,
    private val snapshotNodeDao: SnapshotNodeDao,
    private val summaryService: SummaryService,
    private val methodRepository: MethodRepository,
    private val githubMappingRepository: GithubMappingRepository,
    private val jvmRepository: JvmRepository,
) {
    fun listCustomers(): List<CustomerDto> {
        return customerService.getCustomers(groupId())
    }

    fun listApplications(customerName: String): List<ApplicationDetailDto> {
        return applicationService.getApplicationsDetail(resolveCustomerId(customerName))
    }

    fun listEnvironments(customerName: String): List<EnvironmentDetailDto> {
        return environmentService.getEnvironmentsDetail(resolveCustomerId(customerName))
    }

    fun listSnapshots(customerName: String): List<SnapshotDto> {
        return snapshotService.listSnapshots(resolveCustomerId(customerName))
    }

    fun getSnapshotTree(customerName: String, snapshotId: Long, parent: String): List<McpSnapshotNodeDto> {
        return snapshotNodeService.readSnapshotNode(resolveCustomerId(customerName), snapshotId, parent)
            .map { McpSnapshotNodeDto.from(it) }
    }

    fun searchMethods(customerName: String, snapshotId: Long, signature: String): List<McpSnapshotNodeDto> {
        return snapshotNodeService.getSnapshotNodesBySignatureContaining(resolveCustomerId(customerName), snapshotId, signature)
            .map { McpSnapshotNodeDto.from(it) }
    }

    fun getDeadCode(customerName: String, snapshotId: Long, packagePrefix: String?, limit: Int): List<McpSnapshotNodeDto> {
        val cappedLimit = limit.coerceIn(1, 500)
        return snapshotNodeDao.findAllUnusedMethodNodes(resolveCustomerId(customerName), snapshotId, packagePrefix, cappedLimit)
            .map { McpSnapshotNodeDto.from(it) }
    }

    fun getClassUsageSummary(customerName: String, snapshotId: Long, className: String): McpClassUsageSummaryDto {
        val methods = snapshotNodeService.readSnapshotNode(resolveCustomerId(customerName), snapshotId, className)
            .filter { it.type == "METHOD" }

        return McpClassUsageSummaryDto(
            className = className,
            totalMethods = methods.size,
            usedMethods = methods.count { it.usedCount > 0 },
            unusedMethods = methods.count { it.usedCount == 0 },
            methods = methods.map { McpMethodUsageDto.from(it) }
        )
    }

    fun getPackageUsageSummary(customerName: String, snapshotId: Long, packageName: String): McpPackageUsageSummaryDto {
        val children = snapshotNodeService.readSnapshotNode(resolveCustomerId(customerName), snapshotId, packageName)
        val totalUsed = children.sumOf { it.usedCount }
        val totalUnused = children.sumOf { it.unusedCount }

        return McpPackageUsageSummaryDto(
            packageName = packageName,
            totalUsed = totalUsed,
            totalUnused = totalUnused,
            usageRatio = toUsageRatio(totalUsed.toLong(), totalUnused.toLong()),
            children = children.map { McpSnapshotNodeDto.from(it) }
        )
    }

    fun isMethodUsed(customerName: String, snapshotId: Long, methodSignature: String): List<McpMethodUsageDto> {
        return snapshotNodeService.getSnapshotNodesBySignatureContaining(resolveCustomerId(customerName), snapshotId, methodSignature)
            .filter { it.type == "METHOD" }
            .map { McpMethodUsageDto.from(it) }
    }

    fun analyzeCleanupCandidates(customerName: String, snapshotId: Long, packagePrefix: String?, limit: Int): List<McpCleanupCandidateDto> {
        val customerId = resolveCustomerId(customerName)
        val cappedLimit = limit.coerceIn(1, 500)
        val unusedNodes = snapshotNodeDao.findAllUnusedMethodNodes(customerId, snapshotId, packagePrefix, cappedLimit)

        if (unusedNodes.isEmpty()) {
            return emptyList()
        }

        val methodDetails = methodRepository.findAllByCustomerIdAndSignatureIn(customerId, unusedNodes.map { it.signature })
            .associateBy { it.signature }

        return unusedNodes.map { node ->
            val visibility = methodDetails[node.signature]?.visibility ?: "unknown"
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

            McpCleanupCandidateDto(
                signature = node.signature,
                className = node.parent,
                visibility = visibility,
                confidence = confidence,
                reason = reason,
                lastInvokedAtMillis = node.lastInvokedAtMillis,
                lastInvokedDate = node.lastInvokedAtMillis?.toDateString()
            )
        }.sortedByDescending { CONFIDENCE_ORDER[it.confidence] }
    }

    @Transactional
    fun createSnapshot(
        customerName: String,
        name: String,
        applicationIds: List<Long>,
        environmentIds: List<Long>,
        filterInvokedAtMillis: Long,
        packages: String
    ): SnapshotDto {
        return snapshotService.createSnapshot(
            resolveCustomerId(customerName),
            name,
            applicationIds,
            environmentIds,
            filterInvokedAtMillis,
            packages
        )
    }

    @Transactional
    fun refreshSnapshot(customerName: String, snapshotId: Long): McpSnapshotRefreshResultDto {
        snapshotService.refreshSnapshot(resolveCustomerId(customerName), snapshotId)
        return McpSnapshotRefreshResultDto(
            snapshotId = snapshotId,
            status = "refreshed",
            message = "Snapshot $snapshotId has been refreshed with the latest data."
        )
    }

    fun getSnapshotUsageSummary(customerName: String): List<McpSnapshotUsageSummaryDto> {
        val customerId = resolveCustomerId(customerName)
        val snapshots = snapshotService.listSnapshots(customerId)
        if (snapshots.isEmpty()) {
            return emptyList()
        }

        val usageSummaries = snapshotNodeDao.countMethodUsageSummary(customerId, snapshots.map { it.id })
            .associateBy { it.snapshotId }

        return snapshots.map { snapshot ->
            val usage = usageSummaries[snapshot.id]
            val totalMethods = usage?.totalMethodCount ?: 0
            val usedMethods = usage?.usedMethodCount ?: 0
            val unusedMethods = usage?.unusedMethodCount ?: 0

            McpSnapshotUsageSummaryDto(
                snapshotId = snapshot.id,
                snapshotName = snapshot.name,
                createdAt = snapshot.createdAt,
                packages = snapshot.packages,
                totalMethods = totalMethods,
                usedMethods = usedMethods,
                unusedMethods = unusedMethods,
                usageRatio = toUsageRatio(usedMethods, unusedMethods)
            )
        }
    }

    fun getMethodsNotInvokedSinceLastDeploy(
        customerName: String,
        snapshotId: Long,
        sinceMillis: Long?,
        limit: Int
    ): McpMethodsNotInvokedSinceLastDeployResultDto {
        val customerId = resolveCustomerId(customerName)
        val cappedLimit = limit.coerceIn(1, 500)
        val deployTime = sinceMillis ?: jvmRepository.findLatestPublishedAt(customerId)?.toEpochMilli()

        if (deployTime == null) {
            return McpMethodsNotInvokedSinceLastDeployResultDto(
                error = true,
                message = "No JVM deployment data found. Specify sinceMillis manually."
            )
        }

        val methods = snapshotNodeDao.findMethodsNotInvokedSince(customerId, snapshotId, deployTime, cappedLimit)

        return McpMethodsNotInvokedSinceLastDeployResultDto(
            sinceMillis = deployTime,
            sinceDate = deployTime.toDateString(),
            totalCount = methods.size,
            methods = methods.map { McpMethodReferenceDto.from(it) }
        )
    }

    fun getUsageOverview(customerName: String): McpUsageOverviewDto {
        val customerId = resolveCustomerId(customerName)
        return McpUsageOverviewDto(
            customerName = customerName,
            summary = summaryService.getSummaryByCustomerId(customerId),
            snapshotCount = snapshotService.listSnapshots(customerId).size,
            applications = applicationService.getApplicationsDetail(customerId),
            environments = environmentService.getEnvironmentsDetail(customerId)
        )
    }

    fun resolveWorkspaceByGitUrl(gitUrl: String): McpResolvedWorkspaceDto {
        val base = gitUrl.trimEnd('/').removeSuffix(".git")
        val mappings = githubMappingRepository.findAllByUrlIn(listOf(base, "$base.git"))

        if (mappings.isEmpty()) {
            return McpResolvedWorkspaceDto(
                found = false,
                gitUrl = gitUrl,
                message = "No workspace found for git URL '$gitUrl'. Register a GitHub mapping in the Scavenger UI first."
            )
        }

        val customersById = customerService.getCustomers(groupId()).associateBy { it.id }
        val workspaces = mappings.mapNotNull { mapping ->
            customersById[mapping.customerId]?.let { customer ->
                McpResolvedWorkspaceMatchDto(
                    customer = customer,
                    basePackages = mappings.filter { it.customerId == customer.id }.map { it.basePackage }
                )
            }
        }.distinctBy { it.customer.id }

        if (workspaces.isEmpty()) {
            return McpResolvedWorkspaceDto(
                found = false,
                gitUrl = gitUrl,
                message = "GitHub mapping exists but no matching workspace found in group '${groupId()}'."
            )
        }

        return McpResolvedWorkspaceDto(
            found = true,
            gitUrl = gitUrl,
            workspaces = workspaces
        )
    }

    private fun resolveCustomerId(customerName: String): Long {
        return resolveCustomer(customerName).id
    }

    private fun resolveCustomer(customerName: String): CustomerDto {
        return try {
            customerService.getCustomerByName(groupId(), customerName)
        } catch (_: EmptyResultDataAccessException) {
            throw IllegalArgumentException("Customer '$customerName' not found in group '${groupId()}'")
        }
    }

    private fun groupId(): String = mcpProperties.defaultGroupId

    private fun toUsageRatio(usedMethods: Long, unusedMethods: Long): String {
        val totalMethods = usedMethods + unusedMethods
        return if (totalMethods > 0) {
            "%.1f%%".format(usedMethods.toDouble() / totalMethods * 100)
        } else {
            "N/A"
        }
    }

    private fun Long.toDateString(): String {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    companion object {
        private val CONFIDENCE_ORDER = mapOf("HIGH" to 3, "MEDIUM" to 2, "LOW" to 1)
    }
}
