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

update platform_resource
set schema_text = '{"provider":"tenx-ai-media-service","model":"qwen-image","baseUrl":"http://127.0.0.1:8092","apiKeyRequired":true,"capabilities":["image"]}'
where type = 'model'
  and id = 'model-qwen-image';

update platform_resource
set schema_text = '{"provider":"tenx-ai-media-service","model":"Wan2.2-TI2V-5B","baseUrl":"http://127.0.0.1:8092","apiKeyRequired":true,"capabilities":["video"]}'
where type = 'model'
  and id = 'model-wan22-ti2v-5b';
