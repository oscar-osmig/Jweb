package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * A bulb-like light radiating in every direction from its position (three.js
 * {@code PointLight}). Positioned at {@code (2, 3, 2)} unless placed.
 */
public class PointLight extends ThreeNode<PointLight> {

    private Double intensity;
    private String color;
    private boolean shadows;

    /** Light strength. three.js default: 1. */
    public PointLight intensity(double intensity) {
        this.intensity = intensity;
        return this;
    }

    /** Light color — any CSS color string. Default: white. */
    public PointLight color(String color) {
        this.color = color;
        return this;
    }

    /** Light color from a typed CSS value. */
    public PointLight color(CSSValue color) {
        return color(color.css());
    }

    /**
     * Makes this light cast shadows. One call enables the whole pipeline:
     * the renderer's shadow map, this light's shadow camera, and casting/
     * receiving on every mesh in the scene.
     */
    public PointLight shadows() {
        this.shadows = true;
        return this;
    }

    @Override
    protected String type() {
        return "pointLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (intensity != null) map.put("intensity", num(intensity));
        if (color != null) map.put("color", color);
        if (shadows) map.put("shadows", true);
    }
}
