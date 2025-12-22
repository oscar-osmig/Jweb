package com.osmig.Jweb.framework.db.mongo;

import java.util.*;

/**
 * Fluent schema builder for MongoDB collections.
 *
 * <p>Usage:</p>
 * <pre>
 * Schema.collection("users")
 *     .id("id")
 *     .string("name").required()
 *     .string("email").required().unique()
 *     .integer("age").min(0).max(150)
 *     .list("roles")
 *     .embedded("address", Schema.object()
 *         .string("street")
 *         .string("city")
 *         .string("country")
 *     )
 *     .timestamps()
 *     .register();
 * </pre>
 */
public class Schema {

    private static final Map<String, Schema> schemas = new HashMap<>();

    private final String collectionName;
    private final Map<String, FieldDef> fields = new LinkedHashMap<>();
    private final List<IndexDef> indexes = new ArrayList<>();
    private boolean hasTimestamps = false;

    private Schema(String collectionName) {
        this.collectionName = collectionName;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new schema for a collection.
     */
    public static Schema collection(String name) {
        return new Schema(name);
    }

    /**
     * Creates an embedded object schema (no collection name).
     */
    public static Schema object() {
        return new Schema(null);
    }

    /**
     * Gets a registered schema by collection name.
     */
    public static Schema get(String collectionName) {
        return schemas.get(collectionName);
    }

    /**
     * Checks if a schema is registered.
     */
    public static boolean exists(String collectionName) {
        return schemas.containsKey(collectionName);
    }

    // ==================== Field Definitions ====================

    /**
     * Adds the ID field.
     */
    public Schema id(String name) {
        fields.put(name, new FieldDef(name, FieldType.ID));
        return this;
    }

    /**
     * Adds a string field.
     */
    public FieldBuilder string(String name) {
        FieldDef field = new FieldDef(name, FieldType.STRING);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds an integer field.
     */
    public FieldBuilder integer(String name) {
        FieldDef field = new FieldDef(name, FieldType.INTEGER);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a long field.
     */
    public FieldBuilder longField(String name) {
        FieldDef field = new FieldDef(name, FieldType.LONG);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a double/number field.
     */
    public FieldBuilder number(String name) {
        FieldDef field = new FieldDef(name, FieldType.DOUBLE);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a boolean field.
     */
    public FieldBuilder boolean_(String name) {
        FieldDef field = new FieldDef(name, FieldType.BOOLEAN);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a date/timestamp field.
     */
    public FieldBuilder timestamp(String name) {
        FieldDef field = new FieldDef(name, FieldType.TIMESTAMP);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a list/array field.
     */
    public FieldBuilder list(String name) {
        FieldDef field = new FieldDef(name, FieldType.LIST);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds a list field with element type.
     */
    public FieldBuilder list(String name, Class<?> elementType) {
        FieldDef field = new FieldDef(name, FieldType.LIST);
        field.elementType = elementType;
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds an embedded object field.
     */
    public Schema embedded(String name, Schema embeddedSchema) {
        FieldDef field = new FieldDef(name, FieldType.EMBEDDED);
        field.embeddedSchema = embeddedSchema;
        fields.put(name, field);
        return this;
    }

    /**
     * Adds an object/map field (dynamic structure).
     */
    public FieldBuilder object(String name) {
        FieldDef field = new FieldDef(name, FieldType.OBJECT);
        fields.put(name, field);
        return new FieldBuilder(this, field);
    }

    /**
     * Adds createdAt and updatedAt timestamp fields.
     */
    public Schema timestamps() {
        this.hasTimestamps = true;
        FieldDef createdAt = new FieldDef("createdAt", FieldType.TIMESTAMP);
        createdAt.autoCreate = true;
        fields.put("createdAt", createdAt);

        FieldDef updatedAt = new FieldDef("updatedAt", FieldType.TIMESTAMP);
        updatedAt.autoUpdate = true;
        fields.put("updatedAt", updatedAt);
        return this;
    }

    // ==================== Indexes ====================

    /**
     * Adds an index on one or more fields.
     */
    public IndexBuilder index(String... fieldNames) {
        IndexDef index = new IndexDef(fieldNames);
        indexes.add(index);
        return new IndexBuilder(this, index);
    }

    // ==================== Registration ====================

    /**
     * Registers this schema so it can be used with Mongo operations.
     */
    public Schema register() {
        if (collectionName == null) {
            throw new IllegalStateException("Cannot register an embedded object schema");
        }
        schemas.put(collectionName, this);
        return this;
    }

    // ==================== Getters ====================

    public String getCollectionName() {
        return collectionName;
    }

    public Map<String, FieldDef> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public FieldDef getField(String name) {
        // Support nested field access
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            FieldDef parent = fields.get(parts[0]);
            if (parent != null && parent.embeddedSchema != null) {
                return parent.embeddedSchema.getField(parts[1]);
            }
            return null;
        }
        return fields.get(name);
    }

    public List<IndexDef> getIndexes() {
        return Collections.unmodifiableList(indexes);
    }

    public boolean hasTimestamps() {
        return hasTimestamps;
    }

    // ==================== Field Definition ====================

    public enum FieldType {
        ID, STRING, INTEGER, LONG, DOUBLE, BOOLEAN, TIMESTAMP, LIST, EMBEDDED, OBJECT
    }

    public static class FieldDef {
        public final String name;
        public final FieldType type;
        public boolean required = false;
        public boolean unique = false;
        public Object defaultValue = null;
        public Number min = null;
        public Number max = null;
        public Integer minLength = null;
        public Integer maxLength = null;
        public List<String> enumValues = null;
        public String pattern = null;
        public boolean autoCreate = false;
        public boolean autoUpdate = false;
        public Class<?> elementType = null;
        public Schema embeddedSchema = null;

        FieldDef(String name, FieldType type) {
            this.name = name;
            this.type = type;
        }
    }

    // ==================== Field Builder ====================

    public static class FieldBuilder {
        private final Schema schema;
        private final FieldDef field;

        FieldBuilder(Schema schema, FieldDef field) {
            this.schema = schema;
            this.field = field;
        }

        public FieldBuilder required() {
            field.required = true;
            return this;
        }

        public FieldBuilder unique() {
            field.unique = true;
            return this;
        }

        public FieldBuilder default_(Object value) {
            field.defaultValue = value;
            return this;
        }

        public FieldBuilder min(Number value) {
            field.min = value;
            return this;
        }

        public FieldBuilder max(Number value) {
            field.max = value;
            return this;
        }

        public FieldBuilder minLength(int length) {
            field.minLength = length;
            return this;
        }

        public FieldBuilder maxLength(int length) {
            field.maxLength = length;
            return this;
        }

        public FieldBuilder enum_(String... values) {
            field.enumValues = Arrays.asList(values);
            return this;
        }

        public FieldBuilder pattern(String regex) {
            field.pattern = regex;
            return this;
        }

        public FieldBuilder auto() {
            field.autoCreate = true;
            return this;
        }

        public FieldBuilder autoUpdate() {
            field.autoUpdate = true;
            return this;
        }

        // Delegate back to schema for chaining

        public Schema id(String name) { return schema.id(name); }
        public FieldBuilder string(String name) { return schema.string(name); }
        public FieldBuilder integer(String name) { return schema.integer(name); }
        public FieldBuilder longField(String name) { return schema.longField(name); }
        public FieldBuilder number(String name) { return schema.number(name); }
        public FieldBuilder boolean_(String name) { return schema.boolean_(name); }
        public FieldBuilder timestamp(String name) { return schema.timestamp(name); }
        public FieldBuilder list(String name) { return schema.list(name); }
        public FieldBuilder list(String name, Class<?> elementType) { return schema.list(name, elementType); }
        public Schema embedded(String name, Schema embeddedSchema) { return schema.embedded(name, embeddedSchema); }
        public FieldBuilder object(String name) { return schema.object(name); }
        public Schema timestamps() { return schema.timestamps(); }
        public IndexBuilder index(String... fieldNames) { return schema.index(fieldNames); }
        public Schema register() { return schema.register(); }
    }

    // ==================== Index Definition ====================

    public static class IndexDef {
        public final String[] fields;
        public boolean unique = false;
        public boolean sparse = false;
        public boolean text = false;
        public Long expireAfterSeconds = null;

        IndexDef(String[] fields) {
            this.fields = fields;
        }
    }

    public static class IndexBuilder {
        private final Schema schema;
        private final IndexDef index;

        IndexBuilder(Schema schema, IndexDef index) {
            this.schema = schema;
            this.index = index;
        }

        public IndexBuilder unique() {
            index.unique = true;
            return this;
        }

        public IndexBuilder sparse() {
            index.sparse = true;
            return this;
        }

        public IndexBuilder text() {
            index.text = true;
            return this;
        }

        public IndexBuilder expireAfter(java.time.Duration duration) {
            index.expireAfterSeconds = duration.toSeconds();
            return this;
        }

        // Delegate back to schema
        public IndexBuilder index(String... fieldNames) { return schema.index(fieldNames); }
        public Schema register() { return schema.register(); }
    }
}
