package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A torus knot (three.js {@code TorusKnotGeometry}) — the classic 3D
 * showpiece shape. Radius 1, tube thickness 0.4 unless sized.
 *
 * <pre>{@code
 * torusKnot().color("#a855f7").metalness(0.6).roughness(0.2).spin()
 * }</pre>
 */
public class TorusKnot extends MeshNode<TorusKnot> {

    private Double radius;
    private Double tube;

    /** Radius of the knot as a whole. Default 1. */
    public TorusKnot radius(double radius) {
        this.radius = radius;
        return this;
    }

    /** Thickness of the tube the knot is tied from. Default 0.4. */
    public TorusKnot tube(double tube) {
        this.tube = tube;
        return this;
    }

    @Override
    protected String type() {
        return "knot";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        if (tube != null) map.put("tube", num(tube));
        super.fill(map);
    }
}
