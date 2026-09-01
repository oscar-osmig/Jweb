package com.osmig.Jweb.framework.three;

import java.util.Map;

/** A cone mesh (three.js {@code ConeGeometry}). Radius 1, height 1 unless sized. */
public class Cone extends MeshNode<Cone> {

    private Double radius;
    private Double height;

    /** The base radius. Default 1. */
    public Cone radius(double radius) {
        this.radius = radius;
        return this;
    }

    /** The cone's height. Default 1. */
    public Cone height(double height) {
        this.height = height;
        return this;
    }

    @Override
    protected String type() {
        return "cone";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        if (height != null) map.put("height", num(height));
        super.fill(map);
    }
}
