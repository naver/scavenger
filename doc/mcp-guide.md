# MCP Server Guide

Scavenger's API exposes an [MCP](https://modelcontextprotocol.io/) server so AI coding
agents (Claude Code, Cursor, Codex, etc.) can query your runtime dead-code data through
tool calls. It grounds an agent's "is this safe to delete?" reasoning in **actual
production usage** instead of static guesses.

## Prerequisite

The MCP server is embedded in the **API** (`scavenger-api`). Install the Collector and API
first:

* [How to install Collector?](https://github.com/naver/scavenger/blob/develop/doc/installation.md#install-collector)
* [How to install API?](https://github.com/naver/scavenger/blob/develop/doc/installation.md#install-api)

> This guide assumes the API is reachable with the context path `/scavenger` (default),
> so the MCP endpoint is `/scavenger/mcp`.

## Setup

Add a single entry to your MCP client configuration. Authentication reuses your existing
workspace **licenseKey** via the `X-Scavenger-License-Key` header.

```jsonc
{
  "mcpServers": {
    "scavenger": {
      "type": "http",
      "url": "https://your-scavenger-host/scavenger/mcp",
      "headers": { "X-Scavenger-License-Key": "<licenseKey>" }
    }
  }
}
```

The transport is **Streamable HTTP (stateless)** — each request is an independent
JSON-RPC call, no session to manage.

> **Where to find the licenseKey:** it is the workspace key on the API's workspace screen
> — the same value the Java/Python agents use as `apiKey`.

> ⚠️ **The same key lets agents write to the Collector.** Keep MCP client config files out of
> version control and treat a leaked key as a compromised workspace credential: with it, an
> attacker can both read your data and poison the invocation evidence this server reports.

## Tools

All tools resolve your workspace from the license key automatically and run as read-only
queries. Every data tool accepts an optional `env` filter (omit it to aggregate across all
environments, disabled ones included; a disabled environment cannot be named explicitly).

| Tool | Purpose |
| --- | --- |
| `list_scopes` | List environments and applications, with per-(application, environment) coverage. Start here to discover valid names. |
| `is_method_used` | Is a single method used (invoked) anywhere? |
| `get_method_callers` | Runtime direct callers of a method (from call-stack data). |
| `get_stale_methods` | Find stale / never-invoked methods. The workhorse for dead-code discovery. **Set `idleDays` and/or `neverInvoked`** — without them it lists every instrumented method, used ones included. |
| `get_pr_impact` | Bulk usage check for up to 200 signatures at once. |

Method signatures use the fully-qualified format stored by the agent, e.g.
`com.example.demo.MyController.additional()`.

## Response format

Every successful response is a JSON envelope:

```jsonc
{
  "ok": true,
  "data": { /* tool-specific payload */ },
  "coverage": [                                 // one entry per environment (only the filtered env if set)
    { "environment": "prod",
      "collectingSinceMillis": 1767571260000,   // start of the oldest JVM still registered in this environment
      "agentAliveAtMillis": 1768003200000 }      // when an agent last polled (null: no agent state yet)
  ],
  "dataFreshness": { "queryExecutedAtMillis": 1786329861602 }
}
```

> **Read `coverage` before trusting a "not used" result.** A method that looks dead is only
> meaningful evidence if collection has been running long enough and an agent is still
> reporting. "No invocations in 30 days" means nothing if `collectingSinceMillis` is 5 days
> ago, or if `agentAliveAtMillis` is stale. Results are **evidence, not deletion verdicts.**
> `collectingSinceMillis` reflects the JVMs currently registered: JVMs that stopped reporting are
> swept by the Collector, so after a full rolling restart it moves forward even though older
> invocation data is still counted. Coverage is per environment because a method result cannot
> be tied to one application; `list_scopes` returns the per-(application, environment) breakdown.

Failures use a typed error instead of `data`:

```jsonc
{ "ok": false, "error": { "code": "METHOD_NOT_FOUND", "message": "...", "hint": "...", "retryable": false } }
```

Error codes: `AUTH_MISSING`, `AUTH_INVALID`, `METHOD_NOT_FOUND`, `INVALID_ARGUMENT`,
`INTERNAL_ERROR`. `AUTH_*` errors arrive as an HTTP 401 with this envelope as the body; tool
errors arrive inside a normal `tools/call` result. Argument schema violations (missing required
field, wrong type) are reported by the MCP layer itself as `isError: true` with a plain-text
message, not as this envelope.

## Examples

Ask the agent in natural language — it picks the tool and arguments. Under the hood:

**Discover scopes**

```
list_scopes {}
→ environments: [prod, staging(disabled), test], applications: [demo, demo2],
  coverage: [ {demo, prod, ...}, {demo, test, ...}, {demo2, test, ...} ]   // per (application, environment)
```

**Is a method used?**

```
is_method_used { "signature": "com.example.demo.service2.TestService.call()" }
→ { "used": true, "lastInvokedAtMillis": 1786096620000, ... }
```

**Who calls it?**

```
get_method_callers { "signature": "com.example.demo.service2.TestService.call()" }
→ callers: [ { "callerSignature": "com.example.demo.controller.MyController.hello()", "lastInvokedAtMillis": ... } ],
  trackingState: DATA_AVAILABLE
```

**Dead code in prod**

```
get_stale_methods { "env": "prod", "neverInvoked": true, "limit": 50 }
→ methods: [ ... ], nextCursor: "..."   // page with cursor
```

**PR impact (bulk)**

```
get_pr_impact { "signatures": ["com.foo.A.x()", "com.foo.B.y()", ...] }   // up to 200
→ methods: [...], unknownSignatures: [...]
```

## Deployment & security

> **⚠️ Expose only `/scavenger/mcp` to developers.** `scavenger-api`'s `/api/**` has no
> app-level auth — the deployment model assumes an SSO-fronting proxy. Whatever gateway exposes
> the MCP endpoint must route **only `/scavenger/mcp`** and keep `/api/**` behind the existing
> proxy. `/api/**` includes endpoints that return workspace license keys, so exposing it is
> equivalent to handing out every workspace's MCP credential.

**Transport guard** — the API refuses to start unless `spring.ai.mcp.server.protocol` is
`STATELESS` and `type` is `SYNC`; both are load-bearing for tenant isolation. The auth
interceptor follows `spring.ai.mcp.server.streamable-http.mcp-endpoint` (default `/mcp`).

**CORS** is off by default. Browser-based MCP clients (e.g. MCP Inspector) need
`scavenger.mcp.cors.enabled=true`; narrow `scavenger.mcp.cors.allowed-origin-patterns`
(default `*`) to the client's origin.

**Kill switch** — `scavenger.mcp.enabled=false` (restart required) unregisters the five tools:
`/scavenger/mcp` keeps answering and still requires the license key, but `tools/list` is empty.
To remove the endpoint entirely, set Spring AI's `spring.ai.mcp.server.enabled=false`.

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `401` with `AUTH_MISSING` | The `X-Scavenger-License-Key` header is not set. |
| `401` with `AUTH_INVALID` | The license key does not match any workspace. |
| `INVALID_ARGUMENT` on an `env` filter | Unknown or disabled environment — call `list_scopes` for valid names. |
| `get_method_callers` returns `trackingState: DISABLED_OR_NO_DATA` | Call-stack tracking (`callStackTraceMode`) is off, or no call-stack data yet. An empty list is **not** "no callers". The state is evaluated within the requested scope: with an `env` filter it reflects that environment only. |
| `list_scopes` / `coverage` is empty | No agent has reported yet for this workspace. Data appears after the first agent publish. |
| `METHOD_NOT_FOUND` for a method you expect | The signature must match the stored format exactly, and the method must be inside the agent's instrumented packages. Absence is not evidence the method is dead. |
| CORS error from a browser-based MCP client | CORS is off by default — set `scavenger.mcp.cors.enabled=true`, and make sure the gateway does not strip the preflight (`OPTIONS`) request. |
| API fails to start with `spring.ai.mcp.server.protocol must be STATELESS` | The MCP auth model requires `protocol: STATELESS` and `type: SYNC`; restore them, or disable the server with `spring.ai.mcp.server.enabled=false`. |
