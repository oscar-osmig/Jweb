package jweb.js;

import static jweb.js.JsOpts.opts;

/**
 * Keeps the nav link whose {@code href} is the page you are on marked, across
 * client-side navigation and the back button:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions().does(activeLink(".docs-nav-link")).build())
 * }</pre>
 *
 * <p>It only reacts to navigation — a {@code jweb:swap} or a {@code popstate} —
 * so the server's own rendering of the active link is left alone on first
 * paint.</p>
 */
public final class ActiveLink extends Behavior {

    private final String selector;
    private String activeClass = "active";

    /** Internal — start one with {@code activeLink(selector)}. */
    public ActiveLink(String linkSelector) {
        this.selector = linkSelector;
    }

    /** The class marking the current link ({@code active} by default). */
    public ActiveLink activeClass(String className) {
        this.activeClass = className;
        return this;
    }

    @Override
    protected String install() {
        return "JWeb.activeLink("
            + opts().str("selector", selector).str("active", activeClass).js() + ")";
    }
}
