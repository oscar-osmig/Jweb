package com.osmig.Jweb.app.docs;

import static jweb.Js.*;

/**
 * Client-side navigation for the docs.
 *
 * <p>The swap itself is markup: every sidebar link carries {@code data-swap-*}
 * ({@link DocSidebar}), so the client runtime fetches the section, replaces
 * {@code .docs-content}, pushes the history entry and restores it on back —
 * including the {@code ?v=} version param, which is baked into both URLs.</p>
 *
 * <p>This script adds the two things markup cannot say: warm the cache on
 * hover, and keep the link for the section you are on marked.</p>
 */
final class DocsNavScript {
    private DocsNavScript() {}

    /** How long a fetched section stays reusable — five minutes. */
    static final int TTL = 300_000;

    /** The fragment URL for a section, under the version being read. */
    static String contentHref(String section, String version) {
        String base = "/docs/content?section=" + section;
        return DocVersions.isLatest(version) ? base : base + "&v=" + version;
    }

    static String build() {
        return actions()
            .does(guard("__docsNavInit").does(
                prefetch(".docs-nav-link").within(".docs-sidebar").delay(50).cache(TTL),
                activeLink(".docs-nav-link")))
            .build();
    }
}
