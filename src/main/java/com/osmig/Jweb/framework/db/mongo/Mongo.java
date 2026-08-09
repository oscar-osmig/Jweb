package com.osmig.Jweb.framework.db.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for MongoDB operations.
 *
 * <p>Setup:</p>
 * <pre>
 * // Connect to MongoDB
 * Mongo.connect("mongodb://localhost:27017", "mydb");
 *
 * // Or with environment variables
 * Mongo.connect(); // Uses MONGO_URI and MONGO_DB
 * </pre>
 *
 * <p>CRUD Operations:</p>
 * <pre>
 * // Create
 * Doc user = Doc.of("users")
 *     .set("name", "John")
 *     .set("email", "john@example.com");
 * Mongo.save(user);
 *
 * // Read
 * Doc found = Mongo.findById("users", id);
 * List&lt;Doc&gt; users = Mongo.find("users")
 *     .where("age").gte(18)
 *     .toList();
 *
 * // Update
 * Mongo.update("users")
 *     .where("id", id)
 *     .set("name", "Jane")
 *     .execute();
 *
 * // Delete
 * Mongo.delete("users")
 *     .where("id", id)
 *     .execute();
 * </pre>
 *
 * <p>Schema Definition:</p>
 * <pre>
 * Schema.collection("users")
 *     .id("id")
 *     .string("name").required()
 *     .string("email").required().unique()
 *     .timestamps()
 *     .register();
 * </pre>
 */
public class Mongo {

    private static MongoClient client;
    private static MongoDatabase database;
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Schema> schemas = new ConcurrentHashMap<>();

    // ==================== Connection ====================

    /**
     * Connects using environment variables MONGO_URI and MONGO_DB.
     */
    public static void connect() {
        String uri = System.getenv("MONGO_URI");
        String dbName = System.getenv("MONGO_DB");

        if (uri == null) uri = "mongodb://localhost:27017";
        if (dbName == null) dbName = "test";

        connect(uri, dbName);
    }

    /**
     * Connects to MongoDB with the specified URI and database.
     */
    public static void connect(String uri, String databaseName) {
        if (client != null) {
            client.close();
        }
        client = MongoClients.create(uri);
        database = client.getDatabase(databaseName);
        applyRegisteredSchemas();
    }

    /**
     * Connects using an existing MongoClient.
     */
    public static void connect(MongoClient mongoClient, String databaseName) {
        client = mongoClient;
        database = client.getDatabase(databaseName);
        applyRegisteredSchemas();
    }

    /** Creates indexes for every schema registered before the connection. */
    private static void applyRegisteredSchemas() {
        for (Schema schema : schemas.values()) {
            try {
                ensureIndexes(schema);
            } catch (Exception e) {
                com.osmig.Jweb.framework.util.Log.warn(
                    "Could not create indexes for '{}': {}", schema.getCollectionName(), e.getMessage());
            }
        }
    }

    /**
     * Disconnects from MongoDB.
     */
    public static void disconnect() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }

    /**
     * Returns the underlying MongoDatabase.
     */
    public static MongoDatabase getDatabase() {
        ensureConnected();
        return database;
    }

    /**
     * Returns a raw MongoCollection.
     */
    public static MongoCollection<Document> getCollection(String name) {
        ensureConnected();
        return database.getCollection(name);
    }

    // ==================== CRUD Operations ====================

    /**
     * Saves a document (insert or update).
     */
    public static Doc save(Doc doc) {
        ensureConnected();
        MongoCollection<Document> collection = getCollection(doc.getCollectionName());
        Document bson = doc.toBson();

        Schema schema = schemas.get(doc.getCollectionName());
        if (schema != null) {
            schema.validate(bson);
            if (schema.hasTimestamps()) {
                long now = System.currentTimeMillis();
                if (doc.getId() == null) {
                    bson.put("createdAt", now);
                }
                bson.put("updatedAt", now);
            }
        }

        if (doc.getId() == null) {
            // Insert new document
            collection.insertOne(bson);
            // Set the generated ID back on the doc
            ObjectId generatedId = bson.getObjectId("_id");
            if (generatedId != null) {
                doc.set("id", generatedId.toHexString());
            }
        } else {
            // Update existing document
            ObjectId objectId = new ObjectId(doc.getId());
            bson.remove("_id"); // Don't try to update _id
            collection.replaceOne(
                new Document("_id", objectId),
                bson.append("_id", objectId)
            );
        }

        return doc;
    }

    /**
     * Inserts a new document.
     */
    public static Doc insert(Doc doc) {
        ensureConnected();
        MongoCollection<Document> collection = getCollection(doc.getCollectionName());
        Document bson = doc.toBson();

        Schema schema = schemas.get(doc.getCollectionName());
        if (schema != null) {
            schema.validate(bson);
            if (schema.hasTimestamps()) {
                long now = System.currentTimeMillis();
                bson.put("createdAt", now);
                bson.put("updatedAt", now);
            }
        }

        collection.insertOne(bson);

        ObjectId generatedId = bson.getObjectId("_id");
        if (generatedId != null) {
            doc.set("id", generatedId.toHexString());
        }

        return doc;
    }

    /**
     * Finds a document by ID.
     */
    public static Doc findById(String collectionName, String id) {
        ensureConnected();

        if (id == null || !ObjectId.isValid(id)) {
            return null;
        }

        MongoCollection<Document> collection = getCollection(collectionName);
        Document found = collection.find(new Document("_id", new ObjectId(id))).first();

        if (found == null) {
            return null;
        }

        return Doc.fromBson(collectionName, found);
    }

    /**
     * Finds a document by ID, returning Optional.
     */
    public static Optional<Doc> findByIdOptional(String collectionName, String id) {
        return Optional.ofNullable(findById(collectionName, id));
    }

    /**
     * Starts a find query builder.
     */
    public static MongoQuery find(String collectionName) {
        return new MongoQuery(collectionName);
    }

    /**
     * Starts an update query builder.
     */
    public static MongoUpdate update(String collectionName) {
        return new MongoUpdate(collectionName);
    }

    /**
     * Starts a delete query builder.
     */
    public static MongoDelete delete(String collectionName) {
        return new MongoDelete(collectionName);
    }

    /**
     * Counts documents in a collection.
     */
    public static long count(String collectionName) {
        ensureConnected();
        return getCollection(collectionName).countDocuments();
    }

    /**
     * Checks if a document exists by ID.
     */
    public static boolean exists(String collectionName, String id) {
        return findById(collectionName, id) != null;
    }

    /**
     * Deletes a document by ID.
     */
    public static boolean deleteById(String collectionName, String id) {
        if (id == null || !ObjectId.isValid(id)) {
            return false;
        }

        ensureConnected();
        MongoCollection<Document> collection = getCollection(collectionName);
        return collection.deleteOne(new Document("_id", new ObjectId(id))).getDeletedCount() > 0;
    }

    // ==================== POJO Mapping ====================

    /**
     * Saves a POJO and returns the generated ID.
     */
    public static <T> String save(String collectionName, T object) {
        ensureConnected();
        Document doc = Document.parse(toJson(object));

        Schema schema = schemas.get(collectionName);
        if (schema != null) {
            schema.validate(doc);
            if (schema.hasTimestamps()) {
                long now = System.currentTimeMillis();
                if (!doc.containsKey("_id")) {
                    doc.put("createdAt", now);
                }
                doc.put("updatedAt", now);
            }
        }

        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(doc);

        ObjectId id = doc.getObjectId("_id");
        return id != null ? id.toHexString() : null;
    }

    /**
     * Finds a document by ID and maps to POJO.
     */
    public static <T> T findById(String collectionName, String id, Class<T> type) {
        Doc doc = findById(collectionName, id);
        return doc != null ? doc.as(type) : null;
    }

    /**
     * Finds a document by ID and maps to POJO, returning Optional.
     */
    public static <T> Optional<T> findByIdOptional(String collectionName, String id, Class<T> type) {
        return Optional.ofNullable(findById(collectionName, id, type));
    }

    // ==================== Schema Registry ====================

    /**
     * Registers a schema. Called by {@link Schema#register()}; may also be
     * called directly. Declared indexes (and unique fields) are created
     * immediately when connected, or on the next {@code connect(...)}.
     */
    public static void registerSchema(Schema schema) {
        schemas.put(schema.getCollectionName(), schema);
        if (database != null) {
            ensureIndexes(schema);
        }
    }

    /**
     * Creates the indexes declared by a schema (idempotent — MongoDB ignores
     * createIndex calls for indexes that already exist).
     */
    public static void ensureIndexes(Schema schema) {
        ensureConnected();
        MongoCollection<Document> collection = getCollection(schema.getCollectionName());

        // Unique field constraints become unique single-field indexes
        for (Schema.FieldDef field : schema.getFields().values()) {
            if (field.unique) {
                collection.createIndex(
                    com.mongodb.client.model.Indexes.ascending(field.name),
                    new com.mongodb.client.model.IndexOptions().unique(true));
            }
        }

        // Declared indexes
        for (Schema.IndexDef index : schema.getIndexes()) {
            org.bson.conversions.Bson keys = index.text
                ? com.mongodb.client.model.Indexes.compoundIndex(
                      java.util.Arrays.stream(index.fields)
                          .map(com.mongodb.client.model.Indexes::text)
                          .toArray(org.bson.conversions.Bson[]::new))
                : com.mongodb.client.model.Indexes.ascending(index.fields);

            var options = new com.mongodb.client.model.IndexOptions()
                .unique(index.unique)
                .sparse(index.sparse);
            if (index.expireAfterSeconds != null) {
                options.expireAfter(index.expireAfterSeconds, java.util.concurrent.TimeUnit.SECONDS);
            }
            collection.createIndex(keys, options);
        }
    }

    /**
     * Gets a registered schema.
     */
    public static Schema getSchema(String collectionName) {
        return schemas.get(collectionName);
    }

    /**
     * Checks if a schema is registered.
     */
    public static boolean hasSchema(String collectionName) {
        return schemas.containsKey(collectionName);
    }

    // ==================== ObjectMapper ====================

    /**
     * Sets a custom ObjectMapper for POJO serialization.
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    /**
     * Gets the ObjectMapper used for POJO serialization.
     */
    public static ObjectMapper getMapper() {
        return objectMapper;
    }

    // ==================== Helpers ====================

    /** True when a database connection is active. */
    public static boolean isConnected() {
        return database != null;
    }

    private static void ensureConnected() {
        if (database == null) {
            throw new IllegalStateException(
                "MongoDB not connected. Call Mongo.connect() first."
            );
        }
    }

    private static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Generates a new ObjectId string.
     */
    public static String newId() {
        return new ObjectId().toHexString();
    }

    /**
     * Validates if a string is a valid ObjectId.
     */
    public static boolean isValidId(String id) {
        return id != null && ObjectId.isValid(id);
    }
}
