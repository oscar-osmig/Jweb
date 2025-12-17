package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Static factory methods for creating HTML elements.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.elements.Elements.*;
 *
 *   div(class_("container"), id("main"),
 *       h1("Hello World"),
 *       p(class_("lead"), "Welcome!")
 *   )
 */
public final class Elements {

    private Elements() {}

    // ==================== Attribute Builder ====================

    /**
     * Creates a new Attributes builder.
     * Usage: div(attrs().class_("card").id("main"), ...)
     */
    public static Attributes attrs() { return new Attributes(); }

    // ==================== Attribute Shortcuts ====================

    public static Attr id(String value) { return Attr.id(value); }
    public static Attr class_(String value) { return Attr.class_(value); }
    public static Attr style_(String value) { return Attr.style(value); }
    public static Attr href(String value) { return Attr.href(value); }
    public static Attr src(String value) { return Attr.src(value); }
    public static Attr alt(String value) { return Attr.alt(value); }
    public static Attr type(String value) { return Attr.type(value); }
    public static Attr name(String value) { return Attr.name(value); }
    public static Attr value(String value) { return Attr.value(value); }
    public static Attr placeholder(String value) { return Attr.placeholder(value); }
    public static Attr action(String value) { return Attr.action(value); }
    public static Attr method(String value) { return Attr.method(value); }
    public static Attr target(String value) { return Attr.target(value); }
    public static Attr title_(String value) { return Attr.title(value); }
    public static Attr for_(String value) { return Attr.for_(value); }
    public static Attr role(String value) { return Attr.role(value); }
    public static Attr disabled() { return Attr.disabled(); }
    public static Attr checked() { return Attr.checked(); }
    public static Attr required() { return Attr.required(); }
    public static Attr readonly() { return Attr.readonly(); }
    public static Attr hidden() { return Attr.hidden(); }
    public static Attr autofocus() { return Attr.autofocus(); }
    public static Attr data(String name, String value) { return Attr.data(name, value); }
    public static Attr aria(String name, String value) { return Attr.aria(name, value); }
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
        return tag("meta", new Attributes().name(name).attr("content", content));
    }
    public static Tag link(Object... attrs) { return tag("link", attrs); }
    public static Tag link(Attributes attrs) { return new Tag("link", attrs); }
    public static Tag css(String href) {
        return tag("link", new Attributes().attr("rel", "stylesheet").href(href));
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
     * Usage: when(isLoggedIn, () -> span("Welcome!"))
     */
    public static Element when(boolean condition, java.util.function.Supplier<Element> element) {
        if (condition) {
            return element.get();
        }
        return () -> new VFragment(List.of());
    }

    /**
     * Conditionally renders one of two elements.
     *
     * Usage: ifElse(isLoggedIn, () -> span("Welcome!"), () -> link("/login", "Sign In"))
     */
    public static Element ifElse(
            boolean condition,
            java.util.function.Supplier<Element> ifTrue,
            java.util.function.Supplier<Element> ifFalse) {
        return condition ? ifTrue.get() : ifFalse.get();
    }

    // ==================== Generic Tag Factory ====================

    public static Tag tag(String name, Object... items) {
        return Tag.create(name, items);
    }

    public static Tag tag(String name, Attributes attrs, Object... children) {
        return new Tag(name, attrs, Tag.toVNodes(children));
    }
}
