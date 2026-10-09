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
 * Render-scoped registry for the JavaScript a page collects while it renders —
 * the script side of {@link PageStyles}.
 *
 * <p>A {@link com.osmig.Jweb.framework.template.Template}'s {@code scripts()}
 * hook contributes an {@link jweb.Action}: the page's, its layout's, and every
 * template rendered inside them. Each is keyed by a content hash, so ten
 * instances of a component contribute one copy, and a fragment that carries a
 * script the page already ran does not run it again.</p>
 *
 * <p>Delivery mirrors {@link com.osmig.Jweb.framework.js.ClientActions}: the
 * script tag is marked {@code data-jweb-act}, which the runtime executes once
 * when it arrives in a swapped fragment, a streamed chunk or a DOM patch; in
 * the page shell it is an ordinary script before {@code </body>}.</p>
 *
 * <p>Outside a page render there is nowhere to deliver the script, so
 * {@link #add} returns false and the caller falls back to inlining it.</p>
 */
public final class PageScripts {

    private PageScripts() {}

    private static final Map<StateManager.StateContext, Bucket> BY_CONTEXT =
        Collections.synchronizedMap(new WeakHashMap<>());

    private static final class Bucket {
        final Map<String, String> scripts = new LinkedHashMap<>();
        final Set<String> sent = new LinkedHashSet<>();
    }

    private static Bucket bucket(StateManager.StateContext context) {
        return BY_CONTEXT.computeIfAbsent(context, c -> new Bucket());
    }

    /**
     * Adds a template's {@code scripts()} action to the page's collected
     * JavaScript. Empty hooks are ignored; identical scripts collapse.
     *
     * @param template the template being rendered
     */
    public static void collect(com.osmig.Jweb.framework.template.Template template) {
        if (template == null) return;
        java.util.Optional<jweb.Action> script = template.scripts();
        if (script == null || script.isEmpty()) return;
        add(script.get().build());
    }

    /**
     * Adds a block of JavaScript to the page's collected scripts, deduped by
     * content.
     *
     * @param js the JavaScript text
     * @return true when it was recorded (there was a render context)
     */
    public static boolean add(String js) {
        if (js == null || js.isBlank()) return false;
        StateManager.StateContext context = StateManager.getContext();
        if (context == null) return false;
        Bucket b = bucket(context);
        synchronized (b) {
            b.scripts.putIfAbsent(hash(js), js);
        }
        return true;
    }

    /**
     * The context's still-undelivered JavaScript, marking it delivered — or
     * null when nothing is pending.
     */
    public static String drainJs(StateManager.StateContext context) {
        if (context == null) return null;
        Bucket b = BY_CONTEXT.get(context);
        if (b == null) return null;
        StringBuilder js = new StringBuilder();
        synchronized (b) {
            b.scripts.forEach((key, block) -> {
                if (b.sent.add(key)) js.append(block).append(";\n");
            });
        }
        return js.length() == 0 ? null : js.toString();
    }

    /**
     * {@link #drainJs} wrapped in a nonce-stamped {@code <script data-jweb-act>}
     * tag, or {@code ""} when nothing is pending.
     */
    public static String drainScriptTag(StateManager.StateContext context) {
        String js = drainJs(context);
        if (js == null) return "";
        return "<script" + CspNonce.attr() + " data-jweb-act>" + neutralize(js) + "</script>";
    }

    /** Keeps emitted JavaScript inert to the HTML parser. */
    static String neutralize(String js) {
        return js.replaceAll("(?i)</script", "<\\\\/script");
    }

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
