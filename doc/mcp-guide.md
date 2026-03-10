# Scavenger MCP Guide

Scavenger exposes its code usage analysis capabilities as an [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server.
This allows AI assistants such as Claude to query dead code, method usage, and cleanup candidates directly from your runtime data.

## Prerequisites

- Scavenger API server running (default port: `8081`)
- At least one workspace registered with agents reporting data

## Endpoint

```
http://<api-host>:8081/scavenger/mcp
```

The MCP server uses the **Streamable HTTP** transport (HTTP POST + SSE).

## Connecting to the MCP Server

### Claude Desktop

Add the following to `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS) or `%APPDATA%\Claude\claude_desktop_config.json` (Windows):

```json
{
  "mcpServers": {
    "scavenger": {
      "type": "http",
      "url": "http://localhost:8081/scavenger/mcp"
    }
  }
}
```

Restart Claude Desktop to apply the configuration.

### MCP Inspector (for testing)

```bash
npx @modelcontextprotocol/inspector
```

Open `http://localhost:5173` in your browser, set **Transport** to `Streamable HTTP`, and enter the URL above.

### Cursor / Other MCP-compatible clients

Refer to each client's MCP configuration documentation and use the Streamable HTTP URL above.

---

## Available Tools

| Tool | Description |
|------|-------------|
| `listCustomers` | List all registered workspaces |
| `listApplications` | List applications monitored in a workspace |
| `listEnvironments` | List deployment environments (e.g. production, staging) |
| `listSnapshots` | List analysis snapshots for a workspace |
| `getSnapshotUsageSummary` | Get used/unused method counts per snapshot |
| `getSnapshotTree` | Browse the hierarchical code tree of a snapshot |
| `searchMethods` | Search methods by signature substring |
| `isMethodUsed` | Check if a specific method is invoked at runtime |
| `getDeadCode` | Get all unused methods in a snapshot |
| `getClassUsageSummary` | Get method usage breakdown for a class |
| `getPackageUsageSummary` | Get aggregated usage stats for a package |
| `analyzeCleanupCandidates` | Find dead code safe to remove, ranked by confidence |
| `getMethodsNotInvokedSinceLastDeploy` | Find methods that stopped being called after the last deploy |
| `getUsageOverview` | Get a high-level usage overview for a workspace |
| `createSnapshot` | Create a new analysis snapshot |
| `refreshSnapshot` | Refresh an existing snapshot with the latest data |
| `resolveWorkspace` | Find a workspace by git repository URL |

---

## Example Prompts and Responses

### 1. Finding unused code in a package

**Prompt:**
```
my-workspace 워크스페이스에서 스냅샷 목록을 보여주고, 가장 최근 스냅샷의 com.example.service 패키지에 있는 dead code를 찾아줘.
```

**How the AI proceeds:**
1. Calls `listSnapshots(customerName="my-workspace")` to get snapshot list
2. Calls `getDeadCode(customerName="my-workspace", snapshotId=42, packagePrefix="com.example.service")`

**Example response:**
```
스냅샷 목록:
- [42] production-snapshot (2024-03-01)
- [38] staging-snapshot (2024-02-15)

com.example.service 패키지의 dead code (스냅샷 #42):

총 23개의 미사용 메소드가 발견되었습니다.

1. com.example.service.LegacyReportService.generateXmlReport(String) - 마지막 호출: 없음
2. com.example.service.UserService.findByLegacyId(Long) - 마지막 호출: 없음
3. com.example.service.NotificationService.sendFax(String, String) - 마지막 호출: 없음
...
```

---

### 2. Checking if a specific method is still used

**Prompt:**
```
my-workspace의 스냅샷 42에서 UserService.findByLegacyId 메소드가 실제로 사용되고 있어?
```

**How the AI proceeds:**
1. Calls `isMethodUsed(customerName="my-workspace", snapshotId=42, methodSignature="UserService.findByLegacyId")`

**Example response:**
```
UserService.findByLegacyId 메소드는 현재 사용되지 않고 있습니다.

- 매칭된 시그니처: com.example.service.UserService.findByLegacyId(Long)
- 상태: UNUSED
- 마지막 호출: 기록 없음

스냅샷 기간 동안 한 번도 호출된 적이 없습니다. 안전하게 제거를 검토해볼 수 있습니다.
```

---

### 3. Identifying cleanup candidates after a deploy

**Prompt:**
```
my-workspace 스냅샷 42에서 최근 배포 이후에 호출되지 않는 메소드들을 찾아서, 어떤 걸 먼저 제거하면 좋을지 알려줘.
```

**How the AI proceeds:**
1. Calls `getMethodsNotInvokedSinceLastDeploy(customerName="my-workspace", snapshotId=42)`
2. Calls `analyzeCleanupCandidates(customerName="my-workspace", snapshotId=42)` for prioritization

**Example response:**
```
최근 배포 이후 호출되지 않은 메소드: 15개

삭제 우선순위 (신뢰도 높은 순):

🔴 높은 신뢰도 (private 메소드 — 즉시 제거 가능)
1. LegacyReportService.buildXmlNode(Element) - private, 외부 참조 없음
2. LegacyReportService.escapeXml(String) - private, 외부 참조 없음

🟡 중간 신뢰도 (package-private / internal)
3. UserMigrationHelper.transformLegacyFormat(Map) - 내부 사용 여부 재확인 권장

🟠 낮은 신뢰도 (public 메소드 — 외부 호출 가능성 확인 필요)
4. UserService.findByLegacyId(Long) - public API, 외부 클라이언트 확인 필요
5. ReportController.downloadXmlReport() - public endpoint, 실제 트래픽 확인 권장
```

---

### 4. Getting an overview of workspace usage

**Prompt:**
```
my-workspace의 전체 코드 사용률 현황을 요약해줘.
```

**How the AI proceeds:**
1. Calls `getUsageOverview(customerName="my-workspace")`
2. Calls `getSnapshotUsageSummary(customerName="my-workspace")`

**Example response:**
```
📊 my-workspace 코드 사용률 현황

전체 추적 메소드: 12,450개
- 사용됨: 9,832개 (79.0%)
- 미사용: 2,618개 (21.0%)

애플리케이션: 3개 (user-api, order-api, notification-service)
환경: 2개 (production, staging)
스냅샷: 5개

스냅샷별 미사용 메소드:
- production-snapshot: 2,618개 미사용 (21.0%)
- staging-snapshot:    2,891개 미사용 (23.2%)
```

---

### 5. Auto-detecting workspace from a git repository

**Prompt:**
```
https://github.com/naver/scavenger 이 레포의 Scavenger 워크스페이스를 찾아서 dead code 현황을 알려줘.
```

**How the AI proceeds:**
1. Calls `resolveWorkspace(gitUrl="https://github.com/naver/scavenger")`
2. Calls `listSnapshots(customerName=<resolved>)`
3. Calls `getSnapshotUsageSummary(customerName=<resolved>)`

---

## Configuration

`application.yml`에서 MCP 관련 설정을 변경할 수 있습니다.

```yaml
scavenger:
  mcp:
    default-group-id: default-group  # customerName 조회 시 사용할 그룹 ID

spring:
  ai:
    mcp:
      server:
        name: scavenger-mcp
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
        streamable-http:
          mcp-endpoint: /mcp  # 최종 경로: /<context-path>/mcp
```
