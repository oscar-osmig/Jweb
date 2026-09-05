package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import java.util.Collection;
import java.util.function.Function;

/**
 * Main entry point for HTML elements. Use: {@code import static El.*;}
 *
 * @deprecated Replaced by {@code jweb.El} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
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
    /** Creates a datetime attribute for {@code <time>}. */
    public static Attr datetime(String v) { return Attr.datetime(v); }
    /** Creates a loading attribute for images/iframes: "lazy" or "eager". */
    public static Attr loading(String v) { return Attr.loading(v); }

    // ==================== Document ====================
    public static Tag html(Object... c) { return DocumentElements.html(c); }
    public static Tag head(Object... c) { return DocumentElements.head(c); }
    public static Tag body(Object... c) { return DocumentElements.body(c); }
    public static Tag title(String t) { return DocumentElements.title(t); }
    public static Tag meta(Object... a) { return DocumentElements.meta(a); }
    public static Tag metaCharset() { return DocumentElements.metaCharset(); }
    public static Tag metaViewport() { return DocumentElements.metaViewport(); }
    public static Tag link(Object... a) { return DocumentElements.link(a); }
    public static Tag css(String href) { return DocumentElements.css(href); }
    /** {@code script(src("/app.js"), attr("defer", ""))}. Content is never HTML-escaped. */
    public static Tag script(Object... a) { return DocumentElements.script(a); }
    /** @deprecated Use {@code script(src("..."))} instead. */
    @Deprecated
    public static Tag script(String s) { return DocumentElements.script(s); }
    /** Inline JavaScript — emitted verbatim, never HTML-escaped. */
    public static Tag inlineScript(String c) { return DocumentElements.inlineScript(c); }
    public static Tag style(String c) { return DocumentElements.style(c); }
    public static Tag icon(String h) { return DocumentElements.icon(h); }
    public static Tag icon(String h, String s, String t) { return DocumentElements.icon(h, s, t); }
    public static Tag appleIcon(String h) { return DocumentElements.appleIcon(h); }
    public static Tag appleIcon(String h, String s) { return DocumentElements.appleIcon(h, s); }

    // ==================== Semantic ====================
    public static Tag header(Object... c) { return SemanticElements.header(c); }
    public static Tag footer(Object... c) { return SemanticElements.footer(c); }
    public static Tag nav(Object... c) { return SemanticElements.nav(c); }
    public static Tag main(Object... c) { return SemanticElements.main(c); }
    public static Tag section(Object... c) { return SemanticElements.section(c); }
    public static Tag article(Object... c) { return SemanticElements.article(c); }
    public static Tag aside(Object... c) { return SemanticElements.aside(c); }
    public static Tag hgroup(Object... c) { return SemanticElements.hgroup(c); }
    public static Tag search(Object... c) { return SemanticElements.search(c); }
    public static Tag address(Object... c) { return SemanticElements.address(c); }

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
    public static Tag a(Attributes a, Object... c) { return TextElements.a(a, c); }
    public static Tag a(Object... c) { return TextElements.a(c); }
    public static Tag small(Object... c) { return TextElements.small(c); }
    public static Tag code(Object... c) { return TextElements.code(c); }
    public static Tag pre(Object... c) { return TextElements.pre(c); }
    public static Tag time(Object... c) { return TextElements.time(c); }
    public static Tag wbr() { return TextElements.wbr(); }
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
    /** {@code textarea("Hello")} renders {@code <textarea>Hello</textarea>}. For the name use {@code textarea(name("bio"))}. */
    public static Tag textarea(String text) { return Tag.create("textarea", TextElement.of(text)); }
    public static Tag select(Object... c) { return FormElements.select(c); }
    public static Tag option(Object... c) { return FormElements.option(c); }
    public static Tag label(Object... c) { return FormElements.label(c); }
    /** {@code label("Email:")} renders {@code <label>Email:</label>}. For the target use {@code label(for_("email"), ...)}. */
    public static Tag label(String text) { return Tag.create("label", TextElement.of(text)); }
    public static Tag button(Object... c) { return FormElements.button(c); }

    // ==================== Media ====================
    /**
     * {@code img("/logo.png")} renders {@code <img src="/logo.png">} — the
     * deliberate exception to "a lone String is text": {@code <img>} is a void
     * element and cannot contain text, so a String can only be a URL.
     */
    public static Tag img(String s) { return MediaElements.img(s); }
    /** {@code img(src, alt)} — see {@link #img(String)} for why a String here is a URL, not text. */
    public static Tag img(String s, String a) { return MediaElements.img(s, a); }
    /** {@code img(src("/a.png"), alt("A"), loading("lazy"))}. */
    public static Tag img(Object... a) { return MediaElements.img(a); }
    public static Tag video(Object... c) { return MediaElements.video(c); }
    public static Tag audio(Object... c) { return MediaElements.audio(c); }
    public static Tag canvas(Object... c) { return MediaElements.canvas(c); }
    public static Tag iframe(Object... c) { return MediaElements.iframe(c); }
    public static Tag track(Object... a) { return MediaElements.track(a); }
    public static Tag embed(Object... a) { return MediaElements.embed(a); }
    public static Tag object(Object... c) { return MediaElements.object(c); }
    public static Tag param(Object... a) { return MediaElements.param(a); }
    public static Tag map(Object... c) { return MediaElements.map(c); }
    public static Tag area(Object... a) { return MediaElements.area(a); }
    public static Attr srcset(String v) { return PictureElements.srcset(v); }
    /** @deprecated Use {@code img(src(src), alt(alt), srcset(src + " 1x," + src2x + " 2x"))} instead. */
    @Deprecated
    public static Tag responsiveImg(String src, String alt, String src2x) { return PictureElements.responsiveImg(src, alt, src2x); }
    /** @deprecated Use {@code img(src(src), alt(alt), attr("loading", "lazy"), attr("width", ...), attr("height", ...))} instead. */
    @Deprecated
    public static Tag lazyImg(String src, String alt, int w, int h) { return PictureElements.lazyImg(src, alt, w, h); }

    // ==================== SVG ====================
    public static Tag svg(Object... c) { return SVGElements.svg(c); }
    public static Tag path(Object... c) { return SVGElements.path(c); }
    public static Tag circle(Object... c) { return SVGElements.circle(c); }
    public static Tag rect(Object... c) { return SVGElements.rect(c); }
    public static Tag line(Object... c) { return SVGElements.line(c); }
    public static Tag polyline(Object... c) { return SVGElements.polyline(c); }
    public static Tag polygon(Object... c) { return SVGElements.polygon(c); }
    public static Tag g(Object... c) { return SVGElements.g(c); }
    public static Attr d(String v) { return SVGElements.d(v); }
    public static Attr viewBox(String v) { return SVGElements.viewBox(v); }
    public static Attr viewBox(int x, int y, int w, int h) { return SVGElements.viewBox(x, y, w, h); }
    public static Attr fill(String v) { return SVGElements.fill(v); }
    public static Attr stroke(String v) { return SVGElements.stroke(v); }
    public static Attr strokeWidth(int v) { return SVGElements.strokeWidth(v); }

    // ==================== Modern HTML5 Elements ====================
    public static Tag dialog(Object... c) { return ModernElements.dialog(c); }
    public static Tag details(Object... c) { return ModernElements.details(c); }
    public static Tag summary(Object... c) { return ModernElements.summary(c); }
    public static Tag meter(Object... a) { return ModernElements.meter(a); }
    public static Tag meter(double v, double min, double max) { return ModernElements.meter(v, min, max); }
    public static Tag progress(Object... a) { return ModernElements.progress(a); }
    public static Tag progress(double v, double max) { return ModernElements.progress(v, max); }
    /** @deprecated Use {@code progress()} — a progress element with no value is already indeterminate. */
    @Deprecated
    public static Tag progressIndeterminate() { return ModernElements.progressIndeterminate(); }
    public static Tag template(Object... c) { return ModernElements.template(c); }
    public static Tag slot(Object... c) { return ModernElements.slot(c); }
    /** @deprecated Use {@code slot(name("..."))} instead — a lone String means text everywhere else. */
    @Deprecated
    public static Tag slot(String n) { return ModernElements.slot(n); }
    public static Tag output(Object... c) { return ModernElements.output(c); }
    /** @deprecated Use {@code time(datetime("2026-01-21"), text("January 21, 2026"))} instead. */
    @Deprecated
    public static Tag timeWithDatetime(String dt, String txt) { return ModernElements.timeWithDatetime(dt, txt); }
    public static Tag data(String v, String txt) { return ModernElements.data(v, txt); }
    public static Tag bdi(Object... c) { return ModernElements.bdi(c); }
    public static Tag bdo(Object... c) { return ModernElements.bdo(c); }
    public static Tag ruby(Object... c) { return ModernElements.ruby(c); }
    public static Tag rt(Object... c) { return ModernElements.rt(c); }
    public static Tag rp(Object... c) { return ModernElements.rp(c); }

    // ==================== Figure & Caption ====================
    public static Tag figure(Object... c) { return FigureElements.figure(c); }
    public static Tag figure(Attributes a, Object... c) { return FigureElements.figure(a, c); }
    public static Tag figcaption(Object... c) { return FigureElements.figcaption(c); }
    public static Tag figcaption(Attributes a, Object... c) { return FigureElements.figcaption(a, c); }

    // ==================== Definition Lists ====================
    public static Tag dl(Object... c) { return DefinitionElements.dl(c); }
    public static Tag dl(Attributes a, Object... c) { return DefinitionElements.dl(a, c); }
    public static Tag dt(Object... c) { return DefinitionElements.dt(c); }
    public static Tag dt(Attributes a, Object... c) { return DefinitionElements.dt(a, c); }
    public static Tag dd(Object... c) { return DefinitionElements.dd(c); }
    public static Tag dd(Attributes a, Object... c) { return DefinitionElements.dd(a, c); }

    // ==================== Interactive/Semantic Text ====================
    public static Tag abbr(Object... c) { return InteractiveElements.abbr(c); }
    public static Tag dfn(Object... c) { return InteractiveElements.dfn(c); }
    public static Tag cite(Object... c) { return InteractiveElements.cite(c); }
    public static Tag q(Object... c) { return InteractiveElements.q(c); }
    /** {@code q("Hello")} renders {@code <q>Hello</q>}. For a source URL use {@code q(attr("cite", url), ...)}. */
    public static Tag q(String text) { return Tag.create("q", TextElement.of(text)); }
    public static Tag blockquote(Object... c) { return InteractiveElements.blockquote(c); }
    /** {@code blockquote("Quote")} renders {@code <blockquote>Quote</blockquote>}. For a source URL use {@code blockquote(attr("cite", url), ...)}. */
    public static Tag blockquote(String text) { return Tag.create("blockquote", TextElement.of(text)); }
    public static Tag blockquote(Attributes a, Object... c) { return InteractiveElements.blockquote(a, c); }
    public static Tag kbd(Object... c) { return InteractiveElements.kbd(c); }
    public static Tag samp(Object... c) { return InteractiveElements.samp(c); }
    public static Tag mark(Object... c) { return InteractiveElements.mark(c); }
    public static Tag sub(Object... c) { return InteractiveElements.sub(c); }
    public static Tag sup(Object... c) { return InteractiveElements.sup(c); }
    public static Tag ins(Object... c) { return InteractiveElements.ins(c); }
    public static Tag del(Object... c) { return InteractiveElements.del(c); }
    public static Tag s(Object... c) { return InteractiveElements.s(c); }

    // ==================== Picture & Responsive Images ====================
    public static Tag picture(Object... c) { return PictureElements.picture(c); }
    public static Tag picture(Attributes a, Object... c) { return PictureElements.picture(a, c); }
    public static Tag source(Object... c) { return PictureElements.source(c); }

    // ==================== Form Enhancements ====================
    public static Tag datalist(Object... c) { return Tag.create("datalist", c); }
    /** {@code datalist("Browsers")} renders {@code <datalist>Browsers</datalist>}. For the id use {@code datalist(id("browsers"), ...)}. */
    public static Tag datalist(String text) { return Tag.create("datalist", TextElement.of(text)); }
    public static Tag datalist(Attributes a, Object... c) { return FormEnhancements.datalist(a, c); }
    public static Tag optgroup(Object... c) { return Tag.create("optgroup", c); }
    /** {@code optgroup("Cars")} renders {@code <optgroup>Cars</optgroup>}. For the label use {@code optgroup(attr("label", "Cars"), ...)}. */
    public static Tag optgroup(String text) { return Tag.create("optgroup", TextElement.of(text)); }
    public static Tag fieldset(Object... c) { return FormEnhancements.fieldset(c); }
    public static Tag fieldset(Attributes a, Object... c) { return FormEnhancements.fieldset(a, c); }
    public static Tag legend(Object... c) { return FormEnhancements.legend(c); }
    public static Tag legend(Attributes a, Object... c) { return FormEnhancements.legend(a, c); }

    // ==================== Popovers ====================
    public static Attr popover() { return PopoverElements.popover(); }
    public static Attr popover(String type) { return PopoverElements.popover(type); }
    /** The {@code popovertarget} attribute (exact HTML spelling). */
    public static Attr popovertarget(String targetId) { return PopoverElements.popovertarget(targetId); }
    /** The {@code popovertargetaction} attribute (exact HTML spelling). */
    public static Attr popovertargetaction(String action) { return PopoverElements.popovertargetaction(action); }
    /** @deprecated Use {@link #popovertarget(String)} — attribute names use exact HTML spelling. */
    @Deprecated
    public static Attr popoverTarget(String targetId) { return PopoverElements.popovertarget(targetId); }
    /** @deprecated Use {@link #popovertargetaction(String)} — attribute names use exact HTML spelling. */
    @Deprecated
    public static Attr popoverTargetAction(String action) { return PopoverElements.popovertargetaction(action); }
    /** @deprecated Use {@code div(id(id), popover("auto"), ...)} instead. */
    @Deprecated
    public static Tag autoPopover(String id, Object... c) { return PopoverElements.autoPopover(id, c); }
    /** @deprecated Use {@code div(id(id), popover("manual"), ...)} instead. */
    @Deprecated
    public static Tag manualPopover(String id, Object... c) { return PopoverElements.manualPopover(id, c); }
    /** @deprecated Use {@code button(popovertarget(targetId), ...)} instead. */
    @Deprecated
    public static Tag popoverToggleButton(String targetId, Object... c) { return PopoverElements.popoverToggleButton(targetId, c); }

    // ==================== Misc Elements ====================
    public static Tag hr(Object... a) { return Elements.hr(a); }

    // ==================== Conditionals ====================
    public static Element when(boolean condition, Element element) { return Elements.when(condition, element); }
    public static Element when(boolean condition, java.util.function.Supplier<Element> element) { return Elements.when(condition, element); }
    public static Element match(Elements.CondCase... cases) { return Elements.match(cases); }
    public static Elements.CondCase cond(boolean condition, Element element) { return Elements.cond(condition, element); }
    public static Elements.CondCase cond(boolean condition, java.util.function.Supplier<Element> element) { return Elements.cond(condition, element); }
    public static Elements.CondCase otherwise(Element element) { return Elements.otherwise(element); }
    public static Elements.CondCase otherwise(java.util.function.Supplier<Element> element) { return Elements.otherwise(element); }
    public static Element errorBoundary(java.util.function.Supplier<Element> content, Function<Throwable, Element> fallback) { return Elements.errorBoundary(content, fallback); }
    public static Element errorBoundary(java.util.function.Supplier<Element> content, Element fallback) { return Elements.errorBoundary(content, fallback); }

    // ==================== Helpers ====================
    public static TextElement text(String c) { return TextElement.of(c); }
    public static TextElement raw(String h) { return TextElement.raw(h); }
    public static Element fragment(Object... c) { return Elements.fragment(c); }
    public static <T> Element each(Collection<T> i, Function<T, Element> m) { return Elements.each(i, m); }
    public static Tag tag(String n, Object... c) { return Tag.create(n, c); }
}
