# Observability

Request ids on every line, structured logs when you want them, metrics, and traces of what
the framework did for a request — with nothing to add until you want the exports.

## Request ids

Every request gets an id before anything sees it: the caller's `X-Request-ID` when it sends
one (a gateway's, a mobile client's), a fresh one otherwise. The response carries it back in
`X-Request-ID`; every log line written during the request shows it; the error page and the
JSON error quote it.

```java
import jweb.Request;

String id = req.id();                      // the same for the whole request
```

```text
2026-10-14T20:11:03.412-07:00  INFO 4711 --- [MyApp] [nio-8080-exec-3] [7f3a9c21b4d8e05f6a12] jweb.Sql : …
```

The line pattern is Spring Boot's own with `logging.pattern.correlation` set by the framework
to `[requestId traceId] ` — the trace id appears when tracing is on, nothing on lines outside
a request. Set `logging.pattern.correlation` yourself to override, or
`jweb.observability.log-correlation: false` to leave Boot's default. A browser event or a
PubSub push runs with `contextId` (the page's render context) in the logging context instead.

## Structured logs

Spring Boot writes JSON when asked; the request id rides along as a field because it lives in
the MDC:

```yaml
logging:
  structured:
    format:
      console: ecs        # or logstash, gelf
```

## Access log

```java
import jweb.Middlewares;

app.use(Middlewares.logging());           // GET /orders/9 200 12ms, at INFO, with the request id
```

## Metrics

`Metrics.counter/gauge/timer` and `Metrics.middleware()`, mirrored into Micrometer when the
app has a `MeterRegistry` (the actuator). The framework records `http.requests.*`,
`jweb.jobs.*`, `jweb.mail.*`; see the architecture guide.

## Traces (`jweb.Trace`)

A span is a named, timed piece of work with tags and the error that ended it. The framework
opens one around:

| Span | Low-cardinality tags | High-cardinality tags |
|---|---|---|
| `jweb.route` — a route or page render | `http.method`, `route` (the pattern) | — |
| `jweb.sql` — one statement | `db.operation` (select, insert…) | `db.statement` |
| `jweb.job` — one job run | `job` (the name) | `job.id`, `job.attempt` |
| `jweb.pubsub.deliver` — one publish, delivered | `topic` | `subscribers` |
| `jweb.mail.send` | — | `mail.recipients` |
| `jweb.fetch` — an outgoing `Fetch` | `http.method` | `http.url` (no query), `http.status` |
| `jweb.event` — a browser event | `event` (click, submit…) | `handler`, `context` |

An app opens its own:

```java
import jweb.Trace;

record Order(long id) {}
Order order = new Order(9);

try (Trace.Span span = Trace.span("order.price", "kind", "retail")) {   // start tags: low cardinality
    span.tag("order.id", order.id());                                     // later tags: high cardinality
}
Order priced = Trace.in("order.price", () -> order);
Trace.in("cache.warm", () -> {});
```

Without a sink a span costs nothing. With Spring Boot's actuator the app has an
`ObservationRegistry` and the framework binds `ObservationSink` to it: each span is a
Micrometer `Observation` — a timer per name in the meter registry, and with
`micrometer-tracing` and an exporter a span in a real trace, nested under the HTTP request's,
with the trace id in every log line:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

Any `Trace.Sink` bean replaces the bridge; `jweb.observability.tracing: false` turns the
binding off.

## Configuration

| Property | Default | |
|---|---|---|
| `jweb.observability.request-id` | `true` | The filter that gives every request its id |
| `jweb.observability.log-correlation` | `true` | Put `[requestId traceId] ` on every log line of a request |
| `jweb.observability.tracing` | `true` | Bind `Trace` to the app's `ObservationRegistry` when it has one |
