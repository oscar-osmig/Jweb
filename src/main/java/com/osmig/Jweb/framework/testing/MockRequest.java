package com.osmig.Jweb.framework.testing;

import com.osmig.Jweb.framework.server.Request;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Mock request builder for testing JWeb route handlers.
 *
 * <p>Usage:</p>
 * <pre>
 * // Simple GET request
 * Request req = MockRequest.get("/users").build();
 *
 * // POST with body
 * Request req = MockRequest.post("/users")
 *     .body("{\"name\":\"John\"}")
 *     .contentType("application/json")
 *     .build();
 *
 * // With path params (set manually since no routing)
 * Request req = MockRequest.get("/users/123")
 *     .pathParam("id", "123")
 *     .build();
 *
 * // With query params
 * Request req = MockRequest.get("/search")
 *     .queryParam("q", "test")
 *     .queryParam("page", "1")
 *     .build();
 *
 * // With headers
 * Request req = MockRequest.get("/api/data")
 *     .header("Authorization", "Bearer token123")
 *     .header("Accept", "application/json")
 *     .build();
 * </pre>
 */
public class MockRequest {

    private String method = "GET";
    private String path = "/";
    private String body = "";
    private String contentType = "text/html";
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String[]> queryParams = new LinkedHashMap<>();
    private final Map<String, String> pathParams = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    private HttpSession session;

    private MockRequest() {
    }

    // ========== Factory Methods ==========

    public static MockRequest get(String path) {
        return new MockRequest().method("GET").path(path);
    }

    public static MockRequest post(String path) {
        return new MockRequest().method("POST").path(path);
    }

    public static MockRequest put(String path) {
        return new MockRequest().method("PUT").path(path);
    }

    public static MockRequest delete(String path) {
        return new MockRequest().method("DELETE").path(path);
    }

    public static MockRequest request(String method, String path) {
        return new MockRequest().method(method).path(path);
    }

    // ========== Builder Methods ==========

    public MockRequest method(String method) {
        this.method = method;
        return this;
    }

    public MockRequest path(String path) {
        this.path = path;
        return this;
    }

    public MockRequest body(String body) {
        this.body = body;
        return this;
    }

    public MockRequest contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public MockRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public MockRequest queryParam(String name, String value) {
        this.queryParams.put(name, new String[]{value});
        return this;
    }

    public MockRequest queryParams(String name, String... values) {
        this.queryParams.put(name, values);
        return this;
    }

    public MockRequest pathParam(String name, String value) {
        this.pathParams.put(name, value);
        return this;
    }

    public MockRequest attribute(String name, Object value) {
        this.attributes.put(name, value);
        return this;
    }

    public MockRequest session(HttpSession session) {
        this.session = session;
        return this;
    }

    public MockRequest json(String body) {
        this.body = body;
        this.contentType = "application/json";
        return this;
    }

    public MockRequest formData(Map<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        formData.forEach((key, value) -> {
            if (sb.length() > 0) sb.append("&");
            sb.append(key).append("=").append(value);
            this.queryParams.put(key, new String[]{value});
        });
        this.body = sb.toString();
        this.contentType = "application/x-www-form-urlencoded";
        return this;
    }

    // ========== Build ==========

    public Request build() {
        HttpServletRequest servletRequest = createMockServletRequest();
        Request request = new Request(servletRequest);
        request.setPathParams(pathParams);
        return request;
    }

    private HttpServletRequest createMockServletRequest() {
        return new HttpServletRequest() {
            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public String getRequestURI() {
                return path;
            }

            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer("http://localhost" + path);
            }

            @Override
            public String getQueryString() {
                if (queryParams.isEmpty()) return null;
                StringBuilder sb = new StringBuilder();
                queryParams.forEach((name, values) -> {
                    for (String value : values) {
                        if (sb.length() > 0) sb.append("&");
                        sb.append(name).append("=").append(value);
                    }
                });
                return sb.toString();
            }

            @Override
            public String getParameter(String name) {
                String[] values = queryParams.get(name);
                return values != null && values.length > 0 ? values[0] : null;
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return Collections.unmodifiableMap(queryParams);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(queryParams.keySet());
            }

            @Override
            public String[] getParameterValues(String name) {
                return queryParams.get(name);
            }

            @Override
            public String getHeader(String name) {
                return headers.get(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return Collections.enumeration(headers.keySet());
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                String value = headers.get(name);
                return value != null ? Collections.enumeration(List.of(value)) : Collections.emptyEnumeration();
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new StringReader(body));
            }

            @Override
            public ServletInputStream getInputStream() {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                return new ServletInputStream() {
                    @Override
                    public int read() {
                        return bais.read();
                    }

                    @Override
                    public boolean isFinished() {
                        return bais.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }
                };
            }

            @Override
            public HttpSession getSession() {
                return session != null ? session : new MockSession();
            }

            @Override
            public HttpSession getSession(boolean create) {
                if (session == null && create) {
                    session = new MockSession();
                }
                return session;
            }

            @Override
            public Object getAttribute(String name) {
                return attributes.get(name);
            }

            @Override
            public void setAttribute(String name, Object o) {
                attributes.put(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                attributes.remove(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return Collections.enumeration(attributes.keySet());
            }

            @Override
            public String getRemoteAddr() {
                return "127.0.0.1";
            }

            // Remaining methods return defaults
            @Override public String getAuthType() { return null; }
            @Override public Cookie[] getCookies() { return new Cookie[0]; }
            @Override public long getDateHeader(String name) { return -1; }
            @Override public int getIntHeader(String name) { return -1; }
            @Override public String getPathInfo() { return null; }
            @Override public String getPathTranslated() { return null; }
            @Override public String getContextPath() { return ""; }
            @Override public String getRemoteUser() { return null; }
            @Override public boolean isUserInRole(String role) { return false; }
            @Override public java.security.Principal getUserPrincipal() { return null; }
            @Override public String getRequestedSessionId() { return null; }
            @Override public String getServletPath() { return path; }
            @Override public boolean isRequestedSessionIdValid() { return true; }
            @Override public boolean isRequestedSessionIdFromCookie() { return false; }
            @Override public boolean isRequestedSessionIdFromURL() { return false; }
            @Override public String changeSessionId() { return session != null ? session.getId() : null; }
            @Override public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) { return false; }
            @Override public void login(String username, String password) {}
            @Override public void logout() {}
            @Override public Collection<jakarta.servlet.http.Part> getParts() { return List.of(); }
            @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
            @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
            @Override public String getCharacterEncoding() { return "UTF-8"; }
            @Override public void setCharacterEncoding(String env) {}
            @Override public int getContentLength() { return body.length(); }
            @Override public long getContentLengthLong() { return body.length(); }
            @Override public String getProtocol() { return "HTTP/1.1"; }
            @Override public String getScheme() { return "http"; }
            @Override public String getServerName() { return "localhost"; }
            @Override public int getServerPort() { return 8080; }
            @Override public String getRemoteHost() { return "localhost"; }
            @Override public Locale getLocale() { return Locale.getDefault(); }
            @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(List.of(Locale.getDefault())); }
            @Override public boolean isSecure() { return false; }
            @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
            @Override public int getRemotePort() { return 0; }
            @Override public String getLocalName() { return "localhost"; }
            @Override public String getLocalAddr() { return "127.0.0.1"; }
            @Override public int getLocalPort() { return 8080; }
            @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
            @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
            @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) { return null; }
            @Override public boolean isAsyncStarted() { return false; }
            @Override public boolean isAsyncSupported() { return false; }
            @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
            @Override public jakarta.servlet.DispatcherType getDispatcherType() { return jakarta.servlet.DispatcherType.REQUEST; }
            @Override public String getRequestId() { return "mock-" + System.currentTimeMillis(); }
            @Override public String getProtocolRequestId() { return ""; }
            @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
        };
    }
}
