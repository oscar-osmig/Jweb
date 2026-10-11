package jweb;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * jweb.Metrics mirrors into a Micrometer registry once one is bound — the
 * same names, every counter increment, every timer sample, every gauge
 * (including the ones registered before the bind).
 */
class MicrometerSinkTest {

    @AfterEach
    void detach() {
        MicrometerSink.unbind();
    }

    @Test
    void countersAndTimersForwardAfterBinding() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerSink.bind(registry);

        Metrics.counter("test.orders.placed").increment();
        Metrics.counter("test.orders.placed").increment(2);
        assertEquals(3.0, registry.counter("test.orders.placed").count());

        Metrics.timer("test.render").record(5);
        Metrics.timer("test.render").record(() -> {});
        assertEquals(2, registry.timer("test.render").count());
        assertTrue(registry.timer("test.render").totalTime(TimeUnit.MILLISECONDS) >= 5.0);
    }

    @Test
    void gaugesAreReplayedAtBindAndRegisteredAfterIt() {
        AtomicInteger depth = new AtomicInteger(4);
        Metrics.gauge("test.queue.depth", depth::get);          // before the bind

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerSink.bind(registry);
        assertEquals(4.0, registry.find("test.queue.depth").gauge().value(), "replayed at bind");
        depth.set(9);
        assertEquals(9.0, registry.find("test.queue.depth").gauge().value(), "live, not a snapshot");

        Metrics.gauge("test.cache.size", () -> 12);               // after the bind
        assertEquals(12.0, registry.find("test.cache.size").gauge().value());
    }

    @Test
    void theBuiltInMiddlewareReachesTheRegistryToo() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerSink.bind(registry);
        Middleware middleware = Metrics.middleware();
        middleware.handle(null, () -> "ok");
        assertEquals(1.0, registry.counter("http.requests.total").count());
        assertEquals(1.0, registry.counter("http.requests.success").count());
        assertEquals(1, registry.timer("http.request.duration").count());
    }

    @Test
    void unbindingStopsTheMirror() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MicrometerSink.bind(registry);
        Metrics.counter("test.before").increment();
        MicrometerSink.unbind();
        assertNull(Metrics.sink());
        Metrics.counter("test.after").increment();
        assertEquals(1.0, registry.counter("test.before").count());
        assertNull(registry.find("test.after").counter(), "nothing recorded after unbind");
    }
}
