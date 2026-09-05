package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A flat annulus (three.js {@code RingGeometry}), double-sided. Inner radius
 * 0.5, outer radius 1 unless sized. For a 3D donut, use {@code torus()}.
 *
 * <pre>{@code
 * ring(0.8, 1).color("#38bdf8").spin(0, 0, 45)     // a spinning halo
 * }</pre>
 */
public class Ring extends MeshNode<Ring> {

    private double[] radii;

    /** Inner and outer radius. Defaults 0.5 and 1. */
    public Ring radii(double inner, double outer) {
        this.radii = new double[]{inner, outer};
        return this;
    }

    /** Lays the ring flat on the ground (rotation −90° about x). */
    public Ring flat() {
        return rotation(-90, 0, 0);
    }

    @Override
    protected String type() {
        return "ring";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radii != null) map.put("radii", vec(radii));
        super.fill(map);
    }
}
