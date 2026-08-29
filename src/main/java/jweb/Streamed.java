package jweb;

import java.util.function.Supplier;

/**
 * Streaming SSR entry point — flushes the page shell instantly and streams
 * Suspense blocks as their data resolves:
 * {@code app.get("/page", req -> Streamed.of(() -> new BigPage().render()))}.
 *
 * <p>Facade for {@link com.osmig.Jweb.framework.async.Streamed} (a record,
 * so it cannot be extended like the other aliases).</p>
 */
@SuppressWarnings("deprecation")
public final class Streamed {

    private Streamed() {}

    public static com.osmig.Jweb.framework.async.Streamed of(Supplier<? extends Element> page) {
        return com.osmig.Jweb.framework.async.Streamed.of(page);
    }
}
