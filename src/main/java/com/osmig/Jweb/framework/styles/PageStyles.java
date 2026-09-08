package com.osmig.Jweb.framework.styles;

import com.osmig.Jweb.framework.security.CspNonce;
import com.osmig.Jweb.framework.state.StateManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Render-scoped registry for the CSS a page collects while it renders.
 *
 * <p>Two things feed it. A {@link com.osmig.Jweb.framework.template.Template}'s
 * {@code styles()} hook contributes a whole {@link jweb.css.Stylesheet} — the
 * page's, its layout's, and every template rendered inside it. And an inline
 * {@link jweb.Style} that carries a pseudo-class, a media block or a
 * {@code @starting-style} contributes the rules those need, under a generated
 * class name; the plain declarations still ride the {@code style=} attribute.</p>
 *
 * <p>Everything is keyed by a content hash, so the same stylesheet contributed
 * by ten instances of one component, or the same {@code .hover(...)} written on
 * fifty buttons, is emitted once. The hash is also the generated class name
 * ({@code j-3f9a1c}), which makes it deterministic across renders, servers and
 * requests — a swap fragment naming a class the page already carries is a
 * no-op by construction.</p>
 *
 * <p>Delivery mirrors {@link com.osmig.Jweb.framework.js.ClientActions}: every
 * render path drains what it has not sent yet. The page shell puts it in
 * {@code <head>}; a swap fragment carries its own {@code <style>} (a style
 * element inserted through {@code innerHTML} does apply, unlike a script);
 * streamed chunks and WebSocket DOM updates carry theirs the same way.</p>
 *
 * <p>Outside a page render (no {@link StateManager} context — error pages, bare
 * {@code toHtml()} calls, static export) there is nowhere to deliver rules, so
 * {@link #register} returns null and the caller keeps the plain inline
 * declarations only.</p>
 */
public final class PageStyles {

    private PageStyles() {}

    /**
     * One bucket per render context. Weakly keyed so a context that is
     * cleared and dropped takes its CSS with it — the registry never has to
     * be told the request ended.
     */
    private static final Map<StateManager.StateContext, Bucket> BY_CONTEXT =
        Collections.synchronizedMap(new WeakHashMap<>());

    private static final class Bucket {
        /**
         * Content hash -> CSS block, in the order they were contributed.
         * Component stylesheets and generated classes are kept apart so the
         * cascade is deterministic: a stylesheet is what a component declares,
         * a generated class is what one element overrode it with, so the
         * classes are emitted last and win the ties.
         */
        final Map<String, String> sheets = new LinkedHashMap<>();
        final Map<String, String> classes = new LinkedHashMap<>();
        final Set<String> sent = new LinkedHashSet<>();
    }

    private static Bucket bucket(StateManager.StateContext context) {
        return BY_CONTEXT.computeIfAbsent(context, c -> new Bucket());
    }

    // ==================== Contributing ====================

    /**
     * Adds a template's {@code styles()} stylesheet to the page's collected
     * CSS. Null and empty stylesheets are ignored; identical ones collapse.
     *
     * @param template the template being rendered
     */
    public static void collect(com.osmig.Jweb.framework.template.Template template) {
        if (template == null) return;
        jweb.css.Stylesheet sheet = template.styles();
        if (sheet == null) return;
        add(sheet.build());
    }

    /**
     * Adds a block of CSS to the page's collected stylesheet, deduped by
     * content.
     *
     * @param css the CSS text
     * @return true when it was recorded (there was a render context to
     *     record it in)
     */
    public static boolean add(String css) {
        if (css == null || css.isBlank()) return false;
        StateManager.StateContext context = StateManager.getContext();
        if (context == null) return false;
        Bucket b = bucket(context);
        synchronized (b) {
            b.sheets.putIfAbsent(hash(css), css);
        }
        return true;
    }

    /**
     * Registers the variant rules of an inline style under a generated class.
     *
     * @param style the style carrying {@code .hover(...)}, {@code .at(...)}
     *     and friends
     * @return the generated class name, or null when there is no render
     *     context to deliver the rules through
     */
    public static String register(jweb.Style<?> style) {
        if (style == null || !style.hasVariants()) return null;
        StateManager.StateContext context = StateManager.getContext();
        if (context == null) return null;
        String name = classNameFor(style);
        String css = style.variantCss(name);
        Bucket b = bucket(context);
        synchronized (b) {
            b.classes.putIfAbsent(name, css);
        }
        return name;
    }

    /**
     * The class name a style's variants generate — a content hash of the
     * rules themselves, so two styles with the same hover rule share one
     * class and one rule block.
     *
     * @param style the style
     * @return the class name, e.g. {@code j-3f9a1c}
     */
    public static String classNameFor(jweb.Style<?> style) {
        return "j-" + hash(style.variantCss("&")).substring(0, 6);
    }

    /**
     * Writes an inline style onto an element's attribute map: the plain
     * declarations go to {@code style=}, and any conditional rules
     * ({@code .hover(…)}, {@code .at(md(), …)}, {@code .dark(…)}) register a
     * generated class that is appended to {@code class=}.
     *
     * @param style the style
     * @param attrs the attribute map to write into
     */
    public static void applyTo(jweb.Style<?> style, Map<String, String> attrs) {
        String declarations = style.build();
        if (!declarations.isEmpty()) attrs.put("style", declarations);
        addClass(attrs, register(style));
    }

    /**
     * Appends a class to an attribute map's {@code class} attribute.
     *
     * @param attrs the attribute map
     * @param className the class to append; null is ignored
     */
    public static void addClass(Map<String, String> attrs, String className) {
        if (className == null || className.isEmpty()) return;
        String existing = attrs.get("class");
        attrs.put("class", existing == null || existing.isBlank()
            ? className
            : existing + " " + className);
    }

    // ==================== Delivering ====================

    /**
     * The context's still-undelivered CSS, marking it delivered — or null
     * when nothing is pending.
     *
     * @param context the render context (null yields null)
     * @return the CSS text, or null
     */
    public static String drainCss(StateManager.StateContext context) {
        if (context == null) return null;
        Bucket b = BY_CONTEXT.get(context);
        if (b == null) return null;
        StringBuilder css = new StringBuilder();
        synchronized (b) {
            b.sheets.forEach((key, block) -> {
                if (b.sent.add(key)) css.append(block);
            });
            b.classes.forEach((key, block) -> {
                if (b.sent.add(key)) css.append(block);
            });
        }
        return css.length() == 0 ? null : css.toString();
    }

    /**
     * {@link #drainCss} wrapped in a nonce-stamped {@code <style>} tag, or
     * {@code ""} when nothing is pending.
     *
     * @param context the render context
     * @return the style tag, or an empty string
     */
    public static String drainStyleTag(StateManager.StateContext context) {
        String css = drainCss(context);
        if (css == null) return "";
        return "<style" + CspNonce.attr() + ">" + neutralize(css) + "</style>";
    }

    /**
     * Keeps emitted CSS inert to the HTML parser: {@code </style} would
     * terminate the surrounding element mid-rule. Generated CSS never
     * contains it; a hand-written {@code raw(...)} block might.
     */
    static String neutralize(String css) {
        return css.replaceAll("(?i)</style", "<\\\\/style");
    }

    /** First 10 hex chars of SHA-256 — stable across JVMs and renders. */
    private static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(10);
            for (int i = 0; i < 5; i++) hex.append(String.format("%02x", digest[i]));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
