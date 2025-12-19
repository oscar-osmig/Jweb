package com.osmig.Jweb.framework.attributes;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent builder for HTML element attributes.
 * Provides type-safe methods for all common HTML attributes and event handlers.
 *
 * <p>Usage with Elements:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.elements.Elements.*;
 *
 * // Basic attributes
 * div(attrs().id("main").class_("container"))
 *
 * // Multiple classes
 * div(attrs().class_("card").addClass("featured"))
 *
 * // With inline styles using the fluent style builder
 * div(attrs()
 *     .class_("box")
 *     .style()
 *         .display(flex)
 *         .padding(px(10))
 *         .backgroundColor(hex("#f5f5f5"))
 *     .done())
 *
 * // Form attributes
 * form(attrs()
 *     .action("/submit")
 *     .method("POST")
 *     .onSubmit(e -&gt; handleSubmit(e)))
 *
 * // Input with validation
 * input(attrs()
 *     .type("email")
 *     .name("email")
 *     .placeholder("you@example.com")
 *     .required())
 *
 * // Event handlers
 * button(attrs()
 *     .class_("btn")
 *     .onClick(e -&gt; count.set(count.get() + 1)),
 *     text("Click me"))
 * </pre>
 *
 * @see com.osmig.Jweb.framework.elements.Elements#attrs() for creating Attributes instances
 */
public class Attributes {

    /** Stores attribute name-value pairs in insertion order. */
    private final Map<String, String> attributes = new LinkedHashMap<>();

    /** Creates a new empty Attributes builder. */
    public Attributes() {}

    /**
     * Creates an Attributes builder with initial values.
     *
     * @param initial initial attribute map (can be null)
     */
    public Attributes(Map<String, String> initial) {
        if (initial != null) {
            this.attributes.putAll(initial);
        }
    }

    // ==================== Core Attributes ====================

    /** Sets the id attribute. @param value the element ID */
    public Attributes id(String value) { return set("id", value); }

    /** Sets the class attribute. @param value the CSS class(es) */
    public Attributes class_(String value) { return set("class", value); }

    /**
     * Adds a CSS class to existing classes.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").addClass("primary")
     * // Output: class="btn primary"
     * </pre>
     *
     * @param className the class to add
     * @return this for chaining
     */
    public Attributes addClass(String className) {
        String existing = attributes.get("class");
        if (existing == null || existing.isBlank()) {
            return set("class", className);
        }
        return set("class", existing + " " + className);
    }

    /** Sets inline style from a string. @param value the CSS style string */
    public Attributes style(String value) { return set("style", value); }

    /** Sets inline style from a Style builder. @param style the Style object */
    public Attributes style(Style style) { return set("style", style.build()); }

    /** Sets inline style from a CSSValue. @param style the CSSValue */
    public Attributes style(CSSValue style) { return set("style", style.css()); }

    /**
     * Starts a fluent inline style builder that chains back to this Attributes.
     * Call {@link InlineStyle#done()} to finish styling and return to Attributes.
     *
     * <p>Example:</p>
     * <pre>
     * attrs()
     *     .class_("card")
     *     .style()
     *         .display(flex)
     *         .padding(px(10))
     *         .backgroundColor(white)
     *     .done()
     *     .id("main")
     * </pre>
     *
     * @return an InlineStyle builder
     */
    public InlineStyle style() { return new InlineStyle(this); }

    /**
     * Fluent inline style builder that integrates with Attributes.
     * Provides a subset of common CSS properties for inline styling.
     * Call {@link #done()} to return to the parent Attributes builder.
     */
    public static class InlineStyle {
        private final Attributes parent;
        private final Style<?> style = new Style<>();

        InlineStyle(Attributes parent) {
            this.parent = parent;
        }

        /** Finish styling and return to Attributes builder */
        public Attributes done() {
            parent.set("style", style.build());
            return parent;
        }

        // ==================== Layout ====================
        public InlineStyle display(CSSValue value) { style.display(value); return this; }
        public InlineStyle position(CSSValue value) { style.position(value); return this; }
        public InlineStyle top(CSSValue value) { style.top(value); return this; }
        public InlineStyle right(CSSValue value) { style.right(value); return this; }
        public InlineStyle bottom(CSSValue value) { style.bottom(value); return this; }
        public InlineStyle left(CSSValue value) { style.left(value); return this; }
        public InlineStyle zIndex(int value) { style.zIndex(value); return this; }

        // ==================== Flexbox ====================
        public InlineStyle flexDirection(CSSValue value) { style.flexDirection(value); return this; }
        public InlineStyle flexWrap(CSSValue value) { style.flexWrap(value); return this; }
        public InlineStyle justifyContent(CSSValue value) { style.justifyContent(value); return this; }
        public InlineStyle alignItems(CSSValue value) { style.alignItems(value); return this; }
        public InlineStyle alignContent(CSSValue value) { style.alignContent(value); return this; }
        public InlineStyle gap(CSSValue value) { style.gap(value); return this; }
        public InlineStyle flex(int grow, int shrink, CSSValue basis) { style.flex(grow, shrink, basis); return this; }
        public InlineStyle flexGrow(int value) { style.flexGrow(value); return this; }
        public InlineStyle flexShrink(int value) { style.flexShrink(value); return this; }

        // ==================== Grid ====================
        public InlineStyle gridTemplateColumns(String value) { style.gridTemplateColumns(value); return this; }
        public InlineStyle gridTemplateRows(String value) { style.gridTemplateRows(value); return this; }
        public InlineStyle gridColumn(String value) { style.gridColumn(value); return this; }
        public InlineStyle gridRow(String value) { style.gridRow(value); return this; }

        // ==================== Sizing ====================
        public InlineStyle width(CSSValue value) { style.width(value); return this; }
        public InlineStyle height(CSSValue value) { style.height(value); return this; }
        public InlineStyle minWidth(CSSValue value) { style.minWidth(value); return this; }
        public InlineStyle maxWidth(CSSValue value) { style.maxWidth(value); return this; }
        public InlineStyle minHeight(CSSValue value) { style.minHeight(value); return this; }
        public InlineStyle maxHeight(CSSValue value) { style.maxHeight(value); return this; }

        // ==================== Spacing ====================
        public InlineStyle margin(CSSValue value) { style.margin(value); return this; }
        public InlineStyle margin(CSSValue vertical, CSSValue horizontal) { style.margin(vertical, horizontal); return this; }
        public InlineStyle marginTop(CSSValue value) { style.marginTop(value); return this; }
        public InlineStyle marginRight(CSSValue value) { style.marginRight(value); return this; }
        public InlineStyle marginBottom(CSSValue value) { style.marginBottom(value); return this; }
        public InlineStyle marginLeft(CSSValue value) { style.marginLeft(value); return this; }
        public InlineStyle padding(CSSValue value) { style.padding(value); return this; }
        public InlineStyle padding(CSSValue vertical, CSSValue horizontal) { style.padding(vertical, horizontal); return this; }
        public InlineStyle paddingTop(CSSValue value) { style.paddingTop(value); return this; }
        public InlineStyle paddingRight(CSSValue value) { style.paddingRight(value); return this; }
        public InlineStyle paddingBottom(CSSValue value) { style.paddingBottom(value); return this; }
        public InlineStyle paddingLeft(CSSValue value) { style.paddingLeft(value); return this; }

        // ==================== Typography ====================
        public InlineStyle color(CSSValue value) { style.color(value); return this; }
        public InlineStyle fontSize(CSSValue value) { style.fontSize(value); return this; }
        public InlineStyle fontWeight(int value) { style.fontWeight(value); return this; }
        public InlineStyle fontFamily(String value) { style.fontFamily(value); return this; }
        public InlineStyle lineHeight(double value) { style.lineHeight(value); return this; }
        public InlineStyle textAlign(CSSValue value) { style.textAlign(value); return this; }
        public InlineStyle textDecoration(CSSValue value) { style.textDecoration(value); return this; }
        public InlineStyle textTransform(CSSValue value) { style.textTransform(value); return this; }
        public InlineStyle letterSpacing(CSSValue value) { style.letterSpacing(value); return this; }

        // ==================== Background ====================
        public InlineStyle backgroundColor(CSSValue value) { style.backgroundColor(value); return this; }
        public InlineStyle background(CSSValue value) { style.background(value); return this; }
        public InlineStyle backgroundImage(CSSValue value) { style.backgroundImage(value); return this; }
        public InlineStyle backgroundSize(CSSValue value) { style.backgroundSize(value); return this; }
        public InlineStyle backgroundPosition(CSSValue value) { style.backgroundPosition(value); return this; }

        // ==================== Border ====================
        public InlineStyle border(CSSValue width, CSSValue borderStyle, CSSValue color) { style.border(width, borderStyle, color); return this; }
        public InlineStyle borderTop(CSSValue width, CSSValue borderStyle, CSSValue color) { style.borderTop(width, borderStyle, color); return this; }
        public InlineStyle borderBottom(CSSValue width, CSSValue borderStyle, CSSValue color) { style.borderBottom(width, borderStyle, color); return this; }
        public InlineStyle borderRadius(CSSValue value) { style.borderRadius(value); return this; }
        public InlineStyle borderColor(CSSValue value) { style.borderColor(value); return this; }

        // ==================== Effects ====================
        public InlineStyle boxShadow(CSSValue x, CSSValue y, CSSValue blur, CSSValue color) { style.boxShadow(x, y, blur, color); return this; }
        public InlineStyle opacity(double value) { style.opacity(value); return this; }
        public InlineStyle overflow(CSSValue value) { style.overflow(value); return this; }
        public InlineStyle cursor(CSSValue value) { style.cursor(value); return this; }
        public InlineStyle transition(CSSValue property, CSSValue duration, CSSValue timing) { style.transition(property, duration, timing); return this; }
        public InlineStyle transform(CSSValue value) { style.transform(value); return this; }

        // ==================== Generic ====================

        /** Sets any CSS property by name. */
        public InlineStyle prop(String property, String value) { style.prop(property, value); return this; }
        /** Sets any CSS property by name with a CSSValue. */
        public InlineStyle prop(String property, CSSValue value) { style.prop(property, value); return this; }
    }

    // ==================== Common Attributes ====================

    /** Sets the title attribute (tooltip). @param value the title text */
    public Attributes title(String value) { return set("title", value); }
    /** Sets the href attribute for links. @param value the URL */
    public Attributes href(String value) { return set("href", value); }
    /** Sets the target attribute for links. @param value the target window/frame */
    public Attributes target(String value) { return set("target", value); }

    /**
     * Sets target="_blank" with proper security attributes.
     * Automatically adds rel="noopener noreferrer" to prevent tabnabbing.
     *
     * @return this for chaining
     */
    public Attributes targetBlank() {
        return set("target", "_blank").set("rel", "noopener noreferrer");
    }

    /** Sets the src attribute for images/scripts. @param value the source URL */
    public Attributes src(String value) { return set("src", value); }
    /** Sets the alt attribute for images. @param value the alt text */
    public Attributes alt(String value) { return set("alt", value); }
    /** Sets the width attribute. @param value the width (numeric or with unit) */
    public Attributes width(String value) { return set("width", value); }
    /** Sets the height attribute. @param value the height */
    public Attributes height(String value) { return set("height", value); }

    // ==================== Form Attributes ====================

    /** Sets the type attribute for inputs. @param value the input type */
    public Attributes type(String value) { return set("type", value); }
    /** Sets the name attribute for form elements. @param value the name */
    public Attributes name(String value) { return set("name", value); }
    /** Sets the value attribute. @param value the value */
    public Attributes value(String value) { return set("value", value); }
    /** Sets the placeholder attribute for inputs. @param value the placeholder text */
    public Attributes placeholder(String value) { return set("placeholder", value); }
    /** Sets the action attribute for forms. @param value the form action URL */
    public Attributes action(String value) { return set("action", value); }
    /** Sets the method attribute for forms. @param value the HTTP method */
    public Attributes method(String value) { return set("method", value); }
    /** Sets the for attribute for labels. @param value the target element ID */
    public Attributes for_(String value) { return set("for", value); }

    // ==================== Boolean Attributes ====================

    /** Adds the disabled boolean attribute. */
    public Attributes disabled() { return set("disabled", null); }
    /** Conditionally adds the disabled attribute. @param isDisabled whether to disable */
    public Attributes disabled(boolean isDisabled) { return isDisabled ? disabled() : this; }
    /** Adds the checked boolean attribute for checkboxes/radios. */
    public Attributes checked() { return set("checked", null); }
    /** Conditionally adds the checked attribute. @param isChecked whether to check */
    public Attributes checked(boolean isChecked) { return isChecked ? checked() : this; }
    /** Adds the required boolean attribute. */
    public Attributes required() { return set("required", null); }
    /** Adds the readonly boolean attribute. */
    public Attributes readonly() { return set("readonly", null); }
    /** Adds the hidden boolean attribute. */
    public Attributes hidden() { return set("hidden", null); }
    /** Conditionally adds the hidden attribute. @param isHidden whether to hide */
    public Attributes hidden(boolean isHidden) { return isHidden ? hidden() : this; }
    /** Adds the autofocus boolean attribute. */
    public Attributes autofocus() { return set("autofocus", null); }

    // ==================== Data & ARIA Attributes ====================

    /** Sets a data-* attribute. @param name the data name (without "data-") @param value the value */
    public Attributes data(String name, String value) { return set("data-" + name, value); }
    /** Sets an aria-* attribute. @param name the aria name (without "aria-") @param value the value */
    public Attributes aria(String name, String value) { return set("aria-" + name, value); }
    /** Sets the role attribute for ARIA. @param value the ARIA role */
    public Attributes role(String value) { return set("role", value); }

    // ==================== Table Attributes ====================

    /** Sets the colspan attribute for table cells. @param value the number of columns to span */
    public Attributes colspan(int value) { return set("colspan", String.valueOf(value)); }
    /** Sets the rowspan attribute for table cells. @param value the number of rows to span */
    public Attributes rowspan(int value) { return set("rowspan", String.valueOf(value)); }

    // ==================== Event Handlers ====================

    /**
     * Registers a click event handler.
     *
     * <p>Example:</p>
     * <pre>
     * button(attrs().onClick(e -> count.set(count.get() + 1)), text("Click"))
     * </pre>
     *
     * @param handler the handler to execute on click
     * @return this for chaining
     */
    public Attributes onClick(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("click", handler);
        return set("onclick", eh.toJsAttribute());
    }

    /**
     * Registers a change event handler (for inputs, selects, textareas).
     *
     * <p>Example:</p>
     * <pre>
     * input(attrs().onChange(e -> name.set(e.value())))
     * </pre>
     *
     * @param handler the handler to execute on change
     * @return this for chaining
     */
    public Attributes onChange(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("change", handler);
        return set("onchange", eh.toJsAttribute());
    }

    /**
     * Registers an input event handler (fires on every keystroke).
     *
     * <p>Example:</p>
     * <pre>
     * input(attrs().onInput(e -> searchTerm.set(e.value())))
     * </pre>
     *
     * @param handler the handler to execute on input
     * @return this for chaining
     */
    public Attributes onInput(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("input", handler);
        return set("oninput", eh.toJsAttribute());
    }

    /**
     * Registers a submit event handler for forms.
     *
     * <p>Example:</p>
     * <pre>
     * form(attrs().onSubmit(e -> {
     *     e.preventDefault();
     *     submitForm(e.formData());
     * }), ...)
     * </pre>
     *
     * @param handler the handler to execute on submit
     * @return this for chaining
     */
    public Attributes onSubmit(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("submit", handler);
        return set("onsubmit", eh.toJsAttribute());
    }

    /**
     * Registers a focus event handler.
     *
     * @param handler the handler to execute on focus
     * @return this for chaining
     */
    public Attributes onFocus(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("focus", handler);
        return set("onfocus", eh.toJsAttribute());
    }

    /**
     * Registers a blur event handler (when element loses focus).
     *
     * @param handler the handler to execute on blur
     * @return this for chaining
     */
    public Attributes onBlur(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("blur", handler);
        return set("onblur", eh.toJsAttribute());
    }

    /**
     * Registers a keydown event handler.
     *
     * <p>Example:</p>
     * <pre>
     * input(attrs().onKeyDown(e -> {
     *     if ("Enter".equals(e.key())) submitSearch();
     * }))
     * </pre>
     *
     * @param handler the handler to execute on keydown
     * @return this for chaining
     */
    public Attributes onKeyDown(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("keydown", handler);
        return set("onkeydown", eh.toJsAttribute());
    }

    /**
     * Registers a keyup event handler.
     *
     * @param handler the handler to execute on keyup
     * @return this for chaining
     */
    public Attributes onKeyUp(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("keyup", handler);
        return set("onkeyup", eh.toJsAttribute());
    }

    /**
     * Registers a mouseenter event handler.
     *
     * @param handler the handler to execute on mouseenter
     * @return this for chaining
     */
    public Attributes onMouseEnter(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseenter", handler);
        return set("onmouseenter", eh.toJsAttribute());
    }

    /**
     * Registers a mouseleave event handler.
     *
     * @param handler the handler to execute on mouseleave
     * @return this for chaining
     */
    public Attributes onMouseLeave(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseleave", handler);
        return set("onmouseleave", eh.toJsAttribute());
    }

    /**
     * Registers a double-click event handler.
     *
     * @param handler the handler to execute on double-click
     * @return this for chaining
     */
    public Attributes onDoubleClick(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dblclick", handler);
        return set("ondblclick", eh.toJsAttribute());
    }

    /**
     * Registers a generic event handler for any DOM event type.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().on("scroll", e -> handleScroll(e)))
     * </pre>
     *
     * @param eventType the DOM event type (click, change, scroll, etc.)
     * @param handler the handler to execute
     * @return this for chaining
     */
    public Attributes on(String eventType, Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register(eventType, handler);
        return set("on" + eventType, eh.toJsAttribute());
    }

    // ==================== Generic Setters ====================

    /**
     * Sets any HTML attribute by name.
     * Use this for attributes that don't have a dedicated method.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().set("autocomplete", "off")
     * attrs().set("data-custom", "value")
     * </pre>
     *
     * @param name the attribute name
     * @param value the attribute value (null for boolean attributes)
     * @return this for chaining
     */
    public Attributes set(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Sets multiple attributes from a map.
     *
     * @param attrs map of attribute name-value pairs
     * @return this for chaining
     */
    public Attributes setAll(Map<String, String> attrs) {
        this.attributes.putAll(attrs);
        return this;
    }

    // ==================== Build ====================

    /**
     * Returns an immutable copy of the attributes map.
     *
     * @return immutable map of attribute name-value pairs
     */
    public Map<String, String> build() { return Map.copyOf(attributes); }

    /**
     * Returns the mutable attributes map (for internal use).
     *
     * @return the internal attributes map
     */
    public Map<String, String> toMap() { return attributes; }

    /**
     * Checks if any attributes have been set.
     *
     * @return true if no attributes have been set
     */
    public boolean isEmpty() { return attributes.isEmpty(); }

    /**
     * Gets an attribute value by name.
     *
     * @param name the attribute name
     * @return the attribute value, or null if not set
     */
    public String get(String name) { return attributes.get(name); }
}
