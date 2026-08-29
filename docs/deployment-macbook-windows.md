# MacBook + Windows Deployment

## Topology

```text
External users
  |
  | HTTP / HTTPS
  v
MacBook deployment host
  |- agent-platform-center-web
  |- agent-platform-center-api
  |- agent-platform-runtime
  |- tenx-ai-gateway optional
  |- tenx-ai-media-service optional
  |
  | private LAN direct connections
  v
Windows storage server
  |- MySQL
  |- Redis
  |- Elasticsearch
  |- Chroma
  |- RAG API
```

The MacBook is the host exposed to external users. The Windows notebook is the internal storage and data service server. In this deployment, the MacBook API connects directly to Windows MySQL, and future Runtime/RAG features connect directly to Windows Redis, Elasticsearch, Chroma, and RAG API.

## Windows Services

| Service | Default port | Purpose |
| --- | --- | --- |
| MySQL | 3306 | Agent Platform Center configuration and run persistence |
| Redis | 6379 | Runtime cache, session state, task queue |
| Elasticsearch | 9200 | Full-text document search and hybrid retrieval |
| Chroma | 8000 | Vector database |
| RAG API | 8091 | Retrieval and answer grounding service |
| Media Service | 8092 | Image/video asset generation and document-center upload |

External users should not connect to the Windows notebook directly. Allow these ports only from the MacBook LAN IP in Windows firewall.

## MySQL Setup

Run this on Windows MySQL:

```sql
create database agent_platform_center character set utf8mb4 collate utf8mb4_unicode_ci;
create user 'agent_center'@'%' identified by 'change-me';
grant all privileges on agent_platform_center.* to 'agent_center'@'%';
flush privileges;
```

## MacBook API Startup

Start the Spring Boot API on MacBook with the `mysql` profile:

```bash
cd agent-platform-center-api
export WINDOWS_STORAGE_HOST=192.168.1.100
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

## Startup Order

```text
Windows storage services -> MacBook tenx-ai-gateway -> MacBook tenx-ai-media-service -> MacBook API -> MacBook Runtime -> MacBook Web
```

Use H2 only for local development. Use the `mysql` profile when the MacBook deployment connects directly to Windows storage.
