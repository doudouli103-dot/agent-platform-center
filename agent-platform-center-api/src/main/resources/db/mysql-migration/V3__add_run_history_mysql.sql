create table if not exists run_record (
    id varchar(64) primary key,
    agent_id varchar(64) not null,
    agent_name varchar(128) not null default '',
    model varchar(128) not null default '',
    user_message text not null,
    assistant_output text not null,
    status varchar(32) not null,
    gateway_data text not null,
    trace_data text not null,
    created_at timestamp not null default current_timestamp,
    completed_at timestamp null
);

create table if not exists run_event (
    id varchar(64) primary key,
    run_id varchar(64) not null,
    event_name varchar(128) not null,
    event_data text not null,
    created_at timestamp not null default current_timestamp
);
