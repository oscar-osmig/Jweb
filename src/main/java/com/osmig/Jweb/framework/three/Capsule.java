package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A capsule mesh (three.js {@code CapsuleGeometry}) — a cylinder with
 * hemispherical caps. Radius 1, middle length 1 unless sized.
 *
 * <pre>{@code
 * capsule().color("#f59e0b")
 * capsule(0.4, 1.5).rotation(0, 0, 90)     // a lying pill
 * }</pre>
 */
public class Capsule extends MeshNode<Capsule> {

    private Double radius;
    private Double length;

    /** Cap and body radius. Default 1. */
    public Capsule radius(double radius) {
        this.radius = radius;
        return this;
    }

    /** Length of the straight middle section (caps add {@code 2 × radius}). Default 1. */
    public Capsule length(double length) {
        this.length = length;
        return this;
    }

    @Override
    protected String type() {
        return "capsule";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        if (length != null) map.put("length", num(length));
        super.fill(map);
    }
}
