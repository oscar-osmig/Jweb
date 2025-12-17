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

    private final VNode baseElement;
    private final String generatedClass;
    private Style baseStyle;
    private final Map<String, Style> pseudoStyles = new LinkedHashMap<>();

    public StyledElement(VNode baseElement) {
        this.baseElement = baseElement;
        this.generatedClass = "jweb-" + ID_COUNTER.incrementAndGet();
    }

    public StyledElement style(Style style) {
        this.baseStyle = style;
        return this;
    }

    public StyledElement hover(Style style) {
        pseudoStyles.put("hover", style);
        return this;
    }

    public StyledElement focus(Style style) {
        pseudoStyles.put("focus", style);
        return this;
    }

    public StyledElement active(Style style) {
        pseudoStyles.put("active", style);
        return this;
    }

    public StyledElement visited(Style style) {
        pseudoStyles.put("visited", style);
        return this;
    }

    public StyledElement focusVisible(Style style) {
        pseudoStyles.put("focus-visible", style);
        return this;
    }

    public StyledElement focusWithin(Style style) {
        pseudoStyles.put("focus-within", style);
        return this;
    }

    public StyledElement disabled(Style style) {
        pseudoStyles.put("disabled", style);
        return this;
    }

    public StyledElement enabled(Style style) {
        pseudoStyles.put("enabled", style);
        return this;
    }

    public StyledElement checked(Style style) {
        pseudoStyles.put("checked", style);
        return this;
    }

    public StyledElement firstChild(Style style) {
        pseudoStyles.put("first-child", style);
        return this;
    }

    public StyledElement lastChild(Style style) {
        pseudoStyles.put("last-child", style);
        return this;
    }

    public StyledElement nthChild(String expression, Style style) {
        pseudoStyles.put("nth-child(" + expression + ")", style);
        return this;
    }

    public StyledElement before(Style style) {
        pseudoStyles.put(":before", style);
        return this;
    }

    public StyledElement after(Style style) {
        pseudoStyles.put(":after", style);
        return this;
    }

    public StyledElement placeholder(Style style) {
        pseudoStyles.put(":placeholder", style);
        return this;
    }

    @Override
    public VNode toVNode() {
        // If no pseudo-styles, just return the element with inline style
        if (pseudoStyles.isEmpty()) {
            return applyInlineStyle(baseElement);
        }

        // Generate CSS rules
        StringBuilder css = new StringBuilder();

        // Base style rule
        if (baseStyle != null && !baseStyle.isEmpty()) {
            css.append(".").append(generatedClass).append(" { ")
               .append(baseStyle.build())
               .append(" }\n");
        }

        // Pseudo-class rules
        for (Map.Entry<String, Style> entry : pseudoStyles.entrySet()) {
            String pseudo = entry.getKey();
            Style style = entry.getValue();

            if (pseudo.startsWith(":")) {
                // Pseudo-element (::before, ::after, ::placeholder)
                css.append(".").append(generatedClass).append(pseudo).append(" { ")
                   .append(style.build())
                   .append(" }\n");
            } else {
                // Pseudo-class (:hover, :focus, etc.)
                css.append(".").append(generatedClass).append(":").append(pseudo).append(" { ")
                   .append(style.build())
                   .append(" }\n");
            }
        }

        // Add the generated class to the element
        VNode styledElement = addClassToElement(baseElement);

        // Create a fragment with style tag and element
        VNode styleTag = new VRaw("<style>" + css + "</style>");

        List<VNode> nodes = new ArrayList<>();
        nodes.add(styleTag);
        nodes.add(styledElement);

        return new VFragment(nodes);
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
