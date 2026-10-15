package jweb;

import com.osmig.Jweb.framework.events.EventDispatch;
import com.osmig.Jweb.framework.state.StateManager;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import jweb.state.State;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static jweb.El.*;
import static jweb.State.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code jweb.Trace}: spans cost nothing without a sink, reach the sink
 * with their tags and errors, and the framework opens them where it works
 * — SQL, jobs, PubSub, mail, Fetch, browser events — and
 * {@code ObservationSink} turns them into Micrometer observations.
 */
class TraceTest {

    /** A span as the sink saw it: what it started with, what was added, how it ended. */
    record Seen(String name, Map<String, String> low, Map<String, String> high, List<Throwable> errors, boolean closed) {}

    static final class Recording implements Trace.Sink {
        final List<Seen> spans = new CopyOnWriteArrayList<>();
        final List<String> open = new CopyOnWriteArrayList<>();

        @Override
        public Trace.Span start(String name, Map<String, String> tags) {
            Map<String, String> high = new LinkedHashMap<>();
            List<Throwable> errors = new ArrayList<>();
            open.add(name);
            return new Trace.Span() {
                @Override public Trace.Span tag(String key, Object value) { high.put(key, String.valueOf(value)); return this; }
                @Override public Trace.Span error(Throwable error) { errors.add(error); return this; }
                @Override public void close() {
                    open.remove(name);
                    spans.add(new Seen(name, Map.copyOf(tags), Map.copyOf(high), List.copyOf(errors), true));
                }
            };
        }

        Seen only(String name) {
            List<Seen> named = spans.stream().filter(s -> s.name().equals(name)).toList();
            assertEquals(1, named.size(), name + " spans: " + spans);
            return named.get(0);
        }
    }

    Recording sink;

    @BeforeEach
    void record() {
        sink = new Recording();
        Trace.sink(sink);
    }

    @AfterEach
    void clean() {
        Trace.sink(null);
        StateManager.clearContext();
        MDC.clear();
    }

    @Test
    void spansAreFreeWithoutASinkAndReachTheSinkWithTagsAndErrors() {
        Trace.sink(null);
        assertFalse(Trace.active());
        assertSame(Trace.noop(), Trace.span("x"));
        assertEquals(3, (int) Trace.in("x", () -> 3));

        Trace.sink(sink);
        assertTrue(Trace.active());
        try (Trace.Span span = Trace.span("report", "kind", "monthly")) {
            span.tag("rows", 12);
            assertEquals(List.of("report"), sink.open);
        }
        Seen report = sink.only("report");
        assertEquals(Map.of("kind", "monthly"), report.low());
        assertEquals(Map.of("rows", "12"), report.high());
        assertTrue(report.errors().isEmpty());
        assertTrue(sink.open.isEmpty());

        IllegalStateException boom = assertThrows(IllegalStateException.class,
            () -> Trace.in("failing", () -> { throw new IllegalStateException("boom"); }));
        Seen failing = sink.only("failing");
        assertEquals(List.of(boom), failing.errors());
        assertTrue(failing.closed());

        Trace.in("runnable", () -> {});
        sink.only("runnable");

        // a sink that throws never breaks the work
        Trace.sink((name, tags) -> { throw new RuntimeException("sink down"); });
        assertEquals("fine", Trace.in("x", () -> "fine"));
    }

    @Test
    void sqlStatementsAreSpans() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:trace;DB_CLOSE_DELAY=-1");
        Sql.Database db = Sql.of(ds);
        db.execute("create table t (id int)");
        db.update("insert into t values (?)", 1);
        assertEquals(1L, db.query("select count(*) from t").scalar(Long.class));
        assertThrows(Sql.SqlException.class, () -> db.query("select nope from t").rows());

        List<Seen> sql = sink.spans.stream().filter(s -> s.name().equals("jweb.sql")).toList();
        assertEquals(4, sql.size(), sql.toString());
        assertEquals("create", sql.get(0).low().get("db.operation"));
        assertEquals("insert", sql.get(1).low().get("db.operation"));
        assertEquals("select", sql.get(2).low().get("db.operation"));
        assertEquals("insert into t values (?)", sql.get(1).high().get("db.statement"));
        assertEquals(1, sql.get(3).errors().size());
        assertTrue(sql.get(3).errors().get(0) instanceof Sql.SqlException);
        assertEquals("select", Sql.Database.operationOf("  SELECT * from x"));
        assertEquals("", Sql.Database.operationOf(null));
        db.execute("drop table t");
    }

    @Test
    void jobsAreSpansWithTheirNameAttemptAndFailure() {
        Jobs.store(new Jobs.Memory());
        try {
            record Payload(int n) {}
            Jobs.handle("trace.ok", Payload.class, p -> {});
            Jobs.handle("trace.bad", Payload.class, p -> { throw new IllegalStateException("no"); });
            Jobs.Job ok = Jobs.enqueue("trace.ok", new Payload(1));
            Jobs.enqueue("trace.bad", new Payload(2), Jobs.options().retries(0));
            assertEquals(2, Jobs.runPending());

            Seen good = sink.spans.stream().filter(s -> s.name().equals("jweb.job") && s.low().get("job").equals("trace.ok")).findFirst().orElseThrow();
            assertEquals(ok.id(), good.high().get("job.id"));
            assertEquals("1", good.high().get("job.attempt"));
            assertTrue(good.errors().isEmpty());
            Seen bad = sink.spans.stream().filter(s -> s.name().equals("jweb.job") && s.low().get("job").equals("trace.bad")).findFirst().orElseThrow();
            assertEquals(1, bad.errors().size());
        } finally {
            Jobs.store(new Jobs.Memory());
        }
    }

    @Test
    void pubSubDeliveriesMailAndFetchAreSpans() throws Exception {
        PubSub.reset();
        List<String> got = new CopyOnWriteArrayList<>();
        try (PubSub.Subscription sub = PubSub.subscribe("trace.topic", got::add)) {
            PubSub.publish("trace.topic", Map.of("a", 1));
            PubSub.awaitDelivery();
            assertEquals(1, got.size());
        }
        Seen delivery = sink.only("jweb.pubsub.deliver");
        assertEquals("trace.topic", delivery.low().get("topic"));
        assertEquals("1", delivery.high().get("subscribers"));

        Mail.outbox().clear();
        Mail.to("a@example.com").cc("b@example.com").subject("hi").text("there").send();
        Seen mail = sink.only("jweb.mail.send");
        assertEquals("2", mail.high().get("mail.recipients"));
        assertTrue(mail.errors().isEmpty());

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            byte[] body = "{\"ok\":true}".getBytes();
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(201, body.length);
            try (var out = ex.getResponseBody()) { out.write(body); }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/things?secret=1";
            FetchResult result = Fetch.post(url).json(Map.of("x", 1)).send();
            assertEquals(201, result.status());
        } finally {
            server.stop(0);
        }
        Seen fetch = sink.only("jweb.fetch");
        assertEquals("POST", fetch.low().get("http.method"));
        assertEquals("201", fetch.high().get("http.status"));
        assertTrue(fetch.high().get("http.url").endsWith("/things"), "the query string stays out of the trace: " + fetch.high());

        assertThrows(RuntimeException.class, () -> Fetch.get("http://127.0.0.1:1/nope").timeout(java.time.Duration.ofMillis(500)).send());
        List<Seen> fetches = sink.spans.stream().filter(s -> s.name().equals("jweb.fetch")).toList();
        assertEquals(1, fetches.get(1).errors().size());
    }

    static final Id BUMP = id("bump");

    static class Page implements Template {
        State<Integer> n;
        final AtomicReference<String> mdcInHandler = new AtomicReference<>();

        @Override
        public Element render() {
            n = useState(0);
            return div(live(n, v -> p("n=" + v)),
                button(BUMP, onClick(e -> { mdcInHandler.set(MDC.get(EventDispatch.MDC_CONTEXT)); n.set(n.get() + 1); }), "+"));
        }
    }

    @Test
    void browserEventsAreSpansAndCarryTheContextInTheLoggingContext() {
        Page page = new Page();
        try (RenderedPage rendered = JWebTest.render(page)) {
            RenderedPage.Result r = rendered.click(BUMP);
            assertTrue(r.ok(), r.error());
            assertEquals(rendered.contextId(), page.mdcInHandler.get(), "the context id is in the MDC while the handler runs");
            assertNull(MDC.get(EventDispatch.MDC_CONTEXT), "and gone after");
        }
        Seen event = sink.only("jweb.event");
        assertEquals("click", event.low().get("event"));
        assertNotNull(event.high().get("handler"));
        assertTrue(event.high().get("context").startsWith("ctx_"), event.high().toString());
    }

    // ==================== the Micrometer bridge ====================

    @Test
    void observationSinkTurnsSpansIntoObservations() {
        List<Observation.Context> started = new CopyOnWriteArrayList<>();
        List<Observation.Context> stopped = new CopyOnWriteArrayList<>();
        List<Observation.Context> scoped = new CopyOnWriteArrayList<>();
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            @Override public boolean supportsContext(Observation.Context context) { return true; }
            @Override public void onStart(Observation.Context context) { started.add(context); }
            @Override public void onStop(Observation.Context context) { stopped.add(context); }
            @Override public void onScopeOpened(Observation.Context context) { scoped.add(context); }
        });
        ObservationSink bound = ObservationSink.bind(registry);
        assertSame(bound, Trace.sink());
        assertSame(registry, bound.registry());

        IllegalStateException boom = new IllegalStateException("boom");
        try (Trace.Span span = Trace.span("jweb.job", Map.of("job", "welcome"))) {
            span.tag("job.id", "j-1").error(boom);
            assertEquals(1, started.size());
            assertEquals(1, scoped.size(), "the observation is in scope while the span is open");
            assertTrue(stopped.isEmpty());
        }
        assertEquals(1, stopped.size());
        Observation.Context context = stopped.get(0);
        assertEquals("jweb.job", context.getName());
        assertEquals("welcome", context.getLowCardinalityKeyValue("job").getValue());
        assertEquals("j-1", context.getHighCardinalityKeyValue("job.id").getValue());
        assertSame(boom, context.getError());

        ObservationSink.unbind();
        assertNull(Trace.sink());
    }
}
