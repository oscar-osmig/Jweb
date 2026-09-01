package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A filled flat disc (three.js {@code CircleGeometry}), double-sided like
 * {@link Plane}. Radius 1 unless sized. Rotate -90° on x for a ground disc.
 *
 * <p>Named {@code disc} (not {@code circle}) so it can sit next to the SVG
 * {@code circle(...)} element under dual wildcard imports.</p>
 *
 * <pre>{@code
 * disc(3).rotation(-90, 0, 0).color("#1e293b")     // a round stage floor
 * }</pre>
 */
public class Disc extends MeshNode<Disc> {

    private Double radius;

    /** The disc's radius. Default 1. */
    public Disc radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    protected String type() {
        return "disc";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        super.fill(map);
    }
}
