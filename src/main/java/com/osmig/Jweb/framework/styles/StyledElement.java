package com.osmig.Jweb.framework.styles;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VElement;
import com.osmig.Jweb.framework.vdom.VFragment;
import com.osmig.Jweb.framework.vdom.VNode;
import com.osmig.Jweb.framework.vdom.VRaw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps an element with pseudo-class styles (hover, focus, active, etc.).
 * Generates unique class names and a style tag for CSS rules.
 *
 * Usage:
 *   div(attrs().class_("card"))
 *       .style(style().background(white).padding(rem(1)))
 *       .hover(style().background(hex("#f5f5f5")))
 *       .focus(style().outline(px(2), solid, blue))
 */
public class StyledElement implements Element {

    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    /**
     * The pseudo-ELEMENTS, which take a double colon ({@code ::before}).
     * Everything else is a pseudo-class and takes a single colon
     * ({@code :hover}).
     */
    private static final java.util.Set<String> PSEUDO_ELEMENTS = java.util.Set.of(
        "before", "after", "placeholder", "selection",
        "first-line", "first-letter", "marker", "backdrop");

    private final VNode baseElement;
    private final String generatedClass;
    private jweb.Style<?> baseStyle;
    private final Map<String, jweb.Style<?>> pseudoStyles = new LinkedHashMap<>();

    public StyledElement(VNode baseElement) {
        this.baseElement = baseElement;
        this.generatedClass = "jweb-" + ID_COUNTER.incrementAndGet();
    }

    public StyledElement style(jweb.Style<?> style) {
        this.baseStyle = style;
        return this;
    }

    public StyledElement hover(jweb.Style<?> style) {
        pseudoStyles.put("hover", style);
        return this;
    }

    public StyledElement focus(jweb.Style<?> style) {
        pseudoStyles.put("focus", style);
        return this;
    }

    public StyledElement active(jweb.Style<?> style) {
        pseudoStyles.put("active", style);
        return this;
    }

    public StyledElement visited(jweb.Style<?> style) {
        pseudoStyles.put("visited", style);
        return this;
    }

    public StyledElement focusVisible(jweb.Style<?> style) {
        pseudoStyles.put("focus-visible", style);
        return this;
    }

    public StyledElement focusWithin(jweb.Style<?> style) {
        pseudoStyles.put("focus-within", style);
        return this;
    }

    public StyledElement disabled(jweb.Style<?> style) {
        pseudoStyles.put("disabled", style);
        return this;
    }

    public StyledElement enabled(jweb.Style<?> style) {
        pseudoStyles.put("enabled", style);
        return this;
    }

    public StyledElement checked(jweb.Style<?> style) {
        pseudoStyles.put("checked", style);
        return this;
    }

    public StyledElement firstChild(jweb.Style<?> style) {
        pseudoStyles.put("first-child", style);
        return this;
    }

    public StyledElement lastChild(jweb.Style<?> style) {
        pseudoStyles.put("last-child", style);
        return this;
    }

    public StyledElement nthChild(String expression, jweb.Style<?> style) {
        pseudoStyles.put("nth-child(" + expression + ")", style);
        return this;
    }

    public StyledElement before(jweb.Style<?> style) {
        pseudoStyles.put("before", style);
        return this;
    }

    public StyledElement after(jweb.Style<?> style) {
        pseudoStyles.put("after", style);
        return this;
    }

    public StyledElement placeholder(jweb.Style<?> style) {
        pseudoStyles.put("placeholder", style);
        return this;
    }

    /**
     * Escape hatch for any pseudo-CLASS not covered by a named method —
     * emitted with a single colon.
     *
     * <p>Example:</p>
     * <pre>
     * .pseudo("nth-of-type(2n)", style().background(gray))
     * .pseudo("has(&gt; img)", style().padding(zero))
     * </pre>
     *
     * @param name the pseudo-class name, with or without a leading colon
     * @param style the styles to apply
     * @return this for chaining
     */
    public StyledElement pseudo(String name, jweb.Style<?> style) {
        pseudoStyles.put(":" + stripColons(name), style);
        return this;
    }

    /**
     * Escape hatch for any pseudo-ELEMENT not covered by a named method —
     * emitted with a double colon.
     *
     * <p>Example:</p>
     * <pre>
     * .pseudoElement("first-line", style().fontWeight(700))
     * .pseudoElement("-webkit-scrollbar", style().width(px(8)))
     * </pre>
     *
     * @param name the pseudo-element name, with or without leading colons
     * @param style the styles to apply
     * @return this for chaining
     */
    public StyledElement pseudoElement(String name, jweb.Style<?> style) {
        pseudoStyles.put("::" + stripColons(name), style);
        return this;
    }

    private static String stripColons(String name) {
        int i = 0;
        while (i < name.length() && name.charAt(i) == ':') i++;
        return name.substring(i);
    }

    @Override
    public VNode toVNode() {
        // If no pseudo-styles, just return the element with inline style
        if (pseudoStyles.isEmpty()) {
            return applyInlineStyle(baseElement);
        }

        // Generate minified CSS rules
        StringBuilder css = new StringBuilder();

        // Base style rule (minified - no spaces)
        if (baseStyle != null && !baseStyle.isEmpty()) {
            css.append(".").append(generatedClass).append("{")
               .append(baseStyle.build())
               .append("}");
        }

        // Pseudo rules (minified)
        for (Map.Entry<String, jweb.Style<?>> entry : pseudoStyles.entrySet()) {
            css.append(".").append(generatedClass).append(selectorFor(entry.getKey())).append("{")
               .append(entry.getValue().build())
               .append("}");
        }

        // Add the generated class to the element
        VNode styledElement = addClassToElement(baseElement);

        // Create a fragment with style tag and element (minified)
        VNode styleTag = new VRaw("<style>" + css + "</style>");

        List<VNode> nodes = new ArrayList<>();
        nodes.add(styleTag);
        nodes.add(styledElement);

        return new VFragment(nodes);
    }

    /**
     * Turns a stored pseudo key into its selector suffix. Keys that already
     * carry their colons (from {@link #pseudo} / {@link #pseudoElement}) are
     * used as-is; a bare name gets {@code ::} if it names a pseudo-element and
     * {@code :} otherwise.
     */
    private static String selectorFor(String pseudo) {
        if (pseudo.startsWith(":")) {
            return pseudo;
        }
        return (PSEUDO_ELEMENTS.contains(pseudo) ? "::" : ":") + pseudo;
    }

    private VNode applyInlineStyle(VNode node) {
        if (baseStyle == null || baseStyle.isEmpty()) {
            return node;
        }

        if (node instanceof VElement element) {
            Map<String, String> newAttrs = new LinkedHashMap<>(element.getAttributes());
            String existingStyle = newAttrs.get("style");
            if (existingStyle != null && !existingStyle.isEmpty()) {
                newAttrs.put("style", existingStyle + " " + baseStyle.build());
            } else {
                newAttrs.put("style", baseStyle.build());
            }
            return VElement.of(element.getTag(), newAttrs, element.getChildren());
        }

        return node;
    }

    private VNode addClassToElement(VNode node) {
        if (node instanceof VElement element) {
            Map<String, String> newAttrs = new LinkedHashMap<>(element.getAttributes());
            String existingClass = newAttrs.get("class");
            if (existingClass != null && !existingClass.isEmpty()) {
                newAttrs.put("class", existingClass + " " + generatedClass);
            } else {
                newAttrs.put("class", generatedClass);
            }
            return VElement.of(element.getTag(), newAttrs, element.getChildren());
        }

        return node;
    }
}
