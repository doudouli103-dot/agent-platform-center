# Architecture

## Decision

Agent Platform Center uses a separated frontend/backend architecture:

- Frontend: React 19, TypeScript, Vite, Ant Design, Tailwind CSS
- Backend: Spring Boot 2.7 on Java 8
- Runtime: Python FastAPI
- Communication: REST for commands and CRUD, SSE for stream output and trace events
- Persistence: Flyway-managed relational schema, H2 for immediate local startup, PostgreSQL profile for real deployment

## Runtime Boundary

The React frontend talks only to the Spring Boot API. The API is responsible for stable public contracts, permission checks, version records, run records, and published Agent APIs.

The Python runtime is responsible for execution concerns: prompt rendering, model calls, tool calls, skill execution, MCP calls, RAG, and streaming execution events.

## Persistence

The API owns platform configuration persistence. Flyway migrations create the V1 tables:

- `agent_definition`: Agent identity, model, prompt version, skill bindings, MCP bindings, tool bindings, and status.
- `platform_resource`: Model, Prompt, Skill, MCP, and Tool resources, including editable `content` and `schema_text` configuration.

The default profile uses a local H2 file database so the project starts without external services. The `postgres` profile switches the same schema to PostgreSQL. V1 stores binding lists as comma-separated values to keep the first schema small; this should become normalized association tables before advanced filtering, permission checks, or version graph queries.

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
  |- model.token
  |- trace.completed
  |- run.completed
```

## Why REST + SSE First

V1 does not need WebSocket. SSE is enough for token streaming, run status, and trace events. WebSocket can be introduced later if the platform needs long-lived bidirectional control such as terminal sessions, real-time human approval, or interactive runtime steering.
