package com.osmig.Jweb.framework.db.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Fluent query builder for MongoDB find operations.
 *
 * <p>Usage:</p>
 * <pre>
 * // Find one
 * Doc user = Mongo.find("users")
 *     .where("email", "john@example.com")
 *     .first();
 *
 * // Find many with filters
 * List&lt;Doc&gt; users = Mongo.find("users")
 *     .where("age").gte(18)
 *     .where("status", "active")
 *     .orderBy("name")
 *     .limit(10)
 *     .toList();
 * </pre>
 */
public class MongoQuery {

    private final String collectionName;
    private final List<Bson> filters = new ArrayList<>();
    private final List<Bson> sorts = new ArrayList<>();
    private List<String> projection = null;
    private List<String> exclusion = null;
    private int skipCount = 0;
    private int limitCount = 0;

    // For chained comparisons
    private String currentField = null;

    public MongoQuery(String collectionName) {
        this.collectionName = collectionName;
    }

    // ==================== Where Clauses ====================

    /**
     * Adds an equality filter.
     */
    public MongoQuery where(String field, Object value) {
        filters.add(Filters.eq(field, value));
        return this;
    }

    /**
     * Starts a comparison chain for a field.
     */
    public MongoQuery where(String field) {
        this.currentField = field;
        return this;
    }

    /**
     * Greater than.
     */
    public MongoQuery gt(Object value) {
        if (currentField != null) {
            filters.add(Filters.gt(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * Greater than or equal.
     */
    public MongoQuery gte(Object value) {
        if (currentField != null) {
            filters.add(Filters.gte(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * Less than.
     */
    public MongoQuery lt(Object value) {
        if (currentField != null) {
            filters.add(Filters.lt(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * Less than or equal.
     */
    public MongoQuery lte(Object value) {
        if (currentField != null) {
            filters.add(Filters.lte(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * Not equal.
     */
    public MongoQuery ne(Object value) {
        if (currentField != null) {
            filters.add(Filters.ne(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * In list of values.
     */
    public MongoQuery in(Object... values) {
        if (currentField != null) {
            filters.add(Filters.in(currentField, Arrays.asList(values)));
            currentField = null;
        }
        return this;
    }

    /**
     * Not in list of values.
     */
    public MongoQuery notIn(Object... values) {
        if (currentField != null) {
            filters.add(Filters.nin(currentField, Arrays.asList(values)));
            currentField = null;
        }
        return this;
    }

    /**
     * Regex match.
     */
    public MongoQuery regex(String pattern) {
        if (currentField != null) {
            filters.add(Filters.regex(currentField, Pattern.compile(pattern)));
            currentField = null;
        }
        return this;
    }

    /**
     * Regex match with options.
     */
    public MongoQuery regex(String pattern, String options) {
        if (currentField != null) {
            filters.add(Filters.regex(currentField, Pattern.compile(pattern, parseRegexOptions(options))));
            currentField = null;
        }
        return this;
    }

    private int parseRegexOptions(String options) {
        int flags = 0;
        if (options.contains("i")) flags |= Pattern.CASE_INSENSITIVE;
        if (options.contains("m")) flags |= Pattern.MULTILINE;
        if (options.contains("s")) flags |= Pattern.DOTALL;
        return flags;
    }

    /**
     * Field exists in the document.
     */
    public MongoQuery fieldExists() {
        if (currentField != null) {
            filters.add(Filters.exists(currentField, true));
            currentField = null;
        }
        return this;
    }

    /**
     * Field does not exist in the document.
     */
    public MongoQuery fieldNotExists() {
        if (currentField != null) {
            filters.add(Filters.exists(currentField, false));
            currentField = null;
        }
        return this;
    }

    /**
     * Field is null.
     */
    public MongoQuery isNull() {
        if (currentField != null) {
            filters.add(Filters.eq(currentField, null));
            currentField = null;
        }
        return this;
    }

    /**
     * Field is not null.
     */
    public MongoQuery isNotNull() {
        if (currentField != null) {
            filters.add(Filters.ne(currentField, null));
            currentField = null;
        }
        return this;
    }

    /**
     * Array contains value.
     */
    public MongoQuery contains(Object value) {
        if (currentField != null) {
            filters.add(Filters.eq(currentField, value));
            currentField = null;
        }
        return this;
    }

    /**
     * Array contains all values.
     */
    public MongoQuery containsAll(Object... values) {
        if (currentField != null) {
            filters.add(Filters.all(currentField, Arrays.asList(values)));
            currentField = null;
        }
        return this;
    }

    // ==================== OR Conditions ====================

    /**
     * Adds OR conditions.
     */
    @SafeVarargs
    public final MongoQuery or(java.util.function.Function<MongoQuery, MongoQuery>... conditions) {
        List<Bson> orFilters = new ArrayList<>();
        for (var condition : conditions) {
            MongoQuery subQuery = new MongoQuery(collectionName);
            condition.apply(subQuery);
            if (!subQuery.filters.isEmpty()) {
                orFilters.add(subQuery.filters.size() == 1
                    ? subQuery.filters.get(0)
                    : Filters.and(subQuery.filters));
            }
        }
        if (!orFilters.isEmpty()) {
            filters.add(Filters.or(orFilters));
        }
        return this;
    }

    // ==================== Sorting ====================

    /**
     * Orders by field ascending.
     */
    public MongoQuery orderBy(String field) {
        sorts.add(Sorts.ascending(field));
        return this;
    }

    /**
     * Orders descending (call after orderBy).
     */
    public MongoQuery desc() {
        if (!sorts.isEmpty()) {
            Bson last = sorts.remove(sorts.size() - 1);
            // Extract field name and add descending
            String fieldName = last.toBsonDocument().getFirstKey();
            sorts.add(Sorts.descending(fieldName));
        }
        return this;
    }

    /**
     * Orders by field descending.
     */
    public MongoQuery orderByDesc(String field) {
        sorts.add(Sorts.descending(field));
        return this;
    }

    // ==================== Pagination ====================

    /**
     * Skips n documents.
     */
    public MongoQuery skip(int n) {
        this.skipCount = n;
        return this;
    }

    /**
     * Limits to n documents.
     */
    public MongoQuery limit(int n) {
        this.limitCount = n;
        return this;
    }

    // ==================== Projection ====================

    /**
     * Selects only specific fields.
     */
    public MongoQuery select(String... fields) {
        this.projection = Arrays.asList(fields);
        return this;
    }

    /**
     * Excludes specific fields.
     */
    public MongoQuery exclude(String... fields) {
        this.exclusion = Arrays.asList(fields);
        return this;
    }

    // ==================== Execution ====================

    /**
     * Finds the first matching document.
     */
    public Doc first() {
        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        FindIterable<Document> iterable = buildIterable(collection);
        Document doc = iterable.first();
        return doc != null ? Doc.fromBson(collectionName, doc) : null;
    }

    /**
     * Finds the first matching document and maps to POJO.
     */
    public <T> T first(Class<T> type) {
        Doc doc = first();
        return doc != null ? doc.as(type) : null;
    }

    /**
     * Finds all matching documents.
     */
    public List<Doc> toList() {
        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        FindIterable<Document> iterable = buildIterable(collection);

        List<Doc> results = new ArrayList<>();
        for (Document doc : iterable) {
            results.add(Doc.fromBson(collectionName, doc));
        }
        return results;
    }

    /**
     * Finds all matching documents and maps to POJOs.
     */
    public <T> List<T> toList(Class<T> type) {
        List<Doc> docs = toList();
        List<T> results = new ArrayList<>();
        for (Doc doc : docs) {
            results.add(doc.as(type));
        }
        return results;
    }

    /**
     * Counts matching documents.
     */
    public long count() {
        MongoCollection<Document> collection = Mongo.getCollection(collectionName);
        Bson filter = buildFilter();
        return filter != null ? collection.countDocuments(filter) : collection.countDocuments();
    }

    /**
     * Checks if any matching document exists.
     */
    public boolean exists() {
        return first() != null;
    }

    // ==================== Helpers ====================

    private FindIterable<Document> buildIterable(MongoCollection<Document> collection) {
        Bson filter = buildFilter();
        FindIterable<Document> iterable = filter != null
            ? collection.find(filter)
            : collection.find();

        if (!sorts.isEmpty()) {
            iterable = iterable.sort(sorts.size() == 1 ? sorts.get(0) : Sorts.orderBy(sorts));
        }
        if (skipCount > 0) {
            iterable = iterable.skip(skipCount);
        }
        if (limitCount > 0) {
            iterable = iterable.limit(limitCount);
        }
        if (projection != null) {
            Document proj = new Document();
            for (String field : projection) {
                proj.put(field, 1);
            }
            iterable = iterable.projection(proj);
        }
        if (exclusion != null) {
            Document proj = new Document();
            for (String field : exclusion) {
                proj.put(field, 0);
            }
            iterable = iterable.projection(proj);
        }

        return iterable;
    }

    private Bson buildFilter() {
        if (filters.isEmpty()) return null;
        return filters.size() == 1 ? filters.get(0) : Filters.and(filters);
    }
}
