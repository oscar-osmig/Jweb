package jweb.js;

import jweb.Action;
import jweb.Val;

import static jweb.js.JsOpts.opts;
import static jweb.js.JsOpts.quote;

/**
 * Client-side navigation as an action: fetch a fragment, put it in a target,
 * and push a history entry — the same {@code swap()} the runtime performs for
 * {@code data-swap-get} links, reachable from any handler:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * button(onClick(navigate("/docs/content?section=state")
 *                    .target(".docs-content")
 *                    .push("/docs?section=state")
 *                    .cache(300_000)),
 *        "State")
 * }</pre>
 *
 * <p>{@link #cache(int)} shares one store with {@link jweb.Js#prefetch(String)}:
 * a link warmed on hover is already in it when the click arrives, and the swap
 * happens without a round trip.</p>
 */
public final class Navigate implements Action {

    private final String url;
    private final Val urlExpression;

    private String target;
    private String mode;
    private String push;
    private Val pushExpression;
    private Integer cacheTtl;

    /** The TTL {@link #prefetch()} uses — five minutes. */
    public static final int DEFAULT_TTL = 300_000;

    /** Internal — start one with {@code navigate(...)}. */
    public Navigate(String url, Val urlExpression) {
        this.url = url;
        this.urlExpression = urlExpression;
    }

    /** The element whose content is replaced, as a CSS selector. */
    public Navigate target(String selector) {
        this.target = selector;
        return this;
    }

    /** {@link #target(String)} from a handle. */
    public Navigate target(jweb.css.Selector target) {
        return target(target.build());
    }

    /** Replace the target element itself, not its children. */
    public Navigate replace() {
        this.mode = "outer";
        return this;
    }

    /** Morph the target instead of rewriting it — keeps focus and scroll. */
    public Navigate morph() {
        this.mode = "morph";
        return this;
    }

    /** The URL to show in the address bar (no history entry when unset). */
    public Navigate push(String browserUrl) {
        this.push = browserUrl;
        return this;
    }

    /** The URL to show in the address bar, as an expression. */
    public Navigate push(Val browserUrl) {
        this.pushExpression = browserUrl;
        return this;
    }

    /** Serve from — and fill — the swap cache for {@code ttlMs}. */
    public Navigate cache(int ttlMs) {
        this.cacheTtl = ttlMs;
        return this;
    }

    /** {@link #cache(int)} at the default five-minute TTL. */
    public Navigate prefetch() {
        return cache(DEFAULT_TTL);
    }

    @Override
    public String build() {
        return "JWeb.swap(" + (urlExpression != null ? urlExpression.js() : quote(url)) + ",null,"
            + opts().str("target", target)
                    .str("mode", mode)
                    .raw("push", pushExpression != null ? pushExpression.js()
                        : push != null ? quote(push) : null)
                    .num("cache", cacheTtl)
                    .js()
            + ")";
    }
}
