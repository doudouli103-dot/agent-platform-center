# Architecture

## Decision

Agent Platform Center uses a separated frontend/backend architecture:

- Frontend: React 19, TypeScript, Vite, Ant Design, Tailwind CSS
- Backend: Spring Boot 2.7 on Java 8
- Runtime: Python FastAPI
- Communication: REST for commands and CRUD, SSE for stream output and trace events
- Persistence: Flyway-managed relational schema, H2 for local startup, MySQL for MacBook-to-Windows deployment, PostgreSQL retained as an alternative
- Deployment: MacBook is the external-facing host; Windows notebook is the internal storage and data service server

## Runtime Boundary

The React frontend talks only to the Spring Boot API. The API is responsible for stable public contracts, permission checks, version records, run records, and published Agent APIs.

The Python runtime is responsible for execution concerns: prompt rendering, model calls, tool calls, skill execution, MCP calls, RAG, and streaming execution events.

## Persistence

The API owns platform configuration persistence. Flyway migrations create the V1 tables:

- `agent_definition`: Agent identity, model, prompt version, skill bindings, MCP bindings, tool bindings, and status.
- `platform_resource`: Model, Prompt, Skill, MCP, and Tool resources, including editable `content` and `schema_text` configuration.
- `run_record` and `run_event`: Agent run history, streamed event payloads, model output, MCP result events, gateway selection data, and trace completion data.

The default profile uses a local H2 file database so the project starts without external services. The `mysql` profile connects the MacBook API directly to the Windows MySQL service and uses MySQL-specific Flyway migrations. The `postgres` profile is kept as an alternative. V1 stores binding lists as comma-separated values to keep the first schema small; this should become normalized association tables before advanced filtering, permission checks, or version graph queries.

## Deployment Boundary

External users access only the MacBook host. The Windows notebook stays inside the private LAN and provides MySQL, Redis, Elasticsearch, Chroma, and RAG API to the MacBook.

```text
External users -> MacBook Web/API/Runtime/Gateway -> Windows MySQL/Redis/Elasticsearch/Chroma/RAG API
```

## V1 Run Flow

```text
Browser
  |
  | POST /api/runs
  v
Spring Boot API
  |
  | creates run id
  v
Browser
  |
  | GET /api/runs/{runId}/events
  v
SSE stream
  |
  |- run.started
  |- skill.selected
  |- mcp.started
  |- mcp.result
  |- mcp.completed
  |- gateway.selected
  |- model.token
  |- trace.completed
  |- run.completed
```

REST MCP resources are executed by the API when `schema_text` declares `type=rest-api`. The current runtime calls the first configured endpoint, replaces `${message}` in the endpoint path or body with the chat message, and emits the downstream response as `mcp.result`.

## Why REST + SSE First

V1 does not need WebSocket. SSE is enough for token streaming, run status, and trace events. WebSocket can be introduced later if the platform needs long-lived bidirectional control such as terminal sessions, real-time human approval, or interactive runtime steering.
