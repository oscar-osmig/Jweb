package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.attributes.Attributes.InlineStyle;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import com.osmig.Jweb.framework.core.ErrorBoundary;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Static factory methods for creating HTML elements.
 * This is the primary API for building HTML in Java using a fluent DSL.
 *
 * <p>Usage with static import:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.elements.Elements.*;
 *
 * // Simple elements
 * div(class_("container"), id("main"),
 *     h1("Hello World"),
 *     p(class_("lead"), "Welcome!")
 * )
 *
 * // With attributes builder
 * div(attrs().class_("card").id("main").style(style().padding(px(10))),
 *     h2("Card Title"),
 *     p("Card content...")
 * )
 *
 * // Forms
 * form(attrs().action("/submit").method("POST"),
 *     label(for_("email"), "Email:"),
 *     input(attrs().type("email").name("email").placeholder("you@example.com")),
 *     button(type("submit"), "Subscribe")
 * )
 *
 * // Lists with iteration
 * ul(each(items, item -&gt; li(item.getName())))
 *
 * // Conditional rendering
 * when(isLoggedIn, () -&gt; span("Welcome, " + user.getName()))
 * </pre>
 *
 * <p>This class provides factory methods for all standard HTML elements organized by category:</p>
 * <ul>
 *   <li><b>Document:</b> html, head, body, title, meta, link, script, style</li>
 *   <li><b>Semantic:</b> header, footer, nav, main, section, article, aside</li>
 *   <li><b>Headings:</b> h1-h6</li>
 *   <li><b>Text:</b> p, span, div, strong, em, code, pre, blockquote</li>
 *   <li><b>Links:</b> a</li>
 *   <li><b>Lists:</b> ul, ol, li, dl, dt, dd</li>
 *   <li><b>Tables:</b> table, thead, tbody, tr, th, td</li>
 *   <li><b>Forms:</b> form, input, textarea, select, option, button, label</li>
 *   <li><b>Media:</b> img, video, audio, canvas, svg, iframe</li>
 * </ul>
 *
 * @see Attributes for building element attributes
 * @see Attr for individual attribute shortcuts
 */
public final class Elements {

    private Elements() {}

    // ==================== Attribute Builder ====================

    /**
     * Creates a new Attributes builder for complex attribute combinations.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs()
     *     .class_("card")
     *     .id("main")
     *     .style(style().padding(px(10)))
     *     .data("user-id", "123"),
     *     "Content..."
     * )
     * </pre>
     *
     * @return a new Attributes builder
     */
    public static Attributes attrs() { return new Attributes(); }

    // ==================== Attribute Shortcuts ====================
    // These provide convenient single-attribute shortcuts.
    // Use attrs() for combining multiple attributes.

    /** Creates an id attribute. @param value the element ID */
    public static Attr id(String value) { return Attr.id(value); }
    /** Creates a class attribute. Named class_ to avoid Java keyword conflict. @param value the CSS class(es) */
    public static Attr class_(String value) { return Attr.class_(value); }
    /** Creates an inline style attribute. @param value the CSS styles */
    public static Attr style_(String value) { return Attr.style(value); }
    /** Creates an href attribute for links. @param value the URL */
    public static Attr href(String value) { return Attr.href(value); }
    /** Creates a src attribute for images/scripts. @param value the source URL */
    public static Attr src(String value) { return Attr.src(value); }
    /** Creates an alt attribute for images. @param value the alt text */
    public static Attr alt(String value) { return Attr.alt(value); }
    /** Creates a type attribute. @param value the type (e.g., "text", "submit") */
    public static Attr type(String value) { return Attr.type(value); }
    /** Creates a name attribute for form elements. @param value the name */
    public static Attr name(String value) { return Attr.name(value); }
    /** Creates a value attribute. @param value the value */
    public static Attr value(String value) { return Attr.value(value); }
    /** Creates a placeholder attribute for inputs. @param value the placeholder text */
    public static Attr placeholder(String value) { return Attr.placeholder(value); }
    /** Creates an action attribute for forms. @param value the form action URL */
    public static Attr action(String value) { return Attr.action(value); }
    /** Creates a method attribute for forms. @param value the HTTP method */
    public static Attr method(String value) { return Attr.method(value); }
    /** Creates a target attribute for links. @param value the target (e.g., "_blank") */
    public static Attr target(String value) { return Attr.target(value); }
    /** Creates a title attribute. Named title_ to avoid conflict. @param value the title text */
    public static Attr title_(String value) { return Attr.title(value); }
    /** Creates a for attribute for labels. Named for_ to avoid Java keyword. @param value the target element ID */
    public static Attr for_(String value) { return Attr.for_(value); }
    /** Creates a role attribute for ARIA. @param value the ARIA role */
    public static Attr role(String value) { return Attr.role(value); }
    /** Creates a disabled boolean attribute. */
    public static Attr disabled() { return Attr.disabled(); }
    /** Creates a checked boolean attribute for checkboxes/radios. */
    public static Attr checked() { return Attr.checked(); }
    /** Creates a required boolean attribute. */
    public static Attr required() { return Attr.required(); }
    /** Creates a readonly boolean attribute. */
    public static Attr readonly() { return Attr.readonly(); }
    /** Creates a hidden boolean attribute. */
    public static Attr hidden() { return Attr.hidden(); }
    /** Creates an autofocus boolean attribute. */
    public static Attr autofocus() { return Attr.autofocus(); }
    /** Creates a data-* attribute. @param name the data name (without "data-" prefix) @param value the value */
    public static Attr data(String name, String value) { return Attr.data(name, value); }
    /** Creates an aria-* attribute. @param name the aria name (without "aria-" prefix) @param value the value */
    public static Attr aria(String name, String value) { return Attr.aria(name, value); }
    /** Creates any custom attribute. @param name the attribute name @param value the value */
    public static Attr attr(String name, String value) { return Attr.attr(name, value); }

    // ==================== Document Structure ====================

    public static Tag html(Object... children) { return tag("html", children); }
    public static Tag html(Attributes attrs, Object... children) { return tag("html", attrs, children); }
    public static Tag head(Object... children) { return tag("head", children); }
    public static Tag body(Object... children) { return tag("body", children); }
    public static Tag body(Attributes attrs, Object... children) { return tag("body", attrs, children); }

    // ==================== Head Elements ====================

    public static Tag title(String text) { return tag("title", text); }
    public static Tag meta(Object... attrs) { return tag("meta", attrs); }
    public static Tag meta(Attributes attrs) { return new Tag("meta", attrs); }
    public static Tag meta(String name, String content) {
        return tag("meta", new Attributes().name(name).set("content", content));
    }
    public static Tag link(Object... attrs) { return tag("link", attrs); }
    public static Tag link(Attributes attrs) { return new Tag("link", attrs); }
    public static Tag css(String href) {
        return tag("link", new Attributes().set("rel", "stylesheet").href(href));
    }
    public static Tag script(String src) { return tag("script", new Attributes().src(src)); }
    public static Tag inlineScript(String code) { return tag("script", TextElement.raw(code)); }
    public static Tag style(String css) { return tag("style", TextElement.raw(css)); }

    // ==================== Semantic Structure ====================

    public static Tag header(Object... children) { return tag("header", children); }
    public static Tag header(Attributes attrs, Object... children) { return tag("header", attrs, children); }
    public static Tag footer(Object... children) { return tag("footer", children); }
    public static Tag footer(Attributes attrs, Object... children) { return tag("footer", attrs, children); }
    public static Tag nav(Object... children) { return tag("nav", children); }
    public static Tag nav(Attributes attrs, Object... children) { return tag("nav", attrs, children); }
    public static Tag main(Object... children) { return tag("main", children); }
    public static Tag main(Attributes attrs, Object... children) { return tag("main", attrs, children); }
    public static Tag section(Object... children) { return tag("section", children); }
    public static Tag section(Attributes attrs, Object... children) { return tag("section", attrs, children); }
    public static Tag article(Object... children) { return tag("article", children); }
    public static Tag article(Attributes attrs, Object... children) { return tag("article", attrs, children); }
    public static Tag aside(Object... children) { return tag("aside", children); }
    public static Tag aside(Attributes attrs, Object... children) { return tag("aside", attrs, children); }
    public static Tag figure(Object... children) { return tag("figure", children); }
    public static Tag figure(Attributes attrs, Object... children) { return tag("figure", attrs, children); }
    public static Tag figcaption(Object... children) { return tag("figcaption", children); }
    public static Tag hgroup(Object... children) { return tag("hgroup", children); }
    public static Tag hgroup(Attributes attrs, Object... children) { return tag("hgroup", attrs, children); }
    public static Tag search(Object... children) { return tag("search", children); }
    public static Tag search(Attributes attrs, Object... children) { return tag("search", attrs, children); }

    // ==================== Headings ====================

    public static Tag h1(Object... children) { return tag("h1", children); }
    public static Tag h1(Attributes attrs, Object... children) { return tag("h1", attrs, children); }
    public static Tag h2(Object... children) { return tag("h2", children); }
    public static Tag h2(Attributes attrs, Object... children) { return tag("h2", attrs, children); }
    public static Tag h3(Object... children) { return tag("h3", children); }
    public static Tag h3(Attributes attrs, Object... children) { return tag("h3", attrs, children); }
    public static Tag h4(Object... children) { return tag("h4", children); }
    public static Tag h4(Attributes attrs, Object... children) { return tag("h4", attrs, children); }
    public static Tag h5(Object... children) { return tag("h5", children); }
    public static Tag h5(Attributes attrs, Object... children) { return tag("h5", attrs, children); }
    public static Tag h6(Object... children) { return tag("h6", children); }
    public static Tag h6(Attributes attrs, Object... children) { return tag("h6", attrs, children); }

    // ==================== Text Content ====================

    public static Tag p(Object... children) { return tag("p", children); }
    public static Tag p(Attributes attrs, Object... children) { return tag("p", attrs, children); }
    public static Tag span(Object... children) { return tag("span", children); }
    public static Tag span(Attributes attrs, Object... children) { return tag("span", attrs, children); }
    public static Tag div(Object... children) { return tag("div", children); }
    public static Tag div(Attributes attrs, Object... children) { return tag("div", attrs, children); }
    public static Tag strong(Object... children) { return tag("strong", children); }
    public static Tag strong(Attributes attrs, Object... children) { return tag("strong", attrs, children); }
    public static Tag em(Object... children) { return tag("em", children); }
    public static Tag em(Attributes attrs, Object... children) { return tag("em", attrs, children); }
    public static Tag b(Object... children) { return tag("b", children); }
    public static Tag b(Attributes attrs, Object... children) { return tag("b", attrs, children); }
    public static Tag i(Object... children) { return tag("i", children); }
    public static Tag i(Attributes attrs, Object... children) { return tag("i", attrs, children); }
    public static Tag u(Object... children) { return tag("u", children); }
    public static Tag u(Attributes attrs, Object... children) { return tag("u", attrs, children); }
    public static Tag small(Object... children) { return tag("small", children); }
    public static Tag small(Attributes attrs, Object... children) { return tag("small", attrs, children); }
    public static Tag mark(Object... children) { return tag("mark", children); }
    public static Tag mark(Attributes attrs, Object... children) { return tag("mark", attrs, children); }
    public static Tag del(Object... children) { return tag("del", children); }
    public static Tag del(Attributes attrs, Object... children) { return tag("del", attrs, children); }
    public static Tag ins(Object... children) { return tag("ins", children); }
    public static Tag ins(Attributes attrs, Object... children) { return tag("ins", attrs, children); }
    public static Tag sub(Object... children) { return tag("sub", children); }
    public static Tag sub(Attributes attrs, Object... children) { return tag("sub", attrs, children); }
    public static Tag sup(Object... children) { return tag("sup", children); }
    public static Tag sup(Attributes attrs, Object... children) { return tag("sup", attrs, children); }
    public static Tag code(Object... children) { return tag("code", children); }
    public static Tag code(Attributes attrs, Object... children) { return tag("code", attrs, children); }
    public static Tag pre(Object... children) { return tag("pre", children); }
    public static Tag pre(Attributes attrs, Object... children) { return tag("pre", attrs, children); }
    public static Tag blockquote(Object... children) { return tag("blockquote", children); }
    public static Tag blockquote(Attributes attrs, Object... children) { return tag("blockquote", attrs, children); }
    public static Tag hr() { return tag("hr"); }
    public static Tag hr(Attributes attrs) { return new Tag("hr", attrs); }
    public static Tag br() { return tag("br"); }
    public static Tag abbr(Object... children) { return tag("abbr", children); }
    public static Tag abbr(Attributes attrs, Object... children) { return tag("abbr", attrs, children); }
    public static Tag address(Object... children) { return tag("address", children); }
    public static Tag address(Attributes attrs, Object... children) { return tag("address", attrs, children); }
    public static Tag cite(Object... children) { return tag("cite", children); }
    public static Tag kbd(Object... children) { return tag("kbd", children); }
    public static Tag samp(Object... children) { return tag("samp", children); }
    public static Tag var_(Object... children) { return tag("var", children); }
    public static Tag time(Object... children) { return tag("time", children); }
    public static Tag time(Attributes attrs, Object... children) { return tag("time", attrs, children); }
    public static Tag data_(Object... children) { return tag("data", children); }
    public static Tag data_(Attributes attrs, Object... children) { return tag("data", attrs, children); }
    public static Tag wbr() { return tag("wbr"); }
    public static Tag bdi(Object... children) { return tag("bdi", children); }
    public static Tag bdo(Attributes attrs, Object... children) { return tag("bdo", attrs, children); }
    public static Tag q(Object... children) { return tag("q", children); }
    public static Tag q(Attributes attrs, Object... children) { return tag("q", attrs, children); }
    public static Tag dfn(Object... children) { return tag("dfn", children); }
    public static Tag ruby(Object... children) { return tag("ruby", children); }
    public static Tag rt(Object... children) { return tag("rt", children); }
    public static Tag rp(Object... children) { return tag("rp", children); }
    public static Tag s(Object... children) { return tag("s", children); }

    // ==================== Interactive ====================

    public static Tag details(Object... children) { return tag("details", children); }
    public static Tag details(Attributes attrs, Object... children) { return tag("details", attrs, children); }
    public static Tag summary(Object... children) { return tag("summary", children); }
    public static Tag dialog(Object... children) { return tag("dialog", children); }
    public static Tag dialog(Attributes attrs, Object... children) { return tag("dialog", attrs, children); }
    public static Tag menu(Object... children) { return tag("menu", children); }

    // ==================== Links ====================

    public static Tag a(String href, Object... children) {
        return tag("a", new Attributes().href(href), children);
    }
    public static Tag a(Attributes attrs, Object... children) { return tag("a", attrs, children); }
    public static Tag link(String href, String text) { return a(href, text); }

    // ==================== Lists ====================

    public static Tag ul(Object... children) { return tag("ul", children); }
    public static Tag ul(Attributes attrs, Object... children) { return tag("ul", attrs, children); }
    public static Tag ol(Object... children) { return tag("ol", children); }
    public static Tag ol(Attributes attrs, Object... children) { return tag("ol", attrs, children); }
    public static Tag li(Object... children) { return tag("li", children); }
    public static Tag li(Attributes attrs, Object... children) { return tag("li", attrs, children); }
    public static Tag dl(Object... children) { return tag("dl", children); }
    public static Tag dt(Object... children) { return tag("dt", children); }
    public static Tag dd(Object... children) { return tag("dd", children); }

    // ==================== Tables ====================

    public static Tag table(Object... children) { return tag("table", children); }
    public static Tag table(Attributes attrs, Object... children) { return tag("table", attrs, children); }
    public static Tag thead(Object... children) { return tag("thead", children); }
    public static Tag tbody(Object... children) { return tag("tbody", children); }
    public static Tag tfoot(Object... children) { return tag("tfoot", children); }
    public static Tag tr(Object... children) { return tag("tr", children); }
    public static Tag tr(Attributes attrs, Object... children) { return tag("tr", attrs, children); }
    public static Tag th(Object... children) { return tag("th", children); }
    public static Tag th(Attributes attrs, Object... children) { return tag("th", attrs, children); }
    public static Tag td(Object... children) { return tag("td", children); }
    public static Tag td(Attributes attrs, Object... children) { return tag("td", attrs, children); }
    public static Tag caption(Object... children) { return tag("caption", children); }
    public static Tag colgroup(Object... children) { return tag("colgroup", children); }
    public static Tag colgroup(Attributes attrs, Object... children) { return tag("colgroup", attrs, children); }
    public static Tag col(Attributes attrs) { return new Tag("col", attrs); }
    public static Tag col() { return tag("col"); }

    // ==================== Forms ====================

    public static Tag form(Object... children) { return tag("form", children); }
    public static Tag form(Attributes attrs, Object... children) { return tag("form", attrs, children); }
    public static Tag input(Object... attrs) { return tag("input", attrs); }
    public static Tag input(Attributes attrs) { return new Tag("input", attrs); }
    public static Tag input(String type, String name) {
        return tag("input", new Attributes().type(type).name(name));
    }
    public static Tag textarea(Object... items) { return tag("textarea", items); }
    public static Tag textarea(Attributes attrs, Object... children) { return tag("textarea", attrs, children); }
    public static Tag textarea(String name) { return tag("textarea", new Attributes().name(name)); }
    public static Tag select(Attributes attrs, Object... children) { return tag("select", attrs, children); }
    public static Tag option(String value, String text) {
        return tag("option", new Attributes().value(value), text);
    }
    public static Tag option(Attributes attrs, String text) { return tag("option", attrs, text); }
    public static Tag optgroup(Attributes attrs, Object... children) { return tag("optgroup", attrs, children); }
    public static Tag label(Object... children) { return tag("label", children); }
    public static Tag label(Attributes attrs, Object... children) { return tag("label", attrs, children); }
    public static Tag label(String forId, Object... children) {
        return tag("label", new Attributes().for_(forId), children);
    }
    public static Tag button(Object... children) { return tag("button", children); }
    public static Tag button(Attributes attrs, Object... children) { return tag("button", attrs, children); }
    public static Tag fieldset(Object... children) { return tag("fieldset", children); }
    public static Tag fieldset(Attributes attrs, Object... children) { return tag("fieldset", attrs, children); }
    public static Tag legend(Object... children) { return tag("legend", children); }
    public static Tag progress(Attributes attrs) { return new Tag("progress", attrs); }
    public static Tag progress(Attributes attrs, Object... children) { return tag("progress", attrs, children); }
    public static Tag meter(Attributes attrs) { return new Tag("meter", attrs); }
    public static Tag meter(Attributes attrs, Object... children) { return tag("meter", attrs, children); }
    public static Tag output(Object... children) { return tag("output", children); }
    public static Tag output(Attributes attrs, Object... children) { return tag("output", attrs, children); }
    public static Tag datalist(Object... children) { return tag("datalist", children); }
    public static Tag datalist(Attributes attrs, Object... children) { return tag("datalist", attrs, children); }
    public static Tag select(Object... children) { return tag("select", children); }

    // ==================== Media ====================

    public static Tag img(String src) { return tag("img", new Attributes().src(src)); }
    public static Tag img(String src, String alt) { return tag("img", new Attributes().src(src).alt(alt)); }
    public static Tag img(Attributes attrs) { return new Tag("img", attrs); }
    public static Tag video(Attributes attrs, Object... children) { return tag("video", attrs, children); }
    public static Tag audio(Attributes attrs, Object... children) { return tag("audio", attrs, children); }
    public static Tag source(Attributes attrs) { return new Tag("source", attrs); }
    public static Tag canvas(Attributes attrs) { return tag("canvas", attrs); }
    public static Tag svg(Attributes attrs, Object... children) { return tag("svg", attrs, children); }
    public static Tag iframe(Attributes attrs) { return tag("iframe", attrs); }
    public static Tag iframe(Attributes attrs, Object... children) { return tag("iframe", attrs, children); }
    public static Tag picture(Object... children) { return tag("picture", children); }
    public static Tag track(Attributes attrs) { return new Tag("track", attrs); }
    public static Tag embed(Attributes attrs) { return new Tag("embed", attrs); }
    public static Tag object(Attributes attrs, Object... children) { return tag("object", attrs, children); }
    public static Tag param(Attributes attrs) { return new Tag("param", attrs); }
    public static Tag map(Attributes attrs, Object... children) { return tag("map", attrs, children); }
    public static Tag area(Attributes attrs) { return new Tag("area", attrs); }

    // ==================== Scripting ====================

    public static Tag noscript(Object... children) { return tag("noscript", children); }
    public static Tag template_(Object... children) { return tag("template", children); }
    public static Tag template_(Attributes attrs, Object... children) { return tag("template", attrs, children); }
    public static Tag slot(Object... children) { return tag("slot", children); }
    public static Tag slot(Attributes attrs, Object... children) { return tag("slot", attrs, children); }
    public static Tag canvas(Object... children) { return tag("canvas", children); }

    // ==================== Text Helpers ====================

    public static TextElement text(String content) { return TextElement.of(content); }
    public static TextElement raw(String html) { return TextElement.raw(html); }

    // ==================== Fragment ====================

    public static Element fragment(Object... children) {
        return () -> new VFragment(Tag.toVNodes(children));
    }

    // ==================== Collection Helpers ====================

    /**
     * Maps a collection to elements.
     *
     * Usage: each(users, user -> li(user.getName()))
     */
    public static <T> Element each(Collection<T> items, Function<T, Element> mapper) {
        List<VNode> nodes = items.stream()
            .map(mapper)
            .map(Element::toVNode)
            .toList();
        return () -> new VFragment(nodes);
    }

    /**
     * Conditionally renders an element.
     *
     * <p>Usage:</p>
     * <pre>
     * when(isLoggedIn, () -&gt; span("Welcome!"))
     * </pre>
     */
    public static Element when(boolean condition, java.util.function.Supplier<Element> element) {
        if (condition) {
            return element.get();
        }
        return () -> new VFragment(List.of());
    }

    /**
     * Conditionally renders an element (eager evaluation).
     *
     * <p>Usage:</p>
     * <pre>
     * when(isLoggedIn, span("Welcome!"))
     * </pre>
     */
    public static Element when(boolean condition, Element element) {
        if (condition) {
            return element;
        }
        return () -> new VFragment(List.of());
    }

    /**
     * Starts a conditional chain for if/elif/else rendering.
     *
     * <p>Usage:</p>
     * <pre>
     * when(isAdmin)
     *     .then(adminPanel())
     *     .elif(isModerator, modPanel())
     *     .elif(isUser, userPanel())
     *     .otherwise(loginPrompt())
     * </pre>
     *
     * @param condition the initial condition to check
     * @return a Condition builder for chaining
     */
    public static Condition when(boolean condition) {
        return new Condition(condition);
    }

    /**
     * Conditionally renders one of two elements (lazy evaluation).
     *
     * <p>Usage:</p>
     * <pre>
     * ifElse(isLoggedIn, () -&gt; span("Welcome!"), () -&gt; link("/login", "Sign In"))
     * </pre>
     *
     * @deprecated Use {@link #when(boolean)} with .then().otherwise() for cleaner syntax
     */
    @Deprecated
    public static Element ifElse(
            boolean condition,
            java.util.function.Supplier<Element> ifTrue,
            java.util.function.Supplier<Element> ifFalse) {
        return condition ? ifTrue.get() : ifFalse.get();
    }

    // ==================== Condition Builder (if/elif/else) ====================

    /**
     * Builder for if/elif/else conditional rendering chains.
     *
     * <p>Example:</p>
     * <pre>
     * when(isAdmin)
     *     .then(adminPanel())
     *     .elif(isModerator, modPanel())
     *     .elif(isUser, userPanel())
     *     .otherwise(loginPrompt())
     * </pre>
     */
    public static class Condition {
        private boolean matched = false;
        private Element result = null;

        Condition(boolean condition) {
            this.matched = condition;
        }

        /**
         * Specifies the element to render if the condition is true.
         *
         * @param element the element to render
         * @return this builder for chaining
         */
        public Condition then(Element element) {
            if (matched && result == null) {
                result = element;
            }
            return this;
        }

        /**
         * Specifies a lazy element to render if the condition is true.
         *
         * @param element supplier for the element to render
         * @return this builder for chaining
         */
        public Condition then(java.util.function.Supplier<Element> element) {
            if (matched && result == null) {
                result = element.get();
            }
            return this;
        }

        /**
         * Adds an else-if condition.
         *
         * @param condition the condition to check
         * @param element the element to render if this condition is true
         * @return this builder for chaining
         */
        public Condition elif(boolean condition, Element element) {
            if (!matched && result == null && condition) {
                matched = true;
                result = element;
            }
            return this;
        }

        /**
         * Adds an else-if condition with lazy evaluation.
         *
         * @param condition the condition to check
         * @param element supplier for the element to render
         * @return this builder for chaining
         */
        public Condition elif(boolean condition, java.util.function.Supplier<Element> element) {
            if (!matched && result == null && condition) {
                matched = true;
                result = element.get();
            }
            return this;
        }

        /**
         * Specifies the fallback element if no conditions matched.
         * This terminates the chain and returns the final Element.
         *
         * @param element the fallback element
         * @return the matched element or the fallback
         */
        public Element otherwise(Element element) {
            if (result != null) {
                return result;
            }
            return element;
        }

        /**
         * Specifies a lazy fallback element if no conditions matched.
         *
         * @param element supplier for the fallback element
         * @return the matched element or the fallback
         */
        public Element otherwise(java.util.function.Supplier<Element> element) {
            if (result != null) {
                return result;
            }
            return element.get();
        }

        /**
         * Ends the chain without a fallback (renders nothing if no match).
         *
         * @return the matched element or an empty fragment
         */
        public Element end() {
            if (result != null) {
                return result;
            }
            return () -> new VFragment(List.of());
        }
    }

    // ==================== Match Expression (pattern matching style) ====================

    /**
     * Pattern matching style conditional rendering.
     *
     * <p>Usage:</p>
     * <pre>
     * match(
     *     cond(isAdmin, adminPanel()),
     *     cond(isModerator, modPanel()),
     *     cond(isUser, userPanel()),
     *     otherwise(loginPrompt())
     * )
     * </pre>
     *
     * @param cases the condition cases to evaluate
     * @return the element from the first matching condition
     */
    public static Element match(CondCase... cases) {
        for (CondCase c : cases) {
            if (c.matches()) {
                return c.element();
            }
        }
        return () -> new VFragment(List.of());
    }

    /**
     * Creates a condition case for use with match().
     *
     * @param condition the condition to check
     * @param element the element to render if condition is true
     * @return a CondCase
     */
    public static CondCase cond(boolean condition, Element element) {
        return new CondCase(condition, element);
    }

    /**
     * Creates a condition case with lazy evaluation.
     *
     * @param condition the condition to check
     * @param element supplier for the element to render
     * @return a CondCase
     */
    public static CondCase cond(boolean condition, java.util.function.Supplier<Element> element) {
        return new CondCase(condition, condition ? element.get() : null);
    }

    /**
     * Creates a fallback case that always matches (for use as last case in match).
     *
     * @param element the fallback element
     * @return a CondCase that always matches
     */
    public static CondCase otherwise(Element element) {
        return new CondCase(true, element);
    }

    /**
     * Creates a lazy fallback case that always matches.
     *
     * @param element supplier for the fallback element
     * @return a CondCase that always matches
     */
    public static CondCase otherwise(java.util.function.Supplier<Element> element) {
        return new CondCase(true, element.get());
    }

    /**
     * Represents a condition-element pair for pattern matching.
     */
    public record CondCase(boolean matches, Element element) {}

    // ==================== Generic Tag Factory ====================

    public static Tag tag(String name, Object... items) {
        return Tag.create(name, items);
    }

    public static Tag tag(String name, Attributes attrs, Object... children) {
        return new Tag(name, attrs, Tag.toVNodes(children));
    }

    /**
     * Creates an element with InlineStyle (auto-finalizes to Attributes).
     * This enables fluent styling without .done():
     *
     * <pre>
     * div(attrs().style()
     *         .display(flex)
     *         .padding(px(10)),
     *     p("Hello"))
     * </pre>
     */
    public static Tag tag(String name, InlineStyle style, Object... children) {
        return new Tag(name, style.toAttrs(), Tag.toVNodes(children));
    }

    // ==================== InlineStyle Overloads ====================
    // These allow using attrs().style()... directly without .done()

    public static Tag div(InlineStyle style, Object... children) { return tag("div", style, children); }
    public static Tag span(InlineStyle style, Object... children) { return tag("span", style, children); }
    public static Tag p(InlineStyle style, Object... children) { return tag("p", style, children); }
    public static Tag a(InlineStyle style, Object... children) { return tag("a", style, children); }
    public static Tag button(InlineStyle style, Object... children) { return tag("button", style, children); }
    public static Tag section(InlineStyle style, Object... children) { return tag("section", style, children); }
    public static Tag article(InlineStyle style, Object... children) { return tag("article", style, children); }
    public static Tag header(InlineStyle style, Object... children) { return tag("header", style, children); }
    public static Tag footer(InlineStyle style, Object... children) { return tag("footer", style, children); }
    public static Tag nav(InlineStyle style, Object... children) { return tag("nav", style, children); }
    public static Tag main(InlineStyle style, Object... children) { return tag("main", style, children); }
    public static Tag aside(InlineStyle style, Object... children) { return tag("aside", style, children); }
    public static Tag h1(InlineStyle style, Object... children) { return tag("h1", style, children); }
    public static Tag h2(InlineStyle style, Object... children) { return tag("h2", style, children); }
    public static Tag h3(InlineStyle style, Object... children) { return tag("h3", style, children); }
    public static Tag h4(InlineStyle style, Object... children) { return tag("h4", style, children); }
    public static Tag h5(InlineStyle style, Object... children) { return tag("h5", style, children); }
    public static Tag h6(InlineStyle style, Object... children) { return tag("h6", style, children); }
    public static Tag ul(InlineStyle style, Object... children) { return tag("ul", style, children); }
    public static Tag ol(InlineStyle style, Object... children) { return tag("ol", style, children); }
    public static Tag li(InlineStyle style, Object... children) { return tag("li", style, children); }
    public static Tag form(InlineStyle style, Object... children) { return tag("form", style, children); }
    public static Tag input(InlineStyle style) { return new Tag("input", style.toAttrs()); }
    public static Tag textarea(InlineStyle style, Object... children) { return tag("textarea", style, children); }
    public static Tag select(InlineStyle style, Object... children) { return tag("select", style, children); }
    public static Tag label(InlineStyle style, Object... children) { return tag("label", style, children); }
    public static Tag table(InlineStyle style, Object... children) { return tag("table", style, children); }
    public static Tag tr(InlineStyle style, Object... children) { return tag("tr", style, children); }
    public static Tag th(InlineStyle style, Object... children) { return tag("th", style, children); }
    public static Tag td(InlineStyle style, Object... children) { return tag("td", style, children); }
    public static Tag img(InlineStyle style) { return new Tag("img", style.toAttrs()); }
    public static Tag video(InlineStyle style, Object... children) { return tag("video", style, children); }

    // ==================== Error Boundary ====================

    /**
     * Creates an error boundary that wraps content and displays a fallback on error.
     *
     * <p>Usage:</p>
     * <pre>
     * errorBoundary(
     *     () -&gt; riskyComponent.render(),
     *     error -&gt; p("Error: " + error.getMessage())
     * )
     * </pre>
     *
     * @param content the content to render
     * @param fallback function that receives the error and returns a fallback element
     * @return the rendered element or fallback
     */
    public static Element errorBoundary(
            java.util.function.Supplier<Element> content,
            Function<Throwable, Element> fallback) {
        return ErrorBoundary.wrap(content, fallback);
    }

    /**
     * Creates an error boundary with a static fallback element.
     *
     * @param content the content to render
     * @param fallback the static fallback element
     * @return the rendered element or fallback
     */
    public static Element errorBoundary(
            java.util.function.Supplier<Element> content,
            Element fallback) {
        return ErrorBoundary.wrap(content, fallback);
    }

    /**
     * Creates an error boundary that silently fails (renders nothing on error).
     *
     * @param content the content to render
     * @return the rendered element or empty fragment on error
     */
    public static Element tryCatch(java.util.function.Supplier<Element> content) {
        return ErrorBoundary.silent(content);
    }
    public static Tag audio(InlineStyle style, Object... children) { return tag("audio", style, children); }
    public static Tag canvas(InlineStyle style) { return new Tag("canvas", style.toAttrs()); }
    public static Tag svg(InlineStyle style, Object... children) { return tag("svg", style, children); }
    public static Tag iframe(InlineStyle style, Object... children) { return tag("iframe", style, children); }
    public static Tag pre(InlineStyle style, Object... children) { return tag("pre", style, children); }
    public static Tag code(InlineStyle style, Object... children) { return tag("code", style, children); }
    public static Tag blockquote(InlineStyle style, Object... children) { return tag("blockquote", style, children); }
    public static Tag figure(InlineStyle style, Object... children) { return tag("figure", style, children); }
    public static Tag figcaption(InlineStyle style, Object... children) { return tag("figcaption", style, children); }
    public static Tag hgroup(InlineStyle style, Object... children) { return tag("hgroup", style, children); }
    public static Tag search(InlineStyle style, Object... children) { return tag("search", style, children); }
    public static Tag strong(InlineStyle style, Object... children) { return tag("strong", style, children); }
    public static Tag em(InlineStyle style, Object... children) { return tag("em", style, children); }
}
