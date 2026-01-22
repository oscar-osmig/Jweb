package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attributes;

/**
 * Modern HTML5 interactive and semantic elements.
 *
 * Includes:
 * - Dialog and popover elements
 * - Details/summary for disclosure widgets
 * - Progress and meter for status indicators
 * - Template and slot for web components
 * - Data/time for semantic data
 * - Text direction and ruby annotation elements
 */
public final class ModernElements {
    private ModernElements() {}

    // ==================== Dialog Element ====================

    /**
     * Creates a dialog element (modal or non-modal).
     * Use with open attribute or showModal()/show() JS methods.
     *
     * <p>Example:</p>
     * <pre>
     * dialog(attrs().id("confirm-dialog"),
     *     h2("Confirm Action"),
     *     p("Are you sure?"),
     *     button(attrs().onclick("document.getElementById('confirm-dialog').close()"), "Cancel")
     * )
     * </pre>
     */
    public static Tag dialog(Object... children) {
        return Tag.create("dialog", children);
    }

    public static Tag dialog(Attributes attrs, Object... children) {
        return new Tag("dialog", attrs, Tag.toVNodes(children));
    }

    // ==================== Details/Summary ====================

    /**
     * Creates a details disclosure element (collapsible content).
     *
     * <p>Example:</p>
     * <pre>
     * details(
     *     summary("Click to expand"),
     *     p("Hidden content here")
     * )
     * </pre>
     */
    public static Tag details(Object... children) {
        return Tag.create("details", children);
    }

    public static Tag details(Attributes attrs, Object... children) {
        return new Tag("details", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a summary element (heading for details element).
     * Must be the first child of a details element.
     */
    public static Tag summary(Object... children) {
        return Tag.create("summary", children);
    }

    public static Tag summary(Attributes attrs, Object... children) {
        return new Tag("summary", attrs, Tag.toVNodes(children));
    }

    // ==================== Meter Element ====================

    /**
     * Creates a meter element (scalar measurement within a known range).
     *
     * <p>Attributes:</p>
     * <ul>
     *   <li>value - current value (required)</li>
     *   <li>min - lower bound (default: 0)</li>
     *   <li>max - upper bound (default: 1)</li>
     *   <li>low - low threshold</li>
     *   <li>high - high threshold</li>
     *   <li>optimum - optimal value</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>
     * meter(attrs()
     *     .set("value", "0.6")
     *     .set("min", "0")
     *     .set("max", "1")
     *     .set("low", "0.3")
     *     .set("high", "0.7")
     *     .set("optimum", "0.5"))
     * </pre>
     */
    public static Tag meter(Object... attrs) {
        return Tag.create("meter", attrs);
    }

    public static Tag meter(Attributes attrs) {
        return new Tag("meter", attrs);
    }

    /**
     * Creates a meter element with value, min, and max.
     *
     * @param value current value
     * @param min minimum value
     * @param max maximum value
     */
    public static Tag meter(double value, double min, double max) {
        return new Tag("meter", new Attributes()
            .set("value", String.valueOf(value))
            .set("min", String.valueOf(min))
            .set("max", String.valueOf(max)));
    }

    // ==================== Progress Element ====================

    /**
     * Creates a progress element (completion progress indicator).
     *
     * <p>Example:</p>
     * <pre>
     * // Determinate progress
     * progress(attrs().set("value", "70").set("max", "100"))
     *
     * // Indeterminate progress (no value attribute)
     * progress(attrs().set("max", "100"))
     * </pre>
     */
    public static Tag progress(Object... attrs) {
        return Tag.create("progress", attrs);
    }

    public static Tag progress(Attributes attrs) {
        return new Tag("progress", attrs);
    }

    /**
     * Creates a progress element with value and max.
     *
     * @param value current progress value
     * @param max maximum value (default: 1)
     */
    public static Tag progress(double value, double max) {
        return new Tag("progress", new Attributes()
            .set("value", String.valueOf(value))
            .set("max", String.valueOf(max)));
    }

    /**
     * Creates an indeterminate progress element (no value).
     */
    public static Tag progressIndeterminate() {
        return new Tag("progress", new Attributes());
    }

    // ==================== Template Element ====================

    /**
     * Creates a template element (inert HTML fragment).
     * Content is not rendered but can be cloned via JavaScript.
     *
     * <p>Example:</p>
     * <pre>
     * template(attrs().id("card-template"),
     *     div(class_("card"),
     *         h3(class_("card-title")),
     *         p(class_("card-content"))
     *     )
     * )
     * </pre>
     */
    public static Tag template(Object... children) {
        return Tag.create("template", children);
    }

    public static Tag template(Attributes attrs, Object... children) {
        return new Tag("template", attrs, Tag.toVNodes(children));
    }

    // ==================== Slot Element ====================

    /**
     * Creates a slot element (web component content placeholder).
     *
     * <p>Example:</p>
     * <pre>
     * // Named slot
     * slot(attrs().name("header"))
     *
     * // Default slot
     * slot()
     * </pre>
     */
    public static Tag slot(Object... children) {
        return Tag.create("slot", children);
    }

    public static Tag slot(Attributes attrs, Object... children) {
        return new Tag("slot", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a named slot element.
     *
     * @param name the slot name
     */
    public static Tag slot(String name) {
        return new Tag("slot", new Attributes().name(name));
    }

    // ==================== Output Element ====================

    /**
     * Creates an output element (result of a calculation or user action).
     *
     * <p>Attributes:</p>
     * <ul>
     *   <li>for - space-separated IDs of related elements</li>
     *   <li>form - associated form ID</li>
     *   <li>name - element name</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>
     * output(attrs()
     *     .set("for", "input1 input2")
     *     .name("result"))
     * </pre>
     */
    public static Tag output(Attributes attrs, Object... children) {
        return new Tag("output", attrs, Tag.toVNodes(children));
    }

    public static Tag output(Object... children) {
        return Tag.create("output", children);
    }

    // ==================== Data/Time Elements ====================

    /**
     * Creates a time element with datetime attribute.
     *
     * <p>Example:</p>
     * <pre>
     * timeWithDatetime("2026-01-21", "January 21, 2026")
     * // Output: &lt;time datetime="2026-01-21"&gt;January 21, 2026&lt;/time&gt;
     * </pre>
     *
     * @param datetime machine-readable datetime
     * @param displayText human-readable text
     */
    public static Tag timeWithDatetime(String datetime, String displayText) {
        return new Tag("time", new Attributes().set("datetime", datetime), Tag.toVNodes(new Object[]{displayText}));
    }

    /**
     * Creates a data element (machine-readable value).
     *
     * <p>Example:</p>
     * <pre>
     * data("123456", "Product ABC")
     * // Output: &lt;data value="123456"&gt;Product ABC&lt;/data&gt;
     * </pre>
     *
     * @param value machine-readable value
     * @param displayText human-readable text
     */
    public static Tag data(String value, String displayText) {
        return new Tag("data", new Attributes().value(value), Tag.toVNodes(new Object[]{displayText}));
    }

    /**
     * Creates a data element with attributes.
     */
    public static Tag data(Attributes attrs, Object... children) {
        return new Tag("data", attrs, Tag.toVNodes(children));
    }

    // ==================== Text Direction ====================

    /**
     * Creates a bdi element (bi-directional isolation).
     * Isolates text that might be in a different direction from surrounding text.
     *
     * <p>Example:</p>
     * <pre>
     * p("User ", bdi(username), " posted this")
     * </pre>
     */
    public static Tag bdi(Object... children) {
        return Tag.create("bdi", children);
    }

    public static Tag bdi(Attributes attrs, Object... children) {
        return new Tag("bdi", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates a bdo element (bi-directional override).
     * Overrides the text direction.
     *
     * <p>Example:</p>
     * <pre>
     * bdo(attrs().set("dir", "rtl"), "Right to left text")
     * </pre>
     */
    public static Tag bdo(Object... children) {
        return Tag.create("bdo", children);
    }

    public static Tag bdo(Attributes attrs, Object... children) {
        return new Tag("bdo", attrs, Tag.toVNodes(children));
    }

    // ==================== Ruby Annotation ====================

    /**
     * Creates a ruby element (ruby annotation for East Asian typography).
     *
     * <p>Example:</p>
     * <pre>
     * ruby(
     *     text("漢"),
     *     rp("("),
     *     rt("kan"),
     *     rp(")")
     * )
     * </pre>
     */
    public static Tag ruby(Object... children) {
        return Tag.create("ruby", children);
    }

    public static Tag ruby(Attributes attrs, Object... children) {
        return new Tag("ruby", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates an rt element (ruby text - pronunciation or translation).
     */
    public static Tag rt(Object... children) {
        return Tag.create("rt", children);
    }

    public static Tag rt(Attributes attrs, Object... children) {
        return new Tag("rt", attrs, Tag.toVNodes(children));
    }

    /**
     * Creates an rp element (ruby parentheses - fallback for browsers without ruby support).
     */
    public static Tag rp(Object... children) {
        return Tag.create("rp", children);
    }

    public static Tag rp(Attributes attrs, Object... children) {
        return new Tag("rp", attrs, Tag.toVNodes(children));
    }
}
