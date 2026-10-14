-- The first migration: one table, two statements.
create table notes (
    id bigint auto_increment primary key,
    title varchar(200) not null,
    body text,
    done boolean not null default false,
    created_at timestamp not null default current_timestamp
);

insert into notes (title, body) values ('first', 'seeded by V1; has a ; in a string');
