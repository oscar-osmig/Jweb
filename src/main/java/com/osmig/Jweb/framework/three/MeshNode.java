package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * Base of every visible object in a scene. Adds the material surface
 * (three.js {@code MeshStandardMaterial} — physically based, reacts to
 * lights) and the {@code spin} animation preset.
 *
 * @param <SELF> the concrete mesh type, so chained calls keep their exact type
 */
public abstract class MeshNode<SELF extends MeshNode<SELF>> extends ThreeNode<SELF> {

    private String color;
    private Double metalness;
    private Double roughness;
    private double[] spin;

    /** Material color — any CSS color string ({@code "#10b981"}, {@code "coral"}). */
    public SELF color(String color) {
        this.color = color;
        return self();
    }

    /** How metallic the surface is, 0 (dielectric) to 1 (metal). three.js default: 0. */
    public SELF metalness(double metalness) {
        this.metalness = metalness;
        return self();
    }

    /** How rough the surface is, 0 (mirror) to 1 (diffuse). three.js default: 1. */
    public SELF roughness(double roughness) {
        this.roughness = roughness;
        return self();
    }

    /** Continuous rotation at a pleasant default rate (20°/s x, 30°/s y). */
    public SELF spin() {
        return spin(20, 30, 0);
    }

    /** Continuous rotation around the y axis, in degrees per second. */
    public SELF spin(double yDegPerSec) {
        return spin(0, yDegPerSec, 0);
    }

    /** Continuous rotation around each axis, in degrees per second. */
    public SELF spin(double xDegPerSec, double yDegPerSec, double zDegPerSec) {
        this.spin = new double[]{xDegPerSec, yDegPerSec, zDegPerSec};
        return self();
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (color != null) map.put("color", color);
        if (metalness != null) map.put("metal", num(metalness));
        if (roughness != null) map.put("rough", num(roughness));
        if (spin != null) map.put("spin", vec(spin));
    }
}
