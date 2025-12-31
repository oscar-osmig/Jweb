package com.osmig.Jweb.app.docs.sections.api;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiJobs {
    private ApiJobs() {}

    public static Element render() {
        return section(
            h3Title("Background Jobs"),
            para("Run tasks asynchronously without blocking the request."),
            codeBlock("""
import com.osmig.Jweb.framework.async.Jobs;

// Fire and forget
Jobs.run(() -> {
    sendWelcomeEmail(user);
    logAnalytics(event);
});

// Return immediately, email sends in background
app.post("/register", req -> {
    User user = createUser(req);
    Jobs.run(() -> sendWelcomeEmail(user));
    return Response.redirect("/welcome");
});"""),

            h3Title("Scheduled Jobs"),
            codeBlock("""
// Run every 5 minutes
Jobs.every(Duration.ofMinutes(5), () -> {
    cleanupExpiredSessions();
});

// Run every hour
Jobs.every(Duration.ofHours(1), () -> {
    generateDailyReport();
});

// Run at specific time (cron-like)
Jobs.daily(LocalTime.of(2, 0), () -> {
    runNightlyBackup();
});"""),

            h3Title("Delayed Execution"),
            codeBlock("""
// Run after delay
Jobs.delay(Duration.ofSeconds(30), () -> {
    sendReminderEmail(user);
});

// Run after 1 hour
Jobs.delay(Duration.ofHours(1), () -> {
    expireTemporaryLink(linkId);
});"""),

            h3Title("With Result"),
            codeBlock("""
// Get future result
CompletableFuture<Report> future = Jobs.submit(() -> {
    return generateReport(params);
});

// Use when ready
future.thenAccept(report -> {
    emailReport(report);
});

// Or block and wait
Report report = future.get();"""),

            h3Title("Error Handling"),
            codeBlock("""
Jobs.run(() -> {
    try {
        processOrder(order);
    } catch (Exception e) {
        log.error("Order processing failed", e);
        notifyAdmin(e);
    }
});

// With retry
Jobs.retry(3, Duration.ofSeconds(5), () -> {
    callExternalApi(data);
});"""),

            docTip("Use Jobs for anything that doesn't need to block the HTTP response.")
        );
    }
}
