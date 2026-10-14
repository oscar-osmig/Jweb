-- The same version as db/test-migration/V1, with different content: a history that
-- applied the other file must refuse this one.
create table notes (
    id bigint auto_increment primary key,
    title varchar(200) not null
);
