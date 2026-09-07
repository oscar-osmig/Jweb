package com.osmig.Jweb.framework.async;

import java.util.concurrent.CompletableFuture;

/**
 * @deprecated Moved to {@link jweb.BackgroundTask} — same class, shorter import.
 *             {@code Jobs.submit(...)} returns {@code jweb.BackgroundTask}.
 */
@Deprecated
public class BackgroundTask<T> extends jweb.BackgroundTask<T> {

    public BackgroundTask(String name, CompletableFuture<T> future) {
        super(name, future);
    }
}
