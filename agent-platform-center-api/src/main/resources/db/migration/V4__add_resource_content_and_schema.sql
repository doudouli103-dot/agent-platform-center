alter table platform_resource add column if not exists content text not null default '';
alter table platform_resource add column if not exists schema_text text not null default '';
