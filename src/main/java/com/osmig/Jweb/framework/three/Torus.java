package com.osmig.Jweb.framework.three;

import java.util.Map;

/** A ring mesh (three.js {@code TorusGeometry}). Radius 1, tube 0.4 unless sized. */
public class Torus extends MeshNode<Torus> {

    private Double radius;
    private Double tube;

    /** Distance from the center to the middle of the tube. Default 1. */
    public Torus radius(double radius) {
        this.radius = radius;
        return this;
    }

    /** The tube's thickness radius. Default 0.4. */
    public Torus tube(double tube) {
        this.tube = tube;
        return this;
    }

    @Override
    protected String type() {
        return "torus";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        if (tube != null) map.put("tube", num(tube));
        super.fill(map);
    }
}
