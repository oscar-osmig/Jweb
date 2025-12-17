package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.server.Request;

/**
 * Functional interface for route handlers.
 *
 * Handlers receive a Request and return:
 * - An Element (rendered to HTML)
 * - A String (sent as-is)
 * - An Object (serialized to JSON)
 */
@FunctionalInterface
public interface RouteHandler {
    Object handle(Request request);
}
