package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.security.CspNonce;
import com.osmig.Jweb.framework.state.StateManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Render-scoped registry that makes Actions-DSL event handlers CSP-safe.
 *
 * <p>Inline {@code on<type>=} attributes can never run under the nonce-based
 * CSP that {@code Middlewares.recommended()} sends — a nonce covers script
 * <em>elements</em>, not handler attributes. So {@code attrs().onClick(show("x"))}
 * renders a {@code data-jweb-act<type>} attribute carrying an id, the JS
 * itself registers here, and every render path — page shell, swap fragment,
 * streamed chunk, WebSocket DOM update — delivers the still-undelivered
 * definitions as real script code that fills {@code window.__JWEB_ACTIONS__}.
 * The runtime's capture-phase delegation looks the id up when the event
 * fires, with {@code this} bound to the attributed element and the DOM event
 * passed as both {@code event} and {@code e} (the two names inline handlers
 * historically used).</p>
 *
 * <p>Ids are content hashes of the JS, so the same action yields the same id
 * in every render context: a fragment or re-rendered component re-defining
 * an id the page already knows overwrites it with identical code, and
 * identical buttons in a list share one definition.</p>
 *
 * <p>Outside a page render (no {@link StateManager} context — error pages,
 * bare {@code toHtml()} calls, static export) or with the client runtime
 * disabled, {@link #register} returns null and the caller falls back to the
 * legacy inline attribute, which keeps working wherever no CSP is enforced.</p>
 */
public final class ClientActions {

    private ClientActions() {}

    /**
     * Registers an action's JS in the current render context.
     *
     * @param js the JavaScript source ({@code Action.inline()})
     * @return the action id for the {@code data-jweb-act<type>} attribute,
     *     or null when there is no context to deliver definitions through
     *     (caller should emit the legacy inline attribute instead)
     */
    public static String register(String js) {
        if (!JWebRuntime.isEnabled()) return null;
        StateManager.StateContext context = StateManager.getContext();
        if (context == null) return null;
        String id = "a" + hash(js);
        context.registerClientAction(id, js);
        return id;
    }

    /**
     * The context's still-undelivered action definitions as self-contained
     * JS (an IIFE filling {@code window.__JWEB_ACTIONS__}), marking them
     * delivered — or null when nothing is pending. Raw-JS form for emission
     * points that already own a script: streamed chunks and WebSocket DOM
     * updates.
     */
    public static String drainJs(StateManager.StateContext context) {
        if (context == null) return null;
        Map<String, String> unsent = context.drainUnsentClientActions();
        if (unsent.isEmpty()) return null;
        return definitions(unsent);
    }

    /**
     * {@link #drainJs} wrapped in a nonce-stamped script tag, or {@code ""}
     * when nothing is pending. The {@code data-jweb-act} marker is what the
     * runtime's swap() executes when this tag arrives inside a fetched
     * fragment (fragment scripts never run on their own).
     */
    public static String drainScriptTag(StateManager.StateContext context) {
        String js = drainJs(context);
        if (js == null) return "";
        return "<script" + CspNonce.attr() + " data-jweb-act>" + js + "</script>";
    }

    /**
     * Builds the definitions IIFE. Each action becomes
     * {@code A.<id>=function(event){var e=event;<js>};} — {@code this} is
     * bound by the runtime's delegation, and returning {@code false}
     * prevent-defaults like an inline handler would. The DSL helpers
     * ({@code $_}, {@code esc}, {@code fmtDate}) are closed over per batch
     * when the JS references them, so attribute actions no longer depend on
     * some page script having defined them globally.
     */
    private static String definitions(Map<String, String> idToJs) {
        StringBuilder body = new StringBuilder();
        idToJs.forEach((id, js) -> body.append("A.").append(id)
            .append("=function(event){var e=event;").append(js).append("};"));
        StringBuilder sb = new StringBuilder(
            "(function(){var A=window.__JWEB_ACTIONS__=window.__JWEB_ACTIONS__||{};");
        String code = body.toString();
        if (usesHelpers(code)) sb.append(Actions.ScriptBuilder.HELPERS).append(";");
        return neutralize(sb.append(code).append("})()").toString());
    }

    private static boolean usesHelpers(String js) {
        return js.contains("$_(") || js.contains("esc(") || js.contains("fmtDate(");
    }

    /**
     * Keeps emitted JS inert to the HTML parser: {@code </script} would
     * terminate the surrounding script element mid-code, and {@code <!--}
     * can shift the parser into the escaped-script-data state. Both rewrites
     * are identity inside JS string literals — where alone user text can put
     * them — and generated DSL code never produces either outside one.
     * (The hydration-data escape — every {@code <} to {@code \u003C} —
     * would corrupt comparisons like {@code x<5} from
     * {@code whenVar(...).lessThan(5)}; it is only safe inside pure JSON.)
     */
    static String neutralize(String js) {
        return js.replaceAll("(?i)</script", "<\\\\/script").replace("<!--", "<\\!--");
    }

    /** First 10 hex chars of SHA-256 — stable, collision-safe at page scale. */
    private static String hash(String js) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(js.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(10);
            for (int i = 0; i < 5; i++) hex.append(String.format("%02x", digest[i]));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
