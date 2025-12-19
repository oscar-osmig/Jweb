package com.osmig.Jweb.framework.async;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a background task with status tracking.
 *
 * @param <T> the result type
 */
public class BackgroundTask<T> {

    private final String id;
    private final String name;
    private final Instant createdAt;
    private volatile TaskStatus status;
    private volatile T result;
    private volatile Throwable error;
    private volatile int progress;
    private volatile String progressMessage;
    private final CompletableFuture<T> future;

    public BackgroundTask(String name, CompletableFuture<T> future) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = Instant.now();
        this.status = TaskStatus.PENDING;
        this.progress = 0;
        this.future = future;

        future.whenComplete((res, err) -> {
            if (err != null) {
                this.error = err;
                this.status = TaskStatus.FAILED;
            } else {
                this.result = res;
                this.status = TaskStatus.COMPLETED;
                this.progress = 100;
            }
        });
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }

    public int getProgress() {
        return progress;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public boolean isDone() {
        return status == TaskStatus.COMPLETED || status == TaskStatus.FAILED;
    }

    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }

    /**
     * Updates the progress (0-100).
     *
     * @param progress the progress percentage
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        if (this.status == TaskStatus.PENDING) {
            this.status = TaskStatus.RUNNING;
        }
    }

    /**
     * Updates progress with a message.
     *
     * @param progress the progress percentage
     * @param message the progress message
     */
    public void setProgress(int progress, String message) {
        setProgress(progress);
        this.progressMessage = message;
    }

    /**
     * Marks the task as running.
     */
    public void markRunning() {
        this.status = TaskStatus.RUNNING;
    }

    /**
     * Cancels the task if possible.
     *
     * @return true if cancelled
     */
    public boolean cancel() {
        boolean cancelled = future.cancel(true);
        if (cancelled) {
            this.status = TaskStatus.CANCELLED;
        }
        return cancelled;
    }

    /**
     * Task status enum.
     */
    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
