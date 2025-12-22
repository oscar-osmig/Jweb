package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.core.Element;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/**
 * Styled error page for development and production errors.
 */
public final class ErrorPage {

    private ErrorPage() {}

    public static Element render(int status, String title, Exception e) {
        String stackTrace = getStackTrace(e);

        return html(
            head(
                tag("meta", attrs().set("charset", "UTF-8")),
                tag("meta", attrs().set("name", "viewport")
                    .set("content", "width=device-width, initial-scale=1.0")),
                title(status + " - " + title),
                style(globalStyles())
            ),
            body(attrs().class_("error-body"),
                div(attrs().class_("error-container"),
                    // Error icon
                    div(attrs().class_("error-icon"),
                        text(status >= 500 ? "⚠" : "🔍")
                    ),
                    // Status code
                    h1(attrs().class_("error-code"), String.valueOf(status)),
                    // Title
                    h2(attrs().class_("error-title"), title),
                    // Error message
                    p(attrs().class_("error-message"),
                        e.getClass().getSimpleName() + ": " +
                        (e.getMessage() != null ? e.getMessage() : "No message")
                    ),
                    // Stack trace (collapsible)
                    details(attrs().class_("error-details"),
                        summary(attrs().class_("error-summary"), text("Stack Trace")),
                        pre(attrs().class_("error-stack"), code(stackTrace))
                    ),
                    // Actions
                    div(attrs().class_("error-actions"),
                        a(attrs().href("/").class_("error-btn"), text("Go Home")),
                        button(attrs().class_("error-btn error-btn-secondary")
                            .set("onclick", "location.reload()"), text("Retry"))
                    )
                )
            )
        );
    }

    public static Element render404(String path) {
        return html(
            head(
                tag("meta", attrs().set("charset", "UTF-8")),
                tag("meta", attrs().set("name", "viewport")
                    .set("content", "width=device-width, initial-scale=1.0")),
                title("404 - Not Found"),
                style(globalStyles())
            ),
            body(attrs().class_("error-body"),
                div(attrs().class_("error-container"),
                    div(attrs().class_("error-icon"), text("🔍")),
                    h1(attrs().class_("error-code"), "404"),
                    h2(attrs().class_("error-title"), "Page Not Found"),
                    p(attrs().class_("error-message"),
                        "The page ", code(path), " could not be found."
                    ),
                    div(attrs().class_("error-actions"),
                        a(attrs().href("/").class_("error-btn"), text("Go Home"))
                    )
                )
            )
        );
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String globalStyles() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            .error-body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
                min-height: 100vh;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 20px;
            }
            .error-container {
                background: rgba(255, 255, 255, 0.05);
                backdrop-filter: blur(10px);
                -webkit-backdrop-filter: blur(10px);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 20px;
                padding: 40px;
                max-width: 700px;
                width: 100%;
                text-align: center;
                box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            }
            .error-icon {
                font-size: 64px;
                margin-bottom: 20px;
                filter: drop-shadow(0 0 20px rgba(255, 100, 100, 0.5));
            }
            .error-code {
                font-size: 96px;
                font-weight: 800;
                background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
                line-height: 1;
                margin-bottom: 10px;
            }
            .error-title {
                font-size: 24px;
                font-weight: 600;
                color: #fff;
                margin-bottom: 15px;
            }
            .error-message {
                font-size: 16px;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 25px;
                line-height: 1.6;
            }
            .error-message code {
                background: rgba(255, 255, 255, 0.1);
                padding: 2px 8px;
                border-radius: 4px;
                font-family: 'SF Mono', Monaco, 'Courier New', monospace;
                color: #feca57;
            }
            .error-details {
                text-align: left;
                margin-bottom: 25px;
            }
            .error-summary {
                cursor: pointer;
                color: rgba(255, 255, 255, 0.6);
                font-size: 14px;
                padding: 10px;
                background: rgba(255, 255, 255, 0.05);
                border-radius: 8px;
                transition: all 0.2s ease;
            }
            .error-summary:hover {
                color: #fff;
                background: rgba(255, 255, 255, 0.1);
            }
            .error-stack {
                background: rgba(0, 0, 0, 0.3);
                border-radius: 8px;
                padding: 15px;
                margin-top: 10px;
                overflow-x: auto;
                font-size: 12px;
                line-height: 1.6;
                color: rgba(255, 255, 255, 0.8);
                font-family: 'SF Mono', Monaco, 'Courier New', monospace;
                max-height: 300px;
                overflow-y: auto;
            }
            .error-stack::-webkit-scrollbar {
                width: 6px;
                height: 6px;
            }
            .error-stack::-webkit-scrollbar-track {
                background: transparent;
            }
            .error-stack::-webkit-scrollbar-thumb {
                background: rgba(255, 255, 255, 0.2);
                border-radius: 3px;
            }
            .error-actions {
                display: flex;
                gap: 12px;
                justify-content: center;
                flex-wrap: wrap;
            }
            .error-btn {
                display: inline-flex;
                align-items: center;
                padding: 12px 24px;
                font-size: 14px;
                font-weight: 600;
                text-decoration: none;
                border-radius: 10px;
                transition: all 0.2s ease;
                cursor: pointer;
                border: none;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: #fff;
            }
            .error-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
            }
            .error-btn-secondary {
                background: rgba(255, 255, 255, 0.1);
                border: 1px solid rgba(255, 255, 255, 0.2);
            }
            .error-btn-secondary:hover {
                background: rgba(255, 255, 255, 0.2);
                box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
            }
            """;
    }
}
