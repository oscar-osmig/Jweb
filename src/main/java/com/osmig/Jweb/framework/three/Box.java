package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A box mesh (three.js {@code BoxGeometry}). Unit cube unless sized.
 *
 * <pre>{@code
 * box().color("#10b981").spin()
 * box(2).position(0, 1, 0)
 * box(4, 0.2, 4)                 // a floor slab
 * }</pre>
 */
public class Box extends MeshNode<Box> {

    private double[] size;

    /** Side lengths: width (x), height (y), depth (z). Default 1×1×1. */
    public Box size(double width, double height, double depth) {
        this.size = new double[]{width, height, depth};
        return this;
    }

    /** Uniform side length. */
    public Box size(double side) {
        return size(side, side, side);
    }

    @Override
    protected String type() {
        return "box";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (size != null) map.put("size", vec(size));
        super.fill(map);
    }
}
