package jweb;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Spans: what the framework does for a request, named and timed, nested, with
 * the error that ended it — so a trace of one slow page shows the SQL it ran,
 * the mail it sent, the job it enqueued.
 *
 * <p>The framework opens a span around every route and page render
 * ({@code jweb.route}), every statement {@code Sql} runs ({@code jweb.sql}),
 * every job ({@code jweb.job}), every {@code PubSub} delivery, every
 * {@code Mail.send}, every {@code Fetch} and every browser event
 * ({@code jweb.event}). An app opens its own:</p>
 *
 * <pre>{@code
 * try (Trace.Span span = Trace.span("report.render", "report", kind)) {
 *     span.tag("rows", rows.size());
 *     ...
 * }
 * Invoice invoice = Trace.in("invoice.build", () -> build(order));
 * }</pre>
 *
 * <p>Where spans go is the {@link Sink}'s business. With no sink they cost
 * nothing. {@link ObservationSink} bridges them into Micrometer Observation,
 * which an app has as soon as it adds Spring Boot's actuator — and with
 * {@code micrometer-tracing} and an exporter (OTLP, Zipkin) they become
 * spans in a real trace, with the request's trace id in every log line.</p>
 *
 * <p>Tags given when the span starts are <em>low cardinality</em>: a route
 * pattern, a job name, a topic — the kind a metrics backend can group by.
 * Tags added with {@link Span#tag} afterwards are <em>high cardinality</em>:
 * an id, a statement, a URL — kept on the trace, never a metric dimension.</p>
 */
public final class Trace {

    /** One unit of work: close it when the work ends. */
    public interface Span extends AutoCloseable {

        /** A detail of this work; high cardinality (a trace detail, not a metric dimension). */
        Span tag(String key, Object value);

        /** What ended this work badly. */
        Span error(Throwable error);

        @Override
        void close();
    }

    /** Where spans go. */
    public interface Sink {
        /** Starts a span; {@code tags} are low cardinality. */
        Span start(String name, Map<String, String> tags);
    }

    private static final Span NOOP = new Span() {
        @Override public Span tag(String key, Object value) { return this; }
        @Override public Span error(Throwable error) { return this; }
        @Override public void close() {}
        @Override public String toString() { return "Trace.Span(noop)"; }
    };

    private static volatile Sink sink;

    private Trace() {}

    /** Installs the sink; {@code null} drops every span. */
    public static void sink(Sink newSink) {
        sink = newSink;
    }

    public static Sink sink() {
        return sink;
    }

    /** Whether anything receives spans. */
    public static boolean active() {
        return sink != null;
    }

    /** Starts a span. */
    public static Span span(String name) {
        return span(name, Map.of());
    }

    /** Starts a span with one low-cardinality tag. */
    public static Span span(String name, String key, String value) {
        Map<String, String> tags = new LinkedHashMap<>(1);
        tags.put(key, value == null ? "" : value);
        return span(name, tags);
    }

    /** Starts a span with low-cardinality tags. */
    public static Span span(String name, Map<String, String> tags) {
        Sink s = sink;
        if (s == null) return NOOP;
        try {
            Span span = s.start(name, tags);
            return span == null ? NOOP : span;
        } catch (RuntimeException e) {
            return NOOP;   // a broken sink never breaks the work it watches
        }
    }

    /** Runs the work in a span; an exception is recorded on it and rethrown. */
    public static <T> T in(String name, Supplier<T> work) {
        return in(name, Map.of(), work);
    }

    /** Runs the work in a span with low-cardinality tags. */
    public static <T> T in(String name, Map<String, String> tags, Supplier<T> work) {
        try (Span span = span(name, tags)) {
            try {
                return work.get();
            } catch (RuntimeException | Error e) {
                span.error(e);
                throw e;
            }
        }
    }

    /** Runs the work in a span. */
    public static void in(String name, Runnable work) {
        in(name, Map.of(), () -> {
            work.run();
            return null;
        });
    }

    /** The span that records nothing. */
    public static Span noop() {
        return NOOP;
    }
}
