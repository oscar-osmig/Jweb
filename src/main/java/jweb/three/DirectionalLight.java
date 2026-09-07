package jweb.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * Sun-like light shining from its position toward the origin (three.js
 * {@code DirectionalLight}). Positioned at {@code (3, 5, 2)} unless placed —
 * an angled key light that gives shapes visible depth.
 */
public class DirectionalLight extends ThreeNode<DirectionalLight> {

    private Double intensity;
    private String color;
    private boolean shadows;

    /** Light strength. three.js default: 1. */
    public DirectionalLight intensity(double intensity) {
        this.intensity = intensity;
        return this;
    }

    /** Light color — any CSS color string. Default: white. */
    public DirectionalLight color(String color) {
        this.color = color;
        return this;
    }

    /** Light color from a typed CSS value. */
    public DirectionalLight color(CSSValue color) {
        return color(color.css());
    }

    /**
     * Makes this light cast shadows. One call enables the whole pipeline:
     * the renderer's shadow map, this light's shadow camera, and casting/
     * receiving on every mesh in the scene.
     */
    public DirectionalLight shadows() {
        this.shadows = true;
        return this;
    }

    @Override
    protected String type() {
        return "dirLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (intensity != null) map.put("intensity", num(intensity));
        if (color != null) map.put("color", color);
        if (shadows) map.put("shadows", true);
    }
}
