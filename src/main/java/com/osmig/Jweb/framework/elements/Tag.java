package com.osmig.Jweb.framework.elements;

import com.osmig.Jweb.framework.attributes.Attr;
import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.attributes.Attributes.InlineStyle;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.styles.Style;
import com.osmig.Jweb.framework.styles.StyledElement;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VText;

import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents an HTML tag element with fluent builder API.
 *
 * Usage:
 *   div()
 *       .id("main")
 *       .class_("container")
 *       .text("Hello World")
 *
 *   div()
 *       .id("card")
 *       .children(
 *           h1().text("Title"),
 *           p().text("Content")
 *       )
 */
public class Tag implements Element {

    private final String tagName;
    private final Map<String, String> attributes;
    private final List<VNode> children;

    public Tag(String tagName) {
        this(tagName, new LinkedHashMap<>(), new ArrayList<>());
    }

    public Tag(String tagName, Attributes attributes) {
        this(tagName, new LinkedHashMap<>(attributes.toMap()), new ArrayList<>());
    }

    public Tag(String tagName, List<VNode> children) {
        this(tagName, new LinkedHashMap<>(), new ArrayList<>(children));
    }

    public Tag(String tagName, Attributes attributes, List<VNode> children) {
        this(tagName, new LinkedHashMap<>(attributes.toMap()), new ArrayList<>(children));
    }

    public Tag(String tagName, Map<String, String> attributes, List<VNode> children) {
        this.tagName = tagName;
        this.attributes = new LinkedHashMap<>(attributes);
        this.children = new ArrayList<>(children);
    }

    @Override
    public VNode toVNode() {
        return VElement.of(tagName, attributes, children);
    }

    // ==================== Attribute Methods (Fluent) ====================

    public Tag attr(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    public Tag id(String id) { return attr("id", id); }
    public Tag class_(String className) { return attr("class", className); }
    public Tag addClass(String className) {
        String existing = attributes.getOrDefault("class", "");
        return attr("class", existing.isEmpty() ? className : existing + " " + className);
    }

    // Inline style
    public Tag style(String style) { return attr("style", style); }
    public Tag style(Style style) { return attr("style", style.build()); }

    // Common attributes
    public Tag href(String value) { return attr("href", value); }
    public Tag src(String value) { return attr("src", value); }
    public Tag alt(String value) { return attr("alt", value); }
    public Tag type(String value) { return attr("type", value); }
    public Tag name(String value) { return attr("name", value); }
    public Tag value(String value) { return attr("value", value); }
    public Tag placeholder(String value) { return attr("placeholder", value); }
    public Tag action(String value) { return attr("action", value); }
    public Tag method(String value) { return attr("method", value); }
    public Tag target(String value) { return attr("target", value); }
    public Tag title(String value) { return attr("title", value); }
    public Tag for_(String value) { return attr("for", value); }
    public Tag role(String value) { return attr("role", value); }

    // Boolean attributes.
    // A boolean attribute is stored with a null value so it renders bare
    // ({@code <input required>}) — the same convention Attr and Attributes use.
    public Tag disabled() { return attr("disabled", null); }
    public Tag disabled(boolean value) { return value ? disabled() : this; }
    public Tag checked() { return attr("checked", null); }
    public Tag checked(boolean value) { return value ? checked() : this; }
    public Tag required() { return attr("required", null); }
    public Tag required(boolean value) { return value ? required() : this; }
    public Tag readonly() { return attr("readonly", null); }
    public Tag readonly(boolean value) { return value ? readonly() : this; }
    public Tag hidden() { return attr("hidden", null); }
    public Tag hidden(boolean value) { return value ? hidden() : this; }
    public Tag autofocus() { return attr("autofocus", null); }
    public Tag autofocus(boolean value) { return value ? autofocus() : this; }

    // Data and ARIA
    public Tag data(String name, String value) { return attr("data-" + name, value); }
    public Tag aria(String name, String value) { return attr("aria-" + name, value); }

    // ==================== Event Handlers (Type-Safe) ====================

    /**
     * Registers a click event handler.
     * @param handler the handler to execute on click
     */
    public Tag onClick(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("click", handler);
        return attr("onclick", eh.toJsAttribute());
    }

    /**
     * Registers a change event handler (for inputs, selects, textareas).
     * @param handler the handler to execute on change
     */
    public Tag onChange(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("change", handler);
        return attr("onchange", eh.toJsAttribute());
    }

    /**
     * Registers an input event handler (fires on every keystroke).
     * @param handler the handler to execute on input
     */
    public Tag onInput(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("input", handler);
        return attr("oninput", eh.toJsAttribute());
    }

    /**
     * Registers a submit event handler for forms.
     * @param handler the handler to execute on submit
     */
    public Tag onSubmit(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("submit", handler);
        return attr("onsubmit", eh.toJsAttribute());
    }

    /**
     * Registers a keydown event handler.
     * @param handler the handler to execute on keydown
     */
    public Tag onKeyDown(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("keydown", handler);
        return attr("onkeydown", eh.toJsAttribute());
    }

    /**
     * Registers a keyup event handler.
     * @param handler the handler to execute on keyup
     */
    public Tag onKeyUp(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("keyup", handler);
        return attr("onkeyup", eh.toJsAttribute());
    }

    /**
     * Registers a focus event handler.
     * @param handler the handler to execute on focus
     */
    public Tag onFocus(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("focus", handler);
        return attr("onfocus", eh.toJsAttribute());
    }

    /**
     * Registers a blur event handler (when element loses focus).
     * @param handler the handler to execute on blur
     */
    public Tag onBlur(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("blur", handler);
        return attr("onblur", eh.toJsAttribute());
    }

    /**
     * Registers a mouseenter event handler.
     * @param handler the handler to execute on mouseenter
     */
    public Tag onMouseEnter(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseenter", handler);
        return attr("onmouseenter", eh.toJsAttribute());
    }

    /**
     * Registers a mouseleave event handler.
     * @param handler the handler to execute on mouseleave
     */
    public Tag onMouseLeave(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("mouseleave", handler);
        return attr("onmouseleave", eh.toJsAttribute());
    }

    /**
     * Registers a load event handler.
     * @param handler the handler to execute on load
     */
    public Tag onLoad(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("load", handler);
        return attr("onload", eh.toJsAttribute());
    }

    /**
     * Registers a double-click event handler.
     * @param handler the handler to execute on double-click
     */
    public Tag onDoubleClick(Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register("dblclick", handler);
        return attr("ondblclick", eh.toJsAttribute());
    }

    /**
     * Registers a generic event handler for any DOM event type.
     * @param eventType the DOM event type (click, change, scroll, etc.)
     * @param handler the handler to execute
     */
    public Tag on(String eventType, Consumer<Event> handler) {
        EventHandler eh = EventRegistry.register(eventType, handler);
        return attr("on" + eventType, eh.toJsAttribute());
    }

    // ==================== Content Methods (Fluent) ====================

    /**
     * Add text content to this element.
     * div().text("Hello") -> <div>Hello</div>
     */
    public Tag text(String content) {
        children.add(new VText(content));
        return this;
    }

    /**
     * Add raw HTML content (not escaped).
     * @deprecated Use {@link #unsafeHtml(String)} to make the escape explicit
     */
    @Deprecated
    public Tag raw(String html) {
        children.add(TextElement.raw(html).toVNode());
        return this;
    }

    /**
     * Add raw HTML content (unsafe - not escaped or validated).
     * Use this for trusted HTML content like SVG or pre-rendered HTML.
     *
     * <p>WARNING: Never use with user input - risk of XSS attacks.</p>
     *
     * @param html the raw HTML content
     * @return this tag for chaining
     */
    public Tag unsafeHtml(String html) {
        children.add(TextElement.raw(html).toVNode());
        return this;
    }

    /**
     * Add a child element.
     * div().child(span().text("nested"))
     */
    public Tag child(Element child) {
        if (child != null) {
            children.add(child.toVNode());
        }
        return this;
    }

    /**
     * Add multiple child elements.
     * div().children(h1().text("Title"), p().text("Body"))
     */
    public Tag children(Element... elements) {
        for (Element element : elements) {
            if (element != null) {
                children.add(element.toVNode());
            }
        }
        return this;
    }

    /**
     * Add multiple child elements from an array or list.
     */
    public Tag children(Iterable<? extends Element> elements) {
        for (Element element : elements) {
            if (element != null) {
                children.add(element.toVNode());
            }
        }
        return this;
    }

    /**
     * Map a collection to child elements.
     * ul().each(items, item -> li().text(item.getName()))
     */
    public <T> Tag each(Collection<T> items, Function<T, Element> mapper) {
        for (T item : items) {
            Element element = mapper.apply(item);
            if (element != null) {
                children.add(element.toVNode());
            }
        }
        return this;
    }

    /**
     * Conditionally add a child.
     * div().when(isAdmin, () -> button().text("Delete"))
     */
    public Tag when(boolean condition, java.util.function.Supplier<Element> element) {
        if (condition) {
            Element el = element.get();
            if (el != null) {
                children.add(el.toVNode());
            }
        }
        return this;
    }

    /**
     * Conditionally add one of two children.
     * div().ifElse(isLoggedIn, () -> span().text("Welcome"), () -> a().href("/login").text("Sign In"))
     *
     * @deprecated Use {@code .when(cond, ...)} twice, or
     *             {@code .child(match(cond(c, a), otherwise(b)))} instead.
     */
    @Deprecated
    public Tag ifElse(boolean condition,
                      java.util.function.Supplier<Element> ifTrue,
                      java.util.function.Supplier<Element> ifFalse) {
        Element el = condition ? ifTrue.get() : ifFalse.get();
        if (el != null) {
            children.add(el.toVNode());
        }
        return this;
    }

    // ==================== Styled Element Support ====================

    public StyledElement styled() {
        return new StyledElement(this.toVNode());
    }

    public StyledElement styled(Style baseStyle) {
        return new StyledElement(this.toVNode()).style(baseStyle);
    }

    public StyledElement hover(Style hoverStyle) {
        return new StyledElement(this.toVNode()).hover(hoverStyle);
    }

    public StyledElement focus(Style focusStyle) {
        return new StyledElement(this.toVNode()).focus(focusStyle);
    }

    public StyledElement active(Style activeStyle) {
        return new StyledElement(this.toVNode()).active(activeStyle);
    }

    // ==================== Getters ====================

    public String getTagName() { return tagName; }
    public Map<String, String> getAttributes() { return attributes; }
    public List<VNode> getChildren() { return children; }

    // ==================== Static Helpers ====================

    /**
     * Converts one varargs item into a child node.
     *
     * <p>Accepts {@code null} (renders nothing), {@link VNode}, any
     * {@link jweb.Element} / {@link com.osmig.Jweb.framework.core.Renderable},
     * {@link String} (escaped text), {@link Number} and {@link Boolean}.
     * Anything else throws instead of silently rendering {@code toString()}.
     * ({@link Iterable} and {@code Object[]} arguments are flattened by
     * {@link #toVNodes(Object...)} before reaching this method.)</p>
     *
     * @throws IllegalArgumentException if the value is not a renderable child
     */
    public static VNode toVNode(Object child) {
        if (child == null) return new VText("");
        if (child instanceof VNode vnode) return vnode;
        if (child instanceof jweb.Element element) return element.toVNode();
        if (child instanceof com.osmig.Jweb.framework.core.Renderable renderable) return renderable.toVNode();
        if (child instanceof String text) return new VText(text);
        if (child instanceof Number || child instanceof Boolean) return new VText(String.valueOf(child));
        throw new IllegalArgumentException(
            "Cannot render " + child.getClass().getName() + " as an element child. "
                + "Pass an Element, VNode, String, Number or Boolean — "
                + "use text(String) for escaped text or raw(String) for trusted HTML.");
    }

    /** True for the varargs items that are attributes, not children. */
    private static boolean isAttributeItem(Object item) {
        return item instanceof Attr
            || item instanceof Attributes
            || item instanceof InlineStyle
            || item instanceof com.osmig.Jweb.framework.styles.Style;
    }

    /** An Iterable or Object[] argument is a group of items, not one child. */
    private static Iterable<?> asGroup(Object item) {
        if (item instanceof Iterable<?> iterable) return iterable;
        if (item instanceof Object[] array) return java.util.Arrays.asList(array);
        return null;
    }

    public static List<VNode> toVNodes(Object... children) {
        List<VNode> nodes = new ArrayList<>();
        for (Object child : children) {
            if (child == null) continue;
            if (isAttributeItem(child)) continue;

            Iterable<?> group = asGroup(child);
            if (group != null) {
                for (Object item : group) {
                    if (!isAttributeItem(item)) {
                        nodes.add(toVNode(item));
                    }
                }
            } else {
                nodes.add(toVNode(child));
            }
        }
        return nodes;
    }

    public static Map<String, String> extractAttrs(Object... items) {
        Map<String, String> attrs = new LinkedHashMap<>();
        for (Object item : items) {
            if (item instanceof Attr attr) {
                attrs.put(attr.name(), attr.value());
            } else if (item instanceof Attributes attributes) {
                attrs.putAll(attributes.toMap());
            } else if (item instanceof InlineStyle inlineStyle) {
                attrs.putAll(inlineStyle.toMap());
            } else if (item instanceof com.osmig.Jweb.framework.styles.Style<?> style) {
                // A bare style() builder as an argument becomes the style
                // attribute: div(style().padding(px(4)), text("hi"))
                attrs.put("style", style.build());
            } else {
                // Same extraction rules inside a group (Iterable or Object[]) as
                // at the top level — InlineStyle/Style used to be silently dropped.
                Iterable<?> group = asGroup(item);
                if (group == null) continue;
                for (Object subItem : group) {
                    if (subItem instanceof Attr attr) {
                        attrs.put(attr.name(), attr.value());
                    } else if (subItem instanceof InlineStyle inlineStyle) {
                        attrs.putAll(inlineStyle.toMap());
                    } else if (subItem instanceof Attributes attributes) {
                        attrs.putAll(attributes.toMap());
                    } else if (subItem instanceof com.osmig.Jweb.framework.styles.Style<?> style) {
                        attrs.put("style", style.build());
                    }
                }
            }
        }
        return attrs;
    }

    /**
     * Creates a Tag from mixed Attr objects and children.
     * This enables: div(class_("foo"), id("bar"), p("text"))
     */
    public static Tag create(String tagName, Object... items) {
        Map<String, String> attrs = extractAttrs(items);
        List<VNode> children = toVNodes(items);
        return new Tag(tagName, attrs, children);
    }
}
