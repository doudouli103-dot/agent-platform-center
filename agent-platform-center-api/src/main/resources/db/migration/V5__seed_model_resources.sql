insert into platform_resource (id, name, type, version, description, tags, content, schema_text, status)
select 'model-qwen3-coder-next',
       'Qwen3 Coder Next',
       'model',
       'v1',
       'Coding-oriented model exposed through the AI Gateway.',
       'coder,gateway',
       '',
       '{"provider":"openai-compatible","model":"qwen3-coder-next"}',
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
       '{"provider":"openai-compatible","model":"deepseek-r1"}',
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
       '{"provider":"openai","model":"gpt-5"}',
       'draft'
where not exists (select 1 from platform_resource where id = 'model-gpt-5');
