package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * A text label that always faces the camera (a three.js {@code Sprite} with a
 * canvas-rendered texture) — annotate scenes without loading any font file.
 *
 * <p>Size it with {@link #size(double)} (its height in scene units — width
 * follows the text); {@code scale(...)} does not apply to billboards. All
 * transform, animation and click surface from {@link ThreeNode} works:
 * {@code position}, {@code float_()}, {@code onClick(...)}.</p>
 *
 * <pre>{@code
 * billboard("Sun").position(0, 1.6, 0).color("#fde68a")
 * billboard("Click me").background("rgba(15,23,42,0.85)").onClick(e -> ...)
 * }</pre>
 */
public class Billboard extends ThreeNode<Billboard> {

    private final String text;
    private String color;
    private String background;
    private Double size;

    Billboard(String text) {
        this.text = text;
    }

    /** Text color — any CSS color. Default white. */
    public Billboard color(String color) {
        this.color = color;
        return this;
    }

    /** Text color from a typed CSS value. */
    public Billboard color(CSSValue color) {
        return color(color.css());
    }

    /** A rounded pill behind the text — any CSS color. Default none. */
    public Billboard background(String color) {
        this.background = color;
        return this;
    }

    /** Pill background from a typed CSS value. */
    public Billboard background(CSSValue color) {
        return background(color.css());
    }

    /** The label's height in scene units. Default 0.5. */
    public Billboard size(double height) {
        this.size = height;
        return this;
    }

    @Override
    protected String type() {
        return "label";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("text", text);
        if (color != null) map.put("color", color);
        if (background != null) map.put("bg", background);
        if (size != null) map.put("size", num(size));
    }
}
