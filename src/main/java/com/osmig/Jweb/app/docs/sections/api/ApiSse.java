package com.osmig.Jweb.app.docs.sections.api;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiSse {
    private ApiSse() {}

    public static Element render() {
        return section(
            h3Title("Server-Sent Events"),
            para("Push real-time updates to clients with SSE."),
            codeBlock("""
import jweb.SseEmitter;

// Create SSE endpoint — return the emitter straight from the route
app.get("/events", req -> {
    SseEmitter emitter = SseEmitter.create();

    Jobs.run(() -> {
        while (!emitter.isCompleted()) {
            emitter.send(getData());
            Thread.sleep(1000);
        }
    });

    return emitter;
});"""),

            h3Title("Event Types"),
            codeBlock("""
// Named event (client listens with addEventListener)
emitter.send(SseEvent.of("notification", jsonData));

// Default event (client listens with onmessage)
emitter.send(jsonData);

// With event ID (for reconnection)
emitter.send(SseEvent.create()
    .id("msg-123")
    .name("message")
    .data(data)
    .build());"""),

            h3Title("Client JavaScript"),
            codeBlock("""
// Using Actions DSL
script()
    .raw(\"\"\"
        const es = new EventSource('/events');
        es.addEventListener('notification', (e) => {
            const data = JSON.parse(e.data);
            showNotification(data);
        });
        es.onerror = () => console.log('Connection lost');
    \"\"\")
    .build();"""),

            h3Title("Broadcasting"),
            codeBlock("""
// Shared broadcaster for all connected clients
SseBroadcaster broadcaster = new SseBroadcaster();

// Broadcast to everyone
broadcaster.broadcast("New message!");
broadcaster.broadcast(SseEvent.of("notification", jsonData));

// Broadcast to a channel
broadcaster.broadcast("orders", SseEvent.json("orderUpdate", orderData));

// Client subscribes to a channel
app.get("/orders/updates", req -> {
    SseEmitter emitter = SseEmitter.create();
    broadcaster.subscribe("orders-" + req.query("id"), emitter);
    return emitter;
});"""),

            h3Title("Complete Example"),
            codeBlock("""
// Live dashboard updates
app.get("/dashboard/updates", req -> {
    SseEmitter emitter = SseEmitter.create();

    Jobs.run(() -> {
        emitter.send(SseEvent.json("init", getDashboardData()));

        while (!emitter.isCompleted()) {
            emitter.send(SseEvent.json("stats", getStats()));
            Thread.sleep(5000);
        }
    });

    return emitter;
});"""),

            docTip("SSE auto-reconnects on connection loss. Use event IDs for message replay.")
        );
    }
}
