package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.core.RawContent;
import com.osmig.Jweb.framework.util.Json;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes a handler-style result (what a {@code Guard}, a middleware or a
 * route returns) straight to the servlet response — for the places that
 * answer outside Spring MVC's return-value handling, like the guard
 * interceptor in front of {@code @REST} controllers.
 */
public final class ResponseWriter {

    private ResponseWriter() {}

    public static void write(Object result, HttpServletResponse response) throws IOException {
        // Text bodies always go out as UTF-8 — the container's default for a
        // writer is ISO-8859-1, which would garble any non-Latin JSON or HTML
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (result instanceof ResponseEntity<?> entity) {
            response.setStatus(entity.getStatusCode().value());
            entity.getHeaders().forEach((name, values) -> {
                if (name.equalsIgnoreCase("Content-Type")) return;
                values.forEach(v -> response.addHeader(name, v));
            });
            var contentType = entity.getHeaders().getContentType();
            if (contentType != null) response.setContentType(contentType.toString());
            Object body = entity.getBody();
            if (body == null) {
                response.flushBuffer();
                return;
            }
            if (body instanceof byte[] bytes) {
                if (contentType == null) response.setContentType("application/octet-stream");
                response.getOutputStream().write(bytes);
                return;
            }
            String text = bodyText(body, contentType != null ? null : response);
            response.getWriter().write(text);
            return;
        }
        response.setStatus(200);
        response.getWriter().write(bodyText(result, response));
    }

    /** The body as text, setting a content type on {@code response} when given. */
    private static String bodyText(Object body, HttpServletResponse response) {
        if (body instanceof RawContent raw) {
            if (response != null) response.setContentType(raw.isJson() ? "application/json" : "text/html;charset=UTF-8");
            return raw.toHtml();
        }
        if (body instanceof jweb.Element element) {
            if (response != null) response.setContentType("text/html;charset=UTF-8");
            return element.toHtml();
        }
        if (body instanceof String text) {
            if (response != null) {
                String trimmed = text.stripLeading();
                boolean json = trimmed.startsWith("{") || trimmed.startsWith("[");
                response.setContentType(json ? "application/json" : "text/html;charset=UTF-8");
            }
            return text;
        }
        if (response != null) response.setContentType("application/json");
        return Json.stringify(body);
    }
}
