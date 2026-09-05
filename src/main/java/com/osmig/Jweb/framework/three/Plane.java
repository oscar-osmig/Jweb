package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A flat rectangle (three.js {@code PlaneGeometry}), facing the viewer until
 * rotated. Rendered double-sided so it never vanishes from behind. A ground
 * plane is {@code plane(20, 20).rotation(-90, 0, 0)}.
 */
public class Plane extends MeshNode<Plane> {

    private double[] size;
    private Double mirror;

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
     * a polished floor is {@code plane(20, 20).flat().mirror()}.
     * {@code .color(...)} tints the reflection (darker = dimmer polish).
     * For a satin finish use {@link #mirror(double)}.
     */
    public Plane mirror() {
        this.mirror = 1.0;
        return this;
    }

    /**
     * A mirror of the given strength, 0–1: {@code 1} is chrome-sharp,
     * {@code 0.4} a satin polished floor — the reflection shows through a
     * surface in the plane's {@code color} and {@code roughness}.
     */
    public Plane mirror(double strength) {
        if (strength < 0 || strength > 1) {
            throw new IllegalArgumentException("mirror() strength must be within 0–1 — got " + strength);
        }
        this.mirror = strength;
        return this;
    }

    @Override
    protected String type() {
        return "plane";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (size != null) map.put("size", vec(size));
        // the no-arg form stays `true` so older runtimes/JSON keep working;
        // a strength serializes as the number (1 = chrome, <1 = satin)
        if (mirror != null) map.put("mirror", mirror == 1.0 ? Boolean.TRUE : num(mirror));
        super.fill(map);
    }
}
