# Server-Sent Events (SSE)

JWeb provides SSE support for real-time server-to-client streaming.

## Basic Usage

```java
app.get("/events", req -> {
    SseEmitter emitter = SseEmitter.create();

    // Send initial event
    emitter.send("Connected!");

    // Return the emitter as response
    return emitter.toResponse();
});
```

## Creating Emitters

```java
// Default timeout (30 seconds)
SseEmitter emitter = SseEmitter.create();

// Custom timeout (0 = no timeout)
SseEmitter emitterWithTimeout = SseEmitter.create(60000);  // 60 seconds
SseEmitter emitterNoTimeout = SseEmitter.create(0);        // No timeout
```

## Sending Events

### Simple Text

```java
SseEmitter emitter = SseEmitter.create();
emitter.send("Hello, World!");
```

### Structured Events

```java
SseEmitter emitter = SseEmitter.create();

SseEvent simpleEvent = SseEvent.of("New message received");
emitter.send(simpleEvent);

// With event name
SseEvent namedEvent = SseEvent.create()
    .name("message")
    .data("Hello!")
    .build();
emitter.send(namedEvent);

// With ID and retry
SseEvent fullEvent = SseEvent.create()
    .id("123")
    .name("update")
    .data("{\"count\": 42}")
    .retry(3000)  // Retry after 3 seconds on disconnect
    .build();
emitter.send(fullEvent);
```

### JSON Data

```java
SseEmitter emitter = SseEmitter.create();

Map<String, Object> data = Map.of(
    "type", "notification",
    "message", "You have a new message"
);
emitter.send(SseEvent.json(data));
```

### Keep-Alive Comments

```java
SseEmitter emitter = SseEmitter.create();

// Send a comment (doesn't trigger event on client)
emitter.sendComment("keep-alive");
```

## Completing Emitters

```java
SseEmitter emitter = SseEmitter.create();

// Normal completion
emitter.complete();

// Complete with error
emitter.completeWithError(new RuntimeException("Connection lost"));

// Check if completed
if (emitter.isCompleted()) {
    // Emitter is done
}
```

## Event Callbacks

```java
SseEmitter emitter = SseEmitter.create();
emitter.onComplete(e -> {
    System.out.println("Client disconnected");
    // Cleanup resources
});
emitter.onError(error -> {
    System.err.println("Error: " + error.getMessage());
});
```

## Broadcasting

Use `SseBroadcaster` to send events to multiple clients:

```java
// Create a broadcaster
SseBroadcaster broadcaster = new SseBroadcaster();

// Add clients
app.get("/events", req -> {
    SseEmitter emitter = SseEmitter.create();
    broadcaster.subscribe(emitter);
    return emitter.toResponse();
});

// Broadcast to all clients
broadcaster.broadcast("New update available!");
broadcaster.broadcast(SseEvent.create()
    .name("notification")
    .data("{\"message\": \"Hello everyone!\"}")
    .build()
);
```

## Real-Time Notifications Example

```java
@Component
public class NotificationRoutes implements JWebRoutes {
    private final SseBroadcaster notifications = new SseBroadcaster();

    @Override
    public void configure(JWeb app) {
        // SSE endpoint for receiving notifications
        app.get("/notifications/stream", req -> {
            SseEmitter emitter = SseEmitter.create(0);  // No timeout

            String userId = Auth.getPrincipal(req).getId();
            emitter.onComplete(e -> {
                System.out.println("User " + userId + " disconnected");
            });

            notifications.subscribe(emitter);
            emitter.send("Connected to notification stream");

            return emitter.toResponse();
        });

        // Endpoint to trigger a notification
        app.post("/notifications/send", req -> {
            String message = req.formParam("message");
            notifications.broadcast(SseEvent.create()
                .name("notification")
                .data(Map.of(
                    "message", message,
                    "timestamp", System.currentTimeMillis()
                ))
                .build()
            );
            return Response.json(Map.of("sent", true));
        });
    }
}
```

## Client-Side JavaScript

```javascript
// Connect to SSE endpoint
const eventSource = new EventSource('/events');

// Listen for messages
eventSource.onmessage = (event) => {
    console.log('Received:', event.data);
};

// Listen for specific event types
eventSource.addEventListener('notification', (event) => {
    const data = JSON.parse(event.data);
    showNotification(data.message);
});

// Handle errors
eventSource.onerror = (error) => {
    console.error('SSE error:', error);
};

// Close connection
eventSource.close();
```

## Live Updates Example

```java
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class LiveStatsRoutes implements JWebRoutes {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void configure(JWeb app) {
        app.get("/stats/live", req -> {
            SseEmitter emitter = SseEmitter.create(0);

            // Send stats every 5 seconds
            ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
                if (!emitter.isCompleted()) {
                    Map<String, Object> stats = Map.of(
                        "activeUsers", getActiveUserCount(),
                        "requestsPerSecond", getRequestRate(),
                        "timestamp", System.currentTimeMillis()
                    );
                    emitter.send(SseEvent.create()
                        .name("stats")
                        .data(stats)
                        .build()
                    );
                }
            }, 0, 5, TimeUnit.SECONDS);

            // Cleanup on disconnect
            emitter.onComplete(e -> task.cancel(true));

            return emitter.toResponse();
        });
    }

    private int getActiveUserCount() { return 0; }
    private double getRequestRate() { return 0; }
}
```

## Progress Updates Example

```java
app.post("/upload", req -> {
    SseEmitter emitter = SseEmitter.create(300000);  // 5 min timeout

    // Process upload in background
    Jobs.run(() -> {
        try {
            for (int i = 0; i <= 100; i += 10) {
                emitter.send(SseEvent.create()
                    .name("progress")
                    .data(String.valueOf(i))
                    .build()
                );
                Thread.sleep(500);  // Simulate processing
            }
            emitter.send(SseEvent.create()
                .name("complete")
                .data("{\"status\": \"success\"}")
                .build()
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        } finally {
            emitter.complete();
        }
    });

    return emitter.toResponse();
});
```
