package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import java.util.Collection;
import java.util.function.Function;

/** Main entry point for HTML elements. Use: {@code import static El.*;} */
public final class El {
    private El() {}

    // ==================== Attribute Builder ====================
    public static Attributes attrs() { return new Attributes(); }

    // ==================== Core Attribute Shortcuts ====================
    public static Attr id(String v) { return Attr.id(v); }
    public static Attr class_(String v) { return Attr.class_(v); }
    public static Attr href(String v) { return Attr.href(v); }
    public static Attr src(String v) { return Attr.src(v); }
    public static Attr type(String v) { return Attr.type(v); }
    public static Attr name(String v) { return Attr.name(v); }
    public static Attr value(String v) { return Attr.value(v); }
    public static Attr disabled() { return Attr.disabled(); }
    public static Attr required() { return Attr.required(); }
    public static Attr attr(String n, String v) { return Attr.attr(n, v); }

    // ==================== Document ====================
    public static Tag html(Object... c) { return DocumentElements.html(c); }
    public static Tag head(Object... c) { return DocumentElements.head(c); }
    public static Tag body(Object... c) { return DocumentElements.body(c); }
    public static Tag title(String t) { return DocumentElements.title(t); }
    public static Tag meta(Object... a) { return DocumentElements.meta(a); }
    public static Tag link(Object... a) { return DocumentElements.link(a); }
    public static Tag css(String href) { return DocumentElements.css(href); }
    public static Tag script(String s) { return DocumentElements.script(s); }
    public static Tag inlineScript(String c) { return DocumentElements.inlineScript(c); }
    public static Tag style(String c) { return DocumentElements.style(c); }

    // ==================== Semantic ====================
    public static Tag header(Object... c) { return SemanticElements.header(c); }
    public static Tag footer(Object... c) { return SemanticElements.footer(c); }
    public static Tag nav(Object... c) { return SemanticElements.nav(c); }
    public static Tag main(Object... c) { return SemanticElements.main(c); }
    public static Tag section(Object... c) { return SemanticElements.section(c); }
    public static Tag article(Object... c) { return SemanticElements.article(c); }
    public static Tag aside(Object... c) { return SemanticElements.aside(c); }

    // ==================== Text ====================
    public static Tag div(Object... c) { return TextElements.div(c); }
    public static Tag span(Object... c) { return TextElements.span(c); }
    public static Tag p(Object... c) { return TextElements.p(c); }
    public static Tag h1(Object... c) { return TextElements.h1(c); }
    public static Tag h2(Object... c) { return TextElements.h2(c); }
    public static Tag h3(Object... c) { return TextElements.h3(c); }
    public static Tag h4(Object... c) { return TextElements.h4(c); }
    public static Tag h5(Object... c) { return TextElements.h5(c); }
    public static Tag h6(Object... c) { return TextElements.h6(c); }
    public static Tag strong(Object... c) { return TextElements.strong(c); }
    public static Tag em(Object... c) { return TextElements.em(c); }
    public static Tag a(String h, Object... c) { return TextElements.a(h, c); }
    public static Tag a(Attributes a, Object... c) { return TextElements.a(a, c); }
    public static Tag small(Object... c) { return TextElements.small(c); }
    public static Tag code(Object... c) { return TextElements.code(c); }
    public static Tag pre(Object... c) { return TextElements.pre(c); }
    public static Tag br() { return TextElements.br(); }

    // ==================== Lists ====================
    public static Tag ul(Object... c) { return ListElements.ul(c); }
    public static Tag ol(Object... c) { return ListElements.ol(c); }
    public static Tag li(Object... c) { return ListElements.li(c); }

    // ==================== Table ====================
    public static Tag table(Object... c) { return TableElements.table(c); }
    public static Tag thead(Object... c) { return TableElements.thead(c); }
    public static Tag tbody(Object... c) { return TableElements.tbody(c); }
    public static Tag tr(Object... c) { return TableElements.tr(c); }
    public static Tag th(Object... c) { return TableElements.th(c); }
    public static Tag td(Object... c) { return TableElements.td(c); }

    // ==================== Form ====================
    public static Tag form(Object... c) { return FormElements.form(c); }
    public static Tag input(Object... a) { return FormElements.input(a); }
    public static Tag textarea(Object... c) { return FormElements.textarea(c); }
    public static Tag select(Object... c) { return FormElements.select(c); }
    public static Tag option(String v, String t) { return FormElements.option(v, t); }
    public static Tag label(Object... c) { return FormElements.label(c); }
    public static Tag button(Object... c) { return FormElements.button(c); }

    // ==================== Media ====================
    public static Tag img(String s) { return MediaElements.img(s); }
    public static Tag img(String s, String a) { return MediaElements.img(s, a); }

    // ==================== Helpers ====================
    public static TextElement text(String c) { return TextElement.of(c); }
    public static TextElement raw(String h) { return TextElement.raw(h); }
    public static Element fragment(Object... c) { return Elements.fragment(c); }
    public static <T> Element each(Collection<T> i, Function<T, Element> m) { return Elements.each(i, m); }
    public static Tag tag(String n, Object... c) { return Tag.create(n, c); }
}
