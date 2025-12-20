package com.osmig.Jweb.framework.error;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.validation.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Centralized error handling for JWeb applications.
 *
 * <p>Provides methods to convert exceptions into HTTP responses,
 * either as HTML error pages or JSON error responses.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * // In middleware or error handler
 * try {
 *     return handler.handle(request);
 * } catch (JWebException e) {
 *     return ErrorHandler.toResponse(e, request.path());
 * } catch (Exception e) {
 *     return ErrorHandler.serverError(e, request.path());
 * }
 * </pre>
 */
public final class ErrorHandler {

    private ErrorHandler() {
        // Static utility class
    }

    // ========== JSON Responses ==========

    /**
     * Converts a JWebException to a JSON ResponseEntity.
     */
    public static ResponseEntity<String> toJsonResponse(JWebException e, String path) {
        ErrorResponse error = ErrorResponse.from(e, path);
        return ResponseEntity.status(e.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(error));
    }

    /**
     * Creates a JSON error response for validation failures.
     */
    public static ResponseEntity<String> validationJsonResponse(ValidationResult result, String path) {
        ErrorResponse error = ErrorResponse.validation(result.getAllErrors(), path);
        return ResponseEntity.status(422)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(error));
    }

    /**
     * Creates a generic JSON error response.
     */
    public static ResponseEntity<String> jsonResponse(HttpStatus status, String message, String path) {
        ErrorResponse error = ErrorResponse.of(status.value(), message, path);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.stringify(error));
    }

    // ========== HTML Responses ==========

    /**
     * Converts a JWebException to an HTML ResponseEntity.
     */
    public static ResponseEntity<String> toHtmlResponse(JWebException e) {
        return toHtmlResponse(e.getStatus(), e.getMessage());
    }

    /**
     * Creates an HTML error page response.
     */
    public static ResponseEntity<String> toHtmlResponse(HttpStatus status, String message) {
        Element page = errorPage(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(page.toHtml());
    }

    /**
     * Creates an HTML error page for validation failures.
     */
    public static ResponseEntity<String> validationHtmlResponse(ValidationResult result) {
        Element page = validationErrorPage(result);
        return ResponseEntity.status(422)
                .contentType(MediaType.TEXT_HTML)
                .body(page.toHtml());
    }

    // ========== Error Page Elements ==========

    /**
     * Creates a styled error page element.
     */
    public static Element errorPage(int statusCode, String title, String message) {
        String color = statusCode >= 500 ? "#dc3545" : "#fd7e14";

        return html(
            head(
                title(statusCode + " - " + title),
                style("""
                    body {
                        font-family: system-ui, -apple-system, sans-serif;
                        background: #f8f9fa;
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                    }
                    .error-container {
                        text-align: center;
                        padding: 40px;
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        max-width: 500px;
                    }
                    .error-code {
                        font-size: 72px;
                        font-weight: bold;
                        margin: 0;
                    }
                    .error-title {
                        font-size: 24px;
                        margin: 10px 0 20px;
                        color: #333;
                    }
                    .error-message {
                        color: #666;
                        margin-bottom: 30px;
                    }
                    .home-link {
                        display: inline-block;
                        padding: 12px 24px;
                        background: #007bff;
                        color: white;
                        text-decoration: none;
                        border-radius: 4px;
                    }
                    .home-link:hover {
                        background: #0056b3;
                    }
                    """)
            ),
            body(
                div().class_("error-container").children(
                    h1().class_("error-code").style("color: " + color).text(String.valueOf(statusCode)),
                    h2().class_("error-title").text(title),
                    p().class_("error-message").text(message),
                    a("/").class_("home-link").text("Go Home")
                )
            )
        );
    }

    /**
     * Creates an error page for validation failures.
     */
    public static Element validationErrorPage(ValidationResult result) {
        var errorList = ul();
        result.getAllErrors().forEach((field, messages) -> {
            messages.forEach(msg -> errorList.child(li().text(field + ": " + msg)));
        });

        return html(
            head(
                title("Validation Error"),
                style("""
                    body {
                        font-family: system-ui, -apple-system, sans-serif;
                        background: #f8f9fa;
                        margin: 0;
                        padding: 40px;
                    }
                    .error-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    h1 { color: #dc3545; margin-top: 0; }
                    ul { padding-left: 20px; }
                    li { color: #666; margin: 8px 0; }
                    .home-link {
                        display: inline-block;
                        margin-top: 20px;
                        padding: 10px 20px;
                        background: #007bff;
                        color: white;
                        text-decoration: none;
                        border-radius: 4px;
                    }
                    """)
            ),
            body(
                div().class_("error-container").children(
                    h1("Validation Error"),
                    p("Please correct the following errors:"),
                    errorList,
                    a("/").class_("home-link").text("Go Back")
                )
            )
        );
    }

    // ========== Response Selection ==========

    /**
     * Converts an exception to a response, choosing HTML or JSON based on Accept header.
     */
    public static ResponseEntity<String> toResponse(JWebException e, String path, String acceptHeader) {
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return toJsonResponse(e, path);
        }
        return toHtmlResponse(e);
    }

    /**
     * Creates a server error response from a generic exception.
     */
    public static ResponseEntity<String> serverError(Exception e, String path, String acceptHeader) {
        JWebException jwebEx = JWebException.serverError("An unexpected error occurred", e);
        return toResponse(jwebEx, path, acceptHeader);
    }

    // ========== Middleware Factory ==========

    /**
     * Creates a middleware that handles exceptions and converts them to responses.
     */
    public static com.osmig.Jweb.framework.middleware.Middleware errorHandling() {
        return (req, chain) -> {
            try {
                return chain.next();
            } catch (JWebException e) {
                return toResponse(e, req.path(), req.header("Accept"));
            } catch (Exception e) {
                return serverError(e, req.path(), req.header("Accept"));
            }
        };
    }

    /**
     * Creates a middleware with custom error page rendering.
     */
    public static com.osmig.Jweb.framework.middleware.Middleware errorHandling(
            Function<JWebException, Element> customErrorPage) {
        return (req, chain) -> {
            try {
                return chain.next();
            } catch (JWebException e) {
                String accept = req.header("Accept");
                if (accept != null && accept.contains("application/json")) {
                    return toJsonResponse(e, req.path());
                }
                Element page = customErrorPage.apply(e);
                return ResponseEntity.status(e.getStatus())
                        .contentType(MediaType.TEXT_HTML)
                        .body(page.toHtml());
            } catch (Exception e) {
                return serverError(e, req.path(), req.header("Accept"));
            }
        };
    }
}
