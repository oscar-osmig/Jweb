package com.osmig.Jweb.framework.sse;

/**
 * @deprecated Moved to {@link jweb.SseBroadcaster} — same class, shorter import.
 */
@Deprecated
public class SseBroadcaster extends jweb.SseBroadcaster {

    public SseBroadcaster() {}

    public SseBroadcaster(long heartbeatIntervalMs) {
        super(heartbeatIntervalMs);
    }
}
