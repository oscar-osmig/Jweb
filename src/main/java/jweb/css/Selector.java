package jweb.css;

/**
 * Fluent builder for constructing complex CSS selectors.
 * Supports pseudo-classes, pseudo-elements, attribute selectors,
 * and combinators (descendant, child, sibling).
 *
 * <p>Example:</p>
 * <pre>
 * // Complex selector: .nav > li:hover > a
 * cls("nav").child("li").hover().child("a")
 *
 * // Multiple selectors: h1, h2, h3
 * tag("h1").or(tag("h2")).or(tag("h3"))
 *
 * // Attribute selector: input[type="text"]
 * tag("input").attr("type", "text")
 * </pre>
 */
public class Selector {
    private final StringBuilder sb = new StringBuilder();

    /** Appends the universal selector (*). */
    public Selector all() { sb.append("*"); return this; }

    /** Appends a tag/element selector. @param tagName the HTML tag name */
    public Selector tag(String tagName) { sb.append(tagName); return this; }

    /** Appends a class selector. @param className the class name (no leading dot) */
    public Selector cls(String className) { sb.append(".").append(className); return this; }

    /** Appends an ID selector. @param idName the ID (no leading hash) */
    public Selector id(String idName) { sb.append("#").append(idName); return this; }

    // ========== Pseudo-classes ==========

    /** Appends a pseudo-class. @param name the pseudo-class name */
    public Selector pseudo(String name) { sb.append(":").append(name); return this; }

    /** Appends :hover pseudo-class (mouse over). */
    public Selector hover() { return pseudo("hover"); }

    /** Appends :focus pseudo-class (focused element). */
    public Selector focus() { return pseudo("focus"); }

    /** Appends :active pseudo-class (being clicked). */
    public Selector active() { return pseudo("active"); }

    /** Appends :visited pseudo-class (visited links). */
    public Selector visited() { return pseudo("visited"); }

    /** Appends :first-child pseudo-class. */
    public Selector firstChild() { return pseudo("first-child"); }

    /** Appends :last-child pseudo-class. */
    public Selector lastChild() { return pseudo("last-child"); }

    /** Appends :nth-child(n) pseudo-class. @param n the child index (1-based) */
    public Selector nthChild(int n) { sb.append(":nth-child(").append(n).append(")"); return this; }

    /** Appends :nth-child() with a pattern. @param pattern e.g., "2n", "odd", "3n+1" */
    public Selector nthChild(String pattern) { sb.append(":nth-child(").append(pattern).append(")"); return this; }

    /** Appends :focus-visible pseudo-class (keyboard focus). */
    public Selector focusVisible() { return pseudo("focus-visible"); }

    /** Appends :focus-within pseudo-class (contains focused element). */
    public Selector focusWithin() { return pseudo("focus-within"); }

    /** Appends :disabled pseudo-class. */
    public Selector disabled() { return pseudo("disabled"); }

    /** Appends :enabled pseudo-class. */
    public Selector enabled() { return pseudo("enabled"); }

    /** Appends :checked pseudo-class (checked inputs). */
    public Selector checked() { return pseudo("checked"); }

    /** Appends :empty pseudo-class (no children). */
    public Selector empty() { return pseudo("empty"); }

    /** Appends :popover-open pseudo-class (a popover in its showing state). */
    public Selector popoverOpen() { return pseudo("popover-open"); }

    /** Appends :open pseudo-class (an open details, dialog or select). */
    public Selector open() { return pseudo("open"); }

    /** Appends :not() pseudo-class. @param inner the selector to negate */
    public Selector not(Selector inner) { sb.append(":not(").append(inner.build()).append(")"); return this; }

    /** Appends :has() pseudo-class. @param inner the selector to match */
    public Selector has(Selector inner) { sb.append(":has(").append(inner.build()).append(")"); return this; }

    /** Appends :has() with a raw selector string. @param selector the selector string */
    public Selector has(String selector) { sb.append(":has(").append(selector).append(")"); return this; }

    /** Appends :is() pseudo-class (matches any of the selectors). @param inner the selector */
    public Selector is(Selector inner) { sb.append(":is(").append(inner.build()).append(")"); return this; }

    /** Appends :is() with a raw selector string. */
    public Selector is(String selector) { sb.append(":is(").append(selector).append(")"); return this; }

    /** Appends :where() pseudo-class (like :is but no specificity). @param inner the selector */
    public Selector where(Selector inner) { sb.append(":where(").append(inner.build()).append(")"); return this; }

    /** Appends :where() with a raw selector string. */
    public Selector where(String selector) { sb.append(":where(").append(selector).append(")"); return this; }

    /** Appends :nth-last-child() pseudo-class. */
    public Selector nthLastChild(int n) { sb.append(":nth-last-child(").append(n).append(")"); return this; }

    /** Appends :nth-last-child() with pattern. */
    public Selector nthLastChild(String pattern) { sb.append(":nth-last-child(").append(pattern).append(")"); return this; }

    /** Appends :nth-of-type() pseudo-class. */
    public Selector nthOfType(int n) { sb.append(":nth-of-type(").append(n).append(")"); return this; }

    /** Appends :nth-of-type() with pattern. */
    public Selector nthOfType(String pattern) { sb.append(":nth-of-type(").append(pattern).append(")"); return this; }

    /** Appends :nth-last-of-type() pseudo-class. */
    public Selector nthLastOfType(int n) { sb.append(":nth-last-of-type(").append(n).append(")"); return this; }

    /** Appends :nth-last-of-type() with pattern. */
    public Selector nthLastOfType(String pattern) { sb.append(":nth-last-of-type(").append(pattern).append(")"); return this; }

    /** Appends :first-of-type pseudo-class. */
    public Selector firstOfType() { return pseudo("first-of-type"); }

    /** Appends :last-of-type pseudo-class. */
    public Selector lastOfType() { return pseudo("last-of-type"); }

    /** Appends :only-child pseudo-class. */
    public Selector onlyChild() { return pseudo("only-child"); }

    /** Appends :only-of-type pseudo-class. */
    public Selector onlyOfType() { return pseudo("only-of-type"); }

    /** Appends :root pseudo-class (html element). */
    public Selector root() { return pseudo("root"); }

    /** Appends :target pseudo-class (URL fragment target). */
    public Selector target() { return pseudo("target"); }

    /** Appends :any-link pseudo-class (any link). */
    public Selector anyLink() { return pseudo("any-link"); }

    /** Appends :link pseudo-class (unvisited links). */
    public Selector link() { return pseudo("link"); }

    /** Appends :lang() pseudo-class. @param lang the language code */
    public Selector lang(String lang) { sb.append(":lang(").append(lang).append(")"); return this; }

    // Form-related pseudo-classes
    /** Appends :required pseudo-class. */
    public Selector required() { return pseudo("required"); }

    /** Appends :optional pseudo-class. */
    public Selector optional() { return pseudo("optional"); }

    /** Appends :valid pseudo-class. */
    public Selector valid() { return pseudo("valid"); }

    /** Appends :invalid pseudo-class. */
    public Selector invalid() { return pseudo("invalid"); }

    /** Appends :in-range pseudo-class. */
    public Selector inRange() { return pseudo("in-range"); }

    /** Appends :out-of-range pseudo-class. */
    public Selector outOfRange() { return pseudo("out-of-range"); }

    /** Appends :read-only pseudo-class. */
    public Selector readOnly() { return pseudo("read-only"); }

    /** Appends :read-write pseudo-class. */
    public Selector readWrite() { return pseudo("read-write"); }

    /** Appends :placeholder-shown pseudo-class. */
    public Selector placeholderShown() { return pseudo("placeholder-shown"); }

    /** Appends :default pseudo-class (default form element). */
    public Selector default_() { return pseudo("default"); }

    /** Appends :indeterminate pseudo-class. */
    public Selector indeterminate() { return pseudo("indeterminate"); }

    /** Appends :autofill pseudo-class. */
    public Selector autofill() { return pseudo("autofill"); }

    // State pseudo-classes
    /** Appends :fullscreen pseudo-class. */
    public Selector fullscreen() { return pseudo("fullscreen"); }

    /** Appends :modal pseudo-class. */
    public Selector modal() { return pseudo("modal"); }

    /** Appends :picture-in-picture pseudo-class. */
    public Selector pictureInPicture() { return pseudo("picture-in-picture"); }

    /** Appends :paused pseudo-class (media paused). */
    public Selector paused() { return pseudo("paused"); }

    /** Appends :playing pseudo-class (media playing). */
    public Selector playing() { return pseudo("playing"); }

    /** Appends :current pseudo-class. */
    public Selector current() { return pseudo("current"); }

    /** Appends :past pseudo-class. */
    public Selector past() { return pseudo("past"); }

    /** Appends :future pseudo-class. */
    public Selector future() { return pseudo("future"); }

    // ========== Pseudo-elements ==========

    /** Appends a pseudo-element. @param name the pseudo-element name */
    public Selector pseudoEl(String name) { sb.append("::").append(name); return this; }

    /** Appends ::before pseudo-element. */
    public Selector before() { return pseudoEl("before"); }

    /** Appends ::after pseudo-element. */
    public Selector after() { return pseudoEl("after"); }

    /** Appends ::placeholder pseudo-element. */
    public Selector placeholder() { return pseudoEl("placeholder"); }

    /** Appends ::selection pseudo-element (selected text). */
    public Selector selection() { return pseudoEl("selection"); }

    /** Appends ::first-line pseudo-element. */
    public Selector firstLine() { return pseudoEl("first-line"); }

    /** Appends ::first-letter pseudo-element. */
    public Selector firstLetter() { return pseudoEl("first-letter"); }

    /** Appends ::marker pseudo-element (list marker). */
    public Selector marker() { return pseudoEl("marker"); }

    /** Appends ::backdrop pseudo-element (fullscreen/dialog). */
    public Selector backdrop() { return pseudoEl("backdrop"); }

    /** Appends ::cue pseudo-element (WebVTT captions). */
    public Selector cue() { return pseudoEl("cue"); }

    /** Appends ::cue() with selector. */
    public Selector cue(String selector) { sb.append("::cue(").append(selector).append(")"); return this; }

    /** Appends ::file-selector-button pseudo-element. */
    public Selector fileSelectorButton() { return pseudoEl("file-selector-button"); }

    /** Appends ::slotted() pseudo-element for shadow DOM. */
    public Selector slotted(String selector) { sb.append("::slotted(").append(selector).append(")"); return this; }

    /** Appends ::part() pseudo-element for shadow DOM parts. */
    public Selector part(String name) { sb.append("::part(").append(name).append(")"); return this; }

    /** Appends ::highlight() pseudo-element. */
    public Selector highlight(String name) { sb.append("::highlight(").append(name).append(")"); return this; }

    /** Appends ::spelling-error pseudo-element. */
    public Selector spellingError() { return pseudoEl("spelling-error"); }

    /** Appends ::grammar-error pseudo-element. */
    public Selector grammarError() { return pseudoEl("grammar-error"); }

    /** Appends ::target-text pseudo-element. */
    public Selector targetText() { return pseudoEl("target-text"); }

    // ========== View Transition Pseudo-elements ==========

    /** Appends ::view-transition pseudo-element (root of all view transitions). */
    public Selector viewTransition() { return pseudoEl("view-transition"); }

    /** Appends ::view-transition-group(*) pseudo-element. @param name the transition name */
    public Selector viewTransitionGroup(String name) { sb.append("::view-transition-group(").append(name).append(")"); return this; }

    /** Appends ::view-transition-image-pair(*) pseudo-element. @param name the transition name */
    public Selector viewTransitionImagePair(String name) { sb.append("::view-transition-image-pair(").append(name).append(")"); return this; }

    /** Appends ::view-transition-old(*) pseudo-element (outgoing snapshot). @param name the transition name */
    public Selector viewTransitionOld(String name) { sb.append("::view-transition-old(").append(name).append(")"); return this; }

    /** Appends ::view-transition-new(*) pseudo-element (incoming snapshot). @param name the transition name */
    public Selector viewTransitionNew(String name) { sb.append("::view-transition-new(").append(name).append(")"); return this; }

    // ========== Attribute Selectors ==========

    /** Appends [attr] (has attribute). @param name the attribute name */
    public Selector attr(String name) { sb.append("[").append(name).append("]"); return this; }

    /** Appends [attr="value"]. @param name the attribute name @param value the exact value */
    public Selector attr(String name, String value) { sb.append("[").append(name).append("=\"").append(value).append("\"]"); return this; }

    /** Appends [attr*="value"] (contains). @param name the attribute name @param value the substring */
    public Selector attrContains(String name, String value) { sb.append("[").append(name).append("*=\"").append(value).append("\"]"); return this; }

    /** Appends [attr^="value"] (starts with). @param name the attribute name @param value the prefix */
    public Selector attrStartsWith(String name, String value) { sb.append("[").append(name).append("^=\"").append(value).append("\"]"); return this; }

    /** Appends [attr$="value"] (ends with). @param name the attribute name @param value the suffix */
    public Selector attrEndsWith(String name, String value) { sb.append("[").append(name).append("$=\"").append(value).append("\"]"); return this; }

    // ========== Combinators ==========

    /** Appends descendant combinator (space). Matches any descendant. */
    public Selector descendant(String selector) { sb.append(" ").append(selector); return this; }

    /** Appends descendant combinator (space). Matches any descendant. */
    public Selector descendant(Selector selector) { sb.append(" ").append(selector.build()); return this; }

    /** Appends child combinator (&gt;). Matches direct children only. */
    public Selector child(String selector) { sb.append(" > ").append(selector); return this; }

    /** Appends child combinator (&gt;). Matches direct children only. */
    public Selector child(Selector selector) { sb.append(" > ").append(selector.build()); return this; }

    /** Appends adjacent sibling combinator (+). Matches immediately following sibling. */
    public Selector adjacent(String selector) { sb.append(" + ").append(selector); return this; }

    /** Appends adjacent sibling combinator (+). Matches immediately following sibling. */
    public Selector adjacent(Selector selector) { sb.append(" + ").append(selector.build()); return this; }

    /** Appends general sibling combinator (~). Matches any following sibling. */
    public Selector sibling(String selector) { sb.append(" ~ ").append(selector); return this; }

    /** Appends general sibling combinator (~). Matches any following sibling. */
    public Selector sibling(Selector selector) { sb.append(" ~ ").append(selector.build()); return this; }

    /** Combines with another selector (comma-separated grouping). */
    public Selector or(String selector) { sb.append(", ").append(selector); return this; }

    /** Combines with another selector (comma-separated grouping). */
    public Selector or(Selector selector) { sb.append(", ").append(selector.build()); return this; }

    /** Appends raw CSS string for edge cases not covered by builder methods. */
    public Selector raw(String s) { sb.append(s); return this; }

    /** Builds and returns the complete selector string. @return the CSS selector */
    public String build() { return sb.toString(); }

    @Override public String toString() { return build(); }
}
