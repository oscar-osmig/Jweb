package com.osmig.Jweb.framework.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Background job execution and scheduling.
 *
 * <h2>Run Once</h2>
 * <pre>{@code
 * // Fire and forget
 * Jobs.run(() -> sendEmail(user));
 *
 * // With result
 * CompletableFuture<Report> future = Jobs.submit(() -> generateReport());
 * future.thenAccept(report -> saveReport(report));
 * }</pre>
 *
 * <h2>Tracked Tasks</h2>
 * <pre>{@code
 * BackgroundTask<Report> task = Jobs.track("Generate Report", () -> {
 *     return generateLargeReport();
 * });
 *
 * // Check status later
 * Jobs.getTask(task.getId()).ifPresent(t -> {
 *     System.out.println("Status: " + t.getStatus());
 *     System.out.println("Progress: " + t.getProgress() + "%");
 * });
 * }</pre>
 *
 * <h2>Scheduled Jobs</h2>
 * <pre>{@code
 * // Run every 5 minutes
 * Jobs.schedule("cleanup", Duration.ofMinutes(5), () -> {
 *     cleanupTempFiles();
 * });
 *
 * // Run after delay
 * Jobs.delay(Duration.ofSeconds(30), () -> {
 *     sendReminderEmail();
 * });
 * }</pre>
 *
 * <h2>Progress Tracking</h2>
 * <pre>{@code
 * Jobs.trackWithProgress("Import Data", progress -> {
 *     List<Record> records = loadRecords();
 *     for (int i = 0; i < records.size(); i++) {
 *         processRecord(records.get(i));
 *         progress.update((i + 1) * 100 / records.size(), "Processing record " + (i + 1));
 *     }
 *     return records.size();
 * });
 * }</pre>
 */
public final class Jobs {

    private static final Logger log = LoggerFactory.getLogger(Jobs.class);

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final Map<String, BackgroundTask<?>> tasks = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    private Jobs() {}

    // ========== One-time Execution ==========

    /**
     * Runs a task asynchronously (fire and forget).
     *
     * @param task the task to run
     */
    public static void run(Runnable task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Background task failed", e);
            }
        });
    }

    /**
     * Submits a task and returns a future.
     *
     * @param task the task to run
     * @param <T> the result type
     * @return a CompletableFuture with the result
     */
    public static <T> CompletableFuture<T> submit(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }

    /**
     * Submits a task with no return value.
     *
     * @param task the task to run
     * @return a CompletableFuture that completes when done
     */
    public static CompletableFuture<Void> submit(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    // ========== Tracked Tasks ==========

    /**
     * Runs a tracked background task.
     *
     * @param name the task name
     * @param task the task to run
     * @param <T> the result type
     * @return the background task
     */
    public static <T> BackgroundTask<T> track(String name, Supplier<T> task) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.get();
            } catch (Exception e) {
                log.error("Tracked task '{}' failed", name, e);
                throw e;
            }
        }, executor);

        BackgroundTask<T> bgTask = new BackgroundTask<>(name, future);
        tasks.put(bgTask.getId(), bgTask);
        return bgTask;
    }

    /**
     * Runs a tracked task with progress reporting.
     *
     * @param name the task name
     * @param task the task that receives a progress reporter
     * @param <T> the result type
     * @return the background task
     */
    public static <T> BackgroundTask<T> trackWithProgress(String name, ProgressTask<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        BackgroundTask<T> bgTask = new BackgroundTask<>(name, future);
        tasks.put(bgTask.getId(), bgTask);

        executor.execute(() -> {
            bgTask.markRunning();
            try {
                T result = task.execute(new ProgressReporter(bgTask));
                future.complete(result);
            } catch (Exception e) {
                log.error("Tracked task '{}' failed", name, e);
                future.completeExceptionally(e);
            }
        });

        return bgTask;
    }

    /**
     * Gets a tracked task by ID.
     *
     * @param taskId the task ID
     * @return the task if found
     */
    public static Optional<BackgroundTask<?>> getTask(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    /**
     * Removes completed tasks from tracking.
     */
    public static void cleanupCompletedTasks() {
        tasks.entrySet().removeIf(e -> e.getValue().isDone());
    }

    // ========== Delayed Execution ==========

    /**
     * Runs a task after a delay.
     *
     * @param delay the delay before execution
     * @param task the task to run
     * @return a future that can be used to cancel
     */
    public static ScheduledFuture<?> delay(Duration delay, Runnable task) {
        return scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Delayed task failed", e);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    // ========== Scheduled Jobs ==========

    /**
     * Schedules a recurring job.
     *
     * @param name unique job name
     * @param interval the interval between executions
     * @param job the job to run
     */
    public static void schedule(String name, Duration interval, Runnable job) {
        schedule(name, Duration.ZERO, interval, job);
    }

    /**
     * Schedules a recurring job with initial delay.
     *
     * @param name unique job name
     * @param initialDelay delay before first execution
     * @param interval the interval between executions
     * @param job the job to run
     */
    public static void schedule(String name, Duration initialDelay, Duration interval, Runnable job) {
        // Cancel existing job with same name
        cancel(name);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("Running scheduled job: {}", name);
                job.run();
            } catch (Exception e) {
                log.error("Scheduled job '{}' failed", name, e);
            }
        }, initialDelay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);

        scheduledJobs.put(name, future);
        log.info("Scheduled job '{}' to run every {}", name, interval);
    }

    /**
     * Cancels a scheduled job.
     *
     * @param name the job name
     * @return true if cancelled
     */
    public static boolean cancel(String name) {
        ScheduledFuture<?> future = scheduledJobs.remove(name);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled scheduled job: {}", name);
            return true;
        }
        return false;
    }

    /**
     * Checks if a scheduled job exists.
     *
     * @param name the job name
     * @return true if exists and not cancelled
     */
    public static boolean isScheduled(String name) {
        ScheduledFuture<?> future = scheduledJobs.get(name);
        return future != null && !future.isCancelled();
    }

    /**
     * Shuts down all executors gracefully.
     */
    public static void shutdown() {
        log.info("Shutting down job executors...");
        executor.shutdown();
        scheduler.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ========== Helper Classes ==========

    /**
     * Functional interface for tasks with progress reporting.
     */
    @FunctionalInterface
    public interface ProgressTask<T> {
        T execute(ProgressReporter progress) throws Exception;
    }

    /**
     * Progress reporter for tracked tasks.
     */
    public static class ProgressReporter {
        private final BackgroundTask<?> task;

        ProgressReporter(BackgroundTask<?> task) {
            this.task = task;
        }

        /**
         * Updates progress (0-100).
         */
        public void update(int percent) {
            task.setProgress(percent);
        }

        /**
         * Updates progress with a message.
         */
        public void update(int percent, String message) {
            task.setProgress(percent, message);
        }
    }
}
