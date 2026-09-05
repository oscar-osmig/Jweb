package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class FragmentsSection {
    private FragmentsSection() {}

    public static Element render() {
        return section(
            docTitle("Fragments"),
            para("Server-driven UI without writing JavaScript: any route can return a " +
                 "fragment (an Element without html/body), and declarative swap attributes " +
                 "fetch it and insert it into the page. Swaps are wrapped in a View " +
                 "Transition when the browser supports it."),

            docSubtitle("Basic Swap"),
            para("The server route returns a fragment; any element triggers the swap on click."),
            codeBlock("""
                    // Server: returns just the list markup
                    app.get("/products/list", req ->
                        productList(PAGE.from(req)));

                    // Client: zero JavaScript written
                    button(swap("/products/list?page=2", "#products"),
                        "Next page")"""),

            docSubtitle("Progressive Forms"),
            para("swapForm posts the form data and swaps the returned fragment into the " +
                 "target. With the action attribute set, the form still works when " +
                 "JavaScript is disabled — it just submits natively to the same route."),
            codeBlock("""
                    form(action("/contact/submit"), method("post"),          // no-JS fallback
                            swapForm("/contact/submit", "#form-status"),         // progressive swap
                        field("Name", "name", "text", "Your name"),
                        div(id("form-status")),
                        button(type("submit"), "Send"))

                    // The route returns a status fragment:
                    app.post("/contact/submit", ctx -> {
                        messageStore.save(ctx.formParam("name"), ...);
                        return ContactStatus.success("Message sent!");
                    });"""),

            docSubtitle("Morphing"),
            para("swapMorph updates the target in place instead of replacing its HTML: " +
                 "unchanged nodes are kept, so focus, scroll position, and in-progress " +
                 "input survive. Ideal for lists or dashboards that refresh while the " +
                 "user is interacting."),
            codeBlock("""
                    div(id("inbox"), swapMorph("/inbox/refresh", "#inbox"))"""),

            docSubtitle("History & Events"),
            docList(
                "swapPush(url) adds a browser-history entry; back/forward re-swap naturally",
                "swapOuter(url, sel) replaces the target element itself (outerHTML)",
                "a jweb:swap CustomEvent fires on document after every swap"),

            docTip("Fragment responses are served clean — the framework skips script " +
                   "injection for Elements without a <body>, so swapped content never " +
                   "duplicates the runtime.")
        );
    }
}
