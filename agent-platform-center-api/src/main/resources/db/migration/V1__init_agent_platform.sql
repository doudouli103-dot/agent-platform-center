create table if not exists agent_definition (
    id varchar(64) primary key,
    name varchar(128) not null,
    description varchar(1000) not null,
    model varchar(128) not null,
    prompt_version varchar(128) not null,
    skills varchar(1000) not null,
    mcp_servers varchar(1000) not null,
    status varchar(32) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists platform_resource (
    id varchar(128) primary key,
    name varchar(128) not null,
    type varchar(32) not null,
    version varchar(32) not null,
    description varchar(1000) not null,
    tags varchar(1000) not null,
    status varchar(32) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
