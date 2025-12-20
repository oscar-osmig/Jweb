package com.osmig.Jweb.framework.attributes;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

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
    public Attributes style(Style<?> style) { return set("style", style.build()); }

    /** Sets inline style from a CSSValue. @param style the CSSValue */
    public Attributes style(CSSValue style) { return set("style", style.css()); }

    /**
     * Sets inline style using a lambda builder - NO .done() needed!
     *
     * <p>Example:</p>
     * <pre>
     * attrs()
     *     .class_("card")
     *     .style(s -> s.display(flex).padding(px(10)).backgroundColor(white))
     *     .id("main")
     * </pre>
     *
     * @param builder a lambda that configures the style
     * @return this for chaining
     */
    public Attributes style(UnaryOperator<InlineStyle> builder) {
        InlineStyle s = new InlineStyle(this);
        builder.apply(s);
        return set("style", s.build());
    }

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
     * <p>TIP: Use the lambda version to avoid .done():</p>
     * <pre>
     * attrs().style(s -> s.display(flex).padding(px(10)))
     * </pre>
     *
     * @return an InlineStyle builder
     */
    public InlineStyle style() { return new InlineStyle(this); }

    /**
     * Fluent inline style builder that integrates with Attributes.
     * Extends Style to inherit ALL CSS properties automatically.
     *
     * <p>Three ways to use InlineStyle:</p>
     * <ol>
     *   <li>Pass directly to element - auto-finalizes when used</li>
     *   <li>Chain attributes directly after style (href, id, class_, onClick, etc.)</li>
     *   <li>Call {@link #done()} to explicitly return to Attributes</li>
     * </ol>
     *
     * <p>Example - Direct use (preferred, no .done() needed):</p>
     * <pre>
     * div(attrs().style()
     *         .display(flex)
     *         .padding(px(10)),
     *     p("Hello"))
     * </pre>
     *
     * <p>Example - Chaining attributes after style (NEW!):</p>
     * <pre>
     * a(attrs().style()
     *         .color(blue)
     *         .textDecoration(none)
     *     .href("/home")
     *     .class_("nav-link"),
     *     text("Home"))
     * </pre>
     *
     * <p>Example - With .done() (alternative explicit style):</p>
     * <pre>
     * div(attrs().style()
     *         .display(flex)
     *     .done()
     *     .id("main"),
     *     p("Hello"))
     * </pre>
     *
     * <p>This class has access to every CSS property from Style, including:</p>
     * <ul>
     *   <li>Box Model: margin, padding, border, width, height</li>
     *   <li>Flexbox: display(flex), flexDirection, justifyContent, alignItems, gap</li>
     *   <li>Grid: gridTemplateColumns, gridTemplateRows, gridArea</li>
     *   <li>Positioning: position, top/right/bottom/left, inset, zIndex</li>
     *   <li>Typography: color, fontSize, fontWeight, lineHeight, textAlign</li>
     *   <li>Background: background, backgroundColor, backgroundImage</li>
     *   <li>Effects: transform, transition, animation, boxShadow, filter</li>
     *   <li>Logical Properties: marginInline, paddingBlock, insetInline, etc.</li>
     * </ul>
     */
    public static class InlineStyle extends Style<InlineStyle> {
        private final Attributes parent;

        InlineStyle(Attributes parent) {
            this.parent = parent;
        }

        // Helper to finalize style and return parent
        private Attributes complete() {
            parent.set("style", build());
            return parent;
        }

        /**
         * Finish styling and return to Attributes builder.
         * Use this when you need explicit control over attribute chaining.
         */
        public Attributes done() {
            return complete();
        }

        /**
         * Returns the finalized Attributes with style applied.
         * This allows InlineStyle to be used directly where Attributes is expected.
         */
        public Attributes toAttrs() {
            return complete();
        }

        /**
         * Returns the attributes map for element consumption.
         * Auto-finalizes the style into the parent attributes.
         */
        public Map<String, String> toMap() {
            return complete().toMap();
        }

        /**
         * Returns an immutable copy of the finalized attributes.
         * Auto-finalizes the style into the parent attributes.
         */
        public Map<String, String> buildAttrs() {
            return complete().build();
        }

        // ==================== Core Attribute Shortcuts ====================

        /** Sets the id attribute. @param value the element ID */
        public Attributes id(String value) { return complete().id(value); }

        /** Sets the class attribute. @param value the CSS class(es) */
        public Attributes class_(String value) { return complete().class_(value); }

        /** Adds a CSS class to existing classes. @param className the class to add */
        public Attributes addClass(String className) { return complete().addClass(className); }

        /** Sets the title attribute (tooltip). @param value the title text */
        public Attributes title(String value) { return complete().title(value); }

        // ==================== Link Attribute Shortcuts ====================

        /** Sets the href attribute for links. @param value the URL */
        public Attributes href(String value) { return complete().href(value); }

        /** Sets the target attribute for links. @param value the target window/frame */
        public Attributes target(String value) { return complete().target(value); }

        /** Sets target="_blank" with proper security attributes. */
        public Attributes targetBlank() { return complete().targetBlank(); }

        // ==================== Media Attribute Shortcuts ====================

        /** Sets the src attribute for images/scripts. @param value the source URL */
        public Attributes src(String value) { return complete().src(value); }

        /** Sets the alt attribute for images. @param value the alt text */
        public Attributes alt(String value) { return complete().alt(value); }

        // ==================== Form Attribute Shortcuts ====================

        /** Sets the type attribute for inputs. @param value the input type */
        public Attributes type(String value) { return complete().type(value); }

        /** Sets the name attribute for form elements. @param value the name */
        public Attributes name(String value) { return complete().name(value); }

        /** Sets the value attribute. @param value the value */
        public Attributes value(String value) { return complete().value(value); }

        /** Sets the placeholder attribute for inputs. @param value the placeholder text */
        public Attributes placeholder(String value) { return complete().placeholder(value); }

        /** Sets the action attribute for forms. @param value the form action URL */
        public Attributes action(String value) { return complete().action(value); }

        /** Sets the method attribute for forms. @param value the HTTP method */
        public Attributes method(String value) { return complete().method(value); }

        /** Sets the for attribute for labels. @param value the target element ID */
        public Attributes for_(String value) { return complete().for_(value); }

        // ==================== Boolean Attribute Shortcuts ====================

        /** Adds the disabled boolean attribute. */
        public Attributes disabled() { return complete().disabled(); }

        /** Conditionally adds the disabled attribute. @param isDisabled whether to disable */
        public Attributes disabled(boolean isDisabled) { return complete().disabled(isDisabled); }

        /** Adds the checked boolean attribute for checkboxes/radios. */
        public Attributes checked() { return complete().checked(); }

        /** Conditionally adds the checked attribute. @param isChecked whether to check */
        public Attributes checked(boolean isChecked) { return complete().checked(isChecked); }

        /** Adds the required boolean attribute. */
        public Attributes required() { return complete().required(); }

        /** Adds the readonly boolean attribute. */
        public Attributes readonly() { return complete().readonly(); }

        /** Adds the hidden boolean attribute. */
        public Attributes hidden() { return complete().hidden(); }

        /** Conditionally adds the hidden attribute. @param isHidden whether to hide */
        public Attributes hidden(boolean isHidden) { return complete().hidden(isHidden); }

        /** Adds the autofocus boolean attribute. */
        public Attributes autofocus() { return complete().autofocus(); }

        // ==================== Data & ARIA Attribute Shortcuts ====================

        /** Sets a data-* attribute. @param name the data name (without "data-") @param value the value */
        public Attributes data(String name, String value) { return complete().data(name, value); }

        /** Sets an aria-* attribute. @param name the aria name (without "aria-") @param value the value */
        public Attributes aria(String name, String value) { return complete().aria(name, value); }

        /** Sets the role attribute for ARIA. @param value the ARIA role */
        public Attributes role(String value) { return complete().role(value); }

        // ==================== Table Attribute Shortcuts ====================

        /** Sets the colspan attribute for table cells. @param value the number of columns to span */
        public Attributes colspan(int value) { return complete().colspan(value); }

        /** Sets the rowspan attribute for table cells. @param value the number of rows to span */
        public Attributes rowspan(int value) { return complete().rowspan(value); }

        // ==================== Event Handler Shortcuts ====================

        /** Registers a click event handler. @param handler the handler to execute on click */
        public Attributes onClick(Consumer<Event> handler) { return complete().onClick(handler); }

        /** Registers a change event handler. @param handler the handler to execute on change */
        public Attributes onChange(Consumer<Event> handler) { return complete().onChange(handler); }

        /** Registers an input event handler. @param handler the handler to execute on input */
        public Attributes onInput(Consumer<Event> handler) { return complete().onInput(handler); }

        /** Registers a submit event handler. @param handler the handler to execute on submit */
        public Attributes onSubmit(Consumer<Event> handler) { return complete().onSubmit(handler); }

        /** Registers a focus event handler. @param handler the handler to execute on focus */
        public Attributes onFocus(Consumer<Event> handler) { return complete().onFocus(handler); }

        /** Registers a blur event handler. @param handler the handler to execute on blur */
        public Attributes onBlur(Consumer<Event> handler) { return complete().onBlur(handler); }

        /** Registers a keydown event handler. @param handler the handler to execute on keydown */
        public Attributes onKeyDown(Consumer<Event> handler) { return complete().onKeyDown(handler); }

        /** Registers a keyup event handler. @param handler the handler to execute on keyup */
        public Attributes onKeyUp(Consumer<Event> handler) { return complete().onKeyUp(handler); }

        /** Registers a mouseenter event handler. @param handler the handler to execute on mouseenter */
        public Attributes onMouseEnter(Consumer<Event> handler) { return complete().onMouseEnter(handler); }

        /** Registers a mouseleave event handler. @param handler the handler to execute on mouseleave */
        public Attributes onMouseLeave(Consumer<Event> handler) { return complete().onMouseLeave(handler); }

        /** Registers a double-click event handler. @param handler the handler to execute on double-click */
        public Attributes onDoubleClick(Consumer<Event> handler) { return complete().onDoubleClick(handler); }

        /** Registers a generic event handler. @param eventType the DOM event type @param handler the handler */
        public Attributes on(String eventType, Consumer<Event> handler) { return complete().on(eventType, handler); }

        // ==================== Additional Mouse Event Shortcuts ====================

        /** Registers a mousedown event handler. @param handler the handler to execute on mousedown */
        public Attributes onMouseDown(Consumer<Event> handler) { return complete().onMouseDown(handler); }

        /** Registers a mouseup event handler. @param handler the handler to execute on mouseup */
        public Attributes onMouseUp(Consumer<Event> handler) { return complete().onMouseUp(handler); }

        /** Registers a mousemove event handler. @param handler the handler to execute on mousemove */
        public Attributes onMouseMove(Consumer<Event> handler) { return complete().onMouseMove(handler); }

        /** Registers a mouseover event handler. @param handler the handler to execute on mouseover */
        public Attributes onMouseOver(Consumer<Event> handler) { return complete().onMouseOver(handler); }

        /** Registers a mouseout event handler. @param handler the handler to execute on mouseout */
        public Attributes onMouseOut(Consumer<Event> handler) { return complete().onMouseOut(handler); }

        /** Registers a contextmenu (right-click) event handler. @param handler the handler to execute */
        public Attributes onContextMenu(Consumer<Event> handler) { return complete().onContextMenu(handler); }

        /** Registers a wheel event handler. @param handler the handler to execute on wheel */
        public Attributes onWheel(Consumer<Event> handler) { return complete().onWheel(handler); }

        // ==================== Keyboard Event Shortcuts ====================

        /** Registers a keypress event handler. @param handler the handler to execute on keypress */
        public Attributes onKeyPress(Consumer<Event> handler) { return complete().onKeyPress(handler); }

        // ==================== Drag & Drop Event Shortcuts ====================

        /** Registers a drag event handler. @param handler the handler to execute during drag */
        public Attributes onDrag(Consumer<Event> handler) { return complete().onDrag(handler); }

        /** Registers a dragstart event handler. @param handler the handler to execute on drag start */
        public Attributes onDragStart(Consumer<Event> handler) { return complete().onDragStart(handler); }

        /** Registers a dragend event handler. @param handler the handler to execute on drag end */
        public Attributes onDragEnd(Consumer<Event> handler) { return complete().onDragEnd(handler); }

        /** Registers a dragenter event handler. @param handler the handler to execute when dragged item enters */
        public Attributes onDragEnter(Consumer<Event> handler) { return complete().onDragEnter(handler); }

        /** Registers a dragleave event handler. @param handler the handler to execute when dragged item leaves */
        public Attributes onDragLeave(Consumer<Event> handler) { return complete().onDragLeave(handler); }

        /** Registers a dragover event handler. @param handler the handler to execute when dragged item is over */
        public Attributes onDragOver(Consumer<Event> handler) { return complete().onDragOver(handler); }

        /** Registers a drop event handler. @param handler the handler to execute on drop */
        public Attributes onDrop(Consumer<Event> handler) { return complete().onDrop(handler); }

        // ==================== Touch Event Shortcuts ====================

        /** Registers a touchstart event handler. @param handler the handler to execute on touch start */
        public Attributes onTouchStart(Consumer<Event> handler) { return complete().onTouchStart(handler); }

        /** Registers a touchmove event handler. @param handler the handler to execute on touch move */
        public Attributes onTouchMove(Consumer<Event> handler) { return complete().onTouchMove(handler); }

        /** Registers a touchend event handler. @param handler the handler to execute on touch end */
        public Attributes onTouchEnd(Consumer<Event> handler) { return complete().onTouchEnd(handler); }

        /** Registers a touchcancel event handler. @param handler the handler to execute on touch cancel */
        public Attributes onTouchCancel(Consumer<Event> handler) { return complete().onTouchCancel(handler); }

        // ==================== Scroll Event Shortcut ====================

        /** Registers a scroll event handler. @param handler the handler to execute on scroll */
        public Attributes onScroll(Consumer<Event> handler) { return complete().onScroll(handler); }

        // ==================== Animation & Transition Event Shortcuts ====================

        /** Registers an animationstart event handler. @param handler the handler to execute when animation starts */
        public Attributes onAnimationStart(Consumer<Event> handler) { return complete().onAnimationStart(handler); }

        /** Registers an animationend event handler. @param handler the handler to execute when animation ends */
        public Attributes onAnimationEnd(Consumer<Event> handler) { return complete().onAnimationEnd(handler); }

        /** Registers an animationiteration event handler. @param handler the handler to execute on animation iteration */
        public Attributes onAnimationIteration(Consumer<Event> handler) { return complete().onAnimationIteration(handler); }

        /** Registers a transitionend event handler. @param handler the handler to execute when transition ends */
        public Attributes onTransitionEnd(Consumer<Event> handler) { return complete().onTransitionEnd(handler); }

        // ==================== Media Event Shortcuts ====================

        /** Registers a load event handler. @param handler the handler to execute on load */
        public Attributes onLoad(Consumer<Event> handler) { return complete().onLoad(handler); }

        /** Registers an error event handler. @param handler the handler to execute on error */
        public Attributes onError(Consumer<Event> handler) { return complete().onError(handler); }

        // ==================== Clipboard Event Shortcuts ====================

        /** Registers a copy event handler. @param handler the handler to execute on copy */
        public Attributes onCopy(Consumer<Event> handler) { return complete().onCopy(handler); }

        /** Registers a cut event handler. @param handler the handler to execute on cut */
        public Attributes onCut(Consumer<Event> handler) { return complete().onCut(handler); }

        /** Registers a paste event handler. @param handler the handler to execute on paste */
        public Attributes onPaste(Consumer<Event> handler) { return complete().onPaste(handler); }

        // ==================== Generic Setter Shortcut ====================

        /** Sets any HTML attribute by name. @param name the attribute name @param value the attribute value */
        public Attributes set(String name, String value) { return complete().set(name, value); }
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

    // ==================== Additional Mouse Events ====================

    /**
     * Registers a mousedown event handler.
     *
     * @param handler the handler to execute on mousedown
     * @return this for chaining
     */
    public Attributes onMouseDown(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mousedown", handler);
        return set("onmousedown", eh.toJsAttribute());
    }

    /**
     * Registers a mouseup event handler.
     *
     * @param handler the handler to execute on mouseup
     * @return this for chaining
     */
    public Attributes onMouseUp(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseup", handler);
        return set("onmouseup", eh.toJsAttribute());
    }

    /**
     * Registers a mousemove event handler.
     *
     * @param handler the handler to execute on mousemove
     * @return this for chaining
     */
    public Attributes onMouseMove(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mousemove", handler);
        return set("onmousemove", eh.toJsAttribute());
    }

    /**
     * Registers a mouseover event handler.
     *
     * @param handler the handler to execute on mouseover
     * @return this for chaining
     */
    public Attributes onMouseOver(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseover", handler);
        return set("onmouseover", eh.toJsAttribute());
    }

    /**
     * Registers a mouseout event handler.
     *
     * @param handler the handler to execute on mouseout
     * @return this for chaining
     */
    public Attributes onMouseOut(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseout", handler);
        return set("onmouseout", eh.toJsAttribute());
    }

    /**
     * Registers a contextmenu (right-click) event handler.
     *
     * @param handler the handler to execute on contextmenu
     * @return this for chaining
     */
    public Attributes onContextMenu(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("contextmenu", handler);
        return set("oncontextmenu", eh.toJsAttribute());
    }

    /**
     * Registers a wheel (scroll wheel) event handler.
     *
     * @param handler the handler to execute on wheel
     * @return this for chaining
     */
    public Attributes onWheel(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("wheel", handler);
        return set("onwheel", eh.toJsAttribute());
    }

    // ==================== Keyboard Events ====================

    /**
     * Registers a keypress event handler.
     *
     * @param handler the handler to execute on keypress
     * @return this for chaining
     */
    public Attributes onKeyPress(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("keypress", handler);
        return set("onkeypress", eh.toJsAttribute());
    }

    // ==================== Drag & Drop Events ====================

    /**
     * Registers a drag event handler.
     *
     * @param handler the handler to execute during drag
     * @return this for chaining
     */
    public Attributes onDrag(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("drag", handler);
        return set("ondrag", eh.toJsAttribute());
    }

    /**
     * Registers a dragstart event handler.
     *
     * @param handler the handler to execute on drag start
     * @return this for chaining
     */
    public Attributes onDragStart(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dragstart", handler);
        return set("ondragstart", eh.toJsAttribute());
    }

    /**
     * Registers a dragend event handler.
     *
     * @param handler the handler to execute on drag end
     * @return this for chaining
     */
    public Attributes onDragEnd(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dragend", handler);
        return set("ondragend", eh.toJsAttribute());
    }

    /**
     * Registers a dragenter event handler.
     *
     * @param handler the handler to execute when dragged item enters
     * @return this for chaining
     */
    public Attributes onDragEnter(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dragenter", handler);
        return set("ondragenter", eh.toJsAttribute());
    }

    /**
     * Registers a dragleave event handler.
     *
     * @param handler the handler to execute when dragged item leaves
     * @return this for chaining
     */
    public Attributes onDragLeave(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dragleave", handler);
        return set("ondragleave", eh.toJsAttribute());
    }

    /**
     * Registers a dragover event handler.
     *
     * @param handler the handler to execute when dragged item is over
     * @return this for chaining
     */
    public Attributes onDragOver(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dragover", handler);
        return set("ondragover", eh.toJsAttribute());
    }

    /**
     * Registers a drop event handler.
     *
     * @param handler the handler to execute on drop
     * @return this for chaining
     */
    public Attributes onDrop(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("drop", handler);
        return set("ondrop", eh.toJsAttribute());
    }

    // ==================== Touch Events ====================

    /**
     * Registers a touchstart event handler.
     *
     * @param handler the handler to execute on touch start
     * @return this for chaining
     */
    public Attributes onTouchStart(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("touchstart", handler);
        return set("ontouchstart", eh.toJsAttribute());
    }

    /**
     * Registers a touchmove event handler.
     *
     * @param handler the handler to execute on touch move
     * @return this for chaining
     */
    public Attributes onTouchMove(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("touchmove", handler);
        return set("ontouchmove", eh.toJsAttribute());
    }

    /**
     * Registers a touchend event handler.
     *
     * @param handler the handler to execute on touch end
     * @return this for chaining
     */
    public Attributes onTouchEnd(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("touchend", handler);
        return set("ontouchend", eh.toJsAttribute());
    }

    /**
     * Registers a touchcancel event handler.
     *
     * @param handler the handler to execute on touch cancel
     * @return this for chaining
     */
    public Attributes onTouchCancel(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("touchcancel", handler);
        return set("ontouchcancel", eh.toJsAttribute());
    }

    // ==================== Scroll Event ====================

    /**
     * Registers a scroll event handler.
     *
     * @param handler the handler to execute on scroll
     * @return this for chaining
     */
    public Attributes onScroll(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("scroll", handler);
        return set("onscroll", eh.toJsAttribute());
    }

    // ==================== Animation & Transition Events ====================

    /**
     * Registers an animationstart event handler.
     *
     * @param handler the handler to execute when animation starts
     * @return this for chaining
     */
    public Attributes onAnimationStart(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("animationstart", handler);
        return set("onanimationstart", eh.toJsAttribute());
    }

    /**
     * Registers an animationend event handler.
     *
     * @param handler the handler to execute when animation ends
     * @return this for chaining
     */
    public Attributes onAnimationEnd(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("animationend", handler);
        return set("onanimationend", eh.toJsAttribute());
    }

    /**
     * Registers an animationiteration event handler.
     *
     * @param handler the handler to execute on animation iteration
     * @return this for chaining
     */
    public Attributes onAnimationIteration(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("animationiteration", handler);
        return set("onanimationiteration", eh.toJsAttribute());
    }

    /**
     * Registers a transitionend event handler.
     *
     * @param handler the handler to execute when transition ends
     * @return this for chaining
     */
    public Attributes onTransitionEnd(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("transitionend", handler);
        return set("ontransitionend", eh.toJsAttribute());
    }

    // ==================== Media Events ====================

    /**
     * Registers a load event handler.
     *
     * @param handler the handler to execute on load
     * @return this for chaining
     */
    public Attributes onLoad(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("load", handler);
        return set("onload", eh.toJsAttribute());
    }

    /**
     * Registers an error event handler.
     *
     * @param handler the handler to execute on error
     * @return this for chaining
     */
    public Attributes onError(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("error", handler);
        return set("onerror", eh.toJsAttribute());
    }

    // ==================== Clipboard Events ====================

    /**
     * Registers a copy event handler.
     *
     * @param handler the handler to execute on copy
     * @return this for chaining
     */
    public Attributes onCopy(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("copy", handler);
        return set("oncopy", eh.toJsAttribute());
    }

    /**
     * Registers a cut event handler.
     *
     * @param handler the handler to execute on cut
     * @return this for chaining
     */
    public Attributes onCut(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("cut", handler);
        return set("oncut", eh.toJsAttribute());
    }

    /**
     * Registers a paste event handler.
     *
     * @param handler the handler to execute on paste
     * @return this for chaining
     */
    public Attributes onPaste(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("paste", handler);
        return set("onpaste", eh.toJsAttribute());
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
