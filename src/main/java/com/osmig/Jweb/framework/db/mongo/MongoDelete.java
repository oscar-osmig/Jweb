package com.osmig.Jweb.framework.db.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * Fluent delete builder for MongoDB delete operations.
 *
 * <p>Usage:</p>
 * <pre>
 * // Delete by ID
 * Mongo.delete("users")
 *     .where("id", userId)
 *     .execute();
 *
 * // Delete multiple
 * Mongo.delete("users")
 *     .where("status", "inactive")
 *     .executeAll();
 *
 * // Delete all (dangerous!)
 * Mongo.delete("users")
 *     .all()
 *     .execute();
 * </pre>
 */
public class MongoDelete {

    private final String collectionName;
    private final List<Bson> filters = new ArrayList<>();
    private boolean deleteAll = false;

    public MongoDelete(String collectionName) {
        this.collectionName = collectionName;
    }

    // ==================== Where Clauses ====================

    /**
     * Adds an equality filter.
     */
    public MongoDelete where(String field, Object value) {
        // Convert id to _id
        if ("id".equals(field)) {
            field = "_id";
            if (value instanceof String str && org.bson.types.ObjectId.isValid(str)) {
                value = new org.bson.types.ObjectId(str);
            }
        }
        filters.add(eq(field, value));
        return this;
    }

    /**
     * Starts a comparison chain for a field.
     */
    public DeleteComparison where(String field) {
        return new DeleteComparison(this, field);
    }

    /**
     * Marks this as a delete all operation.
     */
    public MongoDelete all() {
        this.deleteAll = true;
        return this;
    }

    // ==================== Execution ====================

    /**
     * Deletes the first matching document.
     */
    public boolean execute() {
        MongoCollection<Document> collection = Mongo.getCollection(collectionName);

        if (deleteAll) {
            collection.deleteMany(new Document());
            return true;
        }

        if (filters.isEmpty()) {
            throw new IllegalStateException("Delete requires at least one where clause or all()");
        }

        Bson filter = buildFilter();
        DeleteResult result = collection.deleteOne(filter);
        return result.getDeletedCount() > 0;
    }

    /**
     * Deletes all matching documents.
     */
    public long executeAll() {
        MongoCollection<Document> collection = Mongo.getCollection(collectionName);

        Bson filter = buildFilter();
        if (filter == null) {
            filter = new Document();
        }

        DeleteResult result = collection.deleteMany(filter);
        return result.getDeletedCount();
    }

    // ==================== Helpers ====================

    void addFilter(Bson filter) {
        filters.add(filter);
    }

    private Bson buildFilter() {
        if (filters.isEmpty()) return null;
        return filters.size() == 1 ? filters.get(0) : and(filters);
    }

    // ==================== Comparison Builder ====================

    public static class DeleteComparison {
        private final MongoDelete delete;
        private final String field;

        DeleteComparison(MongoDelete delete, String field) {
            this.delete = delete;
            this.field = field;
        }

        public MongoDelete gt(Object value) {
            delete.addFilter(com.mongodb.client.model.Filters.gt(field, value));
            return delete;
        }

        public MongoDelete gte(Object value) {
            delete.addFilter(com.mongodb.client.model.Filters.gte(field, value));
            return delete;
        }

        public MongoDelete lt(Object value) {
            delete.addFilter(com.mongodb.client.model.Filters.lt(field, value));
            return delete;
        }

        public MongoDelete lte(Object value) {
            delete.addFilter(com.mongodb.client.model.Filters.lte(field, value));
            return delete;
        }

        public MongoDelete ne(Object value) {
            delete.addFilter(com.mongodb.client.model.Filters.ne(field, value));
            return delete;
        }

        public MongoDelete in(Object... values) {
            delete.addFilter(com.mongodb.client.model.Filters.in(field, values));
            return delete;
        }
    }
}
