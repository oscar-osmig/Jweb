package com.osmig.Jweb.app.docs;

import jweb.Action;
import jweb.Var;

import static jweb.Js.*;

/**
 * The sidebar search box, wired: typing fetches {@code /docs/search?q=} after
 * a short pause and the fragment that comes back ({@link DocsSearch#results})
 * fills the results container; an empty field clears it; Escape clears both.
 *
 * <p>Nothing is rendered on the client — the hits arrive as HTML built by the
 * same DSL as the page, so the script is only the wiring. The three locals it
 * needs are {@link Var} handles, named once.</p>
 */
final class DocsSearchScript {
    private DocsSearchScript() {}

    /** Milliseconds of quiet typing before a query goes out. */
    static final int DELAY = 150;

    private static final Var Q = local("q");
    private static final Var HTML = local("html");
    private static final Var E = local("e");

    /** The endpoint, with the version being read baked in so hits link into the same docs. */
    static String endpoint(String version) {
        return DocVersions.isLatest(version) ? "/docs/search?q=" : "/docs/search?v=" + version + "&q=";
    }

    static Action build(String version) {
        var input = byId(DocSidebar.SEARCH_INPUT);
        var results = byId(DocSidebar.SEARCH_RESULTS);
        return actions()
            .does(guard("__docsSearchInit").does(
                input.addEventListener("input", debounce(DELAY, callback()
                    .let(Q, input.value().trim())
                    .if_(Q.length().eq(0),
                        results.setHtml(""))
                    .else_(
                        fetch(str(endpoint(version)).plus(call("encodeURIComponent", Q)))
                            .text()
                            .then(callback(HTML).does(results.setHtml(HTML)))
                            .toVal()))),
                input.addEventListener("keydown", callback(E)
                    .if_(eventKey(E).eq("Escape"),
                        input.setValue(""),
                        results.setHtml("")))));
    }
}
