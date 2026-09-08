package com.osmig.Jweb.framework.api;

import jweb.UploadedFile;
import jweb.api.Body;
import jweb.api.Cookie;
import jweb.api.Header;
import jweb.api.Param;
import jweb.api.Query;
import jweb.api.Upload;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@code jweb.api} parameter annotations resolve through
 * {@link JWebArgumentResolver}; {@code @Component} and {@code @Value} are
 * real Spring meta-annotations.
 */
class ApiAnnotationsTest {

    enum Sort { ASC, DESC }

    record User(String name, int age) {}

    @SuppressWarnings("unused")
    static class Api {
        public void byId(@Param int id) {}
        public void named(@Param("userId") long user, @Param String slug) {}
        public void search(@Query String q, @Query(value = "limit", defaultValue = "10") int limit,
                           @Query Optional<Sort> sort, @Query List<Integer> ids, @Query(required = false) String note,
                           @Query boolean flag) {}
        public void body(@Body User user) {}
        public void bodyMap(@Body Map<String, Object> data) {}
        public void bodyText(@Body String raw) {}
        public void bodyOptional(@Body(required = false) Optional<User> user) {}
        public void header(@Header("User-Agent") String agent, @Header(value = "X-Trace", required = false) String trace) {}
        public void cookie(@Cookie("session") String session) {}
        public void upload(@Upload("file") UploadedFile file) {}
        public void request(jweb.Request req) {}
    }

    private static final JWebArgumentResolver RESOLVER = new JWebArgumentResolver();

    private static MethodParameter param(String method, int index) throws NoSuchMethodException {
        Method m = null;
        for (Method candidate : Api.class.getDeclaredMethods()) {
            if (candidate.getName().equals(method)) { m = candidate; break; }
        }
        if (m == null) throw new NoSuchMethodException(method);
        MethodParameter p = new MethodParameter(m, index);
        p.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        return p;
    }

    private static Object resolve(MethodParameter p, MockHttpServletRequest request) throws Exception {
        return RESOLVER.resolveArgument(p, null, new ServletWebRequest(request), null);
    }

    private static MockHttpServletRequest get(String... query) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/x");
        for (int i = 0; i < query.length; i += 2) req.addParameter(query[i], query[i + 1]);
        return req;
    }

    @Test
    void supportsExactlyTheJwebParameters() throws Exception {
        assertTrue(RESOLVER.supportsParameter(param("byId", 0)));
        assertTrue(RESOLVER.supportsParameter(param("body", 0)));
        assertTrue(RESOLVER.supportsParameter(param("request", 0)));
        Method plain = Object.class.getMethod("equals", Object.class);
        assertFalse(RESOLVER.supportsParameter(new MethodParameter(plain, 0)));
    }

    @Test
    void pathParamsConvertAndDefaultToTheParameterName() throws Exception {
        MockHttpServletRequest req = get();
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "42", "userId", "7", "slug", "hi"));
        assertEquals(42, resolve(param("byId", 0), req));
        assertEquals(7L, resolve(param("named", 0), req));
        assertEquals("hi", resolve(param("named", 1), req));

        MockHttpServletRequest bad = get();
        bad.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "x"));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> resolve(param("byId", 0), bad));
        assertEquals(400, e.getStatusCode().value());
        assertTrue(e.getReason().contains("'id'"), e.getReason());
    }

    @Test
    void queryParamsHandleDefaultsOptionalListsAndRequired() throws Exception {
        MockHttpServletRequest req = get("q", "java", "sort", "desc", "ids", "1", "flag", "on");
        req.addParameter("ids", "2");
        assertEquals("java", resolve(param("search", 0), req));
        assertEquals(10, resolve(param("search", 1), req));
        assertEquals(Optional.of(Sort.DESC), resolve(param("search", 2), req));
        assertEquals(List.of(1, 2), resolve(param("search", 3), req));
        assertNull(resolve(param("search", 4), req), "optional String absent → null");
        assertEquals(true, resolve(param("search", 5), req));

        MockHttpServletRequest empty = get();
        assertEquals(Optional.empty(), resolve(param("search", 2), empty));
        assertEquals(List.of(), resolve(param("search", 3), empty));
        assertEquals(false, resolve(param("search", 5), empty), "a missing primitive boolean is false");
        ResponseStatusException missing = assertThrows(ResponseStatusException.class, () -> resolve(param("search", 0), empty));
        assertEquals("Query parameter 'q' is required", missing.getReason());
    }

    @Test
    void bodyParsesJsonIntoRecordsMapsAndText() throws Exception {
        MockHttpServletRequest req = get();
        req.setContent("{\"name\":\"Ada\",\"age\":36}".getBytes());
        assertEquals(new User("Ada", 36), resolve(param("body", 0), req));

        MockHttpServletRequest map = get();
        map.setContent("{\"k\":1}".getBytes());
        assertEquals(Map.of("k", 1), resolve(param("bodyMap", 0), map));

        MockHttpServletRequest text = get();
        text.setContent("raw text".getBytes());
        assertEquals("raw text", resolve(param("bodyText", 0), text));

        MockHttpServletRequest none = get();
        none.setContent(new byte[0]);
        assertEquals(Optional.empty(), resolve(param("bodyOptional", 0), none));
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> resolve(param("body", 0), none));
        assertEquals("Request body is required", e.getReason());

        MockHttpServletRequest broken = get();
        broken.setContent("{not json".getBytes());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> resolve(param("body", 0), broken)).getStatusCode().value());
    }

    @Test
    void headersAndCookies() throws Exception {
        MockHttpServletRequest req = get();
        req.addHeader("User-Agent", "curl");
        req.setCookies(new jakarta.servlet.http.Cookie("session", "abc"));
        assertEquals("curl", resolve(param("header", 0), req));
        assertNull(resolve(param("header", 1), req));
        assertEquals("abc", resolve(param("cookie", 0), req));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> resolve(param("cookie", 0), get())).getStatusCode().value());
    }

    @Test
    void uploadsWrapMultipartFilesAndNeverReturnNull() throws Exception {
        MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
        req.addFile(new MockMultipartFile("file", "report.pdf", "application/pdf", "pdf-bytes".getBytes()));
        UploadedFile file = (UploadedFile) resolve(param("upload", 0), req);
        assertEquals("report.pdf", file.getFilename());
        assertEquals("pdf", file.getExtension());
        assertArrayEquals("pdf-bytes".getBytes(), file.getBytes());

        UploadedFile missing = (UploadedFile) resolve(param("upload", 0), new MockMultipartHttpServletRequest());
        assertTrue(missing.isEmpty());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> resolve(param("upload", 0), get())).getStatusCode().value());
    }

    @Test
    void jwebRequestIsInjectable() throws Exception {
        MockHttpServletRequest req = get("q", "x");
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "9"));
        jweb.Request request = (jweb.Request) resolve(param("request", 0), req);
        assertEquals("/api/v1/x", request.path());
        assertEquals("x", request.query("q"));
        assertEquals("9", request.param("id"));
    }

    // ==================== @Component / @Value ====================

    @Test
    void jwebComponentIsFoundByComponentScanning() {
        var scanner = new ClassPathScanningCandidateComponentProvider(true);
        var names = scanner.findCandidateComponents("com.osmig.Jweb.app.api").stream()
            .map(bd -> bd.getBeanClassName()).toList();
        assertTrue(names.contains("com.osmig.Jweb.app.api.AdminApi"), names.toString());
        assertTrue(names.contains("com.osmig.Jweb.app.api.MessageStore"), names.toString());
    }

    @jweb.api.Component
    static class Holder {
        @jweb.api.Value("${demo.name:none}") String name;
        final long timeout;
        Holder(@jweb.api.Value("${demo.timeout:5}") long timeout) { this.timeout = timeout; }
    }

    @Test
    void jwebValueInjectsProperties() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of("demo.name", "Ada")));
            ctx.register(PropertySourcesPlaceholderConfigurer.class, Holder.class);
            ctx.refresh();
            Holder holder = ctx.getBean(Holder.class);
            assertEquals("Ada", holder.name);
            assertEquals(5L, holder.timeout, "the ${...:default} syntax works through the alias");
        }
    }
}
