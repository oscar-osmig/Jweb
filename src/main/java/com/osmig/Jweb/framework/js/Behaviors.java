package com.osmig.Jweb.framework.js;

import jweb.Action;
import jweb.Func;
import jweb.Val;
import jweb.js.ActiveLink;
import jweb.js.Copy;
import jweb.js.CustomElement;
import jweb.js.LineGutter;
import jweb.js.Navigate;
import jweb.js.Prefetch;
import jweb.js.ScrollSpy;
import jweb.js.SplitPane;
import jweb.js.Stmt;

/**
 * Page behaviors and platform statements — the layer of the JS DSL that exists
 * so an app never has to hand-write the JavaScript its pages actually need:
 * copy buttons, a scroll spy, client-side navigation with prefetching, a
 * draggable split, a textarea's line gutter, Web Components, and reading the
 * client's copy of a server {@code State}.
 *
 * <p>Reachable from {@code import static jweb.Js.*} like everything else. The
 * builders they return are {@code jweb.js.*} types, and every one of them is an
 * {@link Action}, so they attach to elements ({@code onClick(copy(...))}) or go
 * on a page through {@code actions().does(...)}.</p>
 *
 * @deprecated Replaced by {@code jweb.Js} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class Behaviors extends JS {

    protected Behaviors() {}

    // ==================== clipboard ====================

    /**
     * Copies a literal to the clipboard, {@code execCommand} fallback included:
     * {@code button(onClick(copy("npm i jweb").feedback("Copied!")), "Copy")}.
     */
    public static Copy copy(String text) {
        return new Copy(text, null, null);
    }

    /** Copies whatever an expression evaluates to. */
    public static Copy copy(Val text) {
        return new Copy(null, text, null);
    }

    /**
     * Copies the text of the nearest element matching {@code selector} — the
     * trigger's own subtree first, then each ancestor's, then the document. A
     * copy button beside a {@code <pre>} finds that {@code <pre>}.
     */
    public static Copy copyFrom(String selector) {
        return new Copy(null, null, selector);
    }

    // ==================== navigation ====================

    /** Fetches a fragment and swaps it in — client-side navigation as an action. */
    public static Navigate navigate(String url) {
        return new Navigate(url, null);
    }

    /** Client-side navigation to a URL built at runtime. */
    public static Navigate navigate(Val url) {
        return new Navigate(null, url);
    }

    /** Warms the swap cache for a set of links, on hover or on visibility. */
    public static Prefetch prefetch(String linkSelector) {
        return new Prefetch(linkSelector);
    }

    /** Marks the link whose {@code href} is the page you are on. */
    public static ActiveLink activeLink(String linkSelector) {
        return new ActiveLink(linkSelector);
    }

    /** Runs a callback on the browser's back/forward navigation. */
    public static Action onPopState(Func handler) {
        return () -> "window.addEventListener('popstate'," + handler.toExpr() + ")";
    }

    // ==================== reading ====================

    /** An "on this page" rail that follows the reader. */
    public static ScrollSpy scrollSpy(String navSelector, String headingsSelector) {
        return new ScrollSpy(navSelector, headingsSelector);
    }

    // ==================== editors ====================

    /** A draggable divider between two panes. */
    public static SplitPane splitPane(String handleSelector, String leftSelector) {
        return new SplitPane(handleSelector, leftSelector);
    }

    /** Wrap-accurate line numbers beside a textarea. */
    public static LineGutter lineGutter(String textareaSelector, String gutterSelector) {
        return new LineGutter(textareaSelector, gutterSelector);
    }

    /** Re-measures a gutter after its textarea's value was replaced in code. */
    public static Action relineGutter(String textareaSelector) {
        return () -> "JWeb.relineGutter('" + JS.esc(textareaSelector) + "')";
    }

    /** Marks one gutter line (0 clears the mark) — for a compiler error. */
    public static Action markLine(String textareaSelector, Object line) {
        return () -> "JWeb.markLine('" + JS.esc(textareaSelector) + "'," + JS.toJs(line) + ")";
    }

    /** Grows a textarea to fit its content, and keeps it fitting as it is typed. */
    public static Action resizeToContent(String textareaSelector) {
        return () -> "JWeb.resizeToContent('" + JS.esc(textareaSelector) + "')";
    }

    /**
     * Inserts text at the caret of a text field, keeping the browser's undo
     * stack where {@code execCommand('insertText')} is supported.
     */
    public static Action insertText(String selector, Object text) {
        return () -> "JWeb.insertText(document.querySelector('" + JS.esc(selector) + "'),"
            + JS.toJs(text) + ")";
    }

    /** Inserts text at the caret of a field an expression names. */
    public static Action insertText(Val element, Object text) {
        return () -> "JWeb.insertText(" + element.js() + "," + JS.toJs(text) + ")";
    }

    // ==================== DOM the DSL was missing ====================

    /** {@code document.createElement(tag)}. */
    public static JS.El createElement(String tag) {
        return new JS.El("document.createElement('" + JS.esc(tag) + "')");
    }

    /** {@code document.activeElement} — whatever has keyboard focus. */
    public static JS.El activeElement() {
        return new JS.El("document.activeElement");
    }

    /**
     * Parses an HTML string into a DocumentFragment, so a fetched fragment can
     * be picked apart before anything of it reaches the page:
     *
     * <pre>{@code
     * callback("html")
     *     .var_("f", parseHtml(v("html")))
     *     .var_("status", v("f").call("querySelector", "#status"))
     * }</pre>
     *
     * <p>Scripts inside it stay inert, as they do in any template element.</p>
     */
    public static Val parseHtml(Val html) {
        return new Val("(function(h){var t=document.createElement('template');"
            + "t.innerHTML=h;return t.content})(" + html.js() + ")");
    }

    // ==================== server state, client side ====================

    /**
     * The client's copy of a server {@code State} — what {@code JWeb.getState}
     * returns, as an expression:
     *
     * <pre>{@code
     * State<Integer> count = useState(0);
     * ...
     * onClick(setText("total", syncState(count).plus(1)))
     * }</pre>
     */
    public static Val syncState(String stateId) {
        return new Val("JWeb.getState('" + JS.esc(stateId) + "')");
    }

    /** The client's copy of a state value. */
    public static Val syncState(jweb.state.State<?> state) {
        return syncState(state.getId());
    }

    /**
     * Runs a callback whenever a state changes on the client — the callback
     * takes the new value and the old one.
     */
    public static Action onStateChange(String stateId, Func handler) {
        return () -> "JWeb.onState('" + JS.esc(stateId) + "'," + handler.toExpr() + ")";
    }

    /** Runs a callback whenever this state changes on the client. */
    public static Action onStateChange(jweb.state.State<?> state, Func handler) {
        return onStateChange(state.getId(), handler);
    }

    // ==================== statements ====================

    /**
     * An {@code if} statement anywhere a statement goes — inside
     * {@code does(...)}, an {@code asyncFunc} body, a {@code Func}. The
     * early-return guard that used to need raw JavaScript:
     *
     * <pre>{@code
     * asyncFunc("search").params("query").does(
     *     assign("searchQuery", "query"),
     *     sleep(300),
     *     if_(v("searchQuery").neq(v("query")), return_()),   // user typed again
     *     await(get("/api/search").ok(call("showResults", "_data"))))
     * }</pre>
     *
     * <p>{@code Func.if_(condition, ...)} is the same statement written on a
     * function body, and the one that {@code elif}/{@code else_} extend.</p>
     */
    public static Stmt if_(Val condition, Object... statements) {
        StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
        for (Object s : statements) sb.append(JS.toStatement(s));
        return new Stmt(sb.append("}").toString());
    }

    // ==================== the callback's event ====================

    /**
     * {@code e.preventDefault()} — the event of the callback this statement is
     * in, which the DSL names {@code e} (both in {@code callback("e", ...)} and
     * in an attribute action, where the runtime binds {@code e} for you).
     */
    public static Stmt preventDefault() {
        return new Stmt("e.preventDefault()");
    }

    /** {@code e.stopPropagation()} on the callback's event. */
    public static Stmt stopPropagation() {
        return new Stmt("e.stopPropagation()");
    }

    /** {@code e.stopImmediatePropagation()} on the callback's event. */
    public static Stmt stopImmediatePropagation() {
        return new Stmt("e.stopImmediatePropagation()");
    }

    // ==================== web components ====================

    /** Defines a Web Component — {@code customElements.define}, typed. */
    public static CustomElement customElement(String name) {
        return new CustomElement(name);
    }

    /** {@code element.attachShadow({mode})} — {@code "open"} or {@code "closed"}. */
    public static Val attachShadow(Val element, String mode) {
        return new Val(element.js() + ".attachShadow({mode:'" + JS.esc(mode) + "'})");
    }

    /** {@code element.attachShadow({mode:'open'})}. */
    public static Val attachShadow(Val element) {
        return attachShadow(element, "open");
    }

    /** {@code element.shadowRoot} — null for a closed shadow. */
    public static Val shadowRoot(Val element) {
        return new Val(element.js() + ".shadowRoot");
    }

    /** Puts an element in a named slot: {@code element.slot = name}. */
    public static Stmt assignSlot(Val element, String slotName) {
        return new Stmt(element.js() + ".slot='" + JS.esc(slotName) + "'");
    }

    /** The slot an element was assigned to: {@code element.assignedSlot}. */
    public static Val assignedSlot(Val element) {
        return new Val(element.js() + ".assignedSlot");
    }

    /** What a {@code <slot>} is showing: {@code slot.assignedElements()}. */
    public static Val assignedElements(Val slot) {
        return new Val(slot.js() + ".assignedElements()");
    }

    /** What a {@code <slot>} is showing, text nodes included. */
    public static Val assignedNodes(Val slot) {
        return new Val(slot.js() + ".assignedNodes()");
    }

    /** Runs a callback when a {@code <slot>}'s content changes. */
    public static Action onSlotChange(Val slot, Func handler) {
        return () -> slot.js() + ".addEventListener('slotchange'," + handler.toExpr() + ")";
    }
}
