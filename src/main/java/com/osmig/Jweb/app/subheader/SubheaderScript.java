package com.osmig.Jweb.app.subheader;

import static jweb.Js.*;

/**
 * The "On This Page" rail: one {@code scrollSpy} over the h2/h3 headings of the
 * docs content.
 *
 * <p>The behavior owns the rules that used to live here as raw JavaScript: the
 * active header is the last one at or above an activation line 25% down the
 * visible content area (not a fixed pixel offset), the last header wins at the
 * very bottom of the scroll container, a clicked link scrolls smoothly without
 * the spy fighting it, and the rail rebuilds whenever the docs content is
 * swapped — so links never point at detached nodes.</p>
 *
 * <p>Link presentation is CSS ({@code .subheader-link}, with {@code data-level}
 * carrying the heading depth) — see {@code DocsPage.docsStyles()}.</p>
 */
public final class SubheaderScript {
    private SubheaderScript() {}

    public static String build() {
        return actions()
            .does(guard("__subheaderInit").does(
                scrollSpy("#subheader-nav", "h2, h3")
                    .within(".docs-content")
                    .linkClass("subheader-link")
                    .host("#subheader-sidebar")
                    .hasHeadingsClass("has-headers")
                    .scrollMargin(24)))
            .build();
    }
}
