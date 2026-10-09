package jweb.js;

import static jweb.js.JsOpts.opts;

/**
 * Line numbers beside a {@code <textarea>}, measured so that they survive soft
 * wrapping — line 7 in the gutter is line 7 in the compiler's error message:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * inlineScript(actions()
 *     .does(lineGutter("#editor", "#lines").mirror("#mirror"))
 *     .build())
 * }</pre>
 *
 * <p>Each number's height comes from a hidden mirror laid out with the
 * textarea's own text metrics. Point {@link #mirror(String)} at an element
 * styled like the editor when the styling matters; otherwise the behavior
 * builds its own and copies the computed font metrics across.</p>
 *
 * <p>It re-measures on input and on any width change (a split-pane drag, a
 * collapsed sidebar, a window resize) through a ResizeObserver, and keeps the
 * gutter scrolled with the textarea. {@link jweb.Js#relineGutter(String)}
 * re-measures after code is replaced without an input event, and
 * {@link jweb.Js#markLine(String, Object)} highlights one line.</p>
 */
public final class LineGutter extends Behavior {

    private final String textarea;
    private final String gutter;
    private String mirror;
    private String errorClass = "errline";

    /** Internal — start one with {@code lineGutter(textarea, gutter)}. */
    public LineGutter(String textareaSelector, String gutterSelector) {
        this.textarea = textareaSelector;
        this.gutter = gutterSelector;
    }

    /** An existing hidden element to measure wrapped lines in. */
    public LineGutter mirror(String selector) {
        this.mirror = selector;
        return this;
    }

    /** {@link #mirror(String)} from a handle. */
    public LineGutter mirror(jweb.css.Selector mirror) {
        return mirror(mirror.build());
    }

    /** The class {@link jweb.Js#markLine} puts on the marked number. */
    public LineGutter errorClass(String className) {
        this.errorClass = className;
        return this;
    }

    @Override
    protected String install() {
        return "JWeb.lineGutter("
            + opts().str("textarea", textarea)
                    .str("gutter", gutter)
                    .str("mirror", mirror)
                    .str("errorClass", errorClass)
                    .js()
            + ")";
    }
}
