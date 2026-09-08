package jweb;

import com.osmig.Jweb.framework.attributes.HtmlAttributes;
import jweb.Attributes.InlineStyle;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.TextElement;
import com.osmig.Jweb.framework.events.EventHandler;
import com.osmig.Jweb.framework.events.EventRegistry;
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
public class Tag implements Element, HtmlAttributes<Tag> {

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

    /**
     * The write primitive behind every inherited attribute method — same as
     * {@link #attr(String, String)}, named for {@link HtmlAttributes}.
     */
    @Override
    public Tag set(String name, String value) {
        return attr(name, value);
    }

    /** The read primitive {@link HtmlAttributes} needs for class helpers. */
    @Override
    public String get(String name) {
        return attributes.get(name);
    }

    
    
    




    
    
    
    
    
    
    
    
    
    
    
    


    
    
    
    
    
    
    
    
    
    
    


    

    // ==================== Event Handlers (Type-Safe) ====================



























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
            || item instanceof jweb.Style;
    }

    /** An Iterable or Object[] argument is a group of items, not one child. */
    private static Iterable<?> asGroup(Object item) {
        if (item instanceof Iterable<?> iterable) return iterable;
        if (item instanceof Object[] array) return java.util.Arrays.asList(array);
        return null;
    }

    public static List<VNode> toVNodes(Object... children) {
        List<VNode> nodes = new ArrayList<>();
        // bind(state) is an attribute that also carries default content: the
        // state's value fills the element when nothing else was given
        jweb.state.Bound bound = null;
        for (Object child : children) {
            if (child == null) continue;
            if (child instanceof jweb.state.Bound b) { if (bound == null) bound = b; continue; }
            if (isAttributeItem(child)) continue;

            Iterable<?> group = asGroup(child);
            if (group != null) {
                for (Object item : group) {
                    if (item instanceof jweb.state.Bound b) { if (bound == null) bound = b; continue; }
                    if (!isAttributeItem(item)) {
                        nodes.add(toVNode(item));
                    }
                }
            } else {
                nodes.add(toVNode(child));
            }
        }
        if (nodes.isEmpty() && bound != null) nodes.add(bound.valueNode());
        return nodes;
    }

    public static Map<String, String> extractAttrs(Object... items) {
        Map<String, String> attrs = new LinkedHashMap<>();
        List<jweb.Style<?>> styles = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Attr attr) {
                attrs.put(attr.name(), attr.value());
            } else if (item instanceof Attributes attributes) {
                attrs.putAll(attributes.toMap());
            } else if (item instanceof InlineStyle inlineStyle) {
                attrs.putAll(inlineStyle.toMap());
            } else if (item instanceof jweb.Style<?> style) {
                // A bare style() builder as an argument becomes the style
                // attribute: div(style().padding(px(4)), text("hi")). Any
                // conditional rules it carries (.hover, .at, .dark) become a
                // generated class collected below, so a class_() written in
                // any position still merges instead of overwriting.
                styles.add(style);
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
                    } else if (subItem instanceof jweb.Style<?> style) {
                        styles.add(style);
                    }
                }
            }
        }
        for (jweb.Style<?> style : styles) {
            com.osmig.Jweb.framework.styles.PageStyles.applyTo(style, attrs);
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
        warnOnNestedTitle(tagName, children);
        return new Tag(tagName, attrs, children);
    }

    /** Parents a {@code <title>} element legitimately belongs to. */
    private static final java.util.Set<String> TITLE_PARENTS =
        java.util.Set.of("head", "html", "svg", "g", "symbol", "defs", "a", "use");

    private static final System.Logger LOG = System.getLogger(Tag.class.getName());

    /**
     * {@code title(...)} is the {@code <title>} element, so {@code button(title("Save"))}
     * silently nests a document title where a tooltip was meant. The compiler
     * cannot catch it — a Tag is a valid child anywhere — so say it out loud once.
     */
    private static void warnOnNestedTitle(String tagName, List<VNode> children) {
        if (TITLE_PARENTS.contains(tagName)) return;
        for (VNode child : children) {
            if (child instanceof VElement element && "title".equals(element.getTag())) {
                LOG.log(System.Logger.Level.WARNING,
                    () -> "<title> nested inside <" + tagName + ">: title(\"…\") is the document "
                        + "title element. For the tooltip attribute use " + tagName
                        + "(attrs().title(\"…\"), …).");
                return;
            }
        }
    }
}
