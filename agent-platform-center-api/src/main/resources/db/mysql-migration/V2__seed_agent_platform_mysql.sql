insert into agent_definition (id, name, description, model, prompt_version, skills, mcp_servers, tools, status)
select 'agent-java-architect',
       'Java Architect',
       'Analyze Java and Spring Boot projects, identify architecture risks, and suggest practical fixes.',
       'qwen3-coder-next',
       'java-architect-prompt@v1',
       'java-review@v1,dependency-analysis@v1',
       'filesystem@v1,github@v1',
       'shell@v1',
       'draft'
where not exists (select 1 from agent_definition where id = 'agent-java-architect');

insert into agent_definition (id, name, description, model, prompt_version, skills, mcp_servers, tools, status)
select 'agent-knowledge',
       'Knowledge Agent',
       'Answer questions from documents through retrieval and grounded summaries.',
       'deepseek-r1',
       'knowledge-agent-prompt@v1',
       'rag-answer@v1',
       'document-store@v1',
       'http-request@v1',
       'draft'
where not exists (select 1 from agent_definition where id = 'agent-knowledge');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-qwen3-coder-next',
       'Qwen3 Coder Next',
       'model',
       'v1',
       'Coding-oriented model exposed through the AI Gateway.',
       'coder,gateway',
       '',
       '{"provider":"tenx-ai-gateway","model":"qwen3-coder-next","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-qwen3-coder-next');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-deepseek-r1',
       'DeepSeek R1',
       'model',
       'v1',
       'Reasoning model for analysis, planning, and structured responses.',
       'reasoning,gateway',
       '',
       '{"provider":"tenx-ai-gateway","model":"deepseek-r1","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-deepseek-r1');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-gpt-5',
       'GPT-5',
       'model',
       'v1',
       'Cloud model route managed by the AI Gateway.',
       'cloud,gateway',
       '',
       '{"provider":"tenx-ai-gateway","model":"gpt-5","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-gpt-5');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-qwen-image',
       'Qwen Image',
       'model',
       'v1',
       'Image generation model exposed through the Media Service.',
       'image,media',
       '',
       '{"provider":"tenx-ai-media-service","model":"qwen-image","baseUrl":"http://127.0.0.1:8092","apiKeyRequired":true,"capabilities":["image"]}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-qwen-image');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-wan22-ti2v-5b',
       'Wan2.2 TI2V 5B',
       'model',
       'v1',
       'Video generation model exposed through the Media Service.',
       'video,media',
       '',
       '{"provider":"tenx-ai-media-service","model":"Wan2.2-TI2V-5B","baseUrl":"http://127.0.0.1:8092","apiKeyRequired":true,"capabilities":["video"]}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-wan22-ti2v-5b');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'prompt-java-architect',
       'Java Architect Prompt',
       'prompt',
       'v1',
       'System prompt for Java architecture review, dependency inspection, and change suggestions.',
       'java,architecture,review',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'prompt-java-architect');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'prompt-knowledge-agent',
       'Knowledge Agent Prompt',
       'prompt',
       'v1',
       'Grounded answer prompt for document retrieval and source-aware summaries.',
       'rag,knowledge',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'prompt-knowledge-agent');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'skill-java-review',
       'Java Review',
       'skill',
       'v1',
       'Inspect Java services, controllers, mappers, SQL, and tests for implementation risks.',
       'java,spring,review',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'skill-java-review');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'skill-rag-answer',
       'RAG Answer',
       'skill',
       'v1',
       'Retrieve relevant document chunks, rerank candidates, and compose grounded answers.',
       'rag,retrieval',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'skill-rag-answer');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'mcp-filesystem',
       'Filesystem MCP',
       'mcp',
       'v1',
       'Read, search, and inspect files within an approved workspace boundary.',
       'filesystem,code',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'mcp-filesystem');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'mcp-github',
       'GitHub MCP',
       'mcp',
       'v1',
       'Read repositories, issues, pull requests, and code review context from GitHub.',
       'github,repo',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'mcp-github');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'tool-shell',
       'Shell Tool',
       'tool',
       'v1',
       'Execute approved shell commands inside a controlled workspace.',
       'shell,workspace',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'tool-shell');

insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'tool-http-request',
       'HTTP Request',
       'tool',
       'v1',
       'Call approved HTTP endpoints with timeout and response-size limits.',
       'http,integration',
       '',
       '',
       'draft'
where not exists (select 1 from platform_resource where id = 'tool-http-request');
