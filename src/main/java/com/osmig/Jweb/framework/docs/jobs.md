# Background Jobs

JWeb provides utilities for running background tasks and scheduled jobs.

## Basic Usage

```java
import jweb.Jobs;
import java.util.concurrent.CompletableFuture;

record User(String name) {}
record Report(String content) {}

void sendEmail(User user) {}
Report generateReport() { return new Report("Report contents"); }
void saveReport(Report report) {}

User user = new User("Ada");

// Fire and forget
Jobs.run(() -> sendEmail(user));

// With result
CompletableFuture<Report> future = Jobs.submit(() -> generateReport());
future.thenAccept(report -> saveReport(report));
```

## One-Time Execution

### Fire and Forget

```java
record User(String name) {}
void sendNotification(User user) {}
void updateAnalytics() {}

User user = new User("Ada");

Jobs.run(() -> {
    // This runs in background
    sendNotification(user);
    updateAnalytics();
});
```

### With CompletableFuture

```java
import java.util.concurrent.CompletableFuture;

record User(String name) {
    String getName() { return name; }
}
interface UserService {
    User fetchUser(String id);
}
UserService userService = id -> new User("Ada");
String userId = "42";

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
import java.util.concurrent.ScheduledFuture;

record User(String name) {}
void sendReminderEmail(User user) {}
void cleanupTempFiles() {}

User user = new User("Ada");

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
void cleanupExpiredSessions() {}
void syncDataWithExternalService() {}

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
import jweb.BackgroundTask;

record Report(String content) {}
Report generateLargeReport() { return new Report("Report contents"); }

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
record ImportRow(String data) {}
List<ImportRow> loadRecords() { return List.of(new ImportRow("a"), new ImportRow("b")); }
void processRecord(ImportRow record) {}

BackgroundTask<Integer> task = Jobs.trackWithProgress("Import Data", progress -> {
    List<ImportRow> records = loadRecords();
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
record Report(String content) {}
interface ReportService { Report generate(String type); }
ReportService reportService = type -> new Report("Report for " + type);

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
import jakarta.annotation.PreDestroy;

@PreDestroy
public void onShutdown() {
    Jobs.shutdown();
}
```

## Complete Example

```java
interface ReportService {
    Object fetchData(String type);
    Object process(Object data);
    String savePdf(Object report);
}

@Component
public class ReportRoutes implements JWebRoutes {
    private final ReportService reportService;

    public ReportRoutes(ReportService reportService) {
        this.reportService = reportService;
    }

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
