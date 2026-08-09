package com.osmig.Jweb.framework.db.mongo;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaValidationTest {

    private Schema userSchema() {
        return Schema.collection("test_users")
            .id("id")
            .string("name").required().minLength(2).maxLength(50)
            .string("email").required().pattern(".+@.+")
            .string("role").enum_("admin", "user")
            .integer("age").min(0).max(150)
            .timestamps();
    }

    @Test
    void acceptsValidDocument() {
        Document doc = new Document("name", "Ada")
            .append("email", "ada@example.com")
            .append("role", "admin")
            .append("age", 36);
        assertDoesNotThrow(() -> userSchema().validate(doc));
    }

    @Test
    void rejectsMissingRequiredField() {
        Document doc = new Document("name", "Ada");
        var ex = assertThrows(Schema.ValidationException.class, () -> userSchema().validate(doc));
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    void rejectsPatternViolation() {
        Document doc = new Document("name", "Ada").append("email", "not-an-email");
        assertThrows(Schema.ValidationException.class, () -> userSchema().validate(doc));
    }

    @Test
    void rejectsEnumViolation() {
        Document doc = new Document("name", "Ada")
            .append("email", "a@b.c")
            .append("role", "superuser");
        assertThrows(Schema.ValidationException.class, () -> userSchema().validate(doc));
    }

    @Test
    void rejectsOutOfRangeNumber() {
        Document doc = new Document("name", "Ada")
            .append("email", "a@b.c")
            .append("age", 200);
        assertThrows(Schema.ValidationException.class, () -> userSchema().validate(doc));
    }

    @Test
    void rejectsTooShortString() {
        Document doc = new Document("name", "A").append("email", "a@b.c");
        assertThrows(Schema.ValidationException.class, () -> userSchema().validate(doc));
    }

    @Test
    void appliesDefaults() {
        Schema schema = Schema.collection("test_defaults");
        schema.string("status").default_("active");

        Document doc = new Document("other", "x");
        schema.validate(doc);
        assertEquals("active", doc.getString("status"));
    }
}
