package jweb.css;

import com.osmig.Jweb.framework.styles.CSS.Selector;

/**
 * The CSS selector builder — for composing a selector rather than writing it:
 *
 * <pre>{@code
 * import static jweb.Css.*;
 * import static jweb.css.Selectors.*;
 *
 * rule(cls("card").hover())                 // .card:hover
 * rule(tag("li").nthChild("2n+1"))          // li:nth-child(2n+1)
 * rule(cls("form").has("input:invalid"))    // .form:has(input:invalid)
 * rule(select().tag("nav").child("a"))      // nav > a
 * }</pre>
 *
 * <p>These starters are deliberately <em>not</em> part of {@code jweb.Css}:
 * {@code id}, {@code tag} and {@code select} are also HTML DSL names, and
 * sharing them made {@code id("x")} ambiguous under the usual
 * {@code El.*} + {@code Css.*} imports. When you already know the selector,
 * a plain String is the simplest form: {@code rule(".card:hover")}.</p>
 *
 * <p>The legacy String-returning helpers ({@code has(...)}, {@code not(...)},
 * ...) inherited from the framework module remain available but are
 * deprecated — the builder above composes; they only concatenated.</p>
 */
@SuppressWarnings("deprecation")
public class Selectors extends com.osmig.Jweb.framework.styles.Selectors {

    protected Selectors() {}

    /** An empty selector to build up: {@code select().tag("div").cls("container").hover()}. */
    public static Selector select() {
        return new Selector();
    }

    /** The universal selector: {@code rule(all()).boxSizing(borderBox)} → {@code * { ... }}. */
    public static Selector all() {
        return new Selector().all();
    }

    /** A type selector: {@code tag("button")} → {@code button}. */
    public static Selector tag(String tagName) {
        return new Selector().tag(tagName);
    }

    /** A class selector: {@code cls("btn").hover()} → {@code .btn:hover}. */
    public static Selector cls(String className) {
        return new Selector().cls(className);
    }

    /** An ID selector: {@code id("header")} → {@code #header}. */
    public static Selector id(String idName) {
        return new Selector().id(idName);
    }
}
