package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * HTML head with meta tags and global styles.
 */
public class Head implements Template {
    private final String title;

    public Head(String title) {
        this.title = title;
    }

    @Override
    public Element render() {
        return head(
            meta(attr("charset", "UTF-8")),
            meta(name("viewport"), attr("content", "width=device-width, initial-scale=1")),
            title(title),
            style(css())
        );
    }

    private String css() {
        return """
            *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
            html { font-size: 16px; scroll-behavior: smooth; }
            body {
                font-family: system-ui, -apple-system, sans-serif;
                line-height: 1.6;
                color: #1e293b;
                background: #fff;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }
            a { color: #6366f1; text-decoration: none; }
            a:hover { color: #4f46e5; }
            """;
    }
}
