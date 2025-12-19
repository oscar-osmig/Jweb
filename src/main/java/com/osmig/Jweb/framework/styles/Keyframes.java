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

    /**
     * Creates a new keyframes animation builder.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("fadeIn")
     *     .from(new Style&lt;&gt;().opacity(0))
     *     .to(new Style&lt;&gt;().opacity(1))
     *     .build()
     * </pre>
     *
     * @param name the animation name (used with CSS animation-name property)
     * @return a new Keyframes builder instance
     */
    public static Keyframes keyframes(String name) {
        return new Keyframes(name);
    }

    // ==================== Frame Definition ====================

    /**
     * Adds styles for the 'from' (0%) keyframe.
     * Equivalent to {@code at(0, style)}.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("fadeIn").from(new Style&lt;&gt;().opacity(0))
     * </pre>
     *
     * @param style the Style object with CSS properties for this keyframe
     * @return this builder for chaining
     */
    public Keyframes from(Style<?> style) {
        frames.put("from", style);
        return this;
    }

    /**
     * Adds styles for the 'to' (100%) keyframe.
     * Equivalent to {@code at(100, style)}.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("fadeIn").to(new Style&lt;&gt;().opacity(1))
     * </pre>
     *
     * @param style the Style object with CSS properties for this keyframe
     * @return this builder for chaining
     */
    public Keyframes to(Style<?> style) {
        frames.put("to", style);
        return this;
    }

    /**
     * Adds styles at a specific percentage point.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("bounce")
     *     .at(0, new Style&lt;&gt;().transform(translateY(zero)))
     *     .at(50, new Style&lt;&gt;().transform(translateY(px(-20))))
     *     .at(100, new Style&lt;&gt;().transform(translateY(zero)))
     * </pre>
     *
     * @param percentage the keyframe position (0-100)
     * @param style the Style object with CSS properties for this keyframe
     * @return this builder for chaining
     */
    public Keyframes at(int percentage, Style<?> style) {
        frames.put(percentage + "%", style);
        return this;
    }

    /**
     * Adds styles at a specific percentage with decimal precision.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("precise").at(33.33, new Style&lt;&gt;().opacity(0.5))
     * </pre>
     *
     * @param percentage the keyframe position with decimal precision
     * @param style the Style object with CSS properties for this keyframe
     * @return this builder for chaining
     */
    public Keyframes at(double percentage, Style<?> style) {
        frames.put(formatPercent(percentage), style);
        return this;
    }

    /**
     * Adds the same styles at multiple percentage points.
     *
     * <p>Example:</p>
     * <pre>
     * keyframes("flash")
     *     .at(new int[]{0, 50, 100}, new Style&lt;&gt;().opacity(1))
     *     .at(new int[]{25, 75}, new Style&lt;&gt;().opacity(0))
     * </pre>
     *
     * @param percentages array of keyframe positions
     * @param style the Style object to apply at all specified positions
     * @return this builder for chaining
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

    /**
     * Returns the animation name.
     *
     * @return the animation name
     */
    public String getName() {
        return name;
    }

    /**
     * Builds the complete CSS @keyframes rule string.
     *
     * <p>Example output:</p>
     * <pre>
     * @keyframes fadeIn {
     *   from { opacity: 0; }
     *   to { opacity: 1; }
     * }
     * </pre>
     *
     * @return the formatted CSS @keyframes rule
     */
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
     * Creates a fade-in animation (opacity 0 to 1).
     * Animation name: "fadeIn"
     *
     * <p>Usage:</p>
     * <pre>
     * style().animation(anim("fadeIn"), s(0.5), easeOut)
     * </pre>
     *
     * @return Keyframes for fade-in animation
     */
    public static Keyframes fadeIn() {
        return keyframes("fadeIn")
            .from(new Style<>().opacity(0))
            .to(new Style<>().opacity(1));
    }

    /**
     * Creates a fade-out animation (opacity 1 to 0).
     * Animation name: "fadeOut"
     *
     * @return Keyframes for fade-out animation
     */
    public static Keyframes fadeOut() {
        return keyframes("fadeOut")
            .from(new Style<>().opacity(1))
            .to(new Style<>().opacity(0));
    }

    /**
     * Creates a slide-in from left animation.
     * Slides from -100% X position with fade.
     * Animation name: "slideInLeft"
     *
     * @return Keyframes for slide-in-left animation
     */
    public static Keyframes slideInLeft() {
        return keyframes("slideInLeft")
            .from(new Style<>().transform(CSS.translateX(CSSUnits.percent(-100))).opacity(0))
            .to(new Style<>().transform(CSS.translateX(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from right animation.
     * Slides from +100% X position with fade.
     * Animation name: "slideInRight"
     *
     * @return Keyframes for slide-in-right animation
     */
    public static Keyframes slideInRight() {
        return keyframes("slideInRight")
            .from(new Style<>().transform(CSS.translateX(CSSUnits.percent(100))).opacity(0))
            .to(new Style<>().transform(CSS.translateX(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from top animation.
     * Slides from -100% Y position with fade.
     * Animation name: "slideInTop"
     *
     * @return Keyframes for slide-in-top animation
     */
    public static Keyframes slideInTop() {
        return keyframes("slideInTop")
            .from(new Style<>().transform(CSS.translateY(CSSUnits.percent(-100))).opacity(0))
            .to(new Style<>().transform(CSS.translateY(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a slide-in from bottom animation.
     * Slides from +100% Y position with fade.
     * Animation name: "slideInBottom"
     *
     * @return Keyframes for slide-in-bottom animation
     */
    public static Keyframes slideInBottom() {
        return keyframes("slideInBottom")
            .from(new Style<>().transform(CSS.translateY(CSSUnits.percent(100))).opacity(0))
            .to(new Style<>().transform(CSS.translateY(CSSUnits.zero)).opacity(1));
    }

    /**
     * Creates a pulse animation (subtle scale up/down).
     * Scales from 1 to 1.05 and back.
     * Animation name: "pulse"
     *
     * <p>Usage:</p>
     * <pre>
     * style().animation(anim("pulse"), s(1), ease).animationIterationCount(infinite)
     * </pre>
     *
     * @return Keyframes for pulse animation
     */
    public static Keyframes pulse() {
        return keyframes("pulse")
            .at(0, new Style<>().transform(CSS.scale(1)))
            .at(50, new Style<>().transform(CSS.scale(1.05)))
            .at(100, new Style<>().transform(CSS.scale(1)));
    }

    /**
     * Creates a bounce animation (vertical bouncing).
     * Animation name: "bounce"
     *
     * @return Keyframes for bounce animation
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
     * Creates a shake animation (horizontal shaking).
     * Animation name: "shake"
     *
     * @return Keyframes for shake animation
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
     * Creates a spin animation (360 degree rotation).
     * Animation name: "spin"
     *
     * <p>Usage for loading spinner:</p>
     * <pre>
     * style().animation(anim("spin"), s(1), linear).animationIterationCount(infinite)
     * </pre>
     *
     * @return Keyframes for spin animation
     */
    public static Keyframes spin() {
        return keyframes("spin")
            .from(new Style<>().transform(CSS.rotate(CSSUnits.deg(0))))
            .to(new Style<>().transform(CSS.rotate(CSSUnits.deg(360))));
    }

    /**
     * Creates a zoom-in animation (scale 0 to 1 with fade).
     * Animation name: "zoomIn"
     *
     * @return Keyframes for zoom-in animation
     */
    public static Keyframes zoomIn() {
        return keyframes("zoomIn")
            .from(new Style<>().transform(CSS.scale(0)).opacity(0))
            .to(new Style<>().transform(CSS.scale(1)).opacity(1));
    }

    /**
     * Creates a zoom-out animation (scale 1 to 0 with fade).
     * Animation name: "zoomOut"
     *
     * @return Keyframes for zoom-out animation
     */
    public static Keyframes zoomOut() {
        return keyframes("zoomOut")
            .from(new Style<>().transform(CSS.scale(1)).opacity(1))
            .to(new Style<>().transform(CSS.scale(0)).opacity(0));
    }
}
