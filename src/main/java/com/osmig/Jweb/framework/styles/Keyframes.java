package com.osmig.Jweb.framework.styles;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent builder for CSS @keyframes animations.
 *
 * Usage:
 *   import static com.osmig.Jweb.framework.styles.Keyframes.*;
 *   import static com.osmig.Jweb.framework.styles.Styles.*;
 *
 *   // Simple fade-in animation
 *   keyframes("fadeIn")
 *       .from(style().opacity(0))
 *       .to(style().opacity(1))
 *
 *   // Multi-step animation
 *   keyframes("bounce")
 *       .at(0, style().transform(translateY(zero)))
 *       .at(50, style().transform(translateY(px(-20))))
 *       .at(100, style().transform(translateY(zero)))
 *
 *   // Complex animation with multiple properties
 *   keyframes("slideInFade")
 *       .from(style()
 *           .opacity(0)
 *           .transform(translateX(px(-100))))
 *       .to(style()
 *           .opacity(1)
 *           .transform(translateX(zero)))
 */
public class Keyframes {

    private final String name;
    private final Map<String, Style<?>> frames = new LinkedHashMap<>();

    private Keyframes(String name) {
        this.name = name;
    }

    public static Keyframes keyframes(String name) {
        return new Keyframes(name);
    }

    // ==================== Frame Definition ====================

    /**
     * Add styles for the 'from' (0%) keyframe.
     */
    public Keyframes from(Style<?> style) {
        frames.put("from", style);
        return this;
    }

    /**
     * Add styles for the 'to' (100%) keyframe.
     */
    public Keyframes to(Style<?> style) {
        frames.put("to", style);
        return this;
    }

    /**
     * Add styles at a specific percentage (0-100).
     */
    public Keyframes at(int percentage, Style<?> style) {
        frames.put(percentage + "%", style);
        return this;
    }

    /**
     * Add styles at a specific percentage with decimal precision.
     */
    public Keyframes at(double percentage, Style<?> style) {
        frames.put(formatPercent(percentage), style);
        return this;
    }

    /**
     * Add styles at multiple percentages.
     */
    public Keyframes at(int[] percentages, Style<?> style) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < percentages.length; i++) {
            if (i > 0) key.append(", ");
            key.append(percentages[i]).append("%");
        }
        frames.put(key.toString(), style);
        return this;
    }

    // ==================== Build ====================

    public String getName() {
        return name;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("@keyframes ").append(name).append(" {\n");

        for (Map.Entry<String, Style<?>> entry : frames.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" {\n");
            for (Map.Entry<String, String> prop : entry.getValue().toMap().entrySet()) {
                sb.append("    ").append(prop.getKey()).append(": ").append(prop.getValue()).append(";\n");
            }
            sb.append("  }\n");
        }

        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    private String formatPercent(double value) {
        if (value == (int) value) {
            return (int) value + "%";
        }
        return String.format("%.2f%%", value);
    }

    // ==================== Common Animations ====================

    /**
     * Creates a fade-in animation.
     */
    public static Keyframes fadeIn() {
        return keyframes("fadeIn")
            .from(new Style<>().opacity(0))
            .to(new Style<>().opacity(1));
    }

    /**
     * Creates a fade-out animation.
     */
    public static Keyframes fadeOut() {
        return keyframes("fadeOut")
            .from(new Style<>().opacity(1))
            .to(new Style<>().opacity(0));
    }

    /**
     * Creates a slide-in from left animation.
     */
    public static Keyframes slideInLeft() {
        return keyframes("slideInLeft")
            .from(new Style<>().transform(CSS.translateX(CSSUnits.percent(-100))).opacity(0))
            .to(new Style<>().transform(CSS.translateX(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from right animation.
     */
    public static Keyframes slideInRight() {
        return keyframes("slideInRight")
            .from(new Style<>().transform(CSS.translateX(CSSUnits.percent(100))).opacity(0))
            .to(new Style<>().transform(CSS.translateX(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from top animation.
     */
    public static Keyframes slideInTop() {
        return keyframes("slideInTop")
            .from(new Style<>().transform(CSS.translateY(CSSUnits.percent(-100))).opacity(0))
            .to(new Style<>().transform(CSS.translateY(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from bottom animation.
     */
    public static Keyframes slideInBottom() {
        return keyframes("slideInBottom")
            .from(new Style<>().transform(CSS.translateY(CSSUnits.percent(100))).opacity(0))
            .to(new Style<>().transform(CSS.translateY(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a pulse animation.
     */
    public static Keyframes pulse() {
        return keyframes("pulse")
            .at(0, new Style<>().transform(CSS.scale(1)))
            .at(50, new Style<>().transform(CSS.scale(1.05)))
            .at(100, new Style<>().transform(CSS.scale(1)));
    }

    /**
     * Creates a bounce animation.
     */
    public static Keyframes bounce() {
        return keyframes("bounce")
            .at(0, new Style<>().transform(CSS.translateY(CSSUnits.zero)))
            .at(25, new Style<>().transform(CSS.translateY(CSSUnits.px(-10))))
            .at(50, new Style<>().transform(CSS.translateY(CSSUnits.zero)))
            .at(75, new Style<>().transform(CSS.translateY(CSSUnits.px(-5))))
            .at(100, new Style<>().transform(CSS.translateY(CSSUnits.zero)));
    }

    /**
     * Creates a shake animation.
     */
    public static Keyframes shake() {
        return keyframes("shake")
            .at(0, new Style<>().transform(CSS.translateX(CSSUnits.zero)))
            .at(25, new Style<>().transform(CSS.translateX(CSSUnits.px(-5))))
            .at(50, new Style<>().transform(CSS.translateX(CSSUnits.px(5))))
            .at(75, new Style<>().transform(CSS.translateX(CSSUnits.px(-5))))
            .at(100, new Style<>().transform(CSS.translateX(CSSUnits.zero)));
    }

    /**
     * Creates a spin animation.
     */
    public static Keyframes spin() {
        return keyframes("spin")
            .from(new Style<>().transform(CSS.rotate(CSSUnits.deg(0))))
            .to(new Style<>().transform(CSS.rotate(CSSUnits.deg(360))));
    }

    /**
     * Creates a zoom-in animation.
     */
    public static Keyframes zoomIn() {
        return keyframes("zoomIn")
            .from(new Style<>().transform(CSS.scale(0)).opacity(0))
            .to(new Style<>().transform(CSS.scale(1)).opacity(1));
    }

    /**
     * Creates a zoom-out animation.
     */
    public static Keyframes zoomOut() {
        return keyframes("zoomOut")
            .from(new Style<>().transform(CSS.scale(1)).opacity(1))
            .to(new Style<>().transform(CSS.scale(0)).opacity(0));
    }
}
