package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.app.Theme.*;

/**
 * HTML head partial with meta tags and base styles.
 * Global styles are defined here for elements that can't use inline styles (like body, *).
 */
public class Head implements Template {

    private final String pageTitle;

    public Head(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public static Head create(String pageTitle) {
        return new Head(pageTitle);
    }

    @Override
    public Element render() {
        return head(
            meta(attr("charset", "UTF-8")),
            meta(name("viewport"), attr("content", "width=device-width, initial-scale=1.0")),
            title(pageTitle),
            style(globalStyles())
        );
    }

    /**
     * Global CSS reset and base styles.
     * These are styles that must be in a stylesheet (not inline).
     */
    private String globalStyles() {
        return """
            *, *::before, *::after {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            html {
                font-size: 16px;
                -webkit-font-smoothing: antialiased;
            }
            body {
                font-family: %s;
                line-height: 1.6;
                color: %s;
                background-color: %s;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }
            a {
                color: %s;
                text-decoration: none;
            }
            a:hover {
                text-decoration: underline;
            }
            img {
                max-width: 100%%;
                height: auto;
            }
            """.formatted(
                FONT_FAMILY,
                TEXT.css(),
                BG.css(),
                PRIMARY.css()
            );
    }
}
