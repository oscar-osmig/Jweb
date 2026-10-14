alter table notes add column tag varchar(50);
/* a block comment; with a semicolon */
create index notes_tag on notes (tag);
