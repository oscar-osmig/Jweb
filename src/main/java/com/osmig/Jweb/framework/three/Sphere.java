package com.osmig.Jweb.framework.three;

import java.util.Map;

/** A sphere mesh (three.js {@code SphereGeometry}). Radius 1 unless sized. */
public class Sphere extends MeshNode<Sphere> {

    private Double radius;

    /** The sphere's radius in scene units. Default 1. */
    public Sphere radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    protected String type() {
        return "sphere";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        super.fill(map);
    }
}
