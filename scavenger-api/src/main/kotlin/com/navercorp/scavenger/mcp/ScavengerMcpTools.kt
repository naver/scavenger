package com.navercorp.scavenger.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class ScavengerMcpTools(
    private val mcpService: ScavengerMcpService,
    private val objectMapper: ObjectMapper
) {
    // Scavenger에 등록된 워크스페이스 목록을 조회한다. 다른 tool에 필요한 customerName을 찾기 위해 가장 먼저 호출한다.
    @Tool(
        description = """
            List all available Scavenger workspaces (called 'customers' internally).
            Use this first to find the customerName needed for other tools.
        """
    )
    fun listCustomers(): String {
        return toJson(mcpService.listCustomers())
    }

    // 해당 워크스페이스의 모니터링 중인 애플리케이션 목록을 조회한다. Scavenger Agent가 추적 중인 배포 단위를 나타낸다.
    @Tool(
        description = """
            List all monitored applications for a workspace.
            Applications represent distinct deployable units tracked by Scavenger agents.
        """
    )
    fun listApplications(
        @ToolParam(description = "Workspace name (customerName)") customerName: String
    ): String {
        return toJson(mcpService.listApplications(customerName))
    }

    // 해당 워크스페이스의 배포 환경(production, staging 등) 목록을 조회한다.
    @Tool(
        description = """
            List all deployment environments (e.g. production, staging) for a workspace.
        """
    )
    fun listEnvironments(
        @ToolParam(description = "Workspace name (customerName)") customerName: String
    ): String {
        return toJson(mcpService.listEnvironments(customerName))
    }

    // 해당 워크스페이스의 분석 스냅샷 목록을 조회한다. 스냅샷은 메소드 사용 데이터의 특정 시점 뷰이다. 대부분의 다른 tool에 snapshotId가 필요하다.
    @Tool(
        description = """
            List all analysis snapshots for a workspace.
            Snapshots are point-in-time views of method usage data.
            You need a snapshot ID for most other tools.
        """
    )
    fun listSnapshots(
        @ToolParam(description = "Workspace name (customerName)") customerName: String
    ): String {
        return toJson(mcpService.listSnapshots(customerName))
    }

    // 스냅샷의 계층적 코드 트리를 탐색한다. parent를 빈 문자열로 시작하면 최상위 패키지를, 패키지/클래스 시그니처를 전달하면 하위 항목을 조회한다.
    @Tool(
        description = """
            Browse the hierarchical code tree of a snapshot.
            Start with empty string parent to get top-level packages,
            then drill down by passing a package/class signature as parent.
        """
    )
    fun getSnapshotTree(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Parent node signature. Use empty string for root level.") parent: String
    ): String {
        return toJson(mcpService.getSnapshotTree(customerName, snapshotId, parent))
    }

    // 스냅샷 내에서 시그니처 부분 문자열로 메소드를 검색한다. 특정 클래스나 메소드를 이름으로 찾을 때 유용하다.
    @Tool(
        description = """
            Search for methods within a snapshot by signature substring match.
            Useful for finding specific classes or methods by name.
        """
    )
    fun searchMethods(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Substring to search in method signatures") signature: String
    ): String {
        return toJson(mcpService.searchMethods(customerName, snapshotId, signature))
    }

    // 특정 메소드가 런타임에 실제로 호출되었는지 확인한다. 시그니처 부분 문자열로 검색하여 사용 여부를 보고한다.
    @Tool(
        description = """
            Check if a specific method is actually invoked at runtime.
            Searches by method signature substring and reports usage status.
        """
    )
    fun isMethodUsed(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(
            description = """
                Full or partial method signature,
                e.g. 'com.example.UserService.findById'
            """
        ) methodSignature: String
    ): String {
        return toJson(mcpService.isMethodUsed(customerName, snapshotId, methodSignature))
    }

    // 스냅샷 내의 미사용(dead) 메소드 목록을 조회한다. 런타임에 한 번도 호출되지 않은 메소드를 반환한다.
    @Tool(
        description = """
            Get all unused (dead) methods within a snapshot.
            Returns methods that have never been invoked at runtime.
        """
    )
    fun getDeadCode(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Optional package/class prefix filter", required = false) packagePrefix: String?,
        @ToolParam(description = "Max results to return (default 100, max 500)", required = false) limit: Int?
    ): String {
        return toJson(mcpService.getDeadCode(customerName, snapshotId, packagePrefix, limit ?: 100))
    }

    // 특정 클래스의 메소드별 사용 현황을 조회한다. 어떤 메소드가 사용/미사용인지, 마지막 호출 시점 등을 보여준다.
    @Tool(
        description = """
            Get method usage breakdown for a specific class.
            Shows which methods are used vs unused with last invocation timestamps.
        """
    )
    fun getClassUsageSummary(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Fully qualified class name, e.g. 'com.example.UserService'") className: String
    ): String {
        return toJson(mcpService.getClassUsageSummary(customerName, snapshotId, className))
    }

    // 특정 패키지 하위의 모든 패키지/클래스에 대한 사용률 통계를 집계하여 조회한다.
    @Tool(
        description = """
            Get aggregated usage statistics for all packages/classes under a given package.
        """
    )
    fun getPackageUsageSummary(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Package name, e.g. 'com.example.service'") packageName: String
    ): String {
        return toJson(mcpService.getPackageUsageSummary(customerName, snapshotId, packageName))
    }

    // 안전하게 삭제 가능한 dead code 후보를 신뢰도 순으로 분석한다. private 메소드는 public보다 삭제 안전도가 높다.
    @Tool(
        description = """
            Identify dead code that is safe to remove, ranked by confidence.
            Private methods are safer to remove than public ones.
        """
    )
    fun analyzeCleanupCandidates(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(description = "Optional package prefix filter", required = false) packagePrefix: String?,
        @ToolParam(description = "Max results (default 50, max 500)", required = false) limit: Int?
    ): String {
        return toJson(mcpService.analyzeCleanupCandidates(customerName, snapshotId, packagePrefix, limit ?: 50))
    }

    // 해당 워크스페이스의 코드 사용률 개요를 조회한다. 전체 추적 메소드 수, 스냅샷 수, 애플리케이션/환경별 통계를 포함한다.
    @Tool(
        description = """
            Get a high-level overview of code usage for a workspace:
            total methods tracked, snapshot count, application and environment stats.
        """
    )
    fun getUsageOverview(
        @ToolParam(description = "Workspace name (customerName)") customerName: String
    ): String {
        return toJson(mcpService.getUsageOverview(customerName))
    }

    // 새 분석 스냅샷을 생성한다. 애플리케이션, 환경, 패키지 필터를 지정하여 메소드 사용 데이터를 스냅샷으로 저장한다.
    @Tool(
        description = """
            Create a new analysis snapshot.
            Captures method usage data for specified applications, environments, and packages.
        """
    )
    fun createSnapshot(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot name") name: String,
        @ToolParam(description = "List of application IDs to include") applicationIds: List<Long>,
        @ToolParam(description = "List of environment IDs to include") environmentIds: List<Long>,
        @ToolParam(
            description = """
                Only include methods invoked after this timestamp (epoch millis).
                Use 0 for all time.
            """
        ) filterInvokedAtMillis: Long,
        @ToolParam(description = "Package filter, e.g. 'com.example.'") packages: String
    ): String {
        return toJson(mcpService.createSnapshot(customerName, name, applicationIds, environmentIds, filterInvokedAtMillis, packages))
    }

    // 기존 스냅샷을 최신 데이터로 갱신한다. 스냅샷 설정(애플리케이션, 환경, 패키지)은 유지하고 데이터만 새로고침한다.
    @Tool(
        description = """
            Refresh an existing snapshot with the latest runtime data.
            Keeps the same configuration but updates usage data.
        """
    )
    fun refreshSnapshot(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID to refresh") snapshotId: Long
    ): String {
        return toJson(mcpService.refreshSnapshot(customerName, snapshotId))
    }

    // 모든 스냅샷의 메소드 사용/미사용 수를 한눈에 보여준다. "어떤 스냅샷에서 미사용 메소드가 몇 개야?" 같은 질문에 답변한다.
    @Tool(
        description = """
            Get method usage summary for all snapshots:
            total, used, and unused method counts with usage ratio.
            Use this to quickly find which snapshot has the most dead code.
        """
    )
    fun getSnapshotUsageSummary(
        @ToolParam(description = "Workspace name (customerName)") customerName: String
    ): String {
        return toJson(mcpService.getSnapshotUsageSummary(customerName))
    }

    // 마지막 배포 이후 호출되지 않는 메소드를 찾는다. 이전에는 사용됐지만 최근 배포 후 안 쓰이는 "새로 죽은 코드"를 식별한다.
    @Tool(
        description = """
            Find methods that were previously used but not invoked since the last deployment.
            Identifies 'newly dead' code — methods that stopped being called after a recent deploy.
            Auto-detects last deploy time from JVM data, or accepts a manual timestamp.
        """
    )
    fun getMethodsNotInvokedSinceLastDeploy(
        @ToolParam(description = "Workspace name (customerName)") customerName: String,
        @ToolParam(description = "Snapshot ID") snapshotId: Long,
        @ToolParam(
            description = """
                Optional: cutoff timestamp in epoch millis.
                If omitted, auto-detects from the latest JVM deployment time.
            """,
            required = false
        ) sinceMillis: Long?,
        @ToolParam(description = "Max results (default 100, max 500)", required = false) limit: Int?
    ): String {
        return toJson(mcpService.getMethodsNotInvokedSinceLastDeploy(customerName, snapshotId, sinceMillis, limit ?: 100))
    }

    // Git 저장소 URL로 매칭되는 Scavenger 워크스페이스를 찾는다. 프로젝트 디렉토리에서 자동으로 워크스페이스를 식별할 때 사용한다.
    @Tool(
        description = """
            Resolve a Scavenger workspace by git repository URL.
            Use this to automatically find the matching workspace for the current project.
            Accepts URLs with or without '.git' suffix.
        """
    )
    fun resolveWorkspace(
        @ToolParam(
            description = """
                Git repository URL,
                e.g. 'https://github.com/org/repo.git'
            """
        ) gitUrl: String
    ): String {
        return toJson(mcpService.resolveWorkspaceByGitUrl(gitUrl))
    }

    private fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)
}
