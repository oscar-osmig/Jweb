package com.osmig.Jweb.framework.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central JSON utility class for JWeb framework.
 *
 * <p>Provides a fluent API for JSON serialization and deserialization using Jackson.</p>
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Serialize object to JSON string
 * String json = Json.stringify(myObject);
 *
 * // Parse JSON string to object
 * MyClass obj = Json.parse(jsonString, MyClass.class);
 *
 * // Parse to JsonNode for dynamic access
 * JsonNode node = Json.parseTree(jsonString);
 * String value = node.get("key").asText();
 *
 * // Build JSON objects programmatically
 * String json = Json.object()
 *     .put("name", "John")
 *     .put("age", 30)
 *     .put("active", true)
 *     .toString();
 *
 * // Build JSON arrays
 * String json = Json.array()
 *     .add("item1")
 *     .add("item2")
 *     .toString();
 * </pre>
 */
public final class Json {

    private static final ObjectMapper mapper = createMapper();

    private Json() {
        // Static utility class
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Don't fail on unknown properties (forward compatibility)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Don't fail on empty beans
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    /**
     * Gets the shared ObjectMapper instance.
     * Use this when you need more control over serialization.
     *
     * @return the ObjectMapper instance
     */
    public static ObjectMapper mapper() {
        return mapper;
    }

    // ========== Serialization ==========

    /**
     * Serializes an object to a JSON string.
     *
     * @param value the object to serialize
     * @return JSON string representation
     * @throws JsonException if serialization fails
     */
    public static String stringify(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Serializes an object to a pretty-printed JSON string.
     *
     * @param value the object to serialize
     * @return pretty-printed JSON string
     * @throws JsonException if serialization fails
     */
    public static String stringifyPretty(Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize object to JSON", e);
        }
    }

    // ========== Deserialization ==========

    /**
     * Parses a JSON string to an object of the specified type.
     *
     * @param json the JSON string
     * @param type the target class
     * @param <T>  the target type
     * @return the deserialized object
     * @throws JsonException if parsing fails
     */
    public static <T> T parse(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to parse JSON to " + type.getSimpleName(), e);
        }
    }

    /**
     * Parses a JSON string to an object using a TypeReference.
     * Use this for generic types like {@code List<MyClass>} or {@code Map<String, Object>}.
     *
     * <p>Example:</p>
     * <pre>
     * List&lt;User&gt; users = Json.parse(json, new TypeReference&lt;List&lt;User&gt;&gt;() {});
     * </pre>
     *
     * @param json          the JSON string
     * @param typeReference the type reference
     * @param <T>           the target type
     * @return the deserialized object
     * @throws JsonException if parsing fails
     */
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to parse JSON", e);
        }
    }

    /**
     * Parses a JSON string to a Map.
     *
     * @param json the JSON string
     * @return the parsed map
     * @throws JsonException if parsing fails
     */
    public static Map<String, Object> parseMap(String json) {
        return parse(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Parses a JSON string to a List.
     *
     * @param json the JSON string
     * @return the parsed list
     * @throws JsonException if parsing fails
     */
    public static List<Object> parseList(String json) {
        return parse(json, new TypeReference<List<Object>>() {});
    }

    /**
     * Parses a JSON string to a JsonNode tree for dynamic access.
     *
     * @param json the JSON string
     * @return the JsonNode tree
     * @throws JsonException if parsing fails
     */
    public static JsonNode parseTree(String json) {
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to parse JSON tree", e);
        }
    }

    /**
     * Safely parses a JSON string, returning Optional.empty() on failure.
     *
     * @param json the JSON string
     * @param type the target class
     * @param <T>  the target type
     * @return Optional containing the parsed object, or empty if parsing fails
     */
    public static <T> Optional<T> tryParse(String json, Class<T> type) {
        try {
            return Optional.ofNullable(mapper.readValue(json, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Safely parses a JSON string to a JsonNode, returning Optional.empty() on failure.
     *
     * @param json the JSON string
     * @return Optional containing the JsonNode, or empty if parsing fails
     */
    public static Optional<JsonNode> tryParseTree(String json) {
        try {
            return Optional.ofNullable(mapper.readTree(json));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ========== Node Building ==========

    /**
     * Creates a new empty ObjectNode for building JSON objects.
     *
     * <p>Example:</p>
     * <pre>
     * ObjectNode obj = Json.object()
     *     .put("type", "event")
     *     .put("count", 42);
     * String json = obj.toString();
     * </pre>
     *
     * @return a new ObjectNode
     */
    public static ObjectNode object() {
        return mapper.createObjectNode();
    }

    /**
     * Creates a new empty ArrayNode for building JSON arrays.
     *
     * <p>Example:</p>
     * <pre>
     * ArrayNode arr = Json.array()
     *     .add("item1")
     *     .add(42)
     *     .add(true);
     * String json = arr.toString();
     * </pre>
     *
     * @return a new ArrayNode
     */
    public static ArrayNode array() {
        return mapper.createArrayNode();
    }

    // ========== Utility Methods ==========

    /**
     * Converts an object to a JsonNode.
     *
     * @param value the object to convert
     * @return the JsonNode representation
     */
    public static JsonNode toNode(Object value) {
        return mapper.valueToTree(value);
    }

    /**
     * Converts a JsonNode to an object of the specified type.
     *
     * @param node the JsonNode
     * @param type the target class
     * @param <T>  the target type
     * @return the converted object
     * @throws JsonException if conversion fails
     */
    public static <T> T fromNode(JsonNode node, Class<T> type) {
        try {
            return mapper.treeToValue(node, type);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to convert JsonNode to " + type.getSimpleName(), e);
        }
    }

    /**
     * Checks if a string is valid JSON.
     *
     * @param json the string to check
     * @return true if the string is valid JSON
     */
    public static boolean isValid(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Escapes a string for use in JSON.
     * Handles quotes, backslashes, and control characters.
     *
     * @param s the string to escape
     * @return the escaped string (without surrounding quotes)
     */
    public static String escape(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Exception thrown when JSON processing fails.
     */
    public static class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public JsonException(String message) {
            super(message);
        }
    }
}
