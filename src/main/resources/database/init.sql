CREATE DATABASE dnd;

\c dnd

CREATE TABLE users
(
    id         bigserial PRIMARY KEY,
    username   varchar(100) NOT NULL,
    email      varchar(150) NOT NULL UNIQUE,
    password   varchar(255) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users OWNER TO postgres;

CREATE TABLE books
(
    id          bigserial PRIMARY KEY,
    user_id     bigint NOT NULL
        CONSTRAINT fk_books_user
            REFERENCES users
            ON DELETE CASCADE,
    title       varchar(200) NOT NULL,
    description text,
    cover_url   text,
    status      varchar(30) DEFAULT 'DRAFT',
    created_at  timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at  timestamp DEFAULT CURRENT_TIMESTAMP,
    permission  varchar(20) DEFAULT 'private' NOT NULL
);

ALTER TABLE books OWNER TO postgres;

CREATE TABLE chapters
(
    id            bigserial PRIMARY KEY,
    book_id       bigint NOT NULL
        CONSTRAINT fk_chapters_book
            REFERENCES books
            ON DELETE CASCADE,
    title         varchar(200) NOT NULL,
    content_md    text,
    chapter_order integer NOT NULL,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp DEFAULT CURRENT_TIMESTAMP,
    music_url     varchar(500)
);

ALTER TABLE chapters OWNER TO postgres;

CREATE TABLE book_reader_permissions
(
    id      bigserial PRIMARY KEY,
    book_id bigint NOT NULL
        CONSTRAINT fk_book_reader_book
            REFERENCES books
            ON DELETE CASCADE,
    user_id bigint NOT NULL
        CONSTRAINT fk_book_reader_user
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT unique_book_reader
        UNIQUE (book_id, user_id)
);

ALTER TABLE book_reader_permissions OWNER TO postgres;