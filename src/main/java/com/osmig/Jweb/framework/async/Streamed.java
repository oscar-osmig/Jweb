package com.osmig.Jweb.framework.async;

import java.util.function.Supplier;

/**
 * @deprecated Moved to {@link jweb.Streamed} — a record, so this name cannot be
 *             a subtype: only the {@code of(...)} factory remains here and it
 *             returns {@code jweb.Streamed}. Declare variables as {@code jweb.Streamed}.
 */
@Deprecated
public final class Streamed {

    private Streamed() {}

    public static jweb.Streamed of(Supplier<? extends jweb.Element> page) {
        return jweb.Streamed.of(page);
    }
}
