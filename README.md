# Agent Platform Center

Agent Platform Center is a separated frontend/backend Agent engineering platform starter. It focuses on the first useful loop: configure an Agent, run it, stream output, and inspect execution trace events.

## Modules

```text
agent-platform-center/
├── agent-platform-center-api      # Spring Boot REST + SSE API
├── agent-platform-center-web      # React + TypeScript + Vite console
├── agent-platform-runtime         # Python FastAPI Agent Runtime starter
└── docs                           # Architecture and API notes
```

## Architecture

```text
React Web
   |
   | REST + SSE
   v
Spring Boot API
   |
   | HTTP / OpenAI-compatible chat
   v
Python Agent Runtime / tenx-ai-gateway / tenx-ai-media-service
   |
   v
Model / Skill / MCP / RAG
```

The frontend does not call the runtime directly. Spring Boot stays as the unified API boundary for permission, audit, versioning, and published Agent APIs.

`tenx-ai-gateway` is the model gateway for chat and streaming model calls. `tenx-ai-media-service` owns image/video asset generation and document-center upload for WebUI-style media clients.

## Ports

| Module | Port | Health |
| --- | --- | --- |
| Web | 5176 | http://windows.tentest.cn:5176 |
| API | 8080 | http://windows.tentest.cn:8080/api/health |
| Runtime | 8090 | http://windows.tentest.cn:8090/health |
| tenx-ai-gateway | 8088 | http://macstudio.tentest.cn:8088/healthz |
| Windows MySQL | 3306 | private LAN only |
| Windows Redis | 6379 | private LAN only |
| Windows Elasticsearch | 9200 | private LAN only |
| Windows Chroma | 8000 | private LAN only |
| Windows RAG API | 8091 | private LAN only |
| tenx-ai-media-service | 8092 | http://127.0.0.1:8092/healthz |

## Deployment Topology

For this deployment, Mac Studio is the model capability host and Windows is the application/data host.

```text
External users
  |
  v
Windows: Web / API / Runtime / storage services
  |
  v
Mac Studio: tenx-ai-gateway / model runtimes
```

MacBook is a client/development machine. Browser traffic can open Windows web consoles, while model calls go through the Mac Studio Gateway.

## Start

Deploy and start services in dependency order:

```text
Mac Studio tenx-ai-gateway -> Windows storage services -> Windows API -> Windows Runtime -> Windows Web
```

### 1. Start Windows Storage Services

Start MySQL, Redis, Elasticsearch, Chroma, and the RAG API on the Windows notebook first.

Create the MySQL database:

```sql
create database agent_platform_center character set utf8mb4 collate utf8mb4_unicode_ci;
create user 'agent_center'@'%' identified by 'change-me';
grant all privileges on agent_platform_center.* to 'agent_center'@'%';
flush privileges;
```

Allow service ports only from trusted LAN clients in Windows firewall.

### 2. Start tenx-ai-gateway On Mac Studio

Start the model gateway first when Agent runs should use real model capability.

```bash
cd /Users/lijunwei/PycharmProjects/tenx-ai-gateway
TENX_AI_GATEWAY_API_KEYS=local-dev-key \
TENX_LOCAL_OPENAI_BASE_URL=http://127.0.0.1:4000 \
mvn spring-boot:run
```

Health check:

```bash
curl http://127.0.0.1:8088/healthz
```

If the gateway is not started, Agent Platform Center can still run with local mock output for configuration and UI development.

### 2.1. Start tenx-ai-media-service On Windows

Start the media service when model resources or media clients need image/video generation with document-center upload.

```bash
cd /Users/lijunwei/PycharmProjects/tenx-ai-media-service
TENX_AI_MEDIA_API_KEYS=local-dev-key \
TENX_AI_GATEWAY_BASE_URL=http://macstudio.tentest.cn:8088/v1 \
TENX_AI_GATEWAY_API_KEY=local-dev-key \
TENX_DOCUMENT_CENTER_BASE_URL=http://windows.tentest.cn:8081 \
mvn spring-boot:run
```

Health check:

```bash
curl http://windows.tentest.cn:8092/healthz
```

### 3. Start API On Windows

```bash
cd agent-platform-center-api
mvn spring-boot:run
```

By default the API uses a local H2 file database at `agent-platform-center-api/data/agent-platform-center` so the starter can run immediately. To run with PostgreSQL:

```bash
cd agent-platform-center-api
docker compose up -d
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

For the Windows deployment that connects to the Windows storage server, use the `mysql` profile:

```bash
cd agent-platform-center-api
export WINDOWS_STORAGE_HOST=windows.tentest.cn
export AGENT_CENTER_DB_USERNAME=agent_center
export AGENT_CENTER_DB_PASSWORD=change-me
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run
```

Optional Windows data service endpoints:

```bash
export AGENT_CENTER_REDIS_URL=redis://${WINDOWS_STORAGE_HOST}:6379
export AGENT_CENTER_ES_URL=http://${WINDOWS_STORAGE_HOST}:9200
export AGENT_CENTER_CHROMA_URL=http://${WINDOWS_STORAGE_HOST}:8000
export AGENT_CENTER_RAG_API_URL=http://${WINDOWS_STORAGE_HOST}:8091
```

To use real model capability through `tenx-ai-gateway`, start the gateway first, then enable the integration:

```bash
cd agent-platform-center-api
AGENT_CENTER_GATEWAY_ENABLED=true \
AGENT_CENTER_GATEWAY_BASE_URL=http://macstudio.tentest.cn:8088 \
AGENT_CENTER_GATEWAY_API_KEY=local-dev-key \
mvn spring-boot:run
```

When gateway integration is disabled, the Playground still returns local mock output so configuration screens and SSE can be developed without a running model service.

### 4. Start Runtime On Windows

```bash
cd agent-platform-runtime
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
uvicorn app.main:app --reload --port 8090
```

The runtime is the extension point for future real Skill, MCP, Tool, and RAG execution. The current API does not require it for basic Agent configuration and Chat UI smoke testing.

### 5. Start Web On Windows

```bash
cd agent-platform-center-web
pnpm install
pnpm dev
```

Open http://windows.tentest.cn:5176.

## API Examples

```bash
curl http://windows.tentest.cn:8080/api/health
curl http://windows.tentest.cn:8080/api/agents
curl http://windows.tentest.cn:8080/api/models
curl http://windows.tentest.cn:8080/api/prompts
curl http://windows.tentest.cn:8080/api/skills
curl http://windows.tentest.cn:8080/api/mcp-servers
curl http://windows.tentest.cn:8080/api/tools
curl -X POST http://windows.tentest.cn:8080/api/tools \
  -H 'Content-Type: application/json' \
  -d '{"name":"HTTP Request","version":"v1","description":"Call approved HTTP endpoints","tags":["http","integration"]}'
curl -X POST http://windows.tentest.cn:8080/api/models \
  -H 'Content-Type: application/json' \
  -d '{"name":"Local Qwen","version":"v1","description":"Local OpenAI-compatible Qwen model","tags":["local","coder"],"schemaText":"{\"provider\":\"openai-compatible\",\"baseUrl\":\"http://localhost:4000/v1\"}"}'
curl -X POST http://windows.tentest.cn:8080/api/agents \
  -H 'Content-Type: application/json' \
  -d '{"name":"Code Reviewer","description":"Review Java code","model":"model-qwen3-coder-next@v1","promptVersion":"prompt-java-architect@v1","skills":["skill-java-review@v1"],"mcpServers":["mcp-filesystem@v1"],"tools":["tool-shell@v1"]}'
curl -X POST http://windows.tentest.cn:8080/api/runs \
  -H 'Content-Type: application/json' \
  -d '{"agentId":"agent-java-architect","message":"分析一个 Spring Boot 项目"}'
curl -N http://windows.tentest.cn:8080/api/runs/run-demo/events
```

### MCP for external project APIs

To let an Agent call another project's API, register that API as an MCP resource with a REST schema, then bind the MCP to the Agent. In the current design, MCP is the external system/API connector. Tools remain configurable Agent resources and are not used for external API execution yet.

MCP fields:

- `name`: Display name in the console.
- `version`: Resource version. Use this with the generated id as `<mcp-id>@<version>`.
- `description`: Short purpose of this MCP.
- `tags`: Search and grouping labels.
- `content`: Human-readable notes for what this MCP exposes.
- `schemaText`: JSON string used by the runtime to call the external API.

`schemaText` format for REST APIs:

```json
{
  "type": "rest-api",
  "baseUrl": "http://order-service.internal:8080",
  "auth": {
    "type": "bearer",
    "env": "ORDER_API_TOKEN"
  },
  "endpoints": [
    {
      "name": "queryOrder",
      "method": "POST",
      "path": "/api/orders/search",
      "body": {
        "message": "${message}"
      }
    }
  ]
}
```

Supported runtime fields:

- `type`: Must be `rest-api` to enable real HTTP execution.
- `baseUrl`: External project API host. For this LAN topology, this can point to a Windows LAN address such as `http://windows.tentest.cn:18080`.
- `auth.type`: `bearer`, `api-key`, or omit it for no auth.
- `auth.env`: Environment variable name read by the API process. Do not put real tokens in `schemaText`.
- `endpoints[0].name`: Name returned in the `mcp.result` event.
- `endpoints[0].method`: HTTP method. `GET` and `POST` are the common first choices.
- `endpoints[0].path`: API path. `${message}` is supported in the path.
- `endpoints[0].body`: JSON body. `${message}` is supported in nested string values.

Create an MCP resource:

```bash
curl -X POST http://windows.tentest.cn:8080/api/mcp-servers \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Order API MCP",
    "version": "v1",
    "description": "Call order project APIs from Agent runs",
    "tags": ["order", "api", "internal"],
    "content": "Expose order lookup as an Agent callable MCP.",
    "schemaText": "{\"type\":\"rest-api\",\"baseUrl\":\"http://order-service.internal:8080\",\"auth\":{\"type\":\"bearer\",\"env\":\"ORDER_API_TOKEN\"},\"endpoints\":[{\"name\":\"queryOrder\",\"method\":\"POST\",\"path\":\"/api/orders/search\",\"body\":{\"message\":\"${message}\"}}]}"
  }'
```

For a Windows storage/API server on the LAN, use the Windows service address in `baseUrl`:

```json
{
  "type": "rest-api",
  "baseUrl": "http://192.168.1.20:18080",
  "endpoints": [
    {
      "name": "queryRag",
      "method": "POST",
      "path": "/rag/query",
      "body": {
        "question": "${message}"
      }
    }
  ]
}
```

Bind the generated MCP id with its version when creating an Agent. If the MCP creation response returns `id=mcp-order-api-mcp`, bind it as `mcp-order-api-mcp@v1`:

```json
{
  "name": "Order Analysis Agent",
  "description": "Analyze order issues with project API context.",
  "model": "model-qwen3-coder-next@v1",
  "promptVersion": "prompt-java-architect@v1",
  "skills": ["skill-java-review@v1"],
  "mcpServers": ["mcp-order-api-mcp@v1"],
  "tools": []
}
```

Run the Agent and read MCP events:

```bash
RUN_JSON=$(curl -s -X POST http://windows.tentest.cn:8080/api/runs \
  -H 'Content-Type: application/json' \
  -d '{"agentId":"agent-java-architect","message":"查询订单 1001"}')

EVENTS_URL=$(printf '%s' "$RUN_JSON" | sed -n 's/.*"eventsUrl":"\([^"]*\)".*/\1/p')
curl -N "http://windows.tentest.cn:8080$EVENTS_URL"
```

Runtime behavior:

- `schemaText.type=rest-api` enables real HTTP execution.
- The first endpoint in `schemaText.endpoints` is called during the Agent run.
- `${message}` inside the endpoint path or body is replaced with the current chat message.
- `auth.type=bearer` reads the token from the environment variable named by `auth.env`.
- `auth.type=api-key` sends the environment value as `X-API-Key`.
- Chat SSE emits `mcp.started`, `mcp.result`, and `mcp.completed`.
- Run history stores the MCP events in `run_event`; Chat history shows the final run result.

Direct gateway contract used by Agent Platform Center:

```bash
curl -s http://127.0.0.1:8088/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer local-dev-key' \
  -d '{"model":"qwen3-coder-next","messages":[{"role":"user","content":"你好"}],"stream":false}'
```

Direct media-service contract for image/video model resources:

```bash
curl -s http://127.0.0.1:8092/api/v1/images/generations \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer local-dev-key' \
  -d '{"model":"qwen-image","prompt":"生成一张架构图","size":"1024x1024","n":1}'
```

## V1 Scope

- Agent, Prompt, Skill, MCP sample configuration
- Free creation for Model resources
- Free creation for Prompt, Skill, MCP, and Tool resources
- Agent bindings for Prompt, Skill, MCP, and Tools
- REST API for configuration and run creation
- SSE for token streaming and trace events
- REST MCP execution for calling configured external project APIs
- Optional `tenx-ai-gateway` integration for chat model calls
- Optional `tenx-ai-media-service` resource metadata for image/video generation
- React console with Agent list and Playground
- Python runtime starter for future real Agent execution

Out of scope for V1: login, complex RBAC, marketplace, workflow, multi-agent orchestration, billing, and production observability.
