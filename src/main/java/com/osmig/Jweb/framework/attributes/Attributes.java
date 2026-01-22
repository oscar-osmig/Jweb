package com.osmig.Jweb.framework.attributes;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.js.Actions.Action;
import com.osmig.Jweb.framework.ref.Ref;
import com.osmig.Jweb.framework.styles.CSSValue;
import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.framework.transition.TransitionBuilder;
import com.osmig.Jweb.framework.transition.TransitionBuilder.TransitionReceiver;

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
public class Attributes implements TransitionReceiver {

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

    /**
     * Sets multiple CSS classes at once.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().classes("btn", "primary", "lg")
     * // Output: class="btn primary lg"
     * </pre>
     *
     * @param classNames the class names to add
     * @return this for chaining
     */
    public Attributes classes(String... classNames) {
        if (classNames == null || classNames.length == 0) {
            return this;
        }
        return set("class", String.join(" ", classNames));
    }

    /**
     * Conditionally sets the class attribute.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_(isActive, "active")
     * // Output: class="active" if isActive is true, no class attribute otherwise
     * </pre>
     *
     * @param condition whether to apply the class
     * @param className the class name to apply if condition is true
     * @return this for chaining
     */
    public Attributes class_(boolean condition, String className) {
        return condition ? set("class", className) : this;
    }

    /**
     * Conditionally adds a CSS class to existing classes.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").addClass(isActive, "active")
     * // Output: class="btn active" if isActive is true, class="btn" otherwise
     * </pre>
     *
     * @param condition whether to add the class
     * @param className the class name to add if condition is true
     * @return this for chaining
     */
    public Attributes addClass(boolean condition, String className) {
        return condition ? addClass(className) : this;
    }

    /**
     * Fluent conditional class adding.
     * More readable than addClass(boolean, String) for simple conditions.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").classIf("active", isActive).classIf("disabled", isDisabled)
     * </pre>
     *
     * @param className the class name to add if condition is true
     * @param condition whether to add the class
     * @return this for chaining
     */
    public Attributes classIf(String className, boolean condition) {
        return condition ? addClass(className) : this;
    }

    /**
     * Adds class based on ternary condition.
     * Adds trueClass if condition is true, falseClass otherwise.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().class_("btn").classToggle(isOpen, "open", "closed")
     * // Result: class="btn open" or class="btn closed"
     * </pre>
     *
     * @param condition the condition to evaluate
     * @param trueClass the class to add if condition is true
     * @param falseClass the class to add if condition is false
     * @return this for chaining
     */
    public Attributes classToggle(boolean condition, String trueClass, String falseClass) {
        return addClass(condition ? trueClass : falseClass);
    }

    // ==================== Layout Shortcuts ====================

    /**
     * Applies flexbox centering styles (display: flex; justify-content: center; align-items: center).
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().flexCenter(), ...)
     * </pre>
     *
     * @return this for chaining
     */
    public Attributes flexCenter() {
        return set("style", "display:flex;justify-content:center;align-items:center");
    }

    /**
     * Applies flexbox column layout with optional gap.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().flexColumn("1rem"), ...)
     * </pre>
     *
     * @param gap the gap between items (CSS value like "1rem" or "10px")
     * @return this for chaining
     */
    public Attributes flexColumn(String gap) {
        return set("style", "display:flex;flex-direction:column;gap:" + gap);
    }

    /**
     * Applies flexbox column layout without gap.
     *
     * @return this for chaining
     */
    public Attributes flexColumn() {
        return set("style", "display:flex;flex-direction:column");
    }

    /**
     * Applies flexbox row layout with optional gap.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().flexRow("1rem"), ...)
     * </pre>
     *
     * @param gap the gap between items
     * @return this for chaining
     */
    public Attributes flexRow(String gap) {
        return set("style", "display:flex;flex-direction:row;gap:" + gap);
    }

    /**
     * Applies flexbox row layout without gap.
     *
     * @return this for chaining
     */
    public Attributes flexRow() {
        return set("style", "display:flex;flex-direction:row");
    }

    /**
     * Applies flexbox with space-between.
     *
     * @return this for chaining
     */
    public Attributes flexBetween() {
        return set("style", "display:flex;justify-content:space-between;align-items:center");
    }

    /**
     * Applies CSS grid with specified number of equal columns.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs().gridCols(3, "1rem"), ...)  // 3-column grid with 1rem gap
     * </pre>
     *
     * @param cols the number of columns
     * @param gap the gap between items
     * @return this for chaining
     */
    public Attributes gridCols(int cols, String gap) {
        return set("style", "display:grid;grid-template-columns:repeat(" + cols + ",1fr);gap:" + gap);
    }

    /**
     * Applies CSS grid with specified number of equal columns without gap.
     *
     * @param cols the number of columns
     * @return this for chaining
     */
    public Attributes gridCols(int cols) {
        return set("style", "display:grid;grid-template-columns:repeat(" + cols + ",1fr)");
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

        // ==================== Details & Dialog Event Shortcuts ====================

        /** Registers a toggle event handler for details elements. @param handler the handler to execute on toggle */
        public Attributes onToggle(Consumer<Event> handler) { return complete().onToggle(handler); }

        /** Registers a cancel event handler for dialog elements. @param handler the handler to execute on cancel */
        public Attributes onCancel(Consumer<Event> handler) { return complete().onCancel(handler); }

        /** Registers a close event handler for dialog elements. @param handler the handler to execute on close */
        public Attributes onClose(Consumer<Event> handler) { return complete().onClose(handler); }

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

        // ==================== JavaScript Action Event Handler Shortcuts ====================

        /** Sets a click handler using a JavaScript Action. @param action the action to execute on click */
        public Attributes onClick(Action action) { return complete().onClick(action); }

        /** Sets a change handler using a JavaScript Action. @param action the action to execute on change */
        public Attributes onChange(Action action) { return complete().onChange(action); }

        /** Sets an input handler using a JavaScript Action. @param action the action to execute on input */
        public Attributes onInput(Action action) { return complete().onInput(action); }

        /** Sets a submit handler using a JavaScript Action. @param action the action to execute on submit */
        public Attributes onSubmit(Action action) { return complete().onSubmit(action); }

        /** Sets a focus handler using a JavaScript Action. @param action the action to execute on focus */
        public Attributes onFocus(Action action) { return complete().onFocus(action); }

        /** Sets a blur handler using a JavaScript Action. @param action the action to execute on blur */
        public Attributes onBlur(Action action) { return complete().onBlur(action); }

        /** Sets a keydown handler using a JavaScript Action. @param action the action to execute on keydown */
        public Attributes onKeyDown(Action action) { return complete().onKeyDown(action); }

        /** Sets a keyup handler using a JavaScript Action. @param action the action to execute on keyup */
        public Attributes onKeyUp(Action action) { return complete().onKeyUp(action); }

        /** Sets a mouseenter handler using a JavaScript Action. @param action the action to execute on mouseenter */
        public Attributes onMouseEnter(Action action) { return complete().onMouseEnter(action); }

        /** Sets a mouseleave handler using a JavaScript Action. @param action the action to execute on mouseleave */
        public Attributes onMouseLeave(Action action) { return complete().onMouseLeave(action); }

        /** Sets a double-click handler using a JavaScript Action. @param action the action to execute on dblclick */
        public Attributes onDoubleClick(Action action) { return complete().onDoubleClick(action); }

        /** Sets any event handler using a JavaScript Action. @param eventType the event type @param action the action */
        public Attributes on(String eventType, Action action) { return complete().on(eventType, action); }

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
    /** Sets the scope attribute for th elements. @param value row, col, rowgroup, colgroup */
    public Attributes scope(String value) { return set("scope", value); }
    /** Sets the headers attribute for table cells. @param value space-separated IDs of th elements */
    public Attributes headers(String value) { return set("headers", value); }

    // ==================== Form Validation Attributes ====================

    /** Sets the pattern attribute for regex validation. @param regex the regex pattern */
    public Attributes pattern(String regex) { return set("pattern", regex); }
    /** Sets the min attribute for numeric/date inputs. @param value the minimum value */
    public Attributes min(String value) { return set("min", value); }
    /** Sets the min attribute with number. @param value the minimum value */
    public Attributes min(int value) { return set("min", String.valueOf(value)); }
    /** Sets the max attribute for numeric/date inputs. @param value the maximum value */
    public Attributes max(String value) { return set("max", value); }
    /** Sets the max attribute with number. @param value the maximum value */
    public Attributes max(int value) { return set("max", String.valueOf(value)); }
    /** Sets the step attribute for numeric inputs. @param value the step increment */
    public Attributes step(String value) { return set("step", value); }
    /** Sets the step attribute with number. @param value the step increment */
    public Attributes step(double value) { return set("step", String.valueOf(value)); }
    /** Sets the minlength attribute for text inputs. @param value minimum number of characters */
    public Attributes minlength(int value) { return set("minlength", String.valueOf(value)); }
    /** Sets the maxlength attribute for text inputs. @param value maximum number of characters */
    public Attributes maxlength(int value) { return set("maxlength", String.valueOf(value)); }
    /** Adds the multiple boolean attribute for file/select inputs. */
    public Attributes multiple() { return set("multiple", null); }
    /** Sets the accept attribute for file inputs. @param value acceptable MIME types */
    public Attributes accept(String value) { return set("accept", value); }
    /** Sets the autocomplete attribute. @param value on, off, or specific tokens */
    public Attributes autocomplete(String value) { return set("autocomplete", value); }
    /** Sets the inputmode attribute for virtual keyboards. @param value none, text, decimal, numeric, tel, search, email, url */
    public Attributes inputmode(String value) { return set("inputmode", value); }
    /** Sets the list attribute to connect to a datalist. @param datalistId the datalist element ID */
    public Attributes list(String datalistId) { return set("list", datalistId); }
    /** Sets the form attribute to associate input with a form. @param formId the form element ID */
    public Attributes form(String formId) { return set("form", formId); }
    /** Sets the enctype attribute for forms. @param value encoding type */
    public Attributes enctype(String value) { return set("enctype", value); }
    /** Adds the novalidate boolean attribute to skip form validation. */
    public Attributes novalidate() { return set("novalidate", null); }
    /** Sets the size attribute for inputs/selects. @param value the visible size */
    public Attributes size(int value) { return set("size", String.valueOf(value)); }
    /** Sets the cols attribute for textareas. @param value number of columns */
    public Attributes cols(int value) { return set("cols", String.valueOf(value)); }
    /** Sets the rows attribute for textareas. @param value number of rows */
    public Attributes rows(int value) { return set("rows", String.valueOf(value)); }
    /** Sets the wrap attribute for textareas. @param value soft or hard */
    public Attributes wrap(String value) { return set("wrap", value); }

    // ==================== Global Attributes ====================

    /** Sets the tabindex attribute. @param value tab order (-1 for not focusable, 0 for natural order, positive for explicit order) */
    public Attributes tabindex(int value) { return set("tabindex", String.valueOf(value)); }
    /** Sets the accesskey attribute for keyboard shortcuts. @param value the key character */
    public Attributes accesskey(String value) { return set("accesskey", value); }
    /** Sets the lang attribute for language. @param value language code (e.g., "en", "es", "zh") */
    public Attributes lang(String value) { return set("lang", value); }
    /** Sets the dir attribute for text direction. @param value ltr, rtl, or auto */
    public Attributes dir(String value) { return set("dir", value); }
    /** Sets the translate attribute. @param value yes or no */
    public Attributes translate(String value) { return set("translate", value); }
    /** Adds the contenteditable boolean attribute. */
    public Attributes contenteditable() { return set("contenteditable", "true"); }
    /** Conditionally sets the contenteditable attribute. @param editable whether content is editable */
    public Attributes contenteditable(boolean editable) { return set("contenteditable", String.valueOf(editable)); }
    /** Adds the draggable attribute. @param draggable whether element is draggable */
    public Attributes draggable(boolean draggable) { return set("draggable", String.valueOf(draggable)); }
    /** Adds the spellcheck attribute. @param check whether to enable spellcheck */
    public Attributes spellcheck(boolean check) { return set("spellcheck", String.valueOf(check)); }
    /** Sets the enterkeyhint attribute for virtual keyboards. @param value enter, done, go, next, previous, search, send */
    public Attributes enterkeyhint(String value) { return set("enterkeyhint", value); }
    /** Sets the inert attribute (non-interactive). */
    public Attributes inert() { return set("inert", null); }
    /** Sets the popover attribute. @param value auto or manual */
    public Attributes popover(String value) { return set("popover", value); }
    /** Sets the popovertarget attribute. @param elementId the ID of the popover element */
    public Attributes popovertarget(String elementId) { return set("popovertarget", elementId); }
    /** Sets the popovertargetaction attribute. @param value show, hide, or toggle */
    public Attributes popovertargetaction(String value) { return set("popovertargetaction", value); }
    /** Sets the part attribute for CSS shadow parts. @param value part name(s) */
    public Attributes part(String value) { return set("part", value); }
    /** Sets the slot attribute for shadow DOM. @param value slot name */
    public Attributes slot(String value) { return set("slot", value); }

    // ==================== Link & Resource Attributes ====================

    /** Sets the rel attribute for links. @param value relationship type */
    public Attributes rel(String value) { return set("rel", value); }
    /** Adds the download attribute for forcing downloads. */
    public Attributes download() { return set("download", null); }
    /** Sets the download attribute with a filename. @param filename the suggested filename */
    public Attributes download(String filename) { return set("download", filename); }
    /** Sets the hreflang attribute for link language. @param value language code */
    public Attributes hreflang(String value) { return set("hreflang", value); }
    /** Sets the referrerpolicy attribute. @param value no-referrer, origin, etc. */
    public Attributes referrerpolicy(String value) { return set("referrerpolicy", value); }
    /** Sets the crossorigin attribute. @param value anonymous or use-credentials */
    public Attributes crossorigin(String value) { return set("crossorigin", value); }
    /** Sets the integrity attribute for subresource integrity. @param hash the SRI hash */
    public Attributes integrity(String hash) { return set("integrity", hash); }
    /** Sets the ping attribute for link tracking. @param urls space-separated URLs */
    public Attributes ping(String urls) { return set("ping", urls); }
    /** Sets the media attribute for responsive resources. @param mediaQuery the media query */
    public Attributes media(String mediaQuery) { return set("media", mediaQuery); }
    /** Sets the as attribute for preload hints. @param value resource type (script, style, image, etc.) */
    public Attributes as(String value) { return set("as", value); }

    // ==================== Image & Media Attributes ====================

    /** Sets the srcset attribute for responsive images. @param value srcset descriptor */
    public Attributes srcset(String value) { return set("srcset", value); }
    /** Sets the sizes attribute for responsive images. @param value sizes descriptor */
    public Attributes sizes(String value) { return set("sizes", value); }
    /** Sets the loading attribute for lazy loading. @param value lazy or eager */
    public Attributes loading(String value) { return set("loading", value); }
    /** Sets the decoding attribute for image decoding. @param value sync, async, or auto */
    public Attributes decoding(String value) { return set("decoding", value); }
    /** Sets the fetchpriority attribute. @param value high, low, or auto */
    public Attributes fetchpriority(String value) { return set("fetchpriority", value); }
    /** Adds the ismap boolean attribute for image maps. */
    public Attributes ismap() { return set("ismap", null); }
    /** Sets the usemap attribute for image maps. @param mapName the map name (with #) */
    public Attributes usemap(String mapName) { return set("usemap", mapName); }

    // ==================== Audio/Video Attributes ====================

    /** Adds the controls boolean attribute for media players. */
    public Attributes controls() { return set("controls", null); }
    /** Adds the autoplay boolean attribute for media. */
    public Attributes autoplay() { return set("autoplay", null); }
    /** Adds the loop boolean attribute for media. */
    public Attributes loop() { return set("loop", null); }
    /** Adds the muted boolean attribute for media. */
    public Attributes muted() { return set("muted", null); }
    /** Sets the preload attribute for media. @param value none, metadata, or auto */
    public Attributes preload(String value) { return set("preload", value); }
    /** Sets the poster attribute for video thumbnails. @param url the poster image URL */
    public Attributes poster(String url) { return set("poster", url); }
    /** Adds the playsinline boolean attribute for inline video playback. */
    public Attributes playsinline() { return set("playsinline", null); }
    /** Adds the disablepictureinpicture attribute. */
    public Attributes disablepictureinpicture() { return set("disablepictureinpicture", null); }

    // ==================== Script Attributes ====================

    /** Adds the async boolean attribute for scripts. */
    public Attributes async() { return set("async", null); }
    /** Adds the defer boolean attribute for scripts. */
    public Attributes defer() { return set("defer", null); }
    /** Adds the nomodule attribute for module fallback scripts. */
    public Attributes nomodule() { return set("nomodule", null); }
    /** Sets the nonce attribute for CSP. @param value the nonce value */
    public Attributes nonce(String value) { return set("nonce", value); }

    // ==================== Meta & Document Attributes ====================

    /** Sets the charset attribute for meta tags. @param value character encoding (usually "UTF-8") */
    public Attributes charset(String value) { return set("charset", value); }
    /** Sets the http-equiv attribute for meta tags. @param value HTTP header name */
    public Attributes httpEquiv(String value) { return set("http-equiv", value); }
    /** Sets the content attribute for meta tags. @param value the content value */
    public Attributes content(String value) { return set("content", value); }

    // ==================== SVG Attributes ====================

    /** Sets the viewBox attribute for SVG. @param value e.g., "0 0 100 100" */
    public Attributes viewBox(String value) { return set("viewBox", value); }
    /** Sets the preserveAspectRatio attribute for SVG. @param value e.g., "xMidYMid meet" */
    public Attributes preserveAspectRatio(String value) { return set("preserveAspectRatio", value); }
    /** Sets the xmlns attribute for SVG namespace. @param value namespace URI */
    public Attributes xmlns(String value) { return set("xmlns", value); }
    /** Sets the fill attribute for SVG. @param value color or none */
    public Attributes fill(String value) { return set("fill", value); }
    /** Sets the stroke attribute for SVG. @param value color */
    public Attributes stroke(String value) { return set("stroke", value); }
    /** Sets the stroke-width attribute for SVG. @param value width */
    public Attributes strokeWidth(String value) { return set("stroke-width", value); }
    /** Sets the d attribute for SVG path. @param value path data */
    public Attributes d(String value) { return set("d", value); }
    /** Sets the cx attribute for SVG circles. @param value center x */
    public Attributes cx(String value) { return set("cx", value); }
    /** Sets the cy attribute for SVG circles. @param value center y */
    public Attributes cy(String value) { return set("cy", value); }
    /** Sets the r attribute for SVG circles. @param value radius */
    public Attributes r(String value) { return set("r", value); }
    /** Sets the x attribute for SVG elements. @param value x coordinate */
    public Attributes x(String value) { return set("x", value); }
    /** Sets the y attribute for SVG elements. @param value y coordinate */
    public Attributes y(String value) { return set("y", value); }
    /** Sets the x1 attribute for SVG lines. @param value start x */
    public Attributes x1(String value) { return set("x1", value); }
    /** Sets the y1 attribute for SVG lines. @param value start y */
    public Attributes y1(String value) { return set("y1", value); }
    /** Sets the x2 attribute for SVG lines. @param value end x */
    public Attributes x2(String value) { return set("x2", value); }
    /** Sets the y2 attribute for SVG lines. @param value end y */
    public Attributes y2(String value) { return set("y2", value); }
    /** Sets the points attribute for SVG polygons/polylines. @param value coordinate pairs */
    public Attributes points(String value) { return set("points", value); }
    /** Sets the transform attribute for SVG transformations. @param value transform functions */
    public Attributes transform(String value) { return set("transform", value); }

    // ==================== Microdata Attributes ====================

    /** Sets the itemscope boolean attribute for microdata. */
    public Attributes itemscope() { return set("itemscope", null); }
    /** Sets the itemtype attribute for microdata. @param value schema type URL */
    public Attributes itemtype(String value) { return set("itemtype", value); }
    /** Sets the itemprop attribute for microdata. @param value property name */
    public Attributes itemprop(String value) { return set("itemprop", value); }
    /** Sets the itemid attribute for microdata. @param value global identifier */
    public Attributes itemid(String value) { return set("itemid", value); }
    /** Sets the itemref attribute for microdata. @param value space-separated IDs */
    public Attributes itemref(String value) { return set("itemref", value); }

    // ==================== Dialog & Details Attributes ====================

    /** Adds the open boolean attribute for details/dialog. */
    public Attributes open() { return set("open", null); }
    /** Conditionally adds the open attribute. @param isOpen whether element is open */
    public Attributes open(boolean isOpen) { return isOpen ? open() : this; }

    // ==================== Meter & Progress Attributes ====================

    /** Sets the value attribute for meter/progress. @param value current value */
    public Attributes value(double value) { return set("value", String.valueOf(value)); }
    /** Sets the low attribute for meter. @param value low threshold */
    public Attributes low(double value) { return set("low", String.valueOf(value)); }
    /** Sets the high attribute for meter. @param value high threshold */
    public Attributes high(double value) { return set("high", String.valueOf(value)); }
    /** Sets the optimum attribute for meter. @param value optimal value */
    public Attributes optimum(double value) { return set("optimum", String.valueOf(value)); }

    // ==================== Template & Web Component Attributes ====================

    /** Sets the shadowrootmode attribute for declarative shadow DOM. @param value open or closed */
    public Attributes shadowrootmode(String value) { return set("shadowrootmode", value); }

    // ==================== Semantic Data Attributes ====================

    /** Sets the datetime attribute for time elements. @param value machine-readable datetime */
    public Attributes datetime(String value) { return set("datetime", value); }

    // ==================== iframe Attributes ====================

    /** Sets the sandbox attribute for iframes. @param value sandbox restrictions */
    public Attributes sandbox(String value) { return set("sandbox", value); }
    /** Sets the allow attribute for iframes. @param value feature policy */
    public Attributes allow(String value) { return set("allow", value); }
    /** Adds the allowfullscreen boolean attribute. */
    public Attributes allowfullscreen() { return set("allowfullscreen", null); }
    /** Sets the srcdoc attribute for inline iframe content. @param html the HTML content */
    public Attributes srcdoc(String html) { return set("srcdoc", html); }
    /** Sets the name attribute for iframe targeting. @param value frame name */
    public Attributes frameName(String value) { return set("name", value); }

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

    // ==================== Details & Dialog Events ====================

    /**
     * Registers a toggle event handler for details elements.
     * Fires when the open/closed state changes.
     *
     * @param handler the handler to execute on toggle
     * @return this for chaining
     */
    public Attributes onToggle(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("toggle", handler);
        return set("ontoggle", eh.toJsAttribute());
    }

    /**
     * Registers a cancel event handler for dialog elements.
     * Fires when user cancels the dialog (ESC key).
     *
     * @param handler the handler to execute on cancel
     * @return this for chaining
     */
    public Attributes onCancel(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("cancel", handler);
        return set("oncancel", eh.toJsAttribute());
    }

    /**
     * Registers a close event handler for dialog elements.
     * Fires when dialog is closed.
     *
     * @param handler the handler to execute on close
     * @return this for chaining
     */
    public Attributes onClose(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("close", handler);
        return set("onclose", eh.toJsAttribute());
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

    // ==================== JavaScript Action Event Handlers ====================
    // These allow using the Actions DSL directly with event attributes.
    // Usage: attrs().onClick(show("panel"))
    //        attrs().onClick(all(hide("loader"), show("content")))

    /**
     * Sets a click handler using a JavaScript Action from the Actions DSL.
     *
     * <p>Example:</p>
     * <pre>
     * import static com.osmig.Jweb.framework.js.Actions.*;
     *
     * button(attrs().onClick(show("panel")), "Show Panel")
     * button(attrs().onClick(toggle("dropdown")), "Toggle")
     * button(attrs().onClick(all(hide("a"), show("b"))), "Switch")
     * </pre>
     *
     * @param action the JavaScript action to execute on click
     * @return this for chaining
     */
    public Attributes onClick(Action action) {
        return set("onclick", action.inline());
    }

    /**
     * Sets a change handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on change
     * @return this for chaining
     */
    public Attributes onChange(Action action) {
        return set("onchange", action.inline());
    }

    /**
     * Sets an input handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on input
     * @return this for chaining
     */
    public Attributes onInput(Action action) {
        return set("oninput", action.inline());
    }

    /**
     * Sets a submit handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on submit
     * @return this for chaining
     */
    public Attributes onSubmit(Action action) {
        return set("onsubmit", action.inline());
    }

    /**
     * Sets a focus handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on focus
     * @return this for chaining
     */
    public Attributes onFocus(Action action) {
        return set("onfocus", action.inline());
    }

    /**
     * Sets a blur handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on blur
     * @return this for chaining
     */
    public Attributes onBlur(Action action) {
        return set("onblur", action.inline());
    }

    /**
     * Sets a keydown handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on keydown
     * @return this for chaining
     */
    public Attributes onKeyDown(Action action) {
        return set("onkeydown", action.inline());
    }

    /**
     * Sets a keyup handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on keyup
     * @return this for chaining
     */
    public Attributes onKeyUp(Action action) {
        return set("onkeyup", action.inline());
    }

    /**
     * Sets a mouseenter handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on mouseenter
     * @return this for chaining
     */
    public Attributes onMouseEnter(Action action) {
        return set("onmouseenter", action.inline());
    }

    /**
     * Sets a mouseleave handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on mouseleave
     * @return this for chaining
     */
    public Attributes onMouseLeave(Action action) {
        return set("onmouseleave", action.inline());
    }

    /**
     * Sets a double-click handler using a JavaScript Action.
     *
     * @param action the JavaScript action to execute on double-click
     * @return this for chaining
     */
    public Attributes onDoubleClick(Action action) {
        return set("ondblclick", action.inline());
    }

    /**
     * Sets any event handler using a JavaScript Action.
     *
     * <p>Example:</p>
     * <pre>
     * attrs().on("scroll", throttledScrollHandler)
     * </pre>
     *
     * @param eventType the DOM event type
     * @param action the JavaScript action to execute
     * @return this for chaining
     */
    public Attributes on(String eventType, Action action) {
        return set("on" + eventType, action.inline());
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

    // ==================== Refs ====================

    /**
     * Sets a ref on this element for later reference.
     *
     * <p>Example:</p>
     * <pre>
     * Ref inputRef = Ref.create();
     * input(attrs().ref(inputRef).type("text"))
     * // Later: inputRef.id() returns the element's ID
     * </pre>
     *
     * @param ref the ref to attach
     * @return this for chaining
     */
    public Attributes ref(Ref ref) {
        return id(ref.id());
    }

    // ==================== Transitions ====================

    /**
     * Starts a transition builder for CSS transitions.
     *
     * <p>Example:</p>
     * <pre>
     * div(attrs()
     *     .transition()
     *         .property("opacity", "transform")
     *         .duration("300ms")
     *         .easing("ease-in-out")
     *     .done(),
     *     content
     * )
     * </pre>
     *
     * @return a TransitionBuilder
     */
    public TransitionBuilder transition() {
        return new TransitionBuilder(this);
    }

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
