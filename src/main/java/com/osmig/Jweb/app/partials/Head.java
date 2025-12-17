package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * HTML head partial with meta tags and styles.
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
        style("""
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: system-ui, -apple-system, sans-serif;
                line-height: 1.6;
                color: #333;
            }
            .navbar {
                background: #333;
                padding: 1rem;
            }
            .navbar a {
                color: white;
                text-decoration: none;
                margin-right: 1rem;
            }
            .navbar a:hover { text-decoration: underline; }
            .container {
                max-width: 1200px;
                margin: 0 auto;
                padding: 2rem;
            }
            .footer {
                background: #f5f5f5;
                padding: 2rem;
                text-align: center;
                margin-top: 2rem;
            }
            """)
    );
    }
}
