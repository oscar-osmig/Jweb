package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A ground surface lying flat (y up, centered on the node's position — no
 * rotation to remember) with optional seeded, rolling hills — dunes,
 * meadows, a lawn that isn't a billiard table:
 *
 * <pre>{@code
 * terrain(60, 60).color("#5B7F4A")                 // a flat lawn
 * terrain(60, 60).hills(1.5).color("#C9B58A")      // low dunes
 * terrain(60, 60).hills(3, 12).seed(4)             // taller, broader, reshuffled
 * }</pre>
 *
 * <p>Hills are deterministic per {@link #seed}, so a re-render doesn't
 * reshape the land. Crests rise and troughs dip around the node's height by
 * about half the hill height each way. It's a normal mesh: it casts and
 * receives shadows, and takes clicks, hover and materials like any shape.</p>
 */
public class Terrain extends MeshNode<Terrain> {

    private final double width;
    private final double depth;
    private double[] hills;
    private Integer seed;
    private Integer detail;

    Terrain(double width, double depth) {
        if (width <= 0 || depth <= 0) {
            throw new IllegalArgumentException(
                "terrain() needs a positive width and depth — got " + width + " × " + depth);
        }
        this.width = width;
        this.depth = depth;
    }

    /** Rolling hills of roughly the given height, with features about width/4 across. */
    public Terrain hills(double height) {
        if (height <= 0) throw new IllegalArgumentException("hills() height must be positive — got " + height);
        this.hills = new double[]{height};
        return this;
    }

    /** Hills of the given height; {@code scale} is the feature size in scene units. */
    public Terrain hills(double height, double scale) {
        if (height <= 0 || scale <= 0) {
            throw new IllegalArgumentException(
                "hills(height, scale) must both be positive — got " + height + ", " + scale);
        }
        this.hills = new double[]{height, scale};
        return this;
    }

    /** Reseeds the hill layout. */
    public Terrain seed(int seed) {
        this.seed = seed;
        return this;
    }

    /** Mesh resolution per side (default 96, clamped to 8–256). */
    public Terrain detail(int segments) {
        this.detail = Math.max(8, Math.min(256, segments));
        return this;
    }

    @Override
    protected String type() {
        return "terrain";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("size", vec(new double[]{width, depth}));
        if (hills != null) map.put("hills", vec(hills));
        if (seed != null) map.put("seed", seed);
        if (detail != null) map.put("seg", detail);
        super.fill(map);
    }
}
