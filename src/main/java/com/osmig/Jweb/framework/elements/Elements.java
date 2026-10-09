package com.osmig.Jweb.framework.elements;

import jweb.Tag;
import jweb.Attr;
import jweb.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import jweb.ErrorBoundary;

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
 * <h2>Two rules cover the whole element surface</h2>
 * <ol>
 *   <li><b>Every element is {@code name(Object... itemsAndAttrs)}</b> — attributes
 *       ({@link Attr}, {@link Attributes}, a style builder) and children may be
 *       mixed freely in one call. There is no separate {@code (Attributes, ...)}
 *       overload family; it was redundant.</li>
 *   <li><b>A String argument is escaped text, wherever it appears</b> —
 *       {@code a("Home")} renders {@code <a>Home</a>}, {@code a(href("/"), "Home")}
 *       renders {@code <a href="/">Home</a>}, and {@code label(for_("email"), "Email:")}
 *       sets the target. No element treats its first String as an attribute.
 *       The exceptions are void elements, which cannot contain text, so their
 *       Strings are their most common attributes: {@code img(src)},
 *       {@code img(src, alt)}, {@code meta(name, content)}, {@code input(type, name)};
 *       and the code-bearing {@code inlineScript(js)} / {@code style(css)}, whose
 *       String is emitted verbatim.</li>
 * </ol>
 *
 * <p>Event handlers, the swap family, {@code ref} and state {@code bind} are
 * plain arguments too, so {@code attrs()} is only needed for the long tail:
 * {@code button(id("save"), onClick(e -> save()), style().padding(px(8)), "Save")}.</p>
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
 *
 * @deprecated Replaced by {@code jweb.El} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public class Elements {

    protected Elements() {}

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

    /**
     * An element id as a handle: an element argument here, a selector in a
     * stylesheet ({@code rule(EDITOR.focus(), ...)}) and a JavaScript target
     * ({@code byId(EDITOR)}). Declare it once as a {@code static final} field.
     *
     * @param value the element ID
     * @return the id handle
     */
    public static jweb.Id id(String value) { return jweb.Id.of(value); }
    /**
     * A class name as a handle — the everyday spelling: {@code div(cls("card"))}.
     *
     * <p>The same handle is a selector in a stylesheet
     * ({@code rule(CARD.hover(), ...)}) and a JavaScript target
     * ({@code dom(CARD)}), so the name is typed once. Several words set
     * several classes. {@link #class_(String)} is the plain attribute under
     * the trailing-underscore keyword rule.</p>
     *
     * @param value the CSS class(es)
     * @return the class handle
     */
    public static jweb.Cls cls(String value) { return jweb.Cls.of(value); }
    /**
     * A class scoped to the declaring class, named for it
     * ({@code snippetcard-1}); for a component that does not care what its
     * class is called. Declare it as a {@code static final} field.
     *
     * @return the class handle
     */
    public static jweb.Cls cls() { return jweb.Cls.scoped(); }
    /** Creates a class attribute. Named class_ to avoid Java keyword conflict. @param value the CSS class(es) */
    public static Attr class_(String value) { return Attr.class_(value); }

    /**
     * Joins class name parts into one class attribute, skipping nulls and
     * blanks — so a conditional class never has to be built with {@code +}
     * and a ternary:
     *
     * <pre>
     * div(classes("chip", when(active, "chip-on"), "lg"))     // class="chip chip-on lg"
     * </pre>
     *
     * <p>A part may be a String, {@code null} (skipped), the result of
     * {@link #when(boolean, String)} (an empty element when the condition is
     * false), or a collection/array of those.</p>
     *
     * @param parts the class name parts
     * @return the joined class attribute
     */
    public static Attr classes(Object... parts) {
        StringBuilder joined = new StringBuilder();
        appendClassParts(joined, parts);
        return Attr.class_(joined.toString());
    }

    private static void appendClassParts(StringBuilder into, Object... parts) {
        if (parts == null) return;
        for (Object part : parts) {
            String piece = classPart(part);
            if (piece == null) continue;
            for (String word : piece.strip().split("\\s+")) {
                if (word.isEmpty()) continue;
                if (into.length() > 0) into.append(' ');
                into.append(word);
            }
        }
    }

    private static String classPart(Object part) {
        return switch (part) {
            case null -> null;
            case String s -> s;
            case jweb.Cls cls -> cls.name();
            case jweb.Element element -> plainText(element.toVNode());
            case VNode node -> plainText(node);
            case Iterable<?> items -> {
                StringBuilder nested = new StringBuilder();
                for (Object item : items) appendClassParts(nested, item);
                yield nested.toString();
            }
            case Object[] array -> {
                StringBuilder nested = new StringBuilder();
                appendClassParts(nested, array);
                yield nested.toString();
            }
            default -> throw new IllegalArgumentException(
                "classes(...) takes class name Strings, nulls, when(cond, \"x\") results "
                    + "or collections of those — not " + part.getClass().getName());
        };
    }

    /** The text a class-name part carries: {@code when(cond, "on")} is a text node when it matched. */
    private static String plainText(VNode node) {
        if (node instanceof com.osmig.Jweb.framework.vdom.VText text) return text.content();
        if (node instanceof VFragment fragment) {
            StringBuilder joined = new StringBuilder();
            for (VNode child : fragment.children()) {
                String piece = plainText(child);
                if (piece == null || piece.isBlank()) continue;
                if (joined.length() > 0) joined.append(' ');
                joined.append(piece);
            }
            return joined.toString();
        }
        return "";
    }
    // The style attribute is a bare style() builder argument (jweb.Css.style()), or
    // attrs().style(...); the title attribute is attrs().title(...). Neither has a
    // trailing-underscore shortcut: an underscore marks a Java keyword, nothing else.
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
    /**
     * Creates an enctype attribute for forms. A record form with an
     * {@link jweb.UploadedFile} component sets this on its own.
     *
     * @param value the encoding, usually {@code "multipart/form-data"}
     * @return the enctype attribute
     */
    public static Attr enctype(String value) { return Attr.enctype(value); }
    /** Creates a target attribute for links. @param value the target (e.g., "_blank") */
    public static Attr target(String value) { return Attr.target(value); }
    /** Creates a for attribute for labels. Named for_ to avoid Java keyword. @param value the target element ID */
    public static Attr for_(String value) { return Attr.for_(value); }
    /** Creates a role attribute for ARIA. @param value the ARIA role */
    public static Attr role(String value) { return Attr.role(value); }
    /** Creates a datetime attribute for {@code <time>}. @param value machine-readable datetime */
    public static Attr datetime(String value) { return Attr.datetime(value); }
    /** Creates a loading attribute for images/iframes. @param value "lazy" or "eager" */
    public static Attr loading(String value) { return Attr.loading(value); }
    /** Creates a disabled boolean attribute. */
    public static Attr disabled() { return Attr.disabled(); }
    /** Creates a checked boolean attribute for checkboxes/radios. */
    public static Attr checked() { return Attr.checked(); }
    /** Creates a required boolean attribute. */
    public static Attr required() { return Attr.required(); }
    /** Creates a selected boolean attribute for {@code <option>}. */
    public static Attr selected() { return Attr.selected(); }
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

    // ==================== Event Handlers as Arguments ====================
    // Every handler that Attributes accepts is also a plain element argument,
    // so a button with a server handler and a style needs no attrs() bridge:
    //
    //   button(id("save"), onClick(e -> save()), style().padding(px(8)), "Save")
    //   button(onClick(toggle("panel")), "Menu")          // client-side Action
    //   form(swapForm("/contact", "#status"), ...)        // progressive swap
    //
    // Server handlers (Consumer<Event>) travel over the WebSocket; Actions run in
    // the browser. Both are CSP-safe — see HtmlAttributes.

    /** A server-side handler for any event type: {@code on("pointerdown", e -> ...)}. */
    public static Attributes on(String eventType, java.util.function.Consumer<jweb.Event> handler) { return attrs().on(eventType, handler); }
    /** A client-side Action for any event type: {@code on("pointerdown", show("x"))}. */
    public static Attributes on(String eventType, jweb.Action action) { return attrs().on(eventType, action); }

    public static Attributes onClick(java.util.function.Consumer<jweb.Event> handler) { return attrs().onClick(handler); }
    public static Attributes onChange(java.util.function.Consumer<jweb.Event> handler) { return attrs().onChange(handler); }
    public static Attributes onInput(java.util.function.Consumer<jweb.Event> handler) { return attrs().onInput(handler); }
    public static Attributes onSubmit(java.util.function.Consumer<jweb.Event> handler) { return attrs().onSubmit(handler); }
    public static Attributes onFocus(java.util.function.Consumer<jweb.Event> handler) { return attrs().onFocus(handler); }
    public static Attributes onBlur(java.util.function.Consumer<jweb.Event> handler) { return attrs().onBlur(handler); }
    public static Attributes onKeyDown(java.util.function.Consumer<jweb.Event> handler) { return attrs().onKeyDown(handler); }
    public static Attributes onKeyUp(java.util.function.Consumer<jweb.Event> handler) { return attrs().onKeyUp(handler); }
    public static Attributes onKeyPress(java.util.function.Consumer<jweb.Event> handler) { return attrs().onKeyPress(handler); }
    public static Attributes onMouseEnter(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseEnter(handler); }
    public static Attributes onMouseLeave(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseLeave(handler); }
    public static Attributes onMouseDown(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseDown(handler); }
    public static Attributes onMouseUp(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseUp(handler); }
    public static Attributes onMouseMove(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseMove(handler); }
    public static Attributes onMouseOver(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseOver(handler); }
    public static Attributes onMouseOut(java.util.function.Consumer<jweb.Event> handler) { return attrs().onMouseOut(handler); }
    public static Attributes onContextMenu(java.util.function.Consumer<jweb.Event> handler) { return attrs().onContextMenu(handler); }
    public static Attributes onWheel(java.util.function.Consumer<jweb.Event> handler) { return attrs().onWheel(handler); }
    public static Attributes onDoubleClick(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDoubleClick(handler); }
    /** Alias for {@link #onDoubleClick(java.util.function.Consumer)} matching the DOM event name ({@code dblclick}). */
    public static Attributes onDblClick(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDblClick(handler); }
    public static Attributes onDrag(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDrag(handler); }
    public static Attributes onDragStart(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDragStart(handler); }
    public static Attributes onDragEnd(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDragEnd(handler); }
    public static Attributes onDragEnter(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDragEnter(handler); }
    public static Attributes onDragLeave(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDragLeave(handler); }
    public static Attributes onDragOver(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDragOver(handler); }
    public static Attributes onDrop(java.util.function.Consumer<jweb.Event> handler) { return attrs().onDrop(handler); }
    public static Attributes onTouchStart(java.util.function.Consumer<jweb.Event> handler) { return attrs().onTouchStart(handler); }
    public static Attributes onTouchMove(java.util.function.Consumer<jweb.Event> handler) { return attrs().onTouchMove(handler); }
    public static Attributes onTouchEnd(java.util.function.Consumer<jweb.Event> handler) { return attrs().onTouchEnd(handler); }
    public static Attributes onTouchCancel(java.util.function.Consumer<jweb.Event> handler) { return attrs().onTouchCancel(handler); }
    public static Attributes onScroll(java.util.function.Consumer<jweb.Event> handler) { return attrs().onScroll(handler); }
    public static Attributes onToggle(java.util.function.Consumer<jweb.Event> handler) { return attrs().onToggle(handler); }
    public static Attributes onCancel(java.util.function.Consumer<jweb.Event> handler) { return attrs().onCancel(handler); }
    public static Attributes onClose(java.util.function.Consumer<jweb.Event> handler) { return attrs().onClose(handler); }
    public static Attributes onAnimationStart(java.util.function.Consumer<jweb.Event> handler) { return attrs().onAnimationStart(handler); }
    public static Attributes onAnimationEnd(java.util.function.Consumer<jweb.Event> handler) { return attrs().onAnimationEnd(handler); }
    public static Attributes onAnimationIteration(java.util.function.Consumer<jweb.Event> handler) { return attrs().onAnimationIteration(handler); }
    public static Attributes onTransitionEnd(java.util.function.Consumer<jweb.Event> handler) { return attrs().onTransitionEnd(handler); }
    public static Attributes onLoad(java.util.function.Consumer<jweb.Event> handler) { return attrs().onLoad(handler); }
    public static Attributes onError(java.util.function.Consumer<jweb.Event> handler) { return attrs().onError(handler); }
    public static Attributes onCopy(java.util.function.Consumer<jweb.Event> handler) { return attrs().onCopy(handler); }
    public static Attributes onCut(java.util.function.Consumer<jweb.Event> handler) { return attrs().onCut(handler); }
    public static Attributes onPaste(java.util.function.Consumer<jweb.Event> handler) { return attrs().onPaste(handler); }

    public static Attributes onClick(jweb.Action action) { return attrs().onClick(action); }
    public static Attributes onChange(jweb.Action action) { return attrs().onChange(action); }
    public static Attributes onInput(jweb.Action action) { return attrs().onInput(action); }
    public static Attributes onSubmit(jweb.Action action) { return attrs().onSubmit(action); }
    public static Attributes onFocus(jweb.Action action) { return attrs().onFocus(action); }
    public static Attributes onBlur(jweb.Action action) { return attrs().onBlur(action); }
    public static Attributes onKeyDown(jweb.Action action) { return attrs().onKeyDown(action); }
    public static Attributes onKeyUp(jweb.Action action) { return attrs().onKeyUp(action); }
    public static Attributes onMouseEnter(jweb.Action action) { return attrs().onMouseEnter(action); }
    public static Attributes onMouseLeave(jweb.Action action) { return attrs().onMouseLeave(action); }
    public static Attributes onDoubleClick(jweb.Action action) { return attrs().onDoubleClick(action); }
    /** Alias for {@link #onDoubleClick(jweb.Action)} matching the DOM event name ({@code dblclick}). */
    public static Attributes onDblClick(jweb.Action action) { return attrs().onDblClick(action); }

    // ==================== Server-Driven UI as Arguments ====================

    /** Fetch {@code url} on click and replace {@code targetSelector}'s content with the fragment. */
    public static Attributes swap(String url, String targetSelector) { return attrs().swap(url, targetSelector); }
    /** Like {@link #swap} but replaces the target element itself. */
    public static Attributes swapOuter(String url, String targetSelector) { return attrs().swapOuter(url, targetSelector); }
    /** Like {@link #swap} but morphs the target in place, preserving focus and state. */
    public static Attributes swapMorph(String url, String targetSelector) { return attrs().swapMorph(url, targetSelector); }
    /** Submit the form to {@code actionUrl} and swap the response into {@code targetSelector}. */
    public static Attributes swapForm(String actionUrl, String targetSelector) { return attrs().swapForm(actionUrl, targetSelector); }
    /** Push {@code browserUrl} into history when the swap completes. */
    public static Attributes swapPush(String browserUrl) { return attrs().swapPush(browserUrl); }
    /** {@link #swap(String, String)} with the target as a handle: {@code swap("/x", STATUS)}. */
    public static Attributes swap(String url, jweb.css.Selector target) { return attrs().swap(url, target.build()); }
    /** {@link #swapOuter(String, String)} with the target as a handle. */
    public static Attributes swapOuter(String url, jweb.css.Selector target) { return attrs().swapOuter(url, target.build()); }
    /** {@link #swapMorph(String, String)} with the target as a handle. */
    public static Attributes swapMorph(String url, jweb.css.Selector target) { return attrs().swapMorph(url, target.build()); }
    /** {@link #swapForm(String, String)} with the target as a handle. */
    public static Attributes swapForm(String actionUrl, jweb.css.Selector target) { return attrs().swapForm(actionUrl, target.build()); }
    /** Give the element the ref's id, so the ref's Actions can target it. */
    public static Attributes ref(jweb.Ref ref) { return attrs().ref(ref); }

    /**
     * An element that carries a stylesheet: the rules ride the page's one
     * {@code <style>} exactly as a {@code Template}'s {@code styles()} do, so a
     * static component can own its CSS without being a class.
     *
     * <pre>{@code
     * static final Stylesheet RULES = stylesheet().rule(CARD, style()...);
     * public static Element render(Doc d) { return styled(RULES, article(CARD, ...)); }
     * }</pre>
     *
     * @param sheet the rules the element needs
     * @param element the element
     * @return the element, with its stylesheet attached
     */
    public static jweb.Element styled(jweb.css.Stylesheet sheet, jweb.Element element) {
        return new StyledElement(sheet, element);
    }

    private record StyledElement(jweb.css.Stylesheet sheet, jweb.Element element) implements jweb.Element {
        @Override
        public VNode toVNode() {
            if (sheet != null) com.osmig.Jweb.framework.styles.PageStyles.add(sheet.build());
            return element.toVNode();
        }
    }

    /**
     * Bind an element's text to reactive state — the runtime patches it on
     * every change: {@code span(bind(count), count.get())}.
     */
    public static Attributes bind(com.osmig.Jweb.framework.state.State<?> state) {
        return com.osmig.Jweb.framework.state.StateBinding.bind(state);
    }

    /** Bind an input's value to reactive state, both ways. */
    public static Attributes bindInput(com.osmig.Jweb.framework.state.State<?> state) {
        return com.osmig.Jweb.framework.state.StateBinding.bindInput(state);
    }

    // ==================== Document Structure ====================

    public static Tag html(Object... children) { return tag("html", children); }
    public static Tag head(Object... children) { return tag("head", children); }
    public static Tag body(Object... children) { return tag("body", children); }

    // ==================== Head Elements ====================

    public static Tag title(String text) { return tag("title", text); }
    public static Tag meta(Object... attrs) { return tag("meta", attrs); }
    /** {@code <meta charset="UTF-8">} */
    public static Tag metaCharset() { return DocumentElements.metaCharset(); }
    /** The standard responsive viewport meta tag. */
    public static Tag metaViewport() { return DocumentElements.metaViewport(); }
    public static Tag meta(String name, String content) {
        return tag("meta", new Attributes().name(name).set("content", content));
    }
    public static Tag link(Object... attrs) { return tag("link", attrs); }
    public static Tag css(String href) {
        return tag("link", new Attributes().set("rel", "stylesheet").href(href));
    }

    /**
     * A {@code <script>} element from attributes and children:
     * {@code script(src("/app.js"), attr("defer", ""))}.
     *
     * <p>Content is not escaped for you — use {@link #inlineScript(String)}
     * (or a {@code raw(...)} child) for inline JavaScript.</p>
     */
    public static Tag script(Object... attrs) { return tag("script", attrs); }

    /**
     * A {@code <script src="...">} element.
     *
     * @deprecated Use {@code script(src("..."))} instead — a lone String
     *             argument means text everywhere else in this DSL.
     */
    @Deprecated
    public static Tag script(String src) { return tag("script", new Attributes().src(src)); }

    /** Inline JavaScript. The code is emitted verbatim (never HTML-escaped). */
    public static Tag inlineScript(String code) { return tag("script", TextElement.raw(code)); }
    /**
     * Inline JavaScript from the DSL: {@code inlineScript(actions().does(...))}.
     * The action stays an {@code Action} to the page boundary; the framework
     * serializes it. A page or component usually prefers the
     * {@code Template.scripts()} hook, which dedupes and delivers the script
     * with fragments and patches too.
     */
    public static Tag inlineScript(jweb.Action script) { return inlineScript(script.build()); }
    /** Inline CSS in a {@code <style>} element. The CSS is emitted verbatim. */
    public static Tag style(String css) { return tag("style", TextElement.raw(css)); }

    // ==================== Semantic Structure ====================

    public static Tag header(Object... children) { return tag("header", children); }
    public static Tag footer(Object... children) { return tag("footer", children); }
    public static Tag nav(Object... children) { return tag("nav", children); }
    public static Tag main(Object... children) { return tag("main", children); }
    public static Tag section(Object... children) { return tag("section", children); }
    public static Tag article(Object... children) { return tag("article", children); }
    public static Tag aside(Object... children) { return tag("aside", children); }
    public static Tag figure(Object... children) { return tag("figure", children); }
    public static Tag figcaption(Object... children) { return tag("figcaption", children); }
    public static Tag hgroup(Object... children) { return tag("hgroup", children); }
    public static Tag search(Object... children) { return tag("search", children); }

    // ==================== Headings ====================

    public static Tag h1(Object... children) { return tag("h1", children); }
    public static Tag h2(Object... children) { return tag("h2", children); }
    public static Tag h3(Object... children) { return tag("h3", children); }
    public static Tag h4(Object... children) { return tag("h4", children); }
    public static Tag h5(Object... children) { return tag("h5", children); }
    public static Tag h6(Object... children) { return tag("h6", children); }

    // ==================== Text Content ====================

    public static Tag p(Object... children) { return tag("p", children); }
    public static Tag span(Object... children) { return tag("span", children); }
    public static Tag div(Object... children) { return tag("div", children); }
    public static Tag strong(Object... children) { return tag("strong", children); }
    public static Tag em(Object... children) { return tag("em", children); }
    public static Tag b(Object... children) { return tag("b", children); }
    public static Tag i(Object... children) { return tag("i", children); }
    public static Tag u(Object... children) { return tag("u", children); }
    public static Tag small(Object... children) { return tag("small", children); }
    public static Tag mark(Object... children) { return tag("mark", children); }
    public static Tag del(Object... children) { return tag("del", children); }
    public static Tag ins(Object... children) { return tag("ins", children); }
    public static Tag sub(Object... children) { return tag("sub", children); }
    public static Tag sup(Object... children) { return tag("sup", children); }
    public static Tag code(Object... children) { return tag("code", children); }
    public static Tag pre(Object... children) { return tag("pre", children); }
    /** {@code blockquote("Quote")}; for a source URL: {@code blockquote(attrs().cite(url), "Quote")}. */
    public static Tag blockquote(Object... children) { return tag("blockquote", children); }
    public static Tag hr(Object... attrs) { return tag("hr", attrs); }
    public static Tag br() { return tag("br"); }
    public static Tag abbr(Object... children) { return tag("abbr", children); }
    public static Tag address(Object... children) { return tag("address", children); }
    public static Tag cite(Object... children) { return tag("cite", children); }
    public static Tag kbd(Object... children) { return tag("kbd", children); }
    public static Tag samp(Object... children) { return tag("samp", children); }
    // <var> and <data> have no shortcut: var(...) is the CSS DSL's custom-property
    // reference and data(name, value) is the data-* attribute. Use tag("var", ...)
    // and tag("data", value("..."), "text") for the elements.
    public static Tag time(Object... children) { return tag("time", children); }
    public static Tag wbr() { return tag("wbr"); }
    public static Tag bdi(Object... children) { return tag("bdi", children); }
    public static Tag bdo(Object... children) { return tag("bdo", children); }
    /** {@code q("Hello")}; for a source URL: {@code q(attrs().cite(url), "Hello")}. */
    public static Tag q(Object... children) { return tag("q", children); }
    public static Tag dfn(Object... children) { return tag("dfn", children); }
    public static Tag ruby(Object... children) { return tag("ruby", children); }
    public static Tag rt(Object... children) { return tag("rt", children); }
    public static Tag rp(Object... children) { return tag("rp", children); }
    public static Tag s(Object... children) { return tag("s", children); }

    // ==================== Interactive ====================

    public static Tag details(Object... children) { return tag("details", children); }
    public static Tag summary(Object... children) { return tag("summary", children); }
    public static Tag dialog(Object... children) { return tag("dialog", children); }
    public static Tag menu(Object... children) { return tag("menu", children); }

    // ==================== Links ====================

    /**
     * {@code a(href("/home"), "Home")} renders {@code <a href="/home">Home</a>};
     * {@code a("Home")} renders {@code <a>Home</a>}. A String is always text —
     * the old {@code a(href, text)} form that read the first String as the URL
     * is gone, so {@code a("Hello ", strong("world"))} can no longer silently
     * become a link to "Hello ".
     */
    public static Tag a(Object... children) { return tag("a", children); }

    // ==================== Lists ====================

    public static Tag ul(Object... children) { return tag("ul", children); }
    public static Tag ol(Object... children) { return tag("ol", children); }
    public static Tag li(Object... children) { return tag("li", children); }
    public static Tag dl(Object... children) { return tag("dl", children); }
    public static Tag dt(Object... children) { return tag("dt", children); }
    public static Tag dd(Object... children) { return tag("dd", children); }

    // ==================== Tables ====================

    public static Tag table(Object... children) { return tag("table", children); }
    public static Tag thead(Object... children) { return tag("thead", children); }
    public static Tag tbody(Object... children) { return tag("tbody", children); }
    public static Tag tfoot(Object... children) { return tag("tfoot", children); }
    public static Tag tr(Object... children) { return tag("tr", children); }
    public static Tag th(Object... children) { return tag("th", children); }
    public static Tag td(Object... children) { return tag("td", children); }
    public static Tag caption(Object... children) { return tag("caption", children); }
    public static Tag colgroup(Object... children) { return tag("colgroup", children); }
    public static Tag col(Object... attrs) { return tag("col", attrs); }

    // ==================== Forms ====================

    public static Tag form(Object... children) { return tag("form", children); }
    public static Tag input(Object... attrs) { return tag("input", attrs); }
    public static Tag input(String type, String name) {
        return tag("input", new Attributes().type(type).name(name));
    }
    /** {@code textarea(name("bio"), "Hello")} — the String is the initial text. */
    public static Tag textarea(Object... items) { return tag("textarea", items); }
    public static Tag select(Object... children) { return tag("select", children); }
    /**
     * {@code option(value("us"), "United States")}. A lone {@code option("Chrome")}
     * is text, which the browser also uses as the value — the same result the old
     * {@code option(valueAndText)} produced.
     */
    public static Tag option(Object... children) { return tag("option", children); }
    /** {@code optgroup(attrs().label("Cars"), option(...), ...)}. */
    public static Tag optgroup(Object... children) { return tag("optgroup", children); }
    /** {@code label(for_("email"), "Email:")}; {@code label("Email:")} is just text. */
    public static Tag label(Object... children) { return tag("label", children); }
    public static Tag button(Object... children) { return tag("button", children); }
    public static Tag fieldset(Object... children) { return tag("fieldset", children); }
    public static Tag legend(Object... children) { return tag("legend", children); }
    public static Tag progress(Object... attrs) { return tag("progress", attrs); }
    public static Tag meter(Object... attrs) { return tag("meter", attrs); }
    public static Tag output(Object... children) { return tag("output", children); }
    /** {@code datalist(id("browsers"), option("Chrome"), ...)}. */
    public static Tag datalist(Object... children) { return tag("datalist", children); }

    // ==================== Records as forms ====================

    /**
     * Renders a record as a form — one labelled field per component, typed by
     * the component's Java type, with the CSRF hidden field filled in from the
     * current request:
     *
     * <pre>
     * public record Contact(&#64;Form.Required String name,
     *                       &#64;Form.Required &#64;Form.Email String email,
     *                       &#64;Form.Required &#64;Form.Multiline(rows = 4) String message) {}
     *
     * form(Contact.class).action("/contact").submit("Send")
     * </pre>
     *
     * <p>The 23 {@code textInput}/{@code emailInput}/{@code field} helpers this
     * replaces are gone: a form is a record, and everything else is
     * {@code input(type("text"), name("q"))}.</p>
     *
     * @param recordType the record whose components become the fields
     * @param <T> the record type
     * @return the form builder, itself an {@link Element}
     * @see jweb.Form
     */
    public static <T extends Record> jweb.Form<T> form(Class<T> recordType) {
        return jweb.Form.of(recordType);
    }

    // ==================== Media ====================

    /**
     * {@code img("/logo.png")} renders {@code <img src="/logo.png">}.
     *
     * <p>Deliberate exception to the "a lone String is text" rule: {@code <img>}
     * is a void element, so a String argument can only be a URL.</p>
     */
    public static Tag img(String src) { return tag("img", new Attributes().src(src)); }
    /**
     * {@code img("/logo.png", "Logo")} renders {@code <img src="/logo.png" alt="Logo">}.
     *
     * <p>Deliberate exception to the "a lone String is text" rule: {@code <img>}
     * is a void element and cannot contain text.</p>
     */
    public static Tag img(String src, String alt) { return tag("img", new Attributes().src(src).alt(alt)); }
    /** {@code img(src("/a.png"), alt("A"), loading("lazy"))}. */
    public static Tag img(Object... attrs) { return tag("img", attrs); }
    public static Tag video(Object... children) { return tag("video", children); }
    public static Tag audio(Object... children) { return tag("audio", children); }
    public static Tag source(Object... attrs) { return tag("source", attrs); }
    public static Tag canvas(Object... children) { return tag("canvas", children); }
    public static Tag svg(Object... children) { return tag("svg", children); }
    public static Tag iframe(Object... children) { return tag("iframe", children); }
    public static Tag picture(Object... children) { return tag("picture", children); }
    public static Tag track(Object... attrs) { return tag("track", attrs); }
    public static Tag embed(Object... attrs) { return tag("embed", attrs); }
    public static Tag object(Object... children) { return tag("object", children); }
    public static Tag param(Object... attrs) { return tag("param", attrs); }
    public static Tag map(Object... children) { return tag("map", children); }
    public static Tag area(Object... attrs) { return tag("area", attrs); }

    // ==================== Scripting ====================

    public static Tag noscript(Object... children) { return tag("noscript", children); }
    /** A {@code <template>} element. */
    public static Tag template(Object... children) { return tag("template", children); }
    public static Tag slot(Object... children) { return tag("slot", children); }

    // ==================== Text Helpers ====================

    /**
     * Escaped text as an explicit node.
     *
     * @param content the text
     * @return the text node
     * @deprecated A bare String child already is escaped text — {@code p("Hi")},
     *             not {@code p(text("Hi"))}. The wrapper only survives for the
     *             rare spot that needs an {@link Element} value where no
     *             element factory is in reach.
     */
    @Deprecated
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
    public static <T> Element each(Collection<T> items, Function<T, ? extends jweb.Element> mapper) {
        List<VNode> nodes = items.stream()
            .map(mapper)
            .map(jweb.Element::toVNode)
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
     *
     * @param condition whether to render
     * @param element supplier for the element to render
     * @return the element, or nothing
     */
    public static Element when(boolean condition, java.util.function.Supplier<? extends jweb.Element> element) {
        return condition ? Element.of(element.get()) : nothing();
    }

    /**
     * Conditionally renders an element (eager evaluation).
     *
     * <p>Usage:</p>
     * <pre>
     * when(isLoggedIn, span("Welcome!"))
     * </pre>
     *
     * @param condition whether to render
     * @param element the element to render
     * @return the element, or nothing
     */
    public static Element when(boolean condition, jweb.Element element) {
        return condition ? Element.of(element) : nothing();
    }

    /**
     * Conditionally renders text — the form that makes conditional class names
     * read straight: {@code classes("chip", when(active, "chip-on"))}.
     *
     * @param condition whether to render
     * @param text the text to render
     * @return the text, or nothing
     */
    public static Element when(boolean condition, String text) {
        return condition ? Element.of(TextElement.of(text)) : nothing();
    }

    /**
     * A class only when the condition holds — as an element argument
     * ({@code li(TAB, when(active, ON))}) or inside {@code classes(...)}.
     * Null (nothing) otherwise, which every element skips.
     *
     * @param condition whether to set the class
     * @param cls the class handle
     * @return the handle, or null
     */
    public static jweb.Cls when(boolean condition, jweb.Cls cls) {
        return condition ? cls : null;
    }

    /**
     * Renders one of two things — the else form. Each branch may be an
     * {@link jweb.Element}, a String (text), or {@code null} (nothing):
     *
     * <pre>
     * when(active, span(cls("chip-on"), label), a(href(url), label))
     * </pre>
     *
     * @param condition which branch to render
     * @param ifTrue what to render when the condition holds
     * @param ifFalse what to render otherwise
     * @return the chosen branch
     */
    public static Element when(boolean condition, Object ifTrue, Object ifFalse) {
        return branch(condition ? ifTrue : ifFalse);
    }

    /**
     * The else form with both branches lazy — only the taken branch is built:
     *
     * <pre>
     * when(loggedIn, () -&gt; dashboard(user), () -&gt; loginPrompt())
     * </pre>
     *
     * @param condition which branch to render
     * @param ifTrue supplier for the true branch
     * @param ifFalse supplier for the false branch
     * @return the chosen branch
     */
    public static Element when(boolean condition,
                               java.util.function.Supplier<? extends jweb.Element> ifTrue,
                               java.util.function.Supplier<? extends jweb.Element> ifFalse) {
        return Element.of((condition ? ifTrue : ifFalse).get());
    }

    /**
     * Starts the chained else form, the shape the docs teach when a branch is
     * long enough that the argument list stops reading:
     *
     * <pre>
     * when(active)
     *     .then(span(cls("chip chip-on"), name))
     *     .otherwise(a(href("/tag/" + name), cls("chip"), name))
     * </pre>
     *
     * <p>A chain left without {@code otherwise(...)} is still an element — it
     * renders the {@code then} branch, or nothing.</p>
     *
     * @param condition the condition to check
     * @return the chain builder
     */
    public static jweb.When when(boolean condition) {
        return new jweb.When(condition);
    }

    /** One branch of a {@code when}: an Element, a String (text), or nothing. */
    private static Element branch(Object value) {
        return Element.of(jweb.When.branch(value));
    }

    /** An element that renders nothing. */
    private static Element nothing() {
        return () -> new VFragment(List.of());
    }

    // ==================== Generic Tag Factory ====================

    public static Tag tag(String name, Object... items) {
        return Tag.create(name, items);
    }

    public static Tag tag(String name, Attributes attrs, Object... children) {
        return new Tag(name, attrs, Tag.toVNodes(children));
    }

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
            java.util.function.Supplier<? extends jweb.Element> content,
            Function<Throwable, ? extends jweb.Element> fallback) {
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
            java.util.function.Supplier<? extends jweb.Element> content,
            Element fallback) {
        return ErrorBoundary.wrap(content, fallback);
    }

    /**
     * Creates an error boundary that silently fails (renders nothing on error).
     *
     * @param content the content to render
     * @return the rendered element or empty fragment on error
     */
    public static Element tryCatch(java.util.function.Supplier<? extends jweb.Element> content) {
        return ErrorBoundary.silent(content);
    }
}
