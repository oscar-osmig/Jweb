package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/**
 * The behavior verbs — the JavaScript a real page needs, as DSL calls instead
 * of hand-written script. Every sample here is what the framework's own docs
 * and playground pages run.
 */
public final class JSBehaviors {
    private JSBehaviors() {}

    public static Element render() {
        return section(
            h3Title("Behaviors"),
            para("A behavior is a chunk of client work that installs itself once and keeps "
                 + "running — a copy button, a scroll spy, a draggable split. Each is an Action, "
                 + "so it attaches to an element or goes on the page through actions().does(...). "
                 + "The implementations live in the client runtime, so the generated code is one call."),

            h3Title("Copy to Clipboard"),
            para("navigator.clipboard with the execCommand fallback built in, so it also works "
                 + "on http origins and in browsers that never got the async API."),
            codeBlock("""
// A literal
button(onClick(copy("npm i jweb").feedback("Copied!")), "Copy")

// The nearest matching element's text: the trigger's own subtree,
// then each ancestor's, then the document
button(class_("code-copy-btn"),
    onClick(copyFrom("pre")
        .feedback("Copied!", 1600)      // label swap, then back
        .feedbackClass("copied")        // class while it shows
        .failText("Copy failed")),
    "Copy")

// Delegated — the buttons are re-rendered, the listener is not
delegate(".docs-layout", "click", ".code-copy-btn").handler(
    callback("e", "t").does(
        copyFrom("pre").trigger(v("t")).feedback("Copied!")))"""),

            h3Title("Client-Side Navigation"),
            para("navigate(url) is the runtime's swap as an action. Links marked up with "
                 + "swap(...)/swapPush(...) need no script at all — prefetch(...) and "
                 + "activeLink(...) are the two things markup cannot say."),
            codeBlock("""
// Markup: fetch the fragment, fill the target, push the URL
a(href("/docs?section=state"),
  swap("/docs/content?section=state", ".docs-content"),
  swapPush("/docs?section=state"),
  class_("docs-nav-link"),
  "State")

// Script: warm the cache on hover, keep the current link marked
inlineScript(actions().does(
    prefetch(".docs-nav-link").within(".docs-sidebar").delay(50).cache(300_000),
    activeLink(".docs-nav-link")
).build())

// Or navigate from any handler
button(onClick(navigate("/docs/content?section=state")
        .target(".docs-content")
        .push("/docs?section=state")
        .cache(300_000)),
    "State")

// prefetch() and navigate().cache(ttl) share one store, so a link
// warmed on hover swaps without a round trip. onVisible() prefetches
// when the link scrolls into view instead."""),

            h3Title("Scroll Spy"),
            para("Builds a link per heading, marks the one you are reading, and rebuilds "
                 + "itself whenever the content is swapped."),
            codeBlock("""
inlineScript(actions().does(
    scrollSpy("#toc", "h2, h3")
        .within(".docs-content")          // the scrolling element
        .linkClass("toc-link")            // class on each generated link
        .activeClass("active")            // class on the one in view
        .hasHeadingsClass("has-headings") // on the nav's parent, while any exist
        .scrollMargin(24)                 // space above a clicked heading
).build())

// Each link carries data-level (2 for h2, 3 for h3) and data-index,
// so indentation and size are CSS:
//   .toc-link[data-level="3"] { padding-left: 1.5rem }
// Without .offset(px) the activation line sits 25% down the visible
// area, capped at 160px; at the very bottom the last heading wins."""),

            h3Title("Editors"),
            codeBlock("""
inlineScript(actions().does(
    // Wrap-accurate line numbers: line 7 in the gutter is line 7 in
    // the error message. Re-measures on every width change.
    lineGutter("#editor", "#lines").mirror("#mirror").errorClass("errline"),

    // A draggable divider; flex: 0 0 <pct>% on the left pane
    splitPane("#gutter", "#code")
        .container("#split").minPercent(20).maxPercent(80).persist("split"),

    // A textarea that grows with what is typed into it
    resizeToContent("textarea.grow")
).build())

// After replacing the text in code, or to mark a compiler error:
relineGutter("#editor")
markLine("#editor", v("lineNumber"))     // 0 clears the mark
insertText("#editor", "    ")            // at the caret, undo intact"""),

            h3Title("Reading Server State on the Client"),
            para("The runtime keeps the client's copy of every server State. syncState reads "
                 + "it as an expression; onStateChange runs when it changes."),
            codeBlock("""
State<Integer> count = useState(0);

// An expression — JWeb.getState('<id>')
button(onClick(setText("total", syncState(count).plus(1))), "+1")

// A listener — the callback takes the new value and the old one
inlineScript(actions().does(
    onStateChange(count, callback("now", "before")
        .log("count went from", v("before"), "to", v("now")))
).build())"""),

            h3Title("Web Components"),
            codeBlock("""
inlineScript(actions().does(
    customElement("user-card")
        .observedAttributes("name")
        .shadow()                          // or .shadow("closed")
        .template(div(class_("card"), slot()))
        .connected(callback().log("mounted"))
        .disconnected(callback().log("gone"))
        .attributeChanged(callback("name", "oldValue", "newValue")
            .does(dom(".card").setText(v("newValue"))))
).build())

// Shadow DOM and slots as expressions
attachShadow(v("host"))          // host.attachShadow({mode:'open'})
shadowRoot(v("host"))
assignSlot(v("child"), "footer") // child.slot = 'footer'
assignedElements(v("slot"))
onSlotChange(v("slot"), callback("e").log("slot changed"))

// The definition is guarded: re-running it is a no-op, not the
// NotSupportedError a second define of the same name throws."""),

            h3Title("Progressive Web App"),
            codeBlock("""
import jweb.js.Manifest;
import static jweb.js.Pwa.*;

static final Manifest APP = manifest("JWeb Demo")
    .shortName("JWeb").description("The Java web framework")
    .display("standalone").startUrl("/").scope("/")
    .themeColor("#4f46e5").background("#ffffff")
    .icon("/icon-192.png", "192x192")
    .icon("/icon-512.png", "512x512");

// Routes — publishes /manifest.webmanifest
serve(app, APP);

// Page head
head(title("JWeb"), link())

// Page script
inlineScript(actions().does(registerServiceWorker("/sw.js")).build())"""),

            docTip("Behaviors queue themselves on the client runtime rather than running inline: "
                   + "a page's own scripts parse before the runtime does. Anything queued later — "
                   + "from a swapped fragment — installs immediately.")
        );
    }
}
