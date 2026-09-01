package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * Sun-like light shining from its position toward the origin (three.js
 * {@code DirectionalLight}). Positioned at {@code (3, 5, 2)} unless placed —
 * an angled key light that gives shapes visible depth.
 */
public class DirectionalLight extends ThreeNode<DirectionalLight> {

    private Double intensity;
    private String color;

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

    @Override
    protected String type() {
        return "dirLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (intensity != null) map.put("intensity", num(intensity));
        if (color != null) map.put("color", color);
    }
}
