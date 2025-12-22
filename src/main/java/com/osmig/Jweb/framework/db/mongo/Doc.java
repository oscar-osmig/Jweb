package com.osmig.Jweb.framework.db.mongo;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Dynamic document wrapper for MongoDB documents.
 * Provides typed access to fields and fluent setters.
 *
 * <p>Usage:</p>
 * <pre>
 * // Create a new document
 * Doc user = Doc.of("users")
 *     .set("name", "John")
 *     .set("email", "john@example.com")
 *     .set("age", 25);
 *
 * // Access fields
 * String name = user.get("name");
 * int age = user.get("age");
 * String city = user.get("address.city");
 *
 * // Typed access
 * String name = user.getString("name");
 * int age = user.getInt("age");
 * List&lt;String&gt; roles = user.getList("roles");
 * Doc address = user.getDoc("address");
 * </pre>
 */
public class Doc {

    private final String collectionName;
    private final Map<String, Object> data;

    private Doc(String collectionName) {
        this.collectionName = collectionName;
        this.data = new LinkedHashMap<>();
    }

    private Doc(String collectionName, Map<String, Object> data) {
        this.collectionName = collectionName;
        this.data = new LinkedHashMap<>(data);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new document for a collection.
     */
    public static Doc of(String collectionName) {
        return new Doc(collectionName);
    }

    /**
     * Creates a document from existing data.
     */
    public static Doc of(String collectionName, Map<String, Object> data) {
        return new Doc(collectionName, data);
    }

    /**
     * Creates an embedded object (no collection).
     */
    public static Doc object() {
        return new Doc(null);
    }

    /**
     * Creates a document from a BSON Document.
     */
    public static Doc fromBson(String collectionName, Document bson) {
        Doc doc = new Doc(collectionName);
        if (bson != null) {
            for (Map.Entry<String, Object> entry : bson.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Convert _id to id
                if ("_id".equals(key)) {
                    if (value instanceof ObjectId oid) {
                        doc.data.put("id", oid.toHexString());
                    } else {
                        doc.data.put("id", value);
                    }
                } else if (value instanceof Document nested) {
                    doc.data.put(key, fromBson(null, nested));
                } else if (value instanceof List<?> list) {
                    doc.data.put(key, convertList(list));
                } else if (value instanceof Date date) {
                    doc.data.put(key, LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                } else {
                    doc.data.put(key, value);
                }
            }
        }
        return doc;
    }

    private static List<?> convertList(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Document nested) {
                result.add(fromBson(null, nested));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    // ==================== Setters ====================

    /**
     * Sets a field value.
     */
    public Doc set(String key, Object value) {
        if (key.contains(".")) {
            setNested(key, value);
        } else {
            data.put(key, value);
        }
        return this;
    }

    private void setNested(String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            Object existing = current.get(parts[i]);
            if (existing instanceof Doc doc) {
                current = doc.data;
            } else if (existing instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) existing;
                current = map;
            } else {
                Map<String, Object> nested = new LinkedHashMap<>();
                current.put(parts[i], nested);
                current = nested;
            }
        }
        current.put(parts[parts.length - 1], value);
    }

    /**
     * Sets the ID field.
     */
    public Doc id(String id) {
        return set("id", id);
    }

    /**
     * Removes a field.
     */
    public Doc unset(String key) {
        data.remove(key);
        return this;
    }

    // ==================== Getters ====================

    /**
     * Gets a field value with automatic type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (key.contains(".")) {
            return (T) getNested(key);
        }
        return (T) data.get(key);
    }

    private Object getNested(String key) {
        String[] parts = key.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Doc doc) {
                current = doc.data.get(part);
            } else if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Gets a field with a default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets the document ID.
     */
    public String getId() {
        Object id = data.get("id");
        return id != null ? id.toString() : null;
    }

    /**
     * Gets a string field.
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets a string with default.
     */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an integer field.
     */
    public Integer getInt(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Number num) return num.intValue();
        return Integer.parseInt(value.toString());
    }

    /**
     * Gets an integer with default.
     */
    public int getInt(String key, int defaultValue) {
        Integer value = getInt(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a long field.
     */
    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        return Long.parseLong(value.toString());
    }

    /**
     * Gets a long with default.
     */
    public long getLong(String key, long defaultValue) {
        Long value = getLong(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a double field.
     */
    public Double getDouble(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Number num) return num.doubleValue();
        return Double.parseDouble(value.toString());
    }

    /**
     * Gets a double with default.
     */
    public double getDouble(String key, double defaultValue) {
        Double value = getDouble(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a boolean field.
     */
    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Gets a boolean with default.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a LocalDateTime field.
     */
    public LocalDateTime getDateTime(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        return null;
    }

    /**
     * Gets a list field.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof List<?> list) {
            return (List<T>) list;
        }
        return null;
    }

    /**
     * Gets a list with default.
     */
    public <T> List<T> getList(String key, List<T> defaultValue) {
        List<T> value = getList(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an embedded document.
     */
    public Doc getDoc(String key) {
        Object value = get(key);
        if (value == null) return null;
        if (value instanceof Doc doc) return doc;
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return Doc.of(null, map);
        }
        return null;
    }

    // ==================== Utilities ====================

    /**
     * Checks if a field exists.
     */
    public boolean has(String key) {
        return get(key) != null;
    }

    /**
     * Returns the collection name.
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Returns the raw data map.
     */
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(data);
    }

    /**
     * Returns all field names.
     */
    public Set<String> keys() {
        return data.keySet();
    }

    /**
     * Converts to BSON Document for MongoDB.
     */
    public Document toBson() {
        Document doc = new Document();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Convert id to _id
            if ("id".equals(key)) {
                if (value != null) {
                    String idStr = value.toString();
                    if (ObjectId.isValid(idStr)) {
                        doc.put("_id", new ObjectId(idStr));
                    } else {
                        doc.put("_id", idStr);
                    }
                }
            } else if (value instanceof Doc nested) {
                doc.put(key, nested.toBson());
            } else if (value instanceof List<?> list) {
                doc.put(key, convertListToBson(list));
            } else if (value instanceof LocalDateTime ldt) {
                doc.put(key, Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                doc.put(key, value);
            }
        }
        return doc;
    }

    private List<?> convertListToBson(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Doc nested) {
                result.add(nested.toBson());
            } else {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Converts to a POJO.
     */
    public <T> T as(Class<T> type) {
        return Mongo.getMapper().convertValue(toMap(), type);
    }

    @Override
    public String toString() {
        return "Doc{" + collectionName + ": " + data + "}";
    }
}
