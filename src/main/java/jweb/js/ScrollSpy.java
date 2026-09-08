package jweb.js;

import static jweb.js.JsOpts.opts;

/**
 * An "on this page" rail: builds a link per heading, keeps the link for the
 * heading you are reading marked, and rebuilds itself whenever the content is
 * swapped.
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions()
 *     .does(scrollSpy("#toc", "h2, h3")
 *         .within(".docs-content")
 *         .linkClass("toc-link")
 *         .hasHeadingsClass("has-headings"))
 *     .build())
 * }</pre>
 *
 * <p>Each generated link carries {@code data-level} (2 for an {@code h2}, 3 for
 * an {@code h3}) and {@code data-index}, so indentation and size are CSS, not
 * inline styles. Headings without an id get one.</p>
 *
 * <p>The active heading is the last one at or above an activation line placed
 * 25% down the visible area (capped at 160px) — {@link #offset(int)} fixes it at
 * a pixel instead. At the very bottom of the scroll box the last heading wins,
 * since it may never reach the line.</p>
 */
public final class ScrollSpy extends Behavior {

    private final String nav;
    private final String headings;

    private String container;
    private String host;
    private String hostClass = "has-headings";
    private String activeClass = "active";
    private String linkClass;
    private Integer offset;
    private int scrollMargin = 24;

    /** Internal — start one with {@code scrollSpy(nav, headings)}. */
    public ScrollSpy(String navSelector, String headingsSelector) {
        this.nav = navSelector;
        this.headings = headingsSelector;
    }

    /**
     * The scrolling element the headings live in. Without it the spy watches
     * the window and looks for headings across the document.
     */
    public ScrollSpy within(String contentSelector) {
        this.container = contentSelector;
        return this;
    }

    /** The class put on each generated link. */
    public ScrollSpy linkClass(String className) {
        this.linkClass = className;
        return this;
    }

    /** The class marking the link for the heading in view ({@code active}). */
    public ScrollSpy activeClass(String className) {
        this.activeClass = className;
        return this;
    }

    /**
     * The class added while there is at least one heading — the hook for
     * hiding an empty rail. It lands on the nav's parent unless
     * {@link #host(String)} names another element.
     */
    public ScrollSpy hasHeadingsClass(String className) {
        this.hostClass = className;
        return this;
    }

    /** The element {@link #hasHeadingsClass} is applied to. */
    public ScrollSpy host(String selector) {
        this.host = selector;
        return this;
    }

    /** A fixed activation line, in pixels from the top of the scroll box. */
    public ScrollSpy offset(int px) {
        this.offset = px;
        return this;
    }

    /** Space left above a heading when its link is clicked (24px default). */
    public ScrollSpy scrollMargin(int px) {
        this.scrollMargin = px;
        return this;
    }

    @Override
    protected String install() {
        return "JWeb.scrollSpy("
            + opts().str("nav", nav)
                    .str("headings", headings)
                    .str("container", container)
                    .str("host", host)
                    .str("hostClass", hostClass)
                    .str("active", activeClass)
                    .str("linkClass", linkClass)
                    .num("offset", offset)
                    .num("scrollMargin", scrollMargin)
                    .js()
            + ")";
    }
}
