package com.osmig.Jweb.framework.core;

import java.util.function.Supplier;

/**
 * @deprecated Moved to {@link jweb.ErrorBoundary} — same class, shorter import.
 *             {@code ErrorBoundary.of(...)} returns {@code jweb.ErrorBoundary};
 *             only the static factories still resolve through this name.
 */
@Deprecated
public class ErrorBoundary extends jweb.ErrorBoundary {

    private ErrorBoundary(Supplier<? extends jweb.Element> content) {
        super(content);
    }
}
