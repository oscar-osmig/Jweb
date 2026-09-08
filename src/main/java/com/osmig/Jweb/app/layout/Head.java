package com.osmig.Jweb.app.layout;

import jweb.Element;
import jweb.Template;

import static jweb.El.*;

/**
 * Document head with meta tags. The global stylesheet is
 * {@link Layout#styles()} — the render collects it and puts it here.
 */
public class Head implements Template {
    private final String pageTitle;

    public Head(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public Element render() {
        return head(
            metaCharset(),
            metaViewport(),
            jweb.Seo
                .of(pageTitle, "Build complete web applications entirely in Java — "
                    + "type-safe components, fluent DSL, zero frontend tooling.")
                .siteName("JWeb")
                .render()
        );
    }
}
