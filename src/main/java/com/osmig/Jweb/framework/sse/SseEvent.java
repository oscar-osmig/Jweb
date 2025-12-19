package com.osmig.Jweb.framework.sse;

import com.osmig.Jweb.framework.util.Json;

/**
 * Server-Sent Event data.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Simple message
 * SseEvent.of("Hello");
 *
 * // Named event with ID
 * SseEvent.create()
 *     .id("123")
 *     .name("message")
 *     .data("Hello World")
 *     .build();
 *
 * // JSON data
 * SseEvent.json("update", Map.of("count", 42));
 * }</pre>
 *
 * @param id optional event ID (for client reconnection)
 * @param name optional event type name
 * @param data the event data
 * @param retry optional reconnection time in ms
 */
public record SseEvent(String id, String name, String data, Long retry) {

    /**
     * Creates a simple event with just data.
     */
    public static SseEvent of(String data) {
        return new SseEvent(null, null, data, null);
    }

    /**
     * Creates a named event.
     */
    public static SseEvent of(String name, String data) {
        return new SseEvent(null, name, data, null);
    }

    /**
     * Creates an event with JSON data.
     */
    public static SseEvent json(Object data) {
        return new SseEvent(null, null, Json.stringify(data), null);
    }

    /**
     * Creates a named event with JSON data.
     */
    public static SseEvent json(String name, Object data) {
        return new SseEvent(null, name, Json.stringify(data), null);
    }

    /**
     * Creates a builder for more complex events.
     */
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String data;
        private Long retry;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder data(Object data) {
            this.data = Json.stringify(data);
            return this;
        }

        public Builder retry(long retryMs) {
            this.retry = retryMs;
            return this;
        }

        public SseEvent build() {
            return new SseEvent(id, name, data, retry);
        }
    }
}
