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

String getData() { return "tick"; }

// Create SSE endpoint — return the emitter straight from the route
app.get("/events", req -> {
    SseEmitter emitter = SseEmitter.create();

    Jobs.run(() -> {
        while (!emitter.isCompleted()) {
            emitter.send(getData());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    });

    return emitter;
});"""),

            h3Title("Event Types"),
            codeBlock("""
SseEmitter emitter = SseEmitter.create();
String jsonData = "{count: 1}";
String data = "hello";

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
            para("The sse(...) builder is the EventSource client — on(name, ...) "
                 + "for named events, onMessage(...) for unnamed ones."),
            codeBlock("""
import static jweb.Js.*;
import static jweb.js.JSJson.parse;

inlineScript(actions().does(
    sse("/events")
        .on("notification", callback("e")
            .var_("data", parse(v("e").dot("data")))
            .call("showNotification", v("data")))
        .onError(callback().log("Connection lost"))
        .toVal()
).build())"""),

            h3Title("Broadcasting"),
            codeBlock("""
String jsonData = "{count: 1}";
Map<String, Object> orderData = Map.of("id", 42);

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
Map<String, Object> getDashboardData() { return Map.of(); }
Map<String, Object> getStats() { return Map.of(); }

// Live dashboard updates
app.get("/dashboard/updates", req -> {
    SseEmitter emitter = SseEmitter.create();

    Jobs.run(() -> {
        emitter.send(SseEvent.json("init", getDashboardData()));

        while (!emitter.isCompleted()) {
            emitter.send(SseEvent.json("stats", getStats()));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    });

    return emitter;
});"""),

            docTip("SSE auto-reconnects on connection loss. Use event IDs for message replay.")
        );
    }
}
