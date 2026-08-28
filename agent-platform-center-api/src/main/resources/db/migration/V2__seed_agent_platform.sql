insert into agent_definition (id, name, description, model, prompt_version, skills, mcp_servers, status)
select 'agent-java-architect',
       'Java Architect',
       'Analyze Java and Spring Boot projects, identify architecture risks, and suggest practical fixes.',
       'qwen3-coder-next',
       'java-architect-prompt@v1',
       'java-review@v1,dependency-analysis@v1',
       'filesystem@v1,github@v1',
       'draft'
where not exists (select 1 from agent_definition where id = 'agent-java-architect');

insert into agent_definition (id, name, description, model, prompt_version, skills, mcp_servers, status)
select 'agent-knowledge',
       'Knowledge Agent',
       'Answer questions from documents through retrieval and grounded summaries.',
       'deepseek-r1',
       'knowledge-agent-prompt@v1',
       'rag-answer@v1',
       'document-store@v1',
       'draft'
where not exists (select 1 from agent_definition where id = 'agent-knowledge');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'prompt-java-architect',
       'Java Architect Prompt',
       'prompt',
       'v1',
       'System prompt for Java architecture review, dependency inspection, and change suggestions.',
       'java,architecture,review',
       'draft'
where not exists (select 1 from platform_resource where id = 'prompt-java-architect');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'prompt-knowledge-agent',
       'Knowledge Agent Prompt',
       'prompt',
       'v1',
       'Grounded answer prompt for document retrieval and source-aware summaries.',
       'rag,knowledge',
       'draft'
where not exists (select 1 from platform_resource where id = 'prompt-knowledge-agent');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'skill-java-review',
       'Java Review',
       'skill',
       'v1',
       'Inspect Java services, controllers, mappers, SQL, and tests for implementation risks.',
       'java,spring,review',
       'draft'
where not exists (select 1 from platform_resource where id = 'skill-java-review');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'skill-rag-answer',
       'RAG Answer',
       'skill',
       'v1',
       'Retrieve relevant document chunks, rerank candidates, and compose grounded answers.',
       'rag,retrieval',
       'draft'
where not exists (select 1 from platform_resource where id = 'skill-rag-answer');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'mcp-filesystem',
       'Filesystem MCP',
       'mcp',
       'v1',
       'Read, search, and inspect files within an approved workspace boundary.',
       'filesystem,code',
       'draft'
where not exists (select 1 from platform_resource where id = 'mcp-filesystem');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'mcp-github',
       'GitHub MCP',
       'mcp',
       'v1',
       'Read repositories, issues, pull requests, and code review context from GitHub.',
       'github,repo',
       'draft'
where not exists (select 1 from platform_resource where id = 'mcp-github');
