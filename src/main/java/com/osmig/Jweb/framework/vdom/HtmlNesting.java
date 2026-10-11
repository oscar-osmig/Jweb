package com.osmig.Jweb.framework.vdom;

import com.osmig.Jweb.framework.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The HTML content model, checked where the DSL cannot: between a parent and
 * its children.
 *
 * <p>{@code p(div("x"))} compiles — every element takes {@code Object...} — and
 * renders {@code <p><div>x</div></p>}, which the browser repairs into
 * {@code <p></p><div>x</div>} so the live DOM never matches the tree the
 * code built: selectors miss, live-region patches land beside the wrong
 * node, styles cascade from the wrong parent. The serializer calls
 * {@link #check(String, String)} for every element child, so the mistake is
 * reported once per parent/child pair at render time (with the pair named),
 * and thrown instead when {@code jweb.dev.strict-html} is on.</p>
 *
 * <p>Direct-child rules only — what a browser's tree builder repairs. The
 * table is the HTML living standard's content categories reduced to the
 * cases the parser actually rewrites: phrasing-only parents, list / table /
 * select structure, nested interactive content, and {@code <title>}.</p>
 */
public final class HtmlNesting {

    private HtmlNesting() {}

    private static volatile boolean strict = false;
    private static final Set<String> warned = ConcurrentHashMap.newKeySet();

    /** Flow content that is not phrasing content — what a phrasing-only parent cannot hold. */
    private static final Set<String> FLOW_ONLY = Set.of(
        "address", "article", "aside", "blockquote", "details", "dialog", "div", "dl", "dd", "dt",
        "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6",
        "header", "hgroup", "hr", "li", "main", "menu", "nav", "ol", "p", "pre", "section",
        "table", "caption", "colgroup", "thead", "tbody", "tfoot", "tr", "td", "th", "ul",
        "legend", "option", "optgroup", "summary");

    /** Parents whose content model is phrasing content only. */
    private static final Set<String> PHRASING_PARENTS = Set.of(
        "p", "h1", "h2", "h3", "h4", "h5", "h6", "pre", "span", "em", "strong", "b", "i", "small",
        "code", "kbd", "samp", "var", "label", "abbr", "cite", "q", "sub", "sup", "mark", "time",
        "dfn", "s", "u", "bdi", "bdo", "data", "output", "button", "legend", "dt");

    /** Parents whose element children must come from a fixed set (script/template/slot always allowed). */
    private static final Map<String, Set<String>> STRUCTURE = Map.ofEntries(
        Map.entry("ul", Set.of("li")),
        Map.entry("ol", Set.of("li")),
        Map.entry("menu", Set.of("li")),
        Map.entry("dl", Set.of("dt", "dd", "div")),
        Map.entry("table", Set.of("caption", "colgroup", "thead", "tbody", "tfoot", "tr")),
        Map.entry("thead", Set.of("tr")),
        Map.entry("tbody", Set.of("tr")),
        Map.entry("tfoot", Set.of("tr")),
        Map.entry("tr", Set.of("td", "th")),
        Map.entry("colgroup", Set.of("col")),
        Map.entry("select", Set.of("option", "optgroup", "hr")),
        Map.entry("optgroup", Set.of("option")),
        Map.entry("datalist", Set.of("option")),
        Map.entry("hgroup", Set.of("h1", "h2", "h3", "h4", "h5", "h6", "p")),
        Map.entry("picture", Set.of("source", "img")),
        Map.entry("html", Set.of("head", "body")),
        Map.entry("head", Set.of("title", "base", "link", "meta", "style", "script", "noscript", "template")));

    private static final Set<String> ALWAYS_ALLOWED = Set.of("script", "template", "slot");

    /** Interactive content — what an {@code <a>} or {@code <button>} must not contain. */
    private static final Set<String> INTERACTIVE = Set.of(
        "a", "button", "input", "select", "textarea", "label", "details", "iframe", "embed", "audio", "video");

    /** Turns every reported problem into an {@link IllegalStateException} ({@code jweb.dev.strict-html}). */
    public static void setStrict(boolean value) {
        strict = value;
    }

    public static boolean isStrict() {
        return strict;
    }

    /**
     * Why {@code child} may not be a direct child of {@code parent}, or
     * {@code null} when the pair is fine (or unknown to the table).
     */
    public static String problem(String parent, String child) {
        if (parent == null || child == null) return null;
        if (child.indexOf('-') >= 0 || ALWAYS_ALLOWED.contains(child)) return null;   // custom elements, script, template

        if ("title".equals(child)) {
            return "head".equals(parent) || "svg".equals(parent) ? null
                : "<title> belongs in <head> (or inside <svg>) — for a tooltip use attrs().title(\"...\")";
        }
        Set<String> allowed = STRUCTURE.get(parent);
        if (allowed != null) {
            return allowed.contains(child) ? null
                : "<" + parent + "> accepts only " + list(allowed) + " children; the browser moves the <" + child + "> out of it";
        }
        if (PHRASING_PARENTS.contains(parent) && FLOW_ONLY.contains(child)) {
            return "<" + parent + "> holds phrasing content only (text, span, a, em, code…); the browser closes it before the <"
                + child + "> and the DOM will not match the tree";
        }
        if (("a".equals(parent) || "button".equals(parent) || "label".equals(parent)) && INTERACTIVE.contains(child)
                && !("label".equals(parent) && !"label".equals(child) && !"a".equals(child) && !"button".equals(child))) {
            return "<" + parent + "> must not contain interactive content (<" + child + ">); the browser splits them apart";
        }
        if ("form".equals(parent) && "form".equals(child)) {
            return "<form> must not contain another <form>; the browser drops the inner one";
        }
        return null;
    }

    /**
     * Reports a problem once per parent/child pair — a warning naming both,
     * or an exception in strict mode. Called by the serializer for every
     * element child; cheap when the pair is fine.
     */
    public static void check(String parent, String child) {
        String problem = problem(parent, child);
        if (problem == null) return;
        String message = "Invalid HTML nesting: <" + child + "> inside <" + parent + "> — " + problem;
        if (strict) {
            throw new IllegalStateException(message);
        }
        if (warned.add(parent + ">" + child)) {
            Log.warn("{} (reported once; set jweb.dev.strict-html=true to fail the render instead)", message);
        }
    }

    /** Forgets which pairs were already reported (tests). */
    public static void resetWarnings() {
        warned.clear();
    }

    private static String list(Set<String> names) {
        return String.join(", ", names.stream().sorted().map(n -> "<" + n + ">").toList());
    }
}
