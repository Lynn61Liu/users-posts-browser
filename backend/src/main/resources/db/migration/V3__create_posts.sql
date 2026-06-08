create table posts (
    id bigserial primary key,
    external_id bigint not null,
    raw_source_id bigint not null,
    user_id bigint not null,
    title varchar(255) not null,
    body text not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uq_posts_external_id unique (external_id),
    constraint fk_posts_raw_source foreign key (raw_source_id) references raw_source (id),
    constraint fk_posts_user foreign key (user_id) references users (id)
);
