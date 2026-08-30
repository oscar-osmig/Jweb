package jweb;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.elements.Tag;

/**
 * The HTML DSL — every element, attribute helper, typed input, and
 * conditional in one import:
 *
 * <pre>{@code
 * import static jweb.El.*;
 *
 * div(class_("card"),
 *     h1(text("Hello")),
 *     p(text("Built entirely in Java"))
 * )
 * }</pre>
 *
 * <p>Combines the full surface of the legacy
 * {@code com.osmig.Jweb.framework.elements.El} and
 * {@code com.osmig.Jweb.framework.elements.Elements} entry points, so one
 * wildcard import replaces both.</p>
 *
 * <p>One deliberate difference from the legacy {@code El}:
 * {@code data(name, value)} builds a {@code data-*} <em>attribute</em>
 * (matching {@code Elements}); use {@code data_(...)} for the
 * {@code <data>} element.</p>
 */
@SuppressWarnings("deprecation")
public class El extends com.osmig.Jweb.framework.elements.Elements {

    protected El() {}

    public static Tag icon(String a) { return com.osmig.Jweb.framework.elements.El.icon(a); }
    public static Tag icon(String a, String b, String c) { return com.osmig.Jweb.framework.elements.El.icon(a, b, c); }
    public static Tag appleIcon(String a) { return com.osmig.Jweb.framework.elements.El.appleIcon(a); }
    public static Tag appleIcon(String a, String b) { return com.osmig.Jweb.framework.elements.El.appleIcon(a, b); }
    public static Tag a(Object... a) { return com.osmig.Jweb.framework.elements.El.a(a); }
    public static Tag option(String a) { return com.osmig.Jweb.framework.elements.El.option(a); }
    public static Tag video(Object... a) { return com.osmig.Jweb.framework.elements.El.video(a); }
    public static Tag audio(Object... a) { return com.osmig.Jweb.framework.elements.El.audio(a); }
    public static Attr srcset(String a) { return com.osmig.Jweb.framework.elements.El.srcset(a); }
    /** @deprecated Use {@code img(src(a), alt(b), srcset(a + " 1x," + c + " 2x"))} instead. */
    @Deprecated
    public static Tag responsiveImg(String a, String b, String c) { return com.osmig.Jweb.framework.elements.El.responsiveImg(a, b, c); }
    /** @deprecated Use {@code img(src(a), alt(b), attr("loading", "lazy"), attr("width", ...), attr("height", ...))} instead. */
    @Deprecated
    public static Tag lazyImg(String a, String b, int c, int d) { return com.osmig.Jweb.framework.elements.El.lazyImg(a, b, c, d); }
    public static Tag svg(Object... a) { return com.osmig.Jweb.framework.elements.El.svg(a); }
    public static Tag path(Object... a) { return com.osmig.Jweb.framework.elements.El.path(a); }
    public static Tag circle(Object... a) { return com.osmig.Jweb.framework.elements.El.circle(a); }
    public static Tag rect(Object... a) { return com.osmig.Jweb.framework.elements.El.rect(a); }
    public static Tag line(Object... a) { return com.osmig.Jweb.framework.elements.El.line(a); }
    public static Tag polyline(Object... a) { return com.osmig.Jweb.framework.elements.El.polyline(a); }
    public static Tag polygon(Object... a) { return com.osmig.Jweb.framework.elements.El.polygon(a); }
    public static Tag g(Object... a) { return com.osmig.Jweb.framework.elements.El.g(a); }
    public static Attr d(String a) { return com.osmig.Jweb.framework.elements.El.d(a); }
    public static Attr viewBox(String a) { return com.osmig.Jweb.framework.elements.El.viewBox(a); }
    public static Attr viewBox(int a, int b, int c, int d) { return com.osmig.Jweb.framework.elements.El.viewBox(a, b, c, d); }
    public static Attr fill(String a) { return com.osmig.Jweb.framework.elements.El.fill(a); }
    public static Attr stroke(String a) { return com.osmig.Jweb.framework.elements.El.stroke(a); }
    public static Attr strokeWidth(int a) { return com.osmig.Jweb.framework.elements.El.strokeWidth(a); }
    public static Tag meter(Object... a) { return com.osmig.Jweb.framework.elements.El.meter(a); }
    public static Tag meter(double a, double b, double c) { return com.osmig.Jweb.framework.elements.El.meter(a, b, c); }
    public static Tag progress(Object... a) { return com.osmig.Jweb.framework.elements.El.progress(a); }
    public static Tag progress(double a, double b) { return com.osmig.Jweb.framework.elements.El.progress(a, b); }
    /** @deprecated Use {@code progress()} — a progress element with no value is already indeterminate. */
    @Deprecated
    public static Tag progressIndeterminate() { return com.osmig.Jweb.framework.elements.El.progressIndeterminate(); }
    public static Tag template(Object... a) { return com.osmig.Jweb.framework.elements.El.template(a); }
    /** @deprecated Use {@code slot(name("..."))} instead — a lone String means text everywhere else. */
    @Deprecated
    public static Tag slot(String a) { return com.osmig.Jweb.framework.elements.El.slot(a); }
    /** @deprecated Use {@code time(datetime("2026-01-21"), text("January 21, 2026"))} instead. */
    @Deprecated
    public static Tag timeWithDatetime(String a, String b) { return com.osmig.Jweb.framework.elements.El.timeWithDatetime(a, b); }
    public static Tag bdo(Object... a) { return com.osmig.Jweb.framework.elements.El.bdo(a); }
    public static Tag figcaption(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.figcaption(a, b); }
    public static Tag dl(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.dl(a, b); }
    public static Tag dt(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.dt(a, b); }
    public static Tag dd(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.dd(a, b); }
    public static Tag abbr(String a, String b) { return com.osmig.Jweb.framework.elements.El.abbr(a, b); }
    public static Tag blockquote(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.blockquote(a, b); }
    public static Tag picture(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.picture(a, b); }
    public static Tag source(Object... a) { return com.osmig.Jweb.framework.elements.El.source(a); }
    public static Tag datalist(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.datalist(a, b); }
    public static Tag optgroup(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.optgroup(a, b); }
    public static Tag legend(Attributes a, Object... b) { return com.osmig.Jweb.framework.elements.El.legend(a, b); }
    public static Attr popover() { return com.osmig.Jweb.framework.elements.El.popover(); }
    public static Attr popover(String a) { return com.osmig.Jweb.framework.elements.El.popover(a); }
    /** The {@code popovertarget} attribute (exact HTML spelling). */
    public static Attr popovertarget(String a) { return com.osmig.Jweb.framework.elements.El.popovertarget(a); }
    /** The {@code popovertargetaction} attribute (exact HTML spelling). */
    public static Attr popovertargetaction(String a) { return com.osmig.Jweb.framework.elements.El.popovertargetaction(a); }
    /** @deprecated Use {@link #popovertarget(String)} — attribute names use exact HTML spelling. */
    @Deprecated
    public static Attr popoverTarget(String a) { return com.osmig.Jweb.framework.elements.El.popovertarget(a); }
    /** @deprecated Use {@link #popovertargetaction(String)} — attribute names use exact HTML spelling. */
    @Deprecated
    public static Attr popoverTargetAction(String a) { return com.osmig.Jweb.framework.elements.El.popovertargetaction(a); }
    /** @deprecated Use {@code div(id(a), popover("auto"), ...)} instead. */
    @Deprecated
    public static Tag autoPopover(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.autoPopover(a, b); }
    /** @deprecated Use {@code div(id(a), popover("manual"), ...)} instead. */
    @Deprecated
    public static Tag manualPopover(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.manualPopover(a, b); }
    /** @deprecated Use {@code button(popovertarget(a), ...)} instead. */
    @Deprecated
    public static Tag popoverToggleButton(String a, Object... b) { return com.osmig.Jweb.framework.elements.El.popoverToggleButton(a, b); }
}
