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
Python Agent Runtime / tenx-ai-gateway
   |
   v
Model / Skill / MCP / RAG
```

The frontend does not call the runtime directly. Spring Boot stays as the unified API boundary for permission, audit, versioning, and published Agent APIs.

`tenx-ai-gateway` is the model gateway. Agent Platform Center can call its OpenAI-compatible chat API so Agent runs use the model selected during Agent creation.

## Ports

| Module | Port | Health |
| --- | --- | --- |
| Web | 5173 | http://localhost:5173 |
| API | 8080 | http://localhost:8080/api/health |
| Runtime | 8090 | http://localhost:8090/health |
| tenx-ai-gateway | 8088 | http://127.0.0.1:8088/healthz |

## Start

Deploy and start services in dependency order:

```text
Model service / tenx-ai-gateway -> Spring Boot API -> Python Runtime -> React Web
```

### 1. Start tenx-ai-gateway

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

### 2. Start API

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

To use real model capability through `tenx-ai-gateway`, start the gateway first, then enable the integration:

```bash
cd agent-platform-center-api
AGENT_CENTER_GATEWAY_ENABLED=true \
AGENT_CENTER_GATEWAY_BASE_URL=http://127.0.0.1:8088 \
AGENT_CENTER_GATEWAY_API_KEY=local-dev-key \
mvn spring-boot:run
```

When gateway integration is disabled, the Playground still returns local mock output so configuration screens and SSE can be developed without a running model service.

### 3. Start Runtime

```bash
cd agent-platform-runtime
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
uvicorn app.main:app --reload --port 8090
```

The runtime is the extension point for future real Skill, MCP, Tool, and RAG execution. The current API does not require it for basic Agent configuration and Chat UI smoke testing.

### 4. Start Web

```bash
cd agent-platform-center-web
pnpm install
pnpm dev
```

Open http://localhost:5173.

## API Examples

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/agents
curl http://localhost:8080/api/models
curl http://localhost:8080/api/prompts
curl http://localhost:8080/api/skills
curl http://localhost:8080/api/mcp-servers
curl http://localhost:8080/api/tools
curl -X POST http://localhost:8080/api/tools \
  -H 'Content-Type: application/json' \
  -d '{"name":"HTTP Request","version":"v1","description":"Call approved HTTP endpoints","tags":["http","integration"]}'
curl -X POST http://localhost:8080/api/models \
  -H 'Content-Type: application/json' \
  -d '{"name":"Local Qwen","version":"v1","description":"Local OpenAI-compatible Qwen model","tags":["local","coder"],"schemaText":"{\"provider\":\"openai-compatible\",\"baseUrl\":\"http://localhost:4000/v1\"}"}'
curl -X POST http://localhost:8080/api/agents \
  -H 'Content-Type: application/json' \
  -d '{"name":"Code Reviewer","description":"Review Java code","model":"model-qwen3-coder-next@v1","promptVersion":"prompt-java-architect@v1","skills":["skill-java-review@v1"],"mcpServers":["mcp-filesystem@v1"],"tools":["tool-shell@v1"]}'
curl -X POST http://localhost:8080/api/runs \
  -H 'Content-Type: application/json' \
  -d '{"agentId":"agent-java-architect","message":"分析一个 Spring Boot 项目"}'
curl -N http://localhost:8080/api/runs/run-demo/events
```

Direct gateway contract used by Agent Platform Center:

```bash
curl -s http://127.0.0.1:8088/v1/chat/completions \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer local-dev-key' \
  -d '{"model":"qwen3-coder-next","messages":[{"role":"user","content":"你好"}],"stream":false}'
```

## V1 Scope

- Agent, Prompt, Skill, MCP sample configuration
- Free creation for Model resources
- Free creation for Prompt, Skill, MCP, and Tool resources
- Agent bindings for Prompt, Skill, MCP, and Tools
- REST API for configuration and run creation
- SSE for token streaming and trace events
- Optional `tenx-ai-gateway` integration for model calls
- React console with Agent list and Playground
- Python runtime starter for future real Agent execution

Out of scope for V1: login, complex RBAC, marketplace, workflow, multi-agent orchestration, billing, and production observability.
