create table raw_source (
    id bigserial primary key,
    source_type varchar(20) not null,
    external_id bigint not null,
    raw_payload jsonb not null,
    payload_hash varchar(64) not null,
    synced_at timestamp not null,
    sync_result varchar(20) not null,
    sync_batch_id varchar(64),
    constraint uq_raw_source_source_type_external_id unique (source_type, external_id)
);
