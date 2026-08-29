package jweb;

/**
 * A server-sent-events stream, started with {@code SseEmitter.create()} and
 * returnable straight from any route handler.
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.sse.SseEmitter} —
 * {@code create()} always hands out this subtype, so either name works in
 * declarations.</p>
 */
@SuppressWarnings("deprecation")
public class SseEmitter extends com.osmig.Jweb.framework.sse.SseEmitter {

    /** Internal — start with {@code SseEmitter.create()}. */
    public SseEmitter(long timeoutMs) {
        super(timeoutMs);
    }
}
