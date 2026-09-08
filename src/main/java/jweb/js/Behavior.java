package jweb.js;

import jweb.Action;

/**
 * A page behavior — a chunk of client work that installs itself once and then
 * keeps running: a scroll spy, a draggable split pane, a line gutter, link
 * prefetching. Every behavior is an {@link Action}, so it goes wherever an
 * action goes:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions()
 *     .does(scrollSpy("#toc", ".content h2, .content h3").linkClass("toc-link"))
 *     .build())
 * }</pre>
 *
 * <p>The generated JavaScript queues the install on the client runtime instead
 * of running it inline: a page's own scripts parse <em>before</em> the runtime
 * does, and a behavior needs the runtime (and the DOM) to be up. The queue is
 * drained on init, and anything queued afterwards — from a swapped fragment,
 * say — installs immediately.</p>
 *
 * <p>Event-time verbs ({@code copy}, {@code navigate}, {@code markLine}) are
 * plain Actions instead: by the time a click runs them the runtime is there.</p>
 */
public abstract class Behavior implements Action {

    /** The JavaScript that installs this behavior, runtime and DOM ready. */
    protected abstract String install();

    @Override
    public final String build() {
        return "(window.__JWEB_READY__=window.__JWEB_READY__||[]).push(function(){"
            + install() + "})";
    }
}
