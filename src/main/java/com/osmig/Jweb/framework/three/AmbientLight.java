package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * Even, directionless illumination (three.js {@code AmbientLight}).
 * A scene that declares no lights at all gets soft default lighting instead —
 * declare any light to take over.
 */
public class AmbientLight extends ThreeNode<AmbientLight> {

    private Double intensity;
    private String color;

    /** Light strength. three.js default: 1. */
    public AmbientLight intensity(double intensity) {
        this.intensity = intensity;
        return this;
    }

    /** Light color — any CSS color string. Default: white. */
    public AmbientLight color(String color) {
        this.color = color;
        return this;
    }

    @Override
    protected String type() {
        return "ambLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (intensity != null) map.put("intensity", num(intensity));
        if (color != null) map.put("color", color);
    }
}
