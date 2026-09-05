package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A flat rectangle (three.js {@code PlaneGeometry}), facing the viewer until
 * rotated. Rendered double-sided so it never vanishes from behind. A ground
 * plane is {@code plane(20, 20).rotation(-90, 0, 0)}.
 */
public class Plane extends MeshNode<Plane> {

    private double[] size;
    private boolean mirror;

    /** Width (x) and height (y) in scene units. Default 1×1. */
    public Plane size(double width, double height) {
        this.size = new double[]{width, height};
        return this;
    }

    /**
     * Lays the plane flat on the ground (rotation −90° about x), so a floor is
     * {@code plane(20, 20).flat()} rather than a rotation to remember.
     */
    public Plane flat() {
        return rotation(-90, 0, 0);
    }

    /**
     * Turns the plane into a real-time mirror (three.js {@code Reflector}) —
     * a polished floor is {@code plane(20, 20).rotation(-90, 0, 0).mirror()}.
     * {@code .color(...)} tints the reflection (darker = dimmer polish);
     * other material properties don't apply to a mirror. For a satin finish,
     * lay a translucent plane just above it.
     */
    public Plane mirror() {
        this.mirror = true;
        return this;
    }

    @Override
    protected String type() {
        return "plane";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (size != null) map.put("size", vec(size));
        if (mirror) map.put("mirror", true);
        super.fill(map);
    }
}
