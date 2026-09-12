package com.osmig.Jweb.app.docs.sections.api;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ApiJobs {
    private ApiJobs() {}

    public static Element render() {
        return section(
            h3Title("Background Jobs"),
            para("Run tasks asynchronously without blocking the request."),
            codeBlock("""
import jweb.Jobs;

record User(String email) {}
User createUser(Request req) { return new User("new@example.com"); }
void sendWelcomeEmail(User user) {}
void logAnalytics(String event) {}

User user = new User("a@example.com");

// Fire and forget
Jobs.run(() -> {
    sendWelcomeEmail(user);
    logAnalytics("signup");
});

// Return immediately, email sends in background
app.post("/register", req -> {
    User created = createUser(req);
    Jobs.run(() -> sendWelcomeEmail(created));
    return Response.redirect("/welcome");
});"""),

            h3Title("Scheduled Jobs"),
            codeBlock("""
void cleanupExpiredSessions() {}
void generateDailyReport() {}
void runNightlyBackup() {}

// Run every 5 minutes (named, so it can be cancelled)
Jobs.schedule("session-cleanup", Duration.ofMinutes(5), () -> {
    cleanupExpiredSessions();
});

// Run every hour
Jobs.schedule("hourly-report", Duration.ofHours(1), () -> {
    generateDailyReport();
});

// Initial delay, then repeat every 24 hours
Jobs.schedule("nightly-backup", Duration.ofHours(2), Duration.ofHours(24), () -> {
    runNightlyBackup();
});"""),

            h3Title("Delayed Execution"),
            codeBlock("""
record User(String email) {}
void sendReminderEmail(User user) {}
void expireTemporaryLink(String linkId) {}
User user = new User("a@example.com");
String linkId = "link-42";

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
import java.util.concurrent.CompletableFuture;

record Report(String title) {}
Report generateReport(String params) { return new Report(params); }
void emailReport(Report report) {}
String params = "q4-2026";

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
record Order(long id) {}
void processOrder(Order order) {}
void notifyAdmin(Exception e) {}
void logError(String message, Exception e) {}
Order order = new Order(1);

Jobs.run(() -> {
    try {
        processOrder(order);
    } catch (Exception e) {
        logError("Order processing failed", e);
        notifyAdmin(e);
    }
});

// Cancel or inspect scheduled jobs
Jobs.cancel("session-cleanup");
Jobs.isScheduled("session-cleanup");"""),

            docTip("Use Jobs for anything that doesn't need to block the HTTP response.")
        );
    }
}
