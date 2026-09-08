package jweb.js;

import jweb.Val;

import static jweb.js.JsOpts.opts;

/**
 * Warms the swap cache for a set of links, so the navigation that follows is
 * instant:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions()
 *     .does(prefetch(".docs-nav-link").within(".docs-sidebar").delay(50))
 *     .build())
 * }</pre>
 *
 * <p>The URL comes from {@code data-prefetch}, else {@code data-swap-get}, else
 * {@code href} — so links already marked up for {@link jweb.El#swap} need no
 * extra attribute. {@link #url(Val)} overrides that when the fetched URL is
 * derived from the link rather than written on it.</p>
 *
 * <p>Hover is debounced (a pointer crossing a list should not fetch every item)
 * and keyboard focus counts as intent too. {@link #onVisible()} switches to an
 * IntersectionObserver instead, for lists where everything on screen is likely
 * to be clicked.</p>
 */
public final class Prefetch extends Behavior {

    private final String selector;
    private String scope;
    private Integer delay;
    private int ttl = Navigate.DEFAULT_TTL;
    private boolean visible;
    private Val url;

    /** Internal — start one with {@code prefetch(selector)}. */
    public Prefetch(String selector) {
        this.selector = selector;
    }

    /** Only watch links inside this element (the whole document by default). */
    public Prefetch within(String scopeSelector) {
        this.scope = scopeSelector;
        return this;
    }

    /** Prefetch on hover — the default. */
    public Prefetch onHover() {
        this.visible = false;
        return this;
    }

    /** Prefetch when the link scrolls into view instead of on hover. */
    public Prefetch onVisible() {
        this.visible = true;
        return this;
    }

    /** How long the pointer must rest before the fetch starts (60ms default). */
    public Prefetch delay(int ms) {
        this.delay = ms;
        return this;
    }

    /** How long a prefetched response stays usable (five minutes by default). */
    public Prefetch cache(int ttlMs) {
        this.ttl = ttlMs;
        return this;
    }

    /**
     * The URL to fetch, derived from the link — which the expression names
     * {@code t}: {@code .url(v("t").dot("href").replace("/docs?", "/docs/content?"))}.
     */
    public Prefetch url(Val fromLink) {
        this.url = fromLink;
        return this;
    }

    @Override
    protected String install() {
        return "JWeb.prefetchOn("
            + opts().str("selector", selector)
                    .str("scope", scope)
                    .num("delay", delay)
                    .num("ttl", ttl)
                    .flag("visible", visible)
                    .fn("url", "t", url)
                    .js()
            + ")";
    }
}
