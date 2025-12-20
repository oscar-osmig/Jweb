package com.osmig.Jweb.framework.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Wrapper around HTTP request providing convenient access methods.
 */
public class Request {

    private final HttpServletRequest servletRequest;
    private Map<String, String> pathParams = new HashMap<>();
    private String cachedBody;

    public Request(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    // === Path Parameters ===

    public String param(String name) { return pathParams.get(name); }
    public Long paramLong(String name) {
        String value = param(name);
        return value != null ? Long.parseLong(value) : null;
    }
    public Integer paramInt(String name) {
        String value = param(name);
        return value != null ? Integer.parseInt(value) : null;
    }
    public void setPathParams(Map<String, String> params) {
        this.pathParams = params != null ? params : new HashMap<>();
    }
    public Map<String, String> getPathParams() {
        return Collections.unmodifiableMap(pathParams);
    }

    // === Query Parameters ===

    public String query(String name) { return servletRequest.getParameter(name); }
    public Integer queryInt(String name) {
        String value = query(name);
        return value != null ? Integer.parseInt(value) : null;
    }
    public int queryInt(String name, int defaultValue) {
        String value = query(name);
        if (value == null || value.isBlank()) return defaultValue;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultValue; }
    }
    public Long queryLong(String name) {
        String value = query(name);
        return value != null ? Long.parseLong(value) : null;
    }
    public Boolean queryBool(String name) {
        String value = query(name);
        return value != null ? Boolean.parseBoolean(value) : null;
    }
    public Map<String, String[]> queryParams() { return servletRequest.getParameterMap(); }

    // === Headers ===

    public String header(String name) { return servletRequest.getHeader(name); }
    public Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = servletRequest.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.put(name, servletRequest.getHeader(name));
        }
        return headers;
    }

    // === Request Info ===

    public String method() { return servletRequest.getMethod(); }
    public String path() { return servletRequest.getRequestURI(); }
    public String url() {
        StringBuffer url = servletRequest.getRequestURL();
        String queryString = servletRequest.getQueryString();
        if (queryString != null) url.append("?").append(queryString);
        return url.toString();
    }
    public String queryString() { return servletRequest.getQueryString(); }
    public String contentType() { return servletRequest.getContentType(); }

    // === Body ===

    public String body() {
        if (cachedBody != null) return cachedBody;
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = servletRequest.getReader();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            cachedBody = sb.toString();
            return cachedBody;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read request body", e);
        }
    }

    // === Form Data ===

    public String formParam(String name) { return servletRequest.getParameter(name); }
    public Map<String, String[]> formParams() { return servletRequest.getParameterMap(); }

    // === Cookies ===

    /**
     * Gets a cookie value by name.
     *
     * @param name the cookie name
     * @return the cookie value, or null if not found
     */
    public String cookie(String name) {
        jakarta.servlet.http.Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null) return null;
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Gets all cookies as a map.
     *
     * @return map of cookie names to values
     */
    public Map<String, String> cookies() {
        Map<String, String> result = new HashMap<>();
        jakarta.servlet.http.Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                result.put(cookie.getName(), cookie.getValue());
            }
        }
        return result;
    }

    // === Session ===

    /**
     * Gets the HTTP session, creating one if necessary.
     *
     * @return the session
     */
    public HttpSession session() {
        return servletRequest.getSession(true);
    }

    /**
     * Gets the HTTP session.
     *
     * @param create whether to create a new session if none exists
     * @return the session, or null if none exists and create is false
     */
    public HttpSession session(boolean create) {
        return servletRequest.getSession(create);
    }

    /**
     * Gets a session attribute.
     *
     * @param name the attribute name
     * @param <T>  the expected type
     * @return the attribute value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> T sessionAttr(String name) {
        HttpSession session = servletRequest.getSession(false);
        return session != null ? (T) session.getAttribute(name) : null;
    }

    /**
     * Sets a session attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public void sessionAttr(String name, Object value) {
        servletRequest.getSession(true).setAttribute(name, value);
    }

    // === Request Attributes ===

    /**
     * Gets a request attribute.
     *
     * @param name the attribute name
     * @param <T>  the expected type
     * @return the attribute value, or null
     */
    @SuppressWarnings("unchecked")
    public <T> T attr(String name) {
        return (T) servletRequest.getAttribute(name);
    }

    /**
     * Sets a request attribute.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    public void attr(String name, Object value) {
        servletRequest.setAttribute(name, value);
    }

    // === Client Info ===

    /**
     * Gets the remote IP address.
     *
     * @return the client IP address
     */
    public String ip() {
        String forwarded = header("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }

    /**
     * Gets the User-Agent header.
     *
     * @return the user agent string
     */
    public String userAgent() {
        return header("User-Agent");
    }

    /**
     * Checks if the request is an AJAX/XHR request.
     *
     * @return true if AJAX request
     */
    public boolean isAjax() {
        return "XMLHttpRequest".equals(header("X-Requested-With"));
    }

    /**
     * Checks if the request accepts JSON.
     *
     * @return true if JSON is accepted
     */
    public boolean acceptsJson() {
        String accept = header("Accept");
        return accept != null && accept.contains("application/json");
    }

    /**
     * Checks if the request accepts HTML.
     *
     * @return true if HTML is accepted
     */
    public boolean acceptsHtml() {
        String accept = header("Accept");
        return accept == null || accept.contains("text/html") || accept.contains("*/*");
    }

    // === Underlying Request ===

    public HttpServletRequest raw() { return servletRequest; }
}
