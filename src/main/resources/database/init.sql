create database dnd;

create table book_reader_permissions
(
    id      bigserial
        primary key,
    book_id bigint not null
        constraint fk_book_reader_book
            references books
            on delete cascade,
    user_id bigint not null
        constraint fk_book_reader_user
            references users
            on delete cascade,
    constraint unique_book_reader
        unique (book_id, user_id),
    constraint uknlgahcj00fdervlh4dpimgg9w
        unique ()
);

alter table book_reader_permissions
    owner to postgres;

create table books
(
    id          bigserial
        primary key,
    user_id     bigint                                           not null
        constraint fk_books_user
            references users
            on delete cascade,
    title       varchar(200)                                     not null,
    description text,
    cover_url   text,
    status      varchar(30) default 'DRAFT'::character varying,
    created_at  timestamp   default CURRENT_TIMESTAMP,
    updated_at  timestamp   default CURRENT_TIMESTAMP,
    permission  varchar(20) default 'private'::character varying not null
);

alter table books
    owner to postgres;

create table chapters
(
    id            bigserial
        primary key,
    book_id       bigint       not null
        constraint fk_chapters_book
            references books
            on delete cascade,
    title         varchar(200) not null,
    content_md    text,
    chapter_order integer      not null,
    created_at    timestamp default CURRENT_TIMESTAMP,
    updated_at    timestamp default CURRENT_TIMESTAMP,
    music_url     varchar(500)
);

alter table chapters
    owner to postgres;

create table users
(
    id         bigserial
        primary key,
    username   varchar(100) not null,
    email      varchar(150) not null
        unique,
    password   varchar(255) not null,
    created_at timestamp default CURRENT_TIMESTAMP
);

alter table users
    owner to postgres;

