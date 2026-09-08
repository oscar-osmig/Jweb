package jweb.js;

import static jweb.js.JsOpts.opts;

/**
 * A draggable divider between two panes:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions()
 *     .does(splitPane("#gutter", "#code").minPercent(20).maxPercent(80))
 *     .build())
 * }</pre>
 *
 * <p>Dragging sets {@code flex: 0 0 <pct>%} on the left pane and puts a
 * {@code dragging} class on both the handle and the flex container while the
 * mouse is down — the hooks for a highlight and for suppressing text selection.
 * {@link #persist(String)} remembers the split in {@code localStorage}.</p>
 */
public final class SplitPane extends Behavior {

    private final String handle;
    private final String left;

    private String container;
    private Integer min;
    private Integer max;
    private Double minPercent;
    private Double maxPercent;
    private String persistKey;

    /** Internal — start one with {@code splitPane(handle, left)}. */
    public SplitPane(String handleSelector, String leftSelector) {
        this.handle = handleSelector;
        this.left = leftSelector;
    }

    /** The flex container the two panes sit in (the handle's parent by default). */
    public SplitPane container(String selector) {
        this.container = selector;
        return this;
    }

    /** Smallest the left pane may get, in pixels. */
    public SplitPane min(int px) {
        this.min = px;
        return this;
    }

    /** Largest the left pane may get, in pixels. */
    public SplitPane max(int px) {
        this.max = px;
        return this;
    }

    /** Smallest the left pane may get, as a percentage of the container (10 by default). */
    public SplitPane minPercent(double percent) {
        this.minPercent = percent;
        return this;
    }

    /** Largest the left pane may get, as a percentage of the container (90 by default). */
    public SplitPane maxPercent(double percent) {
        this.maxPercent = percent;
        return this;
    }

    /** Remember the split under this {@code localStorage} key. */
    public SplitPane persist(String key) {
        this.persistKey = key;
        return this;
    }

    @Override
    protected String install() {
        return "JWeb.splitPane("
            + opts().str("handle", handle)
                    .str("left", left)
                    .str("container", container)
                    .num("min", min)
                    .num("max", max)
                    .num("minPercent", minPercent)
                    .num("maxPercent", maxPercent)
                    .str("persist", persistKey)
                    .js()
            + ")";
    }
}
