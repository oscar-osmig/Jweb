package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A cloud of point particles (three.js {@code Points}) — dust motes, spores,
 * rain, snow, drifting embers — as one node and one draw call instead of a
 * hundred meshes:
 *
 * <pre>{@code
 * particles(120).color("#E7D6B1").size(0.03)
 *     .spread(8, 5, 20).position(0, 3, 0)
 *     .drift()                       // slow wander in place
 *
 * particles(400).color("#7FA8C0").size(0.02)
 *     .spread(12, 8, 12).fall(3)     // rain: falls and wraps back to the top
 * }</pre>
 *
 * <p>Positions are seeded deterministically, so a re-render doesn't reshuffle
 * the sky; pass {@link #seed} for a different arrangement. {@code drift} and
 * {@code fall} animate the cloud (and keep the scene's loop alive).</p>
 */
public class Particles extends ThreeNode<Particles> {

    private final int count;
    private String color;
    private Double size;
    private double[] spread;
    private Double drift;
    private Double fall;
    private Double opacity;
    private Integer seed;

    Particles(int count) {
        if (count < 1) throw new IllegalArgumentException("particles() needs a positive count");
        this.count = count;
    }

    /** Particle color. Default: white. */
    public Particles color(String color) {
        this.color = color;
        return this;
    }

    /** Particle color from the CSS DSL's palette. */
    public Particles color(jweb.CSSValue color) {
        this.color = color.css();
        return this;
    }

    /** Point size in scene units (shrinks with distance). Default: 0.05. */
    public Particles size(double size) {
        this.size = size;
        return this;
    }

    /** The box the particles fill, centered on the node's position. Default 10×10×10. */
    public Particles spread(double width, double height, double depth) {
        this.spread = new double[]{width, height, depth};
        return this;
    }

    /** Slow in-place wander at the default pace — dust holding the light. */
    public Particles drift() {
        return drift(1);
    }

    /** In-place wander scaled by {@code speed} (1 = subtle). */
    public Particles drift(double speed) {
        this.drift = speed;
        return this;
    }

    /** Falls at {@code unitsPerSec}, wrapping back to the top — rain, snow, spores. */
    public Particles fall(double unitsPerSec) {
        this.fall = unitsPerSec;
        return this;
    }

    /** Particle opacity, 0–1. Default 1. */
    public Particles opacity(double opacity) {
        this.opacity = opacity;
        return this;
    }

    /** Reseeds the deterministic arrangement. */
    public Particles seed(int seed) {
        this.seed = seed;
        return this;
    }

    @Override
    protected String type() {
        return "particles";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("count", count);
        if (color != null) map.put("color", color);
        if (size != null) map.put("size", num(size));
        if (spread != null) map.put("spread", vec(spread));
        if (drift != null) map.put("drift", num(drift));
        if (fall != null) map.put("fall", num(fall));
        if (opacity != null) map.put("opacity", num(opacity));
        if (seed != null) map.put("seed", seed);
    }
}
