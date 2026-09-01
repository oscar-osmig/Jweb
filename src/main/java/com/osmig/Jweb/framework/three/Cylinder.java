package com.osmig.Jweb.framework.three;

import java.util.Map;

/** A cylinder mesh (three.js {@code CylinderGeometry}). Radius 1, height 1 unless sized. */
public class Cylinder extends MeshNode<Cylinder> {

    private Double radius;
    private double[] radii;
    private Double height;

    /** The cylinder's radius. Default 1. */
    public Cylinder radius(double radius) {
        this.radius = radius;
        return this;
    }

    /** Different top and bottom radii — a truncated cone. */
    public Cylinder radii(double top, double bottom) {
        this.radii = new double[]{top, bottom};
        return this;
    }

    /** The cylinder's height. Default 1. */
    public Cylinder height(double height) {
        this.height = height;
        return this;
    }

    @Override
    protected String type() {
        return "cylinder";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (radius != null) map.put("radius", num(radius));
        if (radii != null) map.put("radii", vec(radii));
        if (height != null) map.put("height", num(height));
        super.fill(map);
    }
}
