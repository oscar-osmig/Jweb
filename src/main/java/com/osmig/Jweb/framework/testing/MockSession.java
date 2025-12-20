package com.osmig.Jweb.framework.testing;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.*;

/**
 * Mock HTTP session for testing.
 *
 * <p>A simple in-memory session implementation for use in tests.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * MockSession session = new MockSession();
 * session.setAttribute("user", userPrincipal);
 *
 * Request req = MockRequest.get("/dashboard")
 *     .session(session)
 *     .build();
 * </pre>
 */
public class MockSession implements HttpSession {

    private final String id;
    private final long creationTime;
    private final Map<String, Object> attributes = new HashMap<>();
    private int maxInactiveInterval = 1800;
    private boolean invalidated = false;

    public MockSession() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
    }

    public MockSession(String id) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getCreationTime() {
        checkInvalidated();
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        checkInvalidated();
        return System.currentTimeMillis();
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        checkInvalidated();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkInvalidated();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkInvalidated();
        if (value == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkInvalidated();
        attributes.remove(name);
    }

    @Override
    public void invalidate() {
        invalidated = true;
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        checkInvalidated();
        return false;
    }

    /**
     * Checks if this session has been invalidated.
     */
    public boolean isInvalidated() {
        return invalidated;
    }

    private void checkInvalidated() {
        if (invalidated) {
            throw new IllegalStateException("Session has been invalidated");
        }
    }

    /**
     * Gets all attributes as a map.
     */
    public Map<String, Object> getAllAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
