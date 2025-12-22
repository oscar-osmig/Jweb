package com.osmig.Jweb.framework.db.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Fluent update builder for MongoDB update operations.
 *
 * <p>Usage:</p>
 * <pre>
 * // Update specific fields
 * Mongo.update("users")
 *     .where("id", userId)
 *     .set("name", "Jane")
 *     .set("age", 26)
 *     .execute();
 *
 * // Increment a field
 * Mongo.update("users")
 *     .where("id", visitorId)
 *     .inc("visitCount", 1)
 *     .execute();
 *
 * // Update multiple documents
 * Mongo.update("users")
 *     .where("status", "pending")
 *     .set("status", "active")
 *     .executeAll();
 * </pre>
 */
public class MongoUpdate {

    private final String collectionName;
    private final List<Bson> filters = new ArrayList<>();
    private final List<Bson> updates = new ArrayList<>();
    private boolean upsert = false;
    private boolean returnNew = false;

    public MongoUpdate(String collectionName) {
        this.collectionName = collectionName;
    }

    // ==================== Where Clauses ====================

    /**
     * Adds an equality filter.
     */
    public MongoUpdate where(String field, Object value) {
        // Convert id to _id
        if ("id".equals(field)) {
            field = "_id";
            if (value instanceof String str && org.bson.types.ObjectId.isValid(str)) {
                value = new org.bson.types.ObjectId(str);
            }
        }
        filters.add(Filters.eq(field, value));
        return this;
    }

    // ==================== Update Operations ====================

    /**
     * Sets a field to a value.
     */
    public MongoUpdate set(String field, Object value) {
        if (value instanceof LocalDateTime ldt) {
            value = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } else if (value instanceof Doc doc) {
            value = doc.toBson();
        }
        updates.add(Updates.set(field, value));
        return this;
    }

    /**
     * Unsets (removes) a field.
     */
    public MongoUpdate unset(String field) {
        updates.add(Updates.unset(field));
        return this;
    }

    /**
     * Increments a numeric field.
     */
    public MongoUpdate inc(String field, Number amount) {
        updates.add(Updates.inc(field, amount));
        return this;
    }

    /**
     * Multiplies a numeric field.
     */
    public MongoUpdate mul(String field, Number multiplier) {
        updates.add(Updates.mul(field, multiplier));
        return this;
    }

    /**
     * Sets the minimum value (only updates if new value is lower).
     */
    public MongoUpdate min(String field, Object value) {
        updates.add(Updates.min(field, value));
        return this;
    }

    /**
     * Sets the maximum value (only updates if new value is higher).
     */
    public MongoUpdate max(String field, Object value) {
        updates.add(Updates.max(field, value));
        return this;
    }

    /**
     * Renames a field.
     */
    public MongoUpdate rename(String oldName, String newName) {
        updates.add(Updates.rename(oldName, newName));
        return this;
    }

    /**
     * Sets the current date/time.
     */
    public MongoUpdate currentDate(String field) {
        updates.add(Updates.currentDate(field));
        return this;
    }

    /**
     * Sets the current timestamp.
     */
    public MongoUpdate currentTimestamp(String field) {
        updates.add(Updates.currentTimestamp(field));
        return this;
    }

    // ==================== Array Operations ====================

    /**
     * Pushes a value to an array.
     */
    public MongoUpdate push(String field, Object value) {
        updates.add(Updates.push(field, value));
        return this;
    }

    /**
     * Pushes multiple values to an array.
     */
    public MongoUpdate pushAll(String field, Object... values) {
        updates.add(Updates.pushEach(field, Arrays.asList(values)));
        return this;
    }

    /**
     * Adds a value to an array only if it doesn't exist.
     */
    public MongoUpdate addToSet(String field, Object value) {
        updates.add(Updates.addToSet(field, value));
        return this;
    }

    /**
     * Removes the first element from an array.
     */
    public MongoUpdate popFirst(String field) {
        updates.add(Updates.popFirst(field));
        return this;
    }

    /**
     * Removes the last element from an array.
     */
    public MongoUpdate popLast(String field) {
        updates.add(Updates.popLast(field));
        return this;
    }

    /**
     * Removes a value from an array.
     */
    public MongoUpdate pull(String field, Object value) {
        updates.add(Updates.pull(field, value));
        return this;
    }

    /**
     * Removes multiple values from an array.
     */
    public MongoUpdate pullAll(String field, Object... values) {
        updates.add(Updates.pullAll(field, Arrays.asList(values)));
        return this;
    }

    // ==================== Options ====================

    /**
     * Enables upsert (insert if not exists).
     */
    public MongoUpdate upsert() {
        this.upsert = true;
        return this;
    }

    /**
     * Returns the new document instead of the old one.
     */
    public MongoUpdate returnNew() {
        this.returnNew = true;
        return this;
    }

    // ==================== Execution ====================

    /**
     * Executes the update on the first matching document.
     */
    public boolean execute() {
        if (filters.isEmpty()) {
            throw new IllegalStateException("Update requires at least one where clause");
        }
        if (updates.isEmpty()) {
            return false;
        }

        // Auto-add updatedAt if schema has timestamps
        Schema schema = Schema.get(collectionName);
        if (schema != null && schema.hasTimestamps()) {
            updates.add(Updates.currentDate("updatedAt"));
        }

        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        Bson filter = buildFilter();
        Bson update = buildUpdate();

        UpdateOptions options = new UpdateOptions().upsert(upsert);
        UpdateResult result = collection.updateOne(filter, update, options);

        return result.getModifiedCount() > 0 || result.getUpsertedId() != null;
    }

    /**
     * Executes the update on all matching documents.
     */
    public long executeAll() {
        if (updates.isEmpty()) {
            return 0;
        }

        // Auto-add updatedAt if schema has timestamps
        Schema schema = Schema.get(collectionName);
        if (schema != null && schema.hasTimestamps()) {
            updates.add(Updates.currentDate("updatedAt"));
        }

        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        Bson filter = buildFilter();
        Bson update = buildUpdate();

        UpdateOptions options = new UpdateOptions().upsert(upsert);

        if (filter == null) {
            filter = new Document(); // Match all
        }

        UpdateResult result = collection.updateMany(filter, update, options);
        return result.getModifiedCount();
    }

    /**
     * Executes the update and returns the document.
     */
    public Doc executeAndGet() {
        if (filters.isEmpty()) {
            throw new IllegalStateException("Update requires at least one where clause");
        }
        if (updates.isEmpty()) {
            return null;
        }

        // Auto-add updatedAt if schema has timestamps
        Schema schema = Schema.get(collectionName);
        if (schema != null && schema.hasTimestamps()) {
            updates.add(Updates.currentDate("updatedAt"));
        }

        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        Bson filter = buildFilter();
        Bson update = buildUpdate();

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
            .upsert(upsert)
            .returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);

        Document doc = collection.findOneAndUpdate(filter, update, options);
        return doc != null ? Doc.fromBson(collectionName, doc) : null;
    }

    // ==================== Helpers ====================

    private Bson buildFilter() {
        if (filters.isEmpty()) return null;
        return filters.size() == 1 ? filters.get(0) : Filters.and(filters);
    }

    private Bson buildUpdate() {
        return updates.size() == 1 ? updates.get(0) : Updates.combine(updates);
    }
}
