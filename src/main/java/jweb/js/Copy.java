package jweb.js;

import jweb.Action;
import jweb.Val;

import static jweb.js.JsOpts.opts;
import static jweb.js.JsOpts.quote;

/**
 * Copy-to-clipboard as an action — {@code copy(text)} for a literal,
 * {@code copyFrom(selector)} for whatever the nearest matching element says:
 *
 * <pre>{@code
 * import static jweb.Js.*;
 *
 * button(onClick(copy("npm i jweb").feedback("Copied!")), "Copy")
 * button(class_("code-copy-btn"),
 *        onClick(copyFrom("pre").feedback("Copied!").feedbackClass("copied")),
 *        "Copy")
 * }</pre>
 *
 * <p>The clipboard write goes through {@code navigator.clipboard} with the
 * {@code execCommand} fallback built in, so it also works on http origins and
 * in browsers that never got the async API.</p>
 *
 * <p>"Nearest" for {@link jweb.Js#copyFrom} means: the trigger's own subtree,
 * then each ancestor's, then the document — so a copy button placed beside a
 * {@code <pre>} finds that {@code <pre>} and no other.</p>
 */
public final class Copy implements Action {

    private final String literal;
    private final Val expression;
    private final String selector;

    private Val trigger = new Val("this");
    private String feedback;
    private String failText;
    private String feedbackClass;
    private int ms = 1600;

    /** Internal — start one with {@code copy(...)} or {@code copyFrom(...)}. */
    public Copy(String literal, Val expression, String selector) {
        this.literal = literal;
        this.expression = expression;
        this.selector = selector;
    }

    /**
     * The element the feedback lands on, and where the {@code copyFrom} search
     * starts. Defaults to {@code this} — the element the handler is on, which
     * is what an {@code onClick(copy(...))} argument wants. Inside a
     * {@code delegate(...)} callback pass the matched element: {@code .trigger(v("t"))}.
     */
    public Copy trigger(Val element) {
        this.trigger = element;
        return this;
    }

    /** Swap the trigger's text to {@code text} for 1.6s after a successful copy. */
    public Copy feedback(String text) {
        this.feedback = text;
        return this;
    }

    /** Swap the trigger's text to {@code text} for {@code ms} after a successful copy. */
    public Copy feedback(String text, int ms) {
        this.feedback = text;
        this.ms = ms;
        return this;
    }

    /** The text shown when the copy fails ({@code "Copy failed"} by default). */
    public Copy failText(String text) {
        this.failText = text;
        return this;
    }

    /** A class toggled on the trigger while the feedback shows. */
    public Copy feedbackClass(String className) {
        this.feedbackClass = className;
        return this;
    }

    /** {@link #feedbackClass(String)} from a handle. */
    public Copy feedbackClass(jweb.Cls cls) {
        return feedbackClass(cls.name());
    }

    @Override
    public String build() {
        String source = selector != null
            ? "(JWeb.nearest(" + trigger.js() + "," + quote(selector) + ")||{}).textContent||''"
            : expression != null ? expression.js() : quote(literal);
        return "JWeb.copyText(" + source + "," + trigger.js() + ","
            + opts().str("text", feedback)
                    .str("failText", failText)
                    .str("className", feedbackClass)
                    .num("ms", feedback == null ? null : ms)
                    .js()
            + ")";
    }
}
