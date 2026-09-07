package com.osmig.Jweb.framework.server;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @deprecated Moved to {@link jweb.Request} — same class, shorter import. This
 *             name is a compatibility alias only: handlers receive
 *             {@code jweb.Request}, so declare parameters as {@code jweb.Request}.
 */
@Deprecated
public class Request extends jweb.Request {

    public Request(HttpServletRequest servletRequest) {
        super(servletRequest);
    }
}
