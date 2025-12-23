package com.osmig.Jweb.framework.navigation;

import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enhanced link component for client-side navigation.
 *
 * <p>Link provides a smarter anchor tag that supports prefetching, partial page
 * swapping, and smooth transitions - all while maintaining SEO-friendly URLs
 * and progressive enhancement (works without JavaScript).</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Simple link (works like normal anchor)
 * Link.to("/about", "About Us")
 *
 * // With prefetch (fetches on hover)
 * Link.to("/products").prefetch().text("Products")
 *
 * // With partial swap (only updates #content)
 * Link.to("/dashboard").swap("#content").text("Dashboard")
 * }</pre>
 *
 * <h2>Full Example</h2>
 * <pre>{@code
 * nav(
 *     Link.to("/").text("Home"),
 *     Link.to("/products").prefetch().text("Products"),
 *     Link.to("/about").prefetch().swap("#main").text("About"),
 *     Link.to("/contact").text("Contact")
 * )
 * }</pre>
 *
 * <h2>With Classes and Attributes</h2>
 * <pre>{@code
 * Link.to("/login")
 *     .class_("btn btn-primary")
 *     .prefetch()
 *     .text("Sign In")
 * }</pre>
 *
 * <h2>How It Works</h2>
 * <ul>
 *   <li><b>Prefetch</b>: On hover, fetches the page in background for instant navigation</li>
 *   <li><b>Swap</b>: Only replaces specified element instead of full page reload</li>
 *   <li><b>Progressive</b>: Falls back to normal navigation if JS disabled</li>
 * </ul>
 *
 * @see Element
 */
public final class Link implements Element {

    private final String href;
    private String text;
    private Element[] children;
    private boolean prefetch = false;
    private String swapTarget = null;
    private String swapStrategy = "innerHTML";
    private boolean pushState = true;
    private String activeClass = null;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    private Link(String href) {
        this.href = href;
    }

    /**
     * Creates a new Link to the specified URL.
     *
     * @param href the destination URL
     * @return a new Link builder
     */
    public static Link to(String href) {
        return new Link(href);
    }

    /**
     * Creates a simple link with text.
     *
     * @param href the destination URL
     * @param text the link text
     * @return the link element
     */
    public static Element to(String href, String text) {
        return new Link(href).text(text);
    }

    /**
     * Creates a link with children elements.
     *
     * @param href the destination URL
     * @param children the child elements
     * @return the link element
     */
    public static Element to(String href, Element... children) {
        return new Link(href).children(children);
    }

    /**
     * Sets the link text.
     *
     * @param text the link text
     * @return this builder
     */
    public Link text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets child elements for the link.
     *
     * @param children the child elements
     * @return this builder
     */
    public Link children(Element... children) {
        this.children = children;
        return this;
    }

    /**
     * Enables prefetching on hover.
     *
     * <p>When enabled, the target page will be fetched in the background
     * when the user hovers over the link, making navigation feel instant.</p>
     *
     * @return this builder
     */
    public Link prefetch() {
        this.prefetch = true;
        return this;
    }

    /**
     * Enables or disables prefetching.
     *
     * @param enabled whether to enable prefetch
     * @return this builder
     */
    public Link prefetch(boolean enabled) {
        this.prefetch = enabled;
        return this;
    }

    /**
     * Sets the swap target for partial page updates.
     *
     * <p>Instead of a full page reload, only the specified element
     * will be updated with the new content.</p>
     *
     * @param selector CSS selector for the element to swap (e.g., "#content", ".main")
     * @return this builder
     */
    public Link swap(String selector) {
        this.swapTarget = selector;
        return this;
    }

    /**
     * Sets the swap strategy.
     *
     * @param strategy "innerHTML" (default), "outerHTML", "beforeend", "afterbegin"
     * @return this builder
     */
    public Link swapStrategy(String strategy) {
        this.swapStrategy = strategy;
        return this;
    }

    /**
     * Whether to push state to browser history.
     *
     * @param push true to update URL (default), false to keep current URL
     * @return this builder
     */
    public Link pushState(boolean push) {
        this.pushState = push;
        return this;
    }

    /**
     * Sets a class to add when this link matches the current URL.
     *
     * @param className the active class (e.g., "active", "current")
     * @return this builder
     */
    public Link activeClass(String className) {
        this.activeClass = className;
        return this;
    }

    /**
     * Sets the CSS class.
     *
     * @param className the CSS class(es)
     * @return this builder
     */
    public Link class_(String className) {
        attributes.put("class", className);
        return this;
    }

    /**
     * Adds a CSS class.
     *
     * @param className the class to add
     * @return this builder
     */
    public Link addClass(String className) {
        String existing = attributes.get("class");
        if (existing == null || existing.isBlank()) {
            attributes.put("class", className);
        } else {
            attributes.put("class", existing + " " + className);
        }
        return this;
    }

    /**
     * Sets the ID.
     *
     * @param id the element ID
     * @return this builder
     */
    public Link id(String id) {
        attributes.put("id", id);
        return this;
    }

    /**
     * Sets the title attribute.
     *
     * @param title the title text
     * @return this builder
     */
    public Link title(String title) {
        attributes.put("title", title);
        return this;
    }

    /**
     * Sets the target attribute.
     *
     * @param target the target (e.g., "_blank")
     * @return this builder
     */
    public Link target(String target) {
        attributes.put("target", target);
        return this;
    }

    /**
     * Opens link in new tab.
     *
     * @return this builder
     */
    public Link newTab() {
        attributes.put("target", "_blank");
        attributes.put("rel", "noopener noreferrer");
        return this;
    }

    /**
     * Sets a data attribute.
     *
     * @param name the data name (without "data-" prefix)
     * @param value the value
     * @return this builder
     */
    public Link data(String name, String value) {
        attributes.put("data-" + name, value);
        return this;
    }

    /**
     * Sets any attribute.
     *
     * @param name the attribute name
     * @param value the value
     * @return this builder
     */
    public Link attr(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public VNode toVNode() {
        Map<String, String> attrs = new LinkedHashMap<>(attributes);
        attrs.put("href", href);

        // Add navigation data attributes
        if (prefetch) {
            attrs.put("data-prefetch", "true");
        }
        if (swapTarget != null) {
            attrs.put("data-swap", swapTarget);
            attrs.put("data-swap-strategy", swapStrategy);
        }
        if (!pushState) {
            attrs.put("data-push-state", "false");
        }
        if (activeClass != null) {
            attrs.put("data-active-class", activeClass);
        }

        // Build children
        java.util.List<VNode> childNodes = new java.util.ArrayList<>();
        if (text != null) {
            childNodes.add(new com.osmig.Jweb.framework.vdom.VText(text));
        }
        if (children != null) {
            for (Element child : children) {
                childNodes.add(child.toVNode());
            }
        }

        return VElement.of("a", attrs, childNodes);
    }

    // ==================== Static Helpers ====================

    /**
     * Creates a nav link with active state detection.
     *
     * @param href the URL
     * @param text the link text
     * @param currentPath the current page path
     * @return a link element
     */
    public static Element navLink(String href, String text, String currentPath) {
        Link link = Link.to(href).text(text).prefetch();
        if (href.equals(currentPath)) {
            link.addClass("active");
        }
        return link;
    }

    /**
     * Creates a button-styled link.
     *
     * @param href the URL
     * @param text the button text
     * @return a link styled as button
     */
    public static Element button(String href, String text) {
        return Link.to(href).class_("btn").text(text);
    }
}
