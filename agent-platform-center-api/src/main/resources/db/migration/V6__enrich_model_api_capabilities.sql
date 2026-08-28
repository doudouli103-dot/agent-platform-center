update platform_resource
set schema_text = '{"provider":"tenx-ai-gateway","model":"qwen3-coder-next","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}'
where type = 'model'
  and id = 'model-qwen3-coder-next';

update platform_resource
set schema_text = '{"provider":"tenx-ai-gateway","model":"deepseek-r1","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}'
where type = 'model'
  and id = 'model-deepseek-r1';

update platform_resource
set schema_text = '{"provider":"tenx-ai-gateway","model":"gpt-5","baseUrl":"http://127.0.0.1:8088","apiKeyRequired":true,"capabilities":["chat","stream"]}'
where type = 'model'
  and id = 'model-gpt-5';
