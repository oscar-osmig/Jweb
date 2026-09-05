package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * A cone of light from its position toward a target (three.js
 * {@code SpotLight}) — a stage lamp, a desk light, a street light. Placed at
 * {@code (2, 4, 2)} unless positioned, and aimed straight down unless
 * {@link #target} says otherwise:
 *
 * <pre>{@code
 * spotLight(40).position(0, 6, 0).angle(25).shadows()      // a pool of light
 * spotLight(30).position(-4, 5, 2).target(0, 1, 0)         // aimed at the subject
 * }</pre>
 */
public class SpotLight extends ThreeNode<SpotLight> {

    private Double intensity;
    private String color;
    private double[] target;
    private Double angle;
    private Double penumbra;
    private boolean shadows;

    /** Light strength. three.js default: 1 — a real lamp a few units away wants 20–60. */
    public SpotLight intensity(double intensity) {
        this.intensity = intensity;
        return this;
    }

    /** Light color — any CSS color string. Default: white. */
    public SpotLight color(String color) {
        this.color = color;
        return this;
    }

    /** Light color from a typed CSS value. */
    public SpotLight color(CSSValue color) {
        return color(color.css());
    }

    /** The point the cone aims at, in scene units. Default: straight down. */
    public SpotLight target(double x, double y, double z) {
        this.target = new double[]{x, y, z};
        return this;
    }

    /** The cone's half-angle in degrees (default 30; 90 is the widest). */
    public SpotLight angle(double degrees) {
        if (degrees <= 0 || degrees > 90) {
            throw new IllegalArgumentException("angle() must be within 0–90 degrees — got " + degrees);
        }
        this.angle = degrees;
        return this;
    }

    /** Edge softness, 0 (hard rim) to 1 (all falloff). Default 0.3. */
    public SpotLight penumbra(double penumbra) {
        if (penumbra < 0 || penumbra > 1) {
            throw new IllegalArgumentException("penumbra() must be within 0–1 — got " + penumbra);
        }
        this.penumbra = penumbra;
        return this;
    }

    /**
     * Makes this light cast shadows. One call enables the whole pipeline:
     * the renderer's shadow map, this light's shadow camera, and casting/
     * receiving on every mesh in the scene.
     */
    public SpotLight shadows() {
        this.shadows = true;
        return this;
    }

    @Override
    protected String type() {
        return "spotLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (intensity != null) map.put("intensity", num(intensity));
        if (color != null) map.put("color", color);
        if (target != null) map.put("target", vec(target));
        if (angle != null) map.put("angle", num(angle));
        if (penumbra != null) map.put("penumbra", num(penumbra));
        if (shadows) map.put("shadows", true);
    }
}
