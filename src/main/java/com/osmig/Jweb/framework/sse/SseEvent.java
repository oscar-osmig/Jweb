package com.osmig.Jweb.framework.sse;

/**
 * @deprecated Moved to {@link jweb.SseEvent} — a record, so this name cannot be
 *             a subtype: only the factories remain here and they return
 *             {@code jweb.SseEvent}. Declare variables as {@code jweb.SseEvent}.
 */
@Deprecated
public final class SseEvent {

    private SseEvent() {}

    public static jweb.SseEvent of(String data) { return jweb.SseEvent.of(data); }

    public static jweb.SseEvent of(String name, String data) { return jweb.SseEvent.of(name, data); }

    public static jweb.SseEvent json(Object data) { return jweb.SseEvent.json(data); }

    public static jweb.SseEvent json(String name, Object data) { return jweb.SseEvent.json(name, data); }

    public static jweb.SseEvent.Builder create() { return jweb.SseEvent.create(); }
}
