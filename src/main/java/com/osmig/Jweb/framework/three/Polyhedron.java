package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * The platonic solids (three.js {@code Tetrahedron/Octahedron/Dodecahedron/
 * IcosahedronGeometry}) — one class, four factories on {@link Three}.
 * Radius 1 unless sized; faceted shading comes from the geometry itself.
 *
 * <pre>{@code
 * icosahedron().color("#22d3ee").wireframe().spin()
 * dodecahedron(1.5).metalness(0.8).roughness(0.3)
 * }</pre>
 */
public class Polyhedron extends MeshNode<Polyhedron> {

    private final String kind;
    private Double radius;

    Polyhedron(String kind) {
        this.kind = kind;
    }

    /** Radius of the circumscribed sphere. Default 1. */
    public Polyhedron radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    protected String type() {
        return kind;
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        super.fill(map);
    }
}
