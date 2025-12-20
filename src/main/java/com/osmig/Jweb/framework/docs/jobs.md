# Background Jobs

JWeb provides utilities for running background tasks and scheduled jobs.

## Basic Usage

```java
import com.osmig.Jweb.framework.async.Jobs;

// Fire and forget
Jobs.run(() -> sendEmail(user));

// With result
CompletableFuture<Report> future = Jobs.submit(() -> generateReport());
future.thenAccept(report -> saveReport(report));
```

## One-Time Execution

### Fire and Forget

```java
Jobs.run(() -> {
    // This runs in background
    sendNotification(user);
    updateAnalytics();
});
```

### With CompletableFuture

```java
CompletableFuture<User> future = Jobs.submit(() -> {
    return userService.fetchUser(userId);
});

// Chain operations
future.thenAccept(user -> {
    System.out.println("Fetched: " + user.getName());
});

// Handle errors
future.exceptionally(error -> {
    System.err.println("Failed: " + error.getMessage());
    return null;
});
```

## Delayed Execution

Run a task after a delay:

```java
import java.time.Duration;

// Run after 30 seconds
Jobs.delay(Duration.ofSeconds(30), () -> {
    sendReminderEmail(user);
});

// Run after 5 minutes
ScheduledFuture<?> future = Jobs.delay(Duration.ofMinutes(5), () -> {
    cleanupTempFiles();
});

// Cancel if needed
future.cancel(false);
```

## Scheduled Jobs

Run recurring tasks:

```java
// Run every 5 minutes
Jobs.schedule("cleanup", Duration.ofMinutes(5), () -> {
    cleanupExpiredSessions();
});

// Run every hour with initial delay
Jobs.schedule("sync", Duration.ofMinutes(10), Duration.ofHours(1), () -> {
    syncDataWithExternalService();
});

// Check if job is scheduled
if (Jobs.isScheduled("cleanup")) {
    System.out.println("Cleanup job is running");
}

// Cancel a scheduled job
Jobs.cancel("cleanup");
```

## Tracked Tasks

Track long-running tasks:

```java
import com.osmig.Jweb.framework.async.BackgroundTask;

BackgroundTask<Report> task = Jobs.track("Generate Report", () -> {
    return generateLargeReport();
});

// Get task ID for status checks
String taskId = task.getId();

// Check status later
Jobs.getTask(taskId).ifPresent(t -> {
    System.out.println("Status: " + t.getStatus());
    if (t.isDone()) {
        Report report = (Report) t.getResult();
    }
});
```

## Progress Tracking

Track progress for long operations:

```java
BackgroundTask<Integer> task = Jobs.trackWithProgress("Import Data", progress -> {
    List<Record> records = loadRecords();
    int processed = 0;

    for (int i = 0; i < records.size(); i++) {
        processRecord(records.get(i));
        processed++;

        // Update progress (0-100)
        int percent = (i + 1) * 100 / records.size();
        progress.update(percent, "Processing record " + (i + 1));
    }

    return processed;
});

// Check progress
Jobs.getTask(task.getId()).ifPresent(t -> {
    System.out.println("Progress: " + t.getProgress() + "%");
    System.out.println("Message: " + t.getProgressMessage());
});
```

## Task Status API

Expose task status via API:

```java
app.post("/api/reports/generate", req -> {
    BackgroundTask<Report> task = Jobs.track("Generate Report", () -> {
        return reportService.generate(req.query("type"));
    });

    return Response.json(Map.of(
        "taskId", task.getId(),
        "status", "started"
    ));
});

app.get("/api/tasks/:id", req -> {
    String taskId = req.param("id");

    return Jobs.getTask(taskId)
        .map(task -> Response.json(Map.of(
            "id", task.getId(),
            "name", task.getName(),
            "status", task.getStatus(),
            "progress", task.getProgress(),
            "message", task.getProgressMessage(),
            "done", task.isDone()
        )))
        .orElse(Response.notFound("Task not found"));
});
```

## Cleanup

Clean up completed tasks:

```java
// Remove all completed tasks from tracking
Jobs.cleanupCompletedTasks();

// Schedule automatic cleanup
Jobs.schedule("task-cleanup", Duration.ofHours(1), () -> {
    Jobs.cleanupCompletedTasks();
});
```

## Graceful Shutdown

Shutdown job executors when application stops:

```java
@PreDestroy
public void onShutdown() {
    Jobs.shutdown();
}
```

## Complete Example

```java
@Component
public class ReportRoutes implements JWebRoutes {
    private final ReportService reportService;

    @Override
    public void configure(JWeb app) {
        // Start report generation
        app.post("/reports/generate", req -> {
            String type = req.formParam("type");

            BackgroundTask<String> task = Jobs.trackWithProgress(
                "Generate " + type + " Report",
                progress -> {
                    progress.update(0, "Initializing...");

                    // Fetch data
                    progress.update(20, "Fetching data...");
                    var data = reportService.fetchData(type);

                    // Process data
                    progress.update(50, "Processing...");
                    var report = reportService.process(data);

                    // Generate PDF
                    progress.update(80, "Generating PDF...");
                    String path = reportService.savePdf(report);

                    progress.update(100, "Complete!");
                    return path;
                }
            );

            return Response.json(Map.of("taskId", task.getId()));
        });

        // Check task status
        app.get("/reports/status/:taskId", req -> {
            String taskId = req.param("taskId");

            return Jobs.getTask(taskId)
                .map(task -> Response.json(Map.of(
                    "status", task.getStatus(),
                    "progress", task.getProgress(),
                    "message", task.getProgressMessage(),
                    "done", task.isDone(),
                    "result", task.isDone() ? task.getResult() : null
                )))
                .orElse(Response.notFound());
        });
    }
}
```

## Client-Side Polling

```javascript
async function generateReport(type) {
    // Start generation
    const response = await fetch('/reports/generate', {
        method: 'POST',
        body: new URLSearchParams({ type })
    });
    const { taskId } = await response.json();

    // Poll for status
    const pollStatus = async () => {
        const statusRes = await fetch(`/reports/status/${taskId}`);
        const status = await statusRes.json();

        updateProgressBar(status.progress);
        updateStatusMessage(status.message);

        if (status.done) {
            downloadReport(status.result);
        } else {
            setTimeout(pollStatus, 1000);
        }
    };

    pollStatus();
}
```
