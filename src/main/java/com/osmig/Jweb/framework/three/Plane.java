package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A flat rectangle (three.js {@code PlaneGeometry}), facing the viewer until
 * rotated. Rendered double-sided so it never vanishes from behind. A ground
 * plane is {@code plane(20, 20).rotation(-90, 0, 0)}.
 */
public class Plane extends MeshNode<Plane> {

    private double[] size;

    /** Width (x) and height (y) in scene units. Default 1×1. */
    public Plane size(double width, double height) {
        this.size = new double[]{width, height};
        return this;
    }

    @Override
    protected String type() {
        return "plane";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (size != null) map.put("size", vec(size));
        super.fill(map);
    }
}
