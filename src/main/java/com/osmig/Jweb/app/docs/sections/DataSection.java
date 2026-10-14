package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class DataSection {
    private DataSection() {}

    public static Element render() {
        return section(
            docTitle("Database"),
            para("Two opt-in data layers: SQL over any JDBC DataSource (Sql, Migrations) and MongoDB (Mongo, Doc, Schema). "
                + "Each loads only when its driver is on the classpath."),

            docSubtitle("SQL: setup"),
            para("Add spring-boot-starter-jdbc and a driver, set spring.datasource.url, and the DataSource bean "
                + "Spring Boot creates is bound to Sql at startup. Migrations under db/migration run first."),
            codeBlock("yaml", """
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/myapp}
    username: ${DATABASE_USER:myapp}
    password: ${DATABASE_PASSWORD:}"""),

            docSubtitle("SQL: queries to records"),
            para("The SQL is yours; the mapping is the framework's. A row maps to a record by column name, "
                + "ignoring case and underscores, so created_at fills createdAt."),
            codeBlock("""
import jweb.Sql;

record User(Long id, String name, String email, LocalDateTime createdAt) {}

List<User> users = Sql.query("select * from users where email like ?", "%@example.com").list(User.class);
Optional<User> one = Sql.query("select * from users where id = ?", 7).first(User.class);
long n = Sql.query("select count(*) from users").scalar(Long.class);
List<User> named = Sql.query("select * from users where name = :name", Map.of("name", "Ada")).list(User.class);

int changed = Sql.update("update users set name = ? where id = ?", "Ada", 7);
long id = Sql.insert("insert into users (name, email) values (?, ?)", "Ada", "ada@example.com").key(Long.class);

Sql.transaction(() -> {
    Sql.update("update accounts set balance = balance - ? where id = ?", 50, 1);
    Sql.update("update accounts set balance = balance + ? where id = ?", 50, 2);
});"""),

            docSubtitle("SQL: a table through a record"),
            para("Columns are the record's components in snake_case; the component named id is the key. "
                + "Forms and tables meet on the record: Form.of renders it, Form.bind validates it, the table stores it."),
            codeBlock("""
import jweb.Sql;

record Todo(Long id, String title, boolean done) {}

Sql.Table<Todo> todos = Sql.table("todos", Todo.class);

Todo t = todos.insert(new Todo(null, "Write docs", false));   // the copy returned carries the generated id
todos.update(new Todo(t.id(), t.title(), true));
Optional<Todo> maybe = todos.find(t.id());
List<Todo> open = todos.where("done = ? order by id", false);  // everything after `where`
long left = todos.count("done = ?", false);
todos.delete(t.id());"""),

            docSubtitle("SQL: migrations"),
            para("Versioned files under src/main/resources/db/migration, applied once each in order at startup and "
                + "remembered in jweb_migrations with a checksum. The name is Flyway's convention, so the files move "
                + "to Flyway unchanged if an app ever wants it."),
            codeBlock("sql", """
-- db/migration/V1__create_todos.sql
create table todos (
    id bigint generated always as identity primary key,
    title varchar(200) not null,
    done boolean not null default false
);"""),
            codeBlock("""
import jweb.Migrations;
import javax.sql.DataSource;

DataSource dataSource = Sql.dataSource();

Migrations.Report report = Migrations.migrate(dataSource);       // what this call applied
List<Migrations.Migration> pending = Migrations.pending(dataSource);
List<Migrations.Applied> history = Migrations.applied(dataSource);"""),
            docTip("A migration edited after it was applied fails the next start, naming the version. "
                + "Put the change in a new file, or Migrations.repair(dataSource) when the edit was deliberate."),

            docSubtitle("MongoDB: connection"),
            para("Add org.mongodb:mongodb-driver-sync and set jweb.data.enabled: true to connect at startup; "
                + "or connect by hand."),
            codeBlock("""
import static jweb.Mongo.*;

// Connect with URI and database name
Mongo.connect("mongodb://localhost:27017", "mydb");

// Or use environment variables (MONGO_URI, MONGO_DB)
Mongo.connect();"""),

            docSubtitle("Documents"),
            para("Use Doc for dynamic document creation and field access."),
            codeBlock("""
import static jweb.Doc.*;

// Create a document
Doc user = Doc.of("users")
    .set("name", "John")
    .set("email", "john@example.com")
    .set("age", 25);

// Save to database
Mongo.save(user);

// Access fields
String name = user.getString("name");
int age = user.getInt("age");"""),

            docSubtitle("Queries"),
            para("Fluent query builder with filters, sorting, and pagination."),
            codeBlock("""
String id = "507f1f77bcf86cd799439011";

// Find by ID
Doc user = Mongo.findById("users", id);

// Find with filters
List<Doc> users = Mongo.find("users")
    .where("age").gte(18)
    .where("status", "active")
    .orderBy("name")
    .limit(10)
    .toList();

// Find first match
Doc admin = Mongo.find("users")
    .where("role", "admin")
    .first();"""),

            docSubtitle("Updates"),
            codeBlock("""
String id = "507f1f77bcf86cd799439011";
String visitorId = "507f191e810c19729de860ea";

// Update fields
Mongo.update("users")
    .where("id", id)
    .set("name", "Jane")
    .set("age", 26)
    .execute();

// Increment a field
Mongo.update("users")
    .where("id", visitorId)
    .inc("visitCount", 1)
    .execute();"""),

            docSubtitle("Deletes"),
            codeBlock("""
String userId = "507f1f77bcf86cd799439011";

// Delete by ID
Mongo.delete("users")
    .where("id", userId)
    .execute();

// Delete multiple
Mongo.delete("users")
    .where("status", "inactive")
    .executeAll();"""),

            docSubtitle("Schema Definition"),
            para("Define schemas for validation and automatic timestamps."),
            codeBlock("""
import static jweb.Schema.*;

Schema.collection("users")
    .id("id")
    .string("name").required()
    .string("email").required().unique()
    .integer("age").min(0).max(150)
    .list("roles")
    .timestamps()
    .register();"""),

            docTip("Schemas add createdAt/updatedAt automatically when timestamps() is called.")
        );
    }
}
