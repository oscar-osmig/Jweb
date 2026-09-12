package jweb;

import com.osmig.Jweb.framework.elements.SVGElements;

/**
 * The HTML DSL — every element, attribute helper, event handler, conditional
 * and the record-driven form, in one import:
 *
 * <pre>{@code
 * import static jweb.El.*;
 *
 * div(cls("card"),
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
 * <p>Class names are {@code cls("a b")}, or {@code classes(...)} when some
 * parts are conditional. A choice is {@code when(cond, ifTrue, ifFalse)}, or
 * {@code when(cond).then(a).otherwise(b)} when a branch runs long. A form is a
 * record: {@code form(Contact.class)} — see {@link Form}.</p>
 *
 * <p>{@code data(name, value)} builds a {@code data-*} attribute; the rare
 * {@code <data>} and {@code <var>} elements are {@code tag("data", ...)} /
 * {@code tag("var", ...)}. Four attributes have no free static name because an
 * element owns it — {@code title}, {@code label}, {@code cite} and
 * {@code style} live on {@code attrs()} (a {@code style()} builder is also an
 * element argument of its own).</p>
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

    // ---- the rest of the SVG element set ----
    // Three names differ from the SVG tag because the tag is already taken by a
    // name authors reach for more often under the four wildcards: text(...) is
    // the text node, and linearGradient/radialGradient are CSS values.

    /** The {@code <defs>} element — reusable definitions referenced by id. */
    public static Tag defs(Object... a) { return SVGElements.defs(a); }
    /** The {@code <symbol>} element — a definition instantiated by {@link #use}. */
    public static Tag symbol(Object... a) { return SVGElements.symbol(a); }
    /** The {@code <use>} element: {@code use(attr("href", "#icon-check"))}. */
    public static Tag use(Object... a) { return SVGElements.use(a); }
    /** The {@code <ellipse>} element. */
    public static Tag ellipse(Object... a) { return SVGElements.ellipse(a); }
    /**
     * The SVG {@code <text>} element — spelled {@code svgText} because
     * {@code text} is the text node everywhere in this DSL.
     *
     * @param a attributes and children
     * @return the {@code <text>} element
     */
    public static Tag svgText(Object... a) { return SVGElements.text(a); }
    /** The {@code <tspan>} element — a run inside {@link #svgText}. */
    public static Tag tspan(Object... a) { return SVGElements.tspan(a); }
    /** The {@code <textPath>} element — text laid along a path. */
    public static Tag textPath(Object... a) { return SVGElements.textPath(a); }
    /**
     * The SVG {@code <linearGradient>} element — spelled {@code svgLinearGradient}
     * because {@code linearGradient(...)} is the CSS value.
     *
     * @param a attributes and children
     * @return the {@code <linearGradient>} element
     */
    public static Tag svgLinearGradient(Object... a) { return SVGElements.linearGradient(a); }
    /**
     * The SVG {@code <radialGradient>} element — spelled {@code svgRadialGradient}
     * because {@code radialGradient(...)} is the CSS value.
     *
     * @param a attributes and children
     * @return the {@code <radialGradient>} element
     */
    public static Tag svgRadialGradient(Object... a) { return SVGElements.radialGradient(a); }
    /** The {@code <stop>} element — one gradient stop. */
    public static Tag stop(Object... a) { return SVGElements.stop(a); }
    /** The {@code <pattern>} element. */
    public static Tag pattern(Object... a) { return SVGElements.pattern(a); }
    /** The {@code <filter>} element — the filter primitives' container. */
    public static Tag filter(Object... a) { return SVGElements.filter(a); }
    /** The {@code <mask>} element. */
    public static Tag mask(Object... a) { return SVGElements.mask(a); }
    /** The {@code <clipPath>} element. */
    public static Tag clipPath(Object... a) { return SVGElements.clipPath(a); }
    /** The SVG {@code <image>} element. */
    public static Tag image(Object... a) { return SVGElements.image(a); }
    /** The {@code <animate>} element. */
    public static Tag animate(Object... a) { return SVGElements.animate(a); }
    /** The {@code <animateTransform>} element. */
    public static Tag animateTransform(Object... a) { return SVGElements.animateTransform(a); }
    /** The {@code <animateMotion>} element. */
    public static Tag animateMotion(Object... a) { return SVGElements.animateMotion(a); }
    /** The {@code <feGaussianBlur>} filter primitive. */
    public static Tag feGaussianBlur(Object... a) { return SVGElements.feGaussianBlur(a); }
    /** The {@code <feDropShadow>} filter primitive. */
    public static Tag feDropShadow(Object... a) { return SVGElements.feDropShadow(a); }
    /** The {@code <feColorMatrix>} filter primitive. */
    public static Tag feColorMatrix(Object... a) { return SVGElements.feColorMatrix(a); }

    // ==================== Dialog and details, as Actions ====================
    // The <dialog> and <details> methods have no attribute twin, so driving them
    // needs a line of JavaScript. These are Actions: button(onClick(openDialog("x")), ...)

    /**
     * Opens a {@code <dialog>} as a modal (with backdrop and focus trap).
     *
     * <p>Named {@code openDialog} rather than {@code showModal} because
     * {@code jweb.Js.showModal} is the div-based modal helper.</p>
     *
     * @param dialogId the dialog's id
     * @return the Action
     */
    public static Action openDialog(String dialogId) {
        return () -> "document.getElementById('" + escapeJs(dialogId) + "').showModal()";
    }

    /**
     * Closes a {@code <dialog>}.
     *
     * @param dialogId the dialog's id
     * @return the Action
     */
    public static Action closeDialog(String dialogId) {
        return () -> "document.getElementById('" + escapeJs(dialogId) + "').close()";
    }

    /**
     * Closes a {@code <dialog>} with a return value, readable as
     * {@code dialog.returnValue}.
     *
     * @param dialogId the dialog's id
     * @param returnValue the value to return
     * @return the Action
     */
    public static Action closeDialog(String dialogId, String returnValue) {
        return () -> "document.getElementById('" + escapeJs(dialogId) + "').close('"
            + escapeJs(returnValue) + "')";
    }

    /**
     * Opens a closed {@code <dialog>} as a modal, closes an open one.
     *
     * @param dialogId the dialog's id
     * @return the Action
     */
    public static Action toggleDialog(String dialogId) {
        return () -> "(function(d){d.open?d.close():d.showModal()})(document.getElementById('"
            + escapeJs(dialogId) + "'))";
    }

    /**
     * Expands a {@code <details>} element.
     *
     * @param detailsId the details element's id
     * @return the Action
     */
    public static Action openDetails(String detailsId) {
        return () -> "document.getElementById('" + escapeJs(detailsId) + "').open=true";
    }

    /**
     * Collapses a {@code <details>} element.
     *
     * @param detailsId the details element's id
     * @return the Action
     */
    public static Action closeDetails(String detailsId) {
        return () -> "document.getElementById('" + escapeJs(detailsId) + "').open=false";
    }

    /**
     * Toggles a {@code <details>} element.
     *
     * @param detailsId the details element's id
     * @return the Action
     */
    public static Action toggleDetails(String detailsId) {
        return () -> "(function(d){d.open=!d.open})(document.getElementById('"
            + escapeJs(detailsId) + "'))";
    }

    private static String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * The {@code <title>} <em>element</em> — a document title, for {@code head(...)}
     * and for the accessible name of an {@code <svg>}.
     *
     * <p>The trap: {@code button(title("Save"), "Save")} compiles and nests a
     * {@code <title>} inside the button instead of setting the tooltip, because
     * the title attribute has no free static name. The tooltip is
     * {@code button(attrs().title("Save"), "Save")}; a {@code <title>} built
     * anywhere but a head/SVG parent logs a warning at render time.</p>
     *
     * @param text the title text
     * @return the {@code <title>} element
     */
    public static Tag title(String text) { return com.osmig.Jweb.framework.elements.Elements.title(text); }
    public static Attr d(String a) { return com.osmig.Jweb.framework.elements.El.d(a); }
    public static Attr viewBox(String a) { return com.osmig.Jweb.framework.elements.El.viewBox(a); }
    public static Attr viewBox(int a, int b, int c, int d) { return com.osmig.Jweb.framework.elements.El.viewBox(a, b, c, d); }
    public static Attr fill(String a) { return com.osmig.Jweb.framework.elements.El.fill(a); }
    public static Attr stroke(String a) { return com.osmig.Jweb.framework.elements.El.stroke(a); }

    // ---- SVG attributes as free statics ----
    // Every coordinate and length takes an int, a double or a String, so
    // circle(cx(50), cy(50), r(40)) and rect(x("10%"), width(180.5)) both read
    // as the markup would. (attrs() carries the same set.)

    /** The {@code stroke-width} attribute. */
    public static Attr strokeWidth(int a) { return svgAttr("stroke-width", a); }
    /** The {@code stroke-width} attribute. */
    public static Attr strokeWidth(double a) { return svgAttr("stroke-width", a); }
    /** The {@code stroke-width} attribute. */
    public static Attr strokeWidth(String a) { return new Attr("stroke-width", a); }
    /** The {@code width} attribute — an SVG length, or an {@code img}/{@code video} size. */
    public static Attr width(int a) { return svgAttr("width", a); }
    /** The {@code width} attribute. */
    public static Attr width(double a) { return svgAttr("width", a); }
    /** The {@code width} attribute. */
    public static Attr width(String a) { return new Attr("width", a); }
    /** The {@code height} attribute — an SVG length, or an {@code img}/{@code video} size. */
    public static Attr height(int a) { return svgAttr("height", a); }
    /** The {@code height} attribute. */
    public static Attr height(double a) { return svgAttr("height", a); }
    /** The {@code height} attribute. */
    public static Attr height(String a) { return new Attr("height", a); }
    /** The {@code x} attribute. */
    public static Attr x(int a) { return svgAttr("x", a); }
    /** The {@code x} attribute. */
    public static Attr x(double a) { return svgAttr("x", a); }
    /** The {@code x} attribute. */
    public static Attr x(String a) { return new Attr("x", a); }
    /** The {@code y} attribute. */
    public static Attr y(int a) { return svgAttr("y", a); }
    /** The {@code y} attribute. */
    public static Attr y(double a) { return svgAttr("y", a); }
    /** The {@code y} attribute. */
    public static Attr y(String a) { return new Attr("y", a); }
    /** The {@code cx} attribute (a circle's or ellipse's centre x). */
    public static Attr cx(int a) { return svgAttr("cx", a); }
    /** The {@code cx} attribute. */
    public static Attr cx(double a) { return svgAttr("cx", a); }
    /** The {@code cx} attribute. */
    public static Attr cx(String a) { return new Attr("cx", a); }
    /** The {@code cy} attribute (a circle's or ellipse's centre y). */
    public static Attr cy(int a) { return svgAttr("cy", a); }
    /** The {@code cy} attribute. */
    public static Attr cy(double a) { return svgAttr("cy", a); }
    /** The {@code cy} attribute. */
    public static Attr cy(String a) { return new Attr("cy", a); }
    /** The {@code r} attribute (a circle's radius). */
    public static Attr r(int a) { return svgAttr("r", a); }
    /** The {@code r} attribute. */
    public static Attr r(double a) { return svgAttr("r", a); }
    /** The {@code r} attribute. */
    public static Attr r(String a) { return new Attr("r", a); }
    /** The {@code rx} attribute (an ellipse's x radius, or a rect's corner radius). */
    public static Attr rx(int a) { return svgAttr("rx", a); }
    /** The {@code rx} attribute. */
    public static Attr rx(double a) { return svgAttr("rx", a); }
    /** The {@code rx} attribute. */
    public static Attr rx(String a) { return new Attr("rx", a); }
    /** The {@code ry} attribute (an ellipse's y radius, or a rect's corner radius). */
    public static Attr ry(int a) { return svgAttr("ry", a); }
    /** The {@code ry} attribute. */
    public static Attr ry(double a) { return svgAttr("ry", a); }
    /** The {@code ry} attribute. */
    public static Attr ry(String a) { return new Attr("ry", a); }
    /** The {@code x1} attribute (a line's start x). */
    public static Attr x1(int a) { return svgAttr("x1", a); }
    /** The {@code x1} attribute. */
    public static Attr x1(double a) { return svgAttr("x1", a); }
    /** The {@code x1} attribute. */
    public static Attr x1(String a) { return new Attr("x1", a); }
    /** The {@code y1} attribute (a line's start y). */
    public static Attr y1(int a) { return svgAttr("y1", a); }
    /** The {@code y1} attribute. */
    public static Attr y1(double a) { return svgAttr("y1", a); }
    /** The {@code y1} attribute. */
    public static Attr y1(String a) { return new Attr("y1", a); }
    /** The {@code x2} attribute (a line's end x). */
    public static Attr x2(int a) { return svgAttr("x2", a); }
    /** The {@code x2} attribute. */
    public static Attr x2(double a) { return svgAttr("x2", a); }
    /** The {@code x2} attribute. */
    public static Attr x2(String a) { return new Attr("x2", a); }
    /** The {@code y2} attribute (a line's end y). */
    public static Attr y2(int a) { return svgAttr("y2", a); }
    /** The {@code y2} attribute. */
    public static Attr y2(double a) { return svgAttr("y2", a); }
    /** The {@code y2} attribute. */
    public static Attr y2(String a) { return new Attr("y2", a); }
    /** The {@code dx} attribute (a text run's x offset). */
    public static Attr dx(int a) { return svgAttr("dx", a); }
    /** The {@code dx} attribute. */
    public static Attr dx(double a) { return svgAttr("dx", a); }
    /** The {@code dx} attribute. */
    public static Attr dx(String a) { return new Attr("dx", a); }
    /** The {@code dy} attribute (a text run's y offset). */
    public static Attr dy(int a) { return svgAttr("dy", a); }
    /** The {@code dy} attribute. */
    public static Attr dy(double a) { return svgAttr("dy", a); }
    /** The {@code dy} attribute. */
    public static Attr dy(String a) { return new Attr("dy", a); }
    /** The {@code points} attribute of a polyline or polygon. */
    public static Attr points(String a) { return new Attr("points", a); }
    /** The {@code pathLength} attribute. */
    public static Attr pathLength(int a) { return svgAttr("pathLength", a); }
    /** The {@code pathLength} attribute. */
    public static Attr pathLength(double a) { return svgAttr("pathLength", a); }
    /** The {@code pathLength} attribute. */
    public static Attr pathLength(String a) { return new Attr("pathLength", a); }
    /** The SVG {@code transform} attribute, e.g. {@code "rotate(45 50 50)"}. */
    public static Attr transform(String a) { return new Attr("transform", a); }
    /** The {@code preserveAspectRatio} attribute, e.g. {@code "xMidYMid meet"}. */
    public static Attr preserveAspectRatio(String a) { return new Attr("preserveAspectRatio", a); }
    /** The {@code stroke-linecap} attribute: {@code butt}, {@code round} or {@code square}. */
    public static Attr strokeLinecap(String a) { return new Attr("stroke-linecap", a); }
    /** The {@code stroke-linejoin} attribute: {@code miter}, {@code round} or {@code bevel}. */
    public static Attr strokeLinejoin(String a) { return new Attr("stroke-linejoin", a); }
    /** The {@code stroke-dasharray} attribute, e.g. {@code "4 2"}. */
    public static Attr strokeDasharray(String a) { return new Attr("stroke-dasharray", a); }
    /** The {@code stroke-dashoffset} attribute. */
    public static Attr strokeDashoffset(double a) { return svgAttr("stroke-dashoffset", a); }
    /** The {@code stroke-dashoffset} attribute. */
    public static Attr strokeDashoffset(String a) { return new Attr("stroke-dashoffset", a); }
    /** The {@code opacity} attribute, {@code 0..1}. */
    public static Attr opacity(double a) { return svgAttr("opacity", a); }
    /** The {@code opacity} attribute. */
    public static Attr opacity(String a) { return new Attr("opacity", a); }
    /** The {@code fill-opacity} attribute, {@code 0..1}. */
    public static Attr fillOpacity(double a) { return svgAttr("fill-opacity", a); }
    /** The {@code fill-opacity} attribute. */
    public static Attr fillOpacity(String a) { return new Attr("fill-opacity", a); }
    /** The {@code stroke-opacity} attribute, {@code 0..1}. */
    public static Attr strokeOpacity(double a) { return svgAttr("stroke-opacity", a); }
    /** The {@code stroke-opacity} attribute. */
    public static Attr strokeOpacity(String a) { return new Attr("stroke-opacity", a); }
    /** The {@code fill-rule} attribute: {@code nonzero} or {@code evenodd}. */
    public static Attr fillRule(String a) { return new Attr("fill-rule", a); }
    /** The {@code clip-rule} attribute: {@code nonzero} or {@code evenodd}. */
    public static Attr clipRule(String a) { return new Attr("clip-rule", a); }
    /** The {@code text-anchor} attribute: {@code start}, {@code middle} or {@code end}. */
    public static Attr textAnchor(String a) { return new Attr("text-anchor", a); }
    /** The {@code dominant-baseline} attribute, e.g. {@code middle}. */
    public static Attr dominantBaseline(String a) { return new Attr("dominant-baseline", a); }
    /** The SVG {@code font-size} attribute. */
    public static Attr fontSize(int a) { return svgAttr("font-size", a); }
    /** The SVG {@code font-size} attribute. */
    public static Attr fontSize(double a) { return svgAttr("font-size", a); }
    /** The SVG {@code font-size} attribute. */
    public static Attr fontSize(String a) { return new Attr("font-size", a); }
    /** The SVG {@code font-family} attribute. */
    public static Attr fontFamily(String a) { return new Attr("font-family", a); }
    /** The SVG {@code font-weight} attribute. */
    public static Attr fontWeight(int a) { return svgAttr("font-weight", a); }
    /** The SVG {@code font-weight} attribute. */
    public static Attr fontWeight(String a) { return new Attr("font-weight", a); }
    /** A gradient stop's {@code offset} attribute, {@code 0..1}. */
    public static Attr offset(double a) { return svgAttr("offset", a); }
    /** A gradient stop's {@code offset} attribute, e.g. {@code "50%"}. */
    public static Attr offset(String a) { return new Attr("offset", a); }
    /** A gradient stop's {@code stop-color} attribute. */
    public static Attr stopColor(String a) { return new Attr("stop-color", a); }
    /** A gradient stop's {@code stop-opacity} attribute, {@code 0..1}. */
    public static Attr stopOpacity(double a) { return svgAttr("stop-opacity", a); }
    /** A gradient stop's {@code stop-opacity} attribute. */
    public static Attr stopOpacity(String a) { return new Attr("stop-opacity", a); }
    /** The {@code xlink:href} attribute (the pre-SVG 2 spelling of {@code href}). */
    public static Attr xlinkHref(String a) { return new Attr("xlink:href", a); }
    /** A blur filter's {@code stdDeviation} attribute. */
    public static Attr stdDeviation(double a) { return svgAttr("stdDeviation", a); }
    /** A blur filter's {@code stdDeviation} attribute. */
    public static Attr stdDeviation(String a) { return new Attr("stdDeviation", a); }

    private static Attr svgAttr(String name, Object value) {
        return new Attr(name, String.valueOf(value));
    }
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
