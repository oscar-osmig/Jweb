package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Modern form enhancement elements including datalist, optgroup,
 * fieldset/legend, and specialized input helpers.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * import static com.osmig.Jweb.framework.elements.FormEnhancements.*;
 * import static com.osmig.Jweb.framework.elements.El.*;
 *
 * // Datalist for autocomplete suggestions
 * input(attrs().attr("list", "browsers")),
 * datalist("browsers",
 *     option("Chrome"),
 *     option("Firefox"),
 *     option("Safari"),
 *     option("Edge")
 * )
 *
 * // Option groups in select
 * select(attrs().name("car"),
 *     optgroup("Swedish Cars",
 *         option(attrs().value("volvo"), "Volvo"),
 *         option(attrs().value("saab"), "Saab")
 *     ),
 *     optgroup("German Cars",
 *         option(attrs().value("bmw"), "BMW"),
 *         option(attrs().value("audi"), "Audi")
 *     )
 * )
 *
 * // Fieldset with legend
 * fieldset(
 *     legend("Personal Information"),
 *     label(attrs().for_("name"), "Name:"),
 *     input(attrs().type("text").name("name").id("name")),
 *     label(attrs().for_("email"), "Email:"),
 *     input(attrs().type("email").name("email").id("email"))
 * )
 *
 * // Color picker
 * colorInput("theme-color", "#3b82f6")
 *
 * // Range slider
 * rangeInput("volume", 0, 100, 50)
 * }</pre>
 *
 * @deprecated Replaced by {@code jweb.el.FormEnhancements} — shorter import, same behavior. Existing code keeps working.
 */
@Deprecated
public class FormEnhancements {
    protected FormEnhancements() {}

    // ==================== Datalist ====================

    /**
     * Creates a datalist element for autocomplete suggestions.
     *
     * @param id the datalist ID (link to input via list attribute)
     * @param children option elements
     * @return a Tag
     */
    public static Tag datalist(String id, Object... children) {
        return Tag.create("datalist", new Attr("id", id), children);
    }

    /**
     * Creates a datalist element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag datalist(Attributes attrs, Object... children) {
        return new Tag("datalist", attrs, Tag.toVNodes(children));
    }

    // ==================== Optgroup ====================

    /**
     * Creates an optgroup element for grouping options in a select.
     *
     * @param label the group label
     * @param children option elements
     * @return a Tag
     */
    public static Tag optgroup(String label, Object... children) {
        return Tag.create("optgroup", new Attr("label", label), children);
    }

    /**
     * Creates a disabled optgroup.
     *
     * @param label the group label
     * @param children option elements
     * @return a Tag
     * @deprecated Use {@code optgroup(attr("label", label), disabled(), ...)} instead.
     */
    @Deprecated
    public static Tag optgroupDisabled(String label, Object... children) {
        return Tag.create("optgroup",
            new Attr("label", label),
            new Attr("disabled", null),
            children);
    }

    // ==================== Fieldset & Legend ====================

    /**
     * Creates a fieldset element for grouping form controls.
     *
     * @param children form elements (include a legend as first child)
     * @return a Tag
     */
    public static Tag fieldset(Object... children) {
        return Tag.create("fieldset", children);
    }

    /**
     * Creates a fieldset element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag fieldset(Attributes attrs, Object... children) {
        return new Tag("fieldset", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a disabled fieldset (disables all contained controls).
     *
     * @param children form elements
     * @return a Tag
     * @deprecated Use {@code fieldset(disabled(), ...)} instead.
     */
    @Deprecated
    public static Tag fieldsetDisabled(Object... children) {
        return Tag.create("fieldset", new Attr("disabled", null), children);
    }

    /**
     * Creates a legend element (caption for a fieldset).
     *
     * @param children the legend content
     * @return a Tag
     */
    public static Tag legend(Object... children) {
        return Tag.create("legend", children);
    }

    /**
     * Creates a legend element with attributes.
     *
     * @param attrs the attributes
     * @param children the children
     * @return a Tag
     */
    public static Tag legend(Attributes attrs, Object... children) {
        return new Tag("legend", attrs, Tag.toVNodes(children));
    }

    // ==================== Specialized Input Helpers ====================
    // One canonical implementation lives in Elements; these delegate so both
    // entry points always agree. ID POLICY: id defaults to name, so
    // label(name, ...) pairs with the control. (These helpers used to set no
    // id at all, which silently broke label-for pairing.)

    /**
     * Creates a color input. The id defaults to the name.
     *
     * @param name the input name
     * @param defaultColor the default color value (e.g., "#ff0000")
     * @return a Tag
     */
    public static Tag colorInput(String name, String defaultColor) {
        return Elements.colorInput(name, defaultColor);
    }

    /**
     * Creates a date input. The id defaults to the name.
     *
     * @param name the input name
     * @return a Tag
     */
    public static Tag dateInput(String name) {
        return Elements.dateInput(name);
    }

    /**
     * Creates a date input with min/max constraints. The id defaults to the name.
     *
     * @param name the input name
     * @param min the minimum date (YYYY-MM-DD)
     * @param max the maximum date (YYYY-MM-DD)
     * @return a Tag
     */
    public static Tag dateInput(String name, String min, String max) {
        return Elements.dateInput(name, min, max);
    }

    /**
     * Creates a time input. The id defaults to the name.
     *
     * @param name the input name
     * @return a Tag
     */
    public static Tag timeInput(String name) {
        return Elements.timeInput(name);
    }

    /**
     * Creates a datetime-local input. The id defaults to the name.
     *
     * @param name the input name
     * @return a Tag
     */
    public static Tag datetimeInput(String name) {
        return Elements.datetimeInput(name);
    }

    /**
     * Creates a month input. The id defaults to the name.
     *
     * @param name the input name
     * @return a Tag
     */
    public static Tag monthInput(String name) {
        return Elements.monthInput(name);
    }

    /**
     * Creates a week input. The id defaults to the name.
     *
     * @param name the input name
     * @return a Tag
     */
    public static Tag weekInput(String name) {
        return Elements.weekInput(name);
    }

    /**
     * Creates a range input (slider). The id defaults to the name.
     *
     * @param name the input name
     * @param min the minimum value
     * @param max the maximum value
     * @param value the default value
     * @return a Tag
     */
    public static Tag rangeInput(String name, int min, int max, int value) {
        return Elements.rangeInput(name, min, max, value);
    }

    /**
     * Creates a range input with step. The id defaults to the name.
     *
     * @param name the input name
     * @param min the minimum value
     * @param max the maximum value
     * @param value the default value
     * @param step the step increment
     * @return a Tag
     */
    public static Tag rangeInput(String name, int min, int max, int value, int step) {
        return Elements.rangeInput(name, min, max, value, step);
    }

    // ==================== Form Action Attributes ====================

    /**
     * Creates a formaction attribute (overrides form action on a submit button).
     *
     * @param url the action URL
     * @return the Attr
     */
    public static Attr formaction(String url) {
        return new Attr("formaction", url);
    }

    /**
     * Creates a formmethod attribute (overrides form method on a submit button).
     *
     * @param method "get", "post", or "dialog"
     * @return the Attr
     */
    public static Attr formmethod(String method) {
        return new Attr("formmethod", method);
    }

    /**
     * Creates a formenctype attribute.
     *
     * @param enctype the encoding type
     * @return the Attr
     */
    public static Attr formenctype(String enctype) {
        return new Attr("formenctype", enctype);
    }

    /**
     * Creates a formtarget attribute.
     *
     * @param target the target (e.g., "_blank", "_self")
     * @return the Attr
     */
    public static Attr formtarget(String target) {
        return new Attr("formtarget", target);
    }

    /**
     * Creates a formnovalidate attribute (disables validation for this button).
     *
     * @return the Attr
     */
    public static Attr formnovalidate() {
        return new Attr("formnovalidate", null);
    }
}
