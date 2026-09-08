package com.osmig.Jweb.framework.api;

import com.osmig.Jweb.framework.JWeb;
import com.osmig.Jweb.framework.error.ErrorHandler;
import com.osmig.Jweb.framework.error.JWebException;
import com.osmig.Jweb.framework.routing.Guards;
import com.osmig.Jweb.framework.server.CurrentRequest;
import com.osmig.Jweb.framework.server.JWebController;
import com.osmig.Jweb.framework.server.ResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Applies the app's guards ({@code app.guard(pattern, ...)}) to what Spring
 * MVC dispatches itself — the {@code @REST} controllers under {@code /api/v*}
 * — so one guard covers pages, router routes and the API under a prefix.
 * {@code JWebController} runs the guards inside its own dispatch and is
 * skipped here.
 */
public final class GuardInterceptor implements HandlerInterceptor {

    private final JWeb app;

    public GuardInterceptor(JWeb app) {
        this.app = app;
    }

    @Override
    public boolean preHandle(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Object handler)
            throws Exception {
        Guards guards = app.getGuards();
        if (guards.isEmpty()) return true;
        if (handler instanceof HandlerMethod method && JWebController.class.isAssignableFrom(method.getBeanType())) {
            return true;
        }
        jweb.Request request = new jweb.Request(servletRequest);
        if (!guards.covers(request.path())) return true;

        Object answer;
        try {
            answer = guards.check(request);
        } catch (JWebException e) {
            answer = ErrorHandler.toJsonResponse(e, request.path());
        }
        if (answer == null) return true;
        ResponseWriter.write(answer, servletResponse);
        CurrentRequest.clear();
        return false;
    }
}
