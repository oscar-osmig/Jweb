package com.osmig.Jweb.framework.server;

import jakarta.servlet.http.HttpServletRequest;
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

    // === Underlying Request ===

    public HttpServletRequest raw() { return servletRequest; }
}
