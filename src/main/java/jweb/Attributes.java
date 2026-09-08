package jweb;

import com.osmig.Jweb.framework.attributes.HtmlAttributes;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.ref.Ref;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.framework.transition.TransitionBuilder;
import com.osmig.Jweb.framework.transition.TransitionBuilder.TransitionReceiver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Fluent builder for HTML element attributes.
 * Provides type-safe methods for all common HTML attributes and event handlers.
 *
 * <p>Usage with Elements:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.elements.Elements.*;
 *
 * // Basic attributes
 * div(attrs().id("main").class_("container"))
 *
 * // Multiple classes
 * div(attrs().class_("card").addClass("featured"))
 *
 * // With inline styles: a style() argument of its own, or the lambda form
 * div(cls("box"), style().display(flex).padding(px(10)))
 * div(attrs().cls("box").style(s -&gt; s.display(flex).padding(px(10))).id("main"))
 *
 * // Form attributes
 * form(attrs()
 *     .action("/submit")
 *     .method("POST")
 *     .onSubmit(e -&gt; handleSubmit(e)))
 *
 * // Input with validation
 * input(attrs()
 *     .type("email")
 *     .name("email")
 *     .placeholder("you@example.com")
 *     .required())
 *
 * // Event handlers
 * button(attrs()
 *     .class_("btn")
 *     .onClick(e -&gt; count.set(count.get() + 1)),
 *     text("Click me"))
 * </pre>
 *
 * @see com.osmig.Jweb.framework.elements.Elements#attrs() for creating Attributes instances
 */
public class Attributes implements HtmlAttributes<Attributes> {

    /** Stores attribute name-value pairs in insertion order. */
    private final Map<String, String> attributes = new LinkedHashMap<>();

    /** Creates a new empty Attributes builder. */
    public Attributes() {}

    /**
     * Creates an Attributes builder with initial values.
     *
     * @param initial initial attribute map (can be null)
     */
    public Attributes(Map<String, String> initial) {
        if (initial != null) {
            this.attributes.putAll(initial);
        }
    }

    // ==================== Core Attributes ====================













    // ==================== Layout Shortcuts (deprecated) ====================

    /**
     * Appends CSS declarations to the {@code style} attribute instead of
     * replacing it, so an existing style (set before or after) survives.
     *
     * @param declarations {@code "prop:value;prop:value"} without a trailing {@code ;}
     * @return this for chaining
     */
    private Attributes appendStyle(String declarations) {
        String existing = attributes.get("style");
        if (existing == null || existing.isBlank()) {
            return set("style", declarations);
        }
        String trimmed = existing.stripTrailing();
        String separator = trimmed.endsWith(";") ? "" : ";";
        return set("style", trimmed + separator + declarations);
    }

    /**
     * Applies flexbox centering styles (display: flex; justify-content: center; align-items: center).
     * Merges into any style already set.
     *
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.flexCenter())} instead.
     */
    @Deprecated
    public Attributes flexCenter() {
        return appendStyle("display:flex;justify-content:center;align-items:center");
    }

    /**
     * Applies flexbox column layout with a gap. Merges into any style already set.
     *
     * @param gap the gap between items (CSS value like "1rem" or "10px")
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.display(flex).flexDirection(column).gap(...))} instead.
     */
    @Deprecated
    public Attributes flexColumn(String gap) {
        return appendStyle("display:flex;flex-direction:column;gap:" + gap);
    }

    /**
     * Applies flexbox column layout without gap. Merges into any style already set.
     *
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.flexCol())} instead.
     */
    @Deprecated
    public Attributes flexColumn() {
        return appendStyle("display:flex;flex-direction:column");
    }

    /**
     * Applies flexbox row layout with a gap. Merges into any style already set.
     *
     * @param gap the gap between items
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.flexRow().gap(...))} instead.
     */
    @Deprecated
    public Attributes flexRow(String gap) {
        return appendStyle("display:flex;flex-direction:row;gap:" + gap);
    }

    /**
     * Applies flexbox row layout without gap. Merges into any style already set.
     *
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.flexRow())} instead.
     */
    @Deprecated
    public Attributes flexRow() {
        return appendStyle("display:flex;flex-direction:row");
    }

    /**
     * Applies flexbox with space-between. Merges into any style already set.
     *
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.flexBetween())} instead.
     */
    @Deprecated
    public Attributes flexBetween() {
        return appendStyle("display:flex;justify-content:space-between;align-items:center");
    }

    /**
     * Applies CSS grid with a number of equal columns. Merges into any style already set.
     *
     * @param cols the number of columns
     * @param gap the gap between items
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.display(grid).gridTemplateColumns(...).gap(...))} instead.
     */
    @Deprecated
    public Attributes gridCols(int cols, String gap) {
        return appendStyle("display:grid;grid-template-columns:repeat(" + cols + ",1fr);gap:" + gap);
    }

    /**
     * Applies CSS grid with a number of equal columns, no gap. Merges into any style already set.
     *
     * @param cols the number of columns
     * @return this for chaining
     * @deprecated Use {@code attrs().style(s -> s.display(grid).gridTemplateColumns(...))} instead.
     */
    @Deprecated
    public Attributes gridCols(int cols) {
        return appendStyle("display:grid;grid-template-columns:repeat(" + cols + ",1fr)");
    }







    /**
     * Sets the inline style from a lambda — the one way to style inside an
     * {@code attrs()} chain, and the chain keeps going afterwards.
     *
     * <p>Example:</p>
     * <pre>
     * attrs()
     *     .cls("card")
     *     .style(s -> s.display(flex).padding(px(10)).backgroundColor(white))
     *     .id("main")
     * </pre>
     *
     * @param builder a lambda that configures the style
     * @return this for chaining
     */
    public Attributes style(UnaryOperator<InlineStyle> builder) {
        InlineStyle s = new InlineStyle(this);
        builder.apply(s);
        return s.complete();
    }

    /**
     * The inline style builder the {@link #style(UnaryOperator)} lambda
     * receives. It is a {@link Style} with a back-reference to the attributes
     * it belongs to, so an element argument carries both.
     *
     * <p>There is no {@code attrs().style()} starter and no {@code .done()}:
     * a style is either an element argument of its own or a lambda.</p>
     *
     * <pre>
     * a(href("/home"), style().color(blue).textDecoration(none), "Home")  // its own argument
     * a(attrs().href("/home").style(s -&gt; s.color(blue)).rel("me"), "Home") // inside a chain
     * </pre>
     */
    public static class InlineStyle extends Style<InlineStyle> {
        private final Attributes parent;

        public InlineStyle(Attributes parent) {
            this.parent = parent;
        }

        // Helper to finalize style and return parent. Conditional rules
        // (.hover, .at, .dark, …) become a generated class on the parent's
        // class attribute; the plain declarations stay inline.
        private Attributes complete() {
            parent.set("style", build());
            com.osmig.Jweb.framework.styles.PageStyles.addClass(
                parent.toMap(), com.osmig.Jweb.framework.styles.PageStyles.register(this));
            return parent;
        }

        /**
         * Returns the attributes map for element consumption.
         * Auto-finalizes the style into the parent attributes.
         *
         * @return the finalized attribute map
         */
        public Map<String, String> toMap() {
            return complete().toMap();
        }

        /**
         * Returns an immutable copy of the finalized attributes.
         * Auto-finalizes the style into the parent attributes.
         *
         * @return an immutable copy of the finalized attributes
         */
        public Map<String, String> buildAttrs() {
            return complete().build();
        }
    }

    // ==================== Common Attributes ====================














    // ==================== Form Attributes ====================









    // ==================== Boolean Attributes ====================
    // Uniform rule: every boolean attribute has a no-arg form and a
    // (boolean) conditional form, and is stored with a null value so it
    // renders bare — <input required> — matching Attr and Tag.














    // ==================== Data & ARIA Attributes ====================





    // ==================== Table Attributes ====================






    // ==================== Form Validation Attributes ====================


























    // ==================== Global Attributes ====================



















    // ==================== Link & Resource Attributes ====================













    // ==================== Image & Media Attributes ====================









    // ==================== Audio/Video Attributes ====================















    // ==================== Script Attributes ====================






    // ==================== Meta & Document Attributes ====================





    // ==================== SVG Attributes ====================
























    // ==================== Microdata Attributes ====================








    // ==================== Dialog & Details Attributes ====================




    // ==================== Meter & Progress Attributes ====================







    // ==================== Template & Web Component Attributes ====================



    // ==================== Semantic Data Attributes ====================



    // ==================== iframe Attributes ====================








    // ==================== Fragment Swaps (server-driven UI) ====================











    // ==================== Event Handlers ====================

























    // ==================== Additional Mouse Events ====================















    // ==================== Keyboard Events ====================



    // ==================== Drag & Drop Events ====================















    // ==================== Touch Events ====================









    // ==================== Scroll Event ====================



    // ==================== Details & Dialog Events ====================







    // ==================== Animation & Transition Events ====================









    // ==================== Media Events ====================





    // ==================== Clipboard Events ====================







    // ==================== JavaScript Action Event Handlers ====================
    // These allow using the Actions DSL directly with event attributes.
    // Usage: attrs().onClick(show("panel"))
    //        attrs().onClick(all(hide("loader"), show("content")))

























    // ==================== Generic Setters ====================

    /**
     * Sets any HTML attribute by name.
     * Use this for attributes that don't have a dedicated method.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().set("autocomplete", "off")
     * attrs().set("data-custom", "value")
     * </pre>
     *
     * @param name the attribute name
     * @param value the attribute value (null for boolean attributes)
     * @return this for chaining
     */
    public Attributes set(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Sets multiple attributes from a map.
     *
     * @param attrs map of attribute name-value pairs
     * @return this for chaining
     */
    public Attributes setAll(Map<String, String> attrs) {
        this.attributes.putAll(attrs);
        return this;
    }

    // ==================== Build ====================

    /**
     * Returns an unmodifiable copy of the attributes map.
     * Null values (bare boolean attributes) are preserved.
     *
     * @return unmodifiable map of attribute name-value pairs
     */
    public Map<String, String> build() {
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /**
     * Returns the mutable attributes map (for internal use).
     *
     * @return the internal attributes map
     */
    public Map<String, String> toMap() { return attributes; }

    // ==================== Refs ====================



    // ==================== Transitions ====================


    /**
     * Checks if any attributes have been set.
     *
     * @return true if no attributes have been set
     */
    public boolean isEmpty() { return attributes.isEmpty(); }

    /**
     * Gets an attribute value by name.
     *
     * @param name the attribute name
     * @return the attribute value, or null if not set
     */
    public String get(String name) { return attributes.get(name); }
}
