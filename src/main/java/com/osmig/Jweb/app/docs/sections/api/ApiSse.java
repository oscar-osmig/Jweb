package com.osmig.Jweb.app.docs.sections.api;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiSse {
    private ApiSse() {}

    public static Element render() {
        return section(
            h3Title("Server-Sent Events"),
            para("Push real-time updates to clients with SSE."),
            codeBlock("""
import com.osmig.Jweb.framework.http.SSE;

// Create SSE endpoint
app.get("/events", req -> SSE.stream(emitter -> {
    // Send events to client
    emitter.send("connected", "Hello!");

    // Send periodic updates
    while (!emitter.isClosed()) {
        emitter.send("update", getData());
        Thread.sleep(1000);
    }
}));"""),

            h3Title("Event Types"),
            codeBlock("""
// Named event (client listens with addEventListener)
emitter.send("notification", jsonData);

// Default event (client listens with onmessage)
emitter.send(jsonData);

// With event ID (for reconnection)
emitter.sendWithId("msg-123", "message", data);"""),

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
// Broadcast to all connected clients
SSE.broadcast("notifications", "New message!");

// Broadcast to specific topic
SSE.topic("orders").send("orderUpdate", orderData);

// Client subscribes to topic
app.get("/orders/updates", req -> {
    String orderId = req.query("id");
    return SSE.subscribe("orders-" + orderId);
});"""),

            h3Title("Complete Example"),
            codeBlock("""
// Live dashboard updates
app.get("/dashboard/updates", req -> SSE.stream(emitter -> {
    emitter.send("init", getDashboardData());

    while (!emitter.isClosed()) {
        var stats = getStats();
        emitter.send("stats", stats);
        Thread.sleep(5000);
    }
}));"""),

            docTip("SSE auto-reconnects on connection loss. Use event IDs for message replay.")
        );
    }
}
