alter table agent_definition add column if not exists tools varchar(1000) not null default '';

insert into platform_resource (id, name, type, version, description, tags, status)
select 'tool-shell',
       'Shell Tool',
       'tool',
       'v1',
       'Execute approved shell commands inside a controlled workspace.',
       'shell,workspace',
       'draft'
where not exists (select 1 from platform_resource where id = 'tool-shell');

insert into platform_resource (id, name, type, version, description, tags, status)
select 'tool-http-request',
       'HTTP Request',
       'tool',
       'v1',
       'Call approved HTTP endpoints with timeout and response-size limits.',
       'http,integration',
       'draft'
where not exists (select 1 from platform_resource where id = 'tool-http-request');
