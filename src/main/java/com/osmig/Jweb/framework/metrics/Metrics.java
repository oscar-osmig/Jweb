package com.osmig.Jweb.framework.metrics;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.middleware.Middleware;
import com.osmig.Jweb.framework.server.Response;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Application metrics collection and reporting.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Add metrics middleware
 * app.use(Metrics.middleware());
 *
 * // Setup metrics endpoint
 * Metrics.setupEndpoint(app);
 * // Creates: /metrics
 *
 * // Custom metrics
 * Metrics.counter("orders.created").increment();
 * Metrics.gauge("users.online", () -> onlineUsers.size());
 * Metrics.timer("payment.processing").record(() -> processPayment());
 * }</pre>
 *
 * <h2>Available Metrics</h2>
 * <ul>
 *   <li><b>Counters</b> - Monotonically increasing values</li>
 *   <li><b>Gauges</b> - Point-in-time values</li>
 *   <li><b>Timers</b> - Duration measurements</li>
 * </ul>
 */
public final class Metrics {

    private static final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private static final Map<String, Gauge> gauges = new ConcurrentHashMap<>();
    private static final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private static final Instant startTime = Instant.now();

    // Built-in metrics
    private static final Counter requestsTotal = counter("http.requests.total");
    private static final Counter requestsSuccess = counter("http.requests.success");
    private static final Counter requestsError = counter("http.requests.error");
    private static final Timer requestDuration = timer("http.request.duration");

    private Metrics() {}

    // ========== Counter ==========

    /**
     * Gets or creates a counter.
     *
     * @param name the metric name
     * @return the counter
     */
    public static Counter counter(String name) {
        return counters.computeIfAbsent(name, Counter::new);
    }

    /**
     * Counter metric - monotonically increasing value.
     */
    public static class Counter {
        private final String name;
        private final LongAdder value = new LongAdder();

        Counter(String name) {
            this.name = name;
        }

        public void increment() {
            value.increment();
        }

        public void increment(long delta) {
            value.add(delta);
        }

        public long get() {
            return value.sum();
        }

        public String getName() {
            return name;
        }
    }

    // ========== Gauge ==========

    /**
     * Registers a gauge with a value supplier.
     *
     * @param name the metric name
     * @param supplier the value supplier
     */
    public static void gauge(String name, java.util.function.Supplier<Number> supplier) {
        gauges.put(name, new Gauge(name, supplier));
    }

    /**
     * Gauge metric - point-in-time value.
     */
    public static class Gauge {
        private final String name;
        private final java.util.function.Supplier<Number> supplier;

        Gauge(String name, java.util.function.Supplier<Number> supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public Number get() {
            return supplier.get();
        }

        public String getName() {
            return name;
        }
    }

    // ========== Timer ==========

    /**
     * Gets or creates a timer.
     *
     * @param name the metric name
     * @return the timer
     */
    public static Timer timer(String name) {
        return timers.computeIfAbsent(name, Timer::new);
    }

    /**
     * Timer metric - tracks duration of operations.
     */
    public static class Timer {
        private final String name;
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTimeNanos = new LongAdder();
        private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong max = new AtomicLong(0);

        Timer(String name) {
            this.name = name;
        }

        /**
         * Records a duration in milliseconds.
         */
        public void record(long durationMs) {
            count.increment();
            long nanos = durationMs * 1_000_000;
            totalTimeNanos.add(nanos);
            updateMin(nanos);
            updateMax(nanos);
        }

        /**
         * Times a runnable operation.
         */
        public void record(Runnable operation) {
            long start = System.nanoTime();
            try {
                operation.run();
            } finally {
                long duration = System.nanoTime() - start;
                count.increment();
                totalTimeNanos.add(duration);
                updateMin(duration);
                updateMax(duration);
            }
        }

        /**
         * Times a supplier operation.
         */
        public <T> T record(java.util.function.Supplier<T> operation) {
            long start = System.nanoTime();
            try {
                return operation.get();
            } finally {
                long duration = System.nanoTime() - start;
                count.increment();
                totalTimeNanos.add(duration);
                updateMin(duration);
                updateMax(duration);
            }
        }

        private void updateMin(long value) {
            long current;
            do {
                current = min.get();
                if (value >= current) return;
            } while (!min.compareAndSet(current, value));
        }

        private void updateMax(long value) {
            long current;
            do {
                current = max.get();
                if (value <= current) return;
            } while (!max.compareAndSet(current, value));
        }

        public long getCount() { return count.sum(); }
        public double getMeanMs() {
            long c = count.sum();
            return c == 0 ? 0 : (totalTimeNanos.sum() / c) / 1_000_000.0;
        }
        public double getMinMs() {
            long m = min.get();
            return m == Long.MAX_VALUE ? 0 : m / 1_000_000.0;
        }
        public double getMaxMs() { return max.get() / 1_000_000.0; }
        public String getName() { return name; }
    }

    // ========== Middleware ==========

    /**
     * Creates middleware that collects HTTP metrics.
     *
     * @return the middleware
     */
    public static Middleware middleware() {
        return (req, chain) -> {
            long start = System.currentTimeMillis();
            try {
                Object result = chain.next();
                requestsTotal.increment();
                requestsSuccess.increment();
                requestDuration.record(System.currentTimeMillis() - start);
                return result;
            } catch (Exception e) {
                requestsTotal.increment();
                requestsError.increment();
                requestDuration.record(System.currentTimeMillis() - start);
                throw e;
            }
        };
    }

    // ========== Endpoint ==========

    /**
     * Gets all metrics as a map.
     *
     * @return metrics data
     */
    public static Map<String, Object> getAll() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Uptime
        result.put("uptime_seconds", java.time.Duration.between(startTime, Instant.now()).getSeconds());
        result.put("timestamp", Instant.now().toString());

        // Counters
        Map<String, Long> counterData = new LinkedHashMap<>();
        counters.forEach((name, counter) -> counterData.put(name, counter.get()));
        result.put("counters", counterData);

        // Gauges
        Map<String, Number> gaugeData = new LinkedHashMap<>();
        gauges.forEach((name, gauge) -> {
            try {
                gaugeData.put(name, gauge.get());
            } catch (Exception e) {
                gaugeData.put(name, -1);
            }
        });
        result.put("gauges", gaugeData);

        // Timers
        Map<String, Map<String, Object>> timerData = new LinkedHashMap<>();
        timers.forEach((name, timer) -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("count", timer.getCount());
            t.put("mean_ms", Math.round(timer.getMeanMs() * 100) / 100.0);
            t.put("min_ms", Math.round(timer.getMinMs() * 100) / 100.0);
            t.put("max_ms", Math.round(timer.getMaxMs() * 100) / 100.0);
            timerData.put(name, t);
        });
        result.put("timers", timerData);

        // JVM metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("memory_used_mb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        jvm.put("memory_total_mb", runtime.totalMemory() / (1024 * 1024));
        jvm.put("memory_max_mb", runtime.maxMemory() / (1024 * 1024));
        jvm.put("processors", runtime.availableProcessors());
        result.put("jvm", jvm);

        return result;
    }

    /**
     * Gets metrics in Prometheus format.
     *
     * @return Prometheus-formatted metrics
     */
    public static String getPrometheus() {
        StringBuilder sb = new StringBuilder();

        // Counters
        for (Counter counter : counters.values()) {
            String name = counter.getName().replace(".", "_");
            sb.append("# TYPE ").append(name).append(" counter\n");
            sb.append(name).append(" ").append(counter.get()).append("\n");
        }

        // Gauges
        for (Gauge gauge : gauges.values()) {
            String name = gauge.getName().replace(".", "_");
            sb.append("# TYPE ").append(name).append(" gauge\n");
            try {
                sb.append(name).append(" ").append(gauge.get()).append("\n");
            } catch (Exception ignored) {}
        }

        // Timers
        for (Timer timer : timers.values()) {
            String name = timer.getName().replace(".", "_");
            sb.append("# TYPE ").append(name).append("_count counter\n");
            sb.append(name).append("_count ").append(timer.getCount()).append("\n");
            sb.append("# TYPE ").append(name).append("_mean_ms gauge\n");
            sb.append(name).append("_mean_ms ").append(timer.getMeanMs()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Sets up the metrics endpoint.
     *
     * @param app the JWeb application
     */
    public static void setupEndpoint(JWeb app) {
        app.get("/metrics", req -> {
            String accept = req.header("Accept");
            if (accept != null && accept.contains("text/plain")) {
                return Response.text(getPrometheus());
            }
            return Response.json(getAll());
        });
    }

    /**
     * Clears all metrics.
     */
    public static void clear() {
        counters.clear();
        gauges.clear();
        timers.clear();
    }
}
