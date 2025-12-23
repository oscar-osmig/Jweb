package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class DataSection {
    private DataSection() {}

    public static Element render() {
        return section(
            docTitle("Database"),
            para("JWeb provides a fluent DSL for MongoDB operations with schemas, queries, updates, and deletes."),

            docSubtitle("Connection"),
            codeBlock("""
import static com.osmig.Jweb.framework.db.mongo.Mongo.*;

// Connect with URI and database name
Mongo.connect("mongodb://localhost:27017", "mydb");

// Or use environment variables (MONGO_URI, MONGO_DB)
Mongo.connect();"""),

            docSubtitle("Documents"),
            para("Use Doc for dynamic document creation and field access."),
            codeBlock("""
import static com.osmig.Jweb.framework.db.mongo.Doc.*;

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
import static com.osmig.Jweb.framework.db.mongo.Schema.*;

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
