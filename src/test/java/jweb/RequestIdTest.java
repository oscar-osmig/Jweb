package jweb;

import com.osmig.Jweb.framework.config.JWebEnvironmentPostProcessor;
import com.osmig.Jweb.framework.error.ErrorResponse;
import com.osmig.Jweb.framework.server.CurrentRequest;
import com.osmig.Jweb.framework.server.ErrorPage;
import com.osmig.Jweb.framework.server.RequestIdFilter;
import com.osmig.Jweb.framework.util.Json;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One id per request: made or accepted by the filter, read by
 * {@code Request.id()}, kept in the logging context, echoed in the response
 * header, printed on the error page and in the JSON error — and the console
 * pattern that puts it on every log line.
 */
class RequestIdTest {

    @AfterEach
    void clean() {
        CurrentRequest.clear();
        MDC.clear();
    }

    @Test
    void aRequestHasOneIdMadeOnceOrTakenFromTheCaller() {
        Request fresh = MockRequest.get("/").build();
        String id = fresh.id();
        assertTrue(id.matches("[0-9a-f]{20}"), id);
        assertEquals(id, fresh.id(), "stable for the request");
        assertEquals(id, fresh.attr(Request.ID_ATTRIBUTE));
        assertNotEquals(id, MockRequest.get("/").build().id());

        Request given = MockRequest.get("/").header("X-Request-ID", "gw-7f3a").build();
        assertEquals("gw-7f3a", given.id());

        Request junk = MockRequest.get("/").header("X-Request-ID", "has space").build();
        assertTrue(junk.id().matches("[0-9a-f]{20}"), "an unsafe caller id is replaced");
        assertNull(Request.idFromHeader("x".repeat(129)));
        assertNull(Request.idFromHeader("  "));
        assertNull(Request.idFromHeader("new\nline"));
        assertEquals("abc-123", Request.idFromHeader(" abc-123 "));
    }

    @Test
    void theFilterSetsTheIdBeforeAnythingAndClearsTheLoggingContextAfter() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/x");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();
        MockFilterChain chain = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) {
                seen.set(MDC.get("requestId"));
                assertEquals(seen.get(), new Request(req).id(), "Request.id() reads what the filter set");
            }
        });
        assertNull(MDC.get("requestId"));
        filter.doFilter(request, response, chain);
        assertNotNull(seen.get());
        assertEquals(seen.get(), response.getHeader("X-Request-ID"));
        assertEquals(seen.get(), request.getAttribute(Request.ID_ATTRIBUTE));
        assertNull(MDC.get("requestId"), "cleared after the request");

        MockHttpServletRequest withId = new MockHttpServletRequest("GET", "/x");
        withId.addHeader("X-Request-ID", "from-the-gateway");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilter(withId, response2, new MockFilterChain());
        assertEquals("from-the-gateway", response2.getHeader("X-Request-ID"));
    }

    @Test
    void theMiddlewareKeepsTheHeaderAndContextWhenThereIsNoFilter() throws Exception {
        Request req = MockRequest.get("/").header("X-Request-ID", "mw-1").build();
        AtomicReference<String> inside = new AtomicReference<>();
        Object result = Middlewares.requestId().handle(req, () -> {
            inside.set(MDC.get("requestId"));
            return "ok";
        });
        assertEquals("ok", result);
        assertEquals("mw-1", inside.get());
        assertEquals("mw-1", req.responseHeaders().get("X-Request-ID"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void errorsCarryTheId() {
        Request req = MockRequest.get("/orders/9").header("X-Request-ID", "err-42").build();
        CurrentRequest.set(req);

        ErrorResponse json = ErrorResponse.from(JWebException.notFound("No such order"), "/orders/9");
        assertEquals("err-42", json.getRequestId());
        Map<String, Object> parsed = Json.parseMap(Json.stringify(json));
        assertEquals("err-42", parsed.get("requestId"));
        assertEquals(404, parsed.get("status"));

        String html = ErrorPage.render(500, "Server Error", new IllegalStateException("boom")).toHtml();
        assertTrue(html.contains("Request ID: <code>err-42</code>"), html);

        CurrentRequest.clear();
        String outside = ErrorPage.render(500, "Server Error", new IllegalStateException("boom")).toHtml();
        assertFalse(outside.contains("Request ID"), "nothing to quote outside a request");
        assertNull(ErrorResponse.of(500, "x", "/").getRequestId());

        MDC.put("requestId", "from-mdc");
        assertEquals("from-mdc", ErrorResponse.of(500, "x", "/").getRequestId(), "a job's or socket's context works too");
    }

    @Test
    void theConsolePatternShowsTheIdOnlyWhenThereIsOne() {
        ch.qos.logback.classic.LoggerContext context = new ch.qos.logback.classic.LoggerContext();
        ch.qos.logback.classic.PatternLayout layout = new ch.qos.logback.classic.PatternLayout();
        layout.setContext(context);
        layout.getInstanceConverterMap().put("clr", org.springframework.boot.logging.logback.ColorConverter::new);
        // Spring Boot wraps the correlation pattern exactly like this in its console default
        layout.setPattern("%clr(" + JWebEnvironmentPostProcessor.CORRELATION_PATTERN + "){faint}%m");
        layout.start();
        assertTrue(layout.isStarted(), "the pattern parses");

        MDC.clear();
        assertEquals("hello", render(layout, context));
        MDC.put("requestId", "req-1");
        assertEquals("[req-1] hello", render(layout, context));
        MDC.put("traceId", "4bf92f3577b34da6a3ce929d0e0e4736");
        assertEquals("[req-1 4bf92f3577b34da6a3ce929d0e0e4736] hello", render(layout, context));
        MDC.remove("requestId");
        assertEquals("[4bf92f3577b34da6a3ce929d0e0e4736] hello", render(layout, context), "a trace without a request: a job, a socket");
    }

    private static String render(ch.qos.logback.classic.PatternLayout layout, ch.qos.logback.classic.LoggerContext context) {
        ch.qos.logback.classic.spi.LoggingEvent event = new ch.qos.logback.classic.spi.LoggingEvent(
            "jweb", context.getLogger("jweb"), ch.qos.logback.classic.Level.INFO, "hello", null, null);
        event.setMDCPropertyMap(new java.util.HashMap<>(MDC.getCopyOfContextMap() == null ? Map.of() : MDC.getCopyOfContextMap()));
        return layout.doLayout(event).replace("\u001b[2m", "").replace("\u001b[0;39m", "");
    }

    @Test
    void theEnvironmentPostProcessorSetsTheDefaultUnlessToldNotTo() {
        MockEnvironment env = new MockEnvironment();
        new JWebEnvironmentPostProcessor().postProcessEnvironment(env, null);
        assertEquals(JWebEnvironmentPostProcessor.CORRELATION_PATTERN, env.getProperty("logging.pattern.correlation"));
        new JWebEnvironmentPostProcessor().postProcessEnvironment(env, null);
        assertEquals(1, env.getPropertySources().stream().filter(s -> JWebEnvironmentPostProcessor.SOURCE.equals(s.getName())).count());

        MockEnvironment own = new MockEnvironment().withProperty("logging.pattern.correlation", "[%X{traceId}] ");
        new JWebEnvironmentPostProcessor().postProcessEnvironment(own, null);
        assertEquals("[%X{traceId}] ", own.getProperty("logging.pattern.correlation"), "the app's own setting wins (added last)");

        MockEnvironment off = new MockEnvironment().withProperty("jweb.observability.log-correlation", "false");
        new JWebEnvironmentPostProcessor().postProcessEnvironment(off, null);
        assertNull(off.getProperty("logging.pattern.correlation"));
    }
}
