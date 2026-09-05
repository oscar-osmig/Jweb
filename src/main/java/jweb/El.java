package jweb;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.elements.Tag;

/**
 * The HTML DSL — every element, attribute helper, event handler, typed input,
 * and conditional in one import:
 *
 * <pre>{@code
 * import static jweb.El.*;
 *
 * div(class_("card"),
 *     h1("Hello"),
 *     p("Built entirely in Java"),
 *     button(id("save"), onClick(e -> save()), style().padding(px(8)), "Save")
 * )
 * }</pre>
 *
 * <p>Two rules cover the surface: every element is {@code name(Object...)},
 * and a String argument is always text. Attributes, handlers, styles and
 * children mix in any order; {@code attrs()} exists for the long tail.</p>
 *
 * <p>Combines the full surface of the legacy
 * {@code com.osmig.Jweb.framework.elements.El} and
 * {@code com.osmig.Jweb.framework.elements.Elements} entry points, so one
 * wildcard import replaces both. {@code data(name, value)} builds a
 * {@code data-*} attribute; the rare {@code <data>} and {@code <var>} elements
 * are {@code tag("data", ...)} / {@code tag("var", ...)}.</p>
 */
@SuppressWarnings("deprecation")
public class El extends com.osmig.Jweb.framework.elements.Elements {

    protected El() {}

    public static Tag icon(String a) { return com.osmig.Jweb.framework.elements.El.icon(a); }
    public static Tag icon(String a, String b, String c) { return com.osmig.Jweb.framework.elements.El.icon(a, b, c); }
    public static Tag appleIcon(String a) { return com.osmig.Jweb.framework.elements.El.appleIcon(a); }
    public static Tag appleIcon(String a, String b) { return com.osmig.Jweb.framework.elements.El.appleIcon(a, b); }
    public static Tag video(Object... a) { return com.osmig.Jweb.framework.elements.El.video(a); }
    public static Tag audio(Object... a) { return com.osmig.Jweb.framework.elements.El.audio(a); }
    public static Attr srcset(String a) { return com.osmig.Jweb.framework.elements.El.srcset(a); }
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
    public static Tag template(Object... a) { return com.osmig.Jweb.framework.elements.El.template(a); }
    public static Tag bdo(Object... a) { return com.osmig.Jweb.framework.elements.El.bdo(a); }
    public static Tag source(Object... a) { return com.osmig.Jweb.framework.elements.El.source(a); }
    public static Attr popover() { return com.osmig.Jweb.framework.elements.El.popover(); }
    public static Attr popover(String a) { return com.osmig.Jweb.framework.elements.El.popover(a); }
    /** The {@code popovertarget} attribute (exact HTML spelling). */
    public static Attr popovertarget(String a) { return com.osmig.Jweb.framework.elements.El.popovertarget(a); }
    /** The {@code popovertargetaction} attribute (exact HTML spelling). */
    public static Attr popovertargetaction(String a) { return com.osmig.Jweb.framework.elements.El.popovertargetaction(a); }
}
