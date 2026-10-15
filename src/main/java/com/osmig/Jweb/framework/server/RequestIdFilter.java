package com.osmig.Jweb.framework.server;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Gives every request an id before anything else sees it — JWeb routes,
 * page routes, {@code @REST} controllers, Spring's own error path — and
 * keeps it in the logging context ({@code MDC requestId}) for the request's
 * duration, so every log line the request writes carries it and the
 * response goes back with it in {@code X-Request-ID}.
 *
 * <p>A caller's own {@code X-Request-ID} is kept when it is sane, so an id
 * can follow a request through a gateway and several services.</p>
 */
public final class RequestIdFilter extends OncePerRequestFilter {

    /** The MDC key the id is kept under. */
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = jweb.Request.idFromHeader(request.getHeader(jweb.Request.ID_HEADER));
        if (id == null) id = jweb.Request.newId();
        request.setAttribute(jweb.Request.ID_ATTRIBUTE, id);
        response.setHeader(jweb.Request.ID_HEADER, id);
        String before = MDC.get(MDC_KEY);
        MDC.put(MDC_KEY, id);
        try {
            chain.doFilter(request, response);
        } finally {
            if (before == null) MDC.remove(MDC_KEY); else MDC.put(MDC_KEY, before);
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
