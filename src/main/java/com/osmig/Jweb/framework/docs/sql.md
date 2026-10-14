# SQL and Migrations

`jweb.Sql` runs SQL against the app's `DataSource` and maps rows to records; `jweb.Migrations`
applies versioned `.sql` files once each. Both are written against `javax.sql.DataSource`
only — an app adds `spring-boot-starter-jdbc` and a driver and sets `spring.datasource.url`,
and the `DataSource` bean is bound at startup.

## Queries

```java
import jweb.Sql;

record User(Long id, String name, String email, LocalDateTime createdAt) {}

List<User> all = Sql.query("select * from users order by name").list(User.class);
Optional<User> byId = Sql.query("select * from users where id = ?", 7).first(User.class);
User one = Sql.query("select * from users where email = ?", "ada@example.com").one(User.class);
long count = Sql.query("select count(*) from users").scalar(Long.class);
boolean any = Sql.query("select 1 from users where email = ?", "ada@example.com").exists();
List<String> names = Sql.query("select name from users").list(String.class);
List<String> labels = Sql.query("select id, name from users").list(row -> row.getLong("id") + " " + row.getString("name"));
```

A row maps to a record by column name, ignoring case and underscores: `created_at` fills
`createdAt`. A column the row lacks leaves the component `null` (zero / `false` for a
primitive). Numbers, `String`, `boolean`, `BigDecimal`, `LocalDate`, `LocalDateTime`,
`LocalTime`, `Instant`, `UUID`, enums by name and `byte[]` convert.

## Parameters

```java
Sql.query("select * from users where age > ? and role = ?", 18, "admin");                 // positional
Sql.query("select * from users where age > :age and role = :role", Map.of("age", 18, "role", "admin"));   // named
Sql.query("select :id::text", Map.of("id", 7));                                           // :: is a cast, not a parameter
```

## Updates and inserts

```java
int changed = Sql.update("update users set name = ? where id = ?", "Ada", 7);
long id = Sql.insert("insert into users (name) values (?)", "Ada").key(Long.class);
Sql.execute("create index users_name on users (name)");
int[] counts = Sql.batch("insert into users (name) values (?)", List.of(new Object[]{"a"}, new Object[]{"b"}));
```

## Transactions

```java
long from = 1, to = 2;

Sql.transaction(() -> {
    Sql.update("update accounts set balance = balance - ? where id = ?", 50, from);
    Sql.update("update accounts set balance = balance + ? where id = ?", 50, to);
});
String receipt = Sql.transaction(() -> {
    Sql.update("insert into audit (note) values (?)", "moved 50");
    return "ok";
});
```

Commit at the end, rollback on any exception; every `Sql` call on the thread inside the
block joins it, and a nested `transaction` joins the outer one.

## A table through a record

```java
record Todo(Long id, String title, boolean done) {}

Sql.Table<Todo> todos = Sql.table("todos", Todo.class);

Todo t = todos.insert(new Todo(null, "Write docs", false));   // id generated and filled in
todos.update(new Todo(t.id(), t.title(), true));
todos.save(t);                                                // insert when id is null, else update
Optional<Todo> found = todos.find(t.id());
Todo must = todos.get(t.id());
List<Todo> open = todos.where("done = ? order by id", false);
long left = todos.count("done = ?", false);
todos.delete(t.id());
todos.deleteWhere("done = ?", true);
```

Columns are the components in `snake_case`; the component named `id` is the key.

## Another database

```java
import javax.sql.DataSource;

DataSource reportsDataSource = Sql.dataSource();
Sql.Database reports = Sql.of(reportsDataSource);
long rows = reports.query("select count(*) from events").scalar(Long.class);
```

Every static `Sql` method is an instance method on `Sql.Database`. `Sql.connect(dataSource)`
binds the default by hand (a test, a `main`).

## Migrations

Files named `V<version>__<name>.sql` under `src/main/resources/db/migration` run at startup
in version order, once each, recorded in `jweb_migrations` with a checksum:

```
db/migration/V1__create_users.sql
db/migration/V2__add_users_email_index.sql
```

```java
import jweb.Migrations;
import javax.sql.DataSource;

DataSource dataSource = Sql.dataSource();

Migrations.Report report = Migrations.migrate(dataSource);
List<Migrations.Migration> pending = Migrations.pending(dataSource);
List<Migrations.Applied> history = Migrations.applied(dataSource);
int repaired = Migrations.repair(dataSource);   // accept edited checksums, when the edit was deliberate
```

- Each file runs in one transaction where DDL is transactional (PostgreSQL, H2, SQLite).
- A file edited after it was applied fails the next start, naming the version.
- Several instances starting at once take turns on a lock row.
- Statements split on `;` outside quotes, comments and `$$` bodies; `-- jweb:whole` on the
  first line sends the file as one statement.
- `jweb.sql.migrate: false` turns the startup run off; `jweb.sql.migrations` moves the
  location. With Flyway or Liquibase on the classpath, JWeb's migrator steps aside.

## Errors

`Sql.SqlException` (unchecked) carries the message and the SQL that failed;
`Migrations.MigrationException` names the file and the statement. Calling `Sql` with no
database bound says what to add.
