package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A rectangular profile swept along a smooth curve through x,y,z points
 * (three.js {@code ExtrudeGeometry} over a Catmull-Rom spline) — moldings,
 * ribs, rails, gutters, cornices; the square-section cousin of {@code tube}:
 *
 * <pre>{@code
 * sweep(0.12, 0.06,            // profile: width × height
 *     -3, 0, 0,
 *      0, 2.2, 0,
 *      3, 0, 0)                // one barrel-vault rib, not 216 boxes
 * }</pre>
 *
 * <p>The profile's width lies across the curve and its height along the
 * curve's normal; the curve passes through every point. {@link #closed()}
 * joins it into a loop; {@link #steps(int)} sets the resolution along it.</p>
 */
public class Sweep extends MeshNode<Sweep> {

    private final double width;
    private final double height;
    private final double[] points;
    private boolean closed;
    private Integer steps;

    Sweep(double width, double height, double[] points) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "sweep() needs a positive profile width and height — got "
                + width + " × " + height);
        }
        if (points.length < 6 || points.length % 3 != 0) {
            throw new IllegalArgumentException(
                "sweep() needs x,y,z triples for at least 2 points — got "
                + points.length + " values");
        }
        this.width = width;
        this.height = height;
        this.points = points;
    }

    /** Joins the curve into a loop (last point back to the first). */
    public Sweep closed() {
        this.closed = true;
        return this;
    }

    /** Segments along the curve. Default: {@code max(24, points × 8)}. */
    public Sweep steps(int steps) {
        if (steps < 1) throw new IllegalArgumentException("steps() must be positive — got " + steps);
        this.steps = steps;
        return this;
    }

    @Override
    protected String type() {
        return "sweep";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("profile", vec(new double[]{width, height}));
        map.put("pts", vec(points));
        if (closed) map.put("closed", true);
        if (steps != null) map.put("steps", steps);
        super.fill(map);
    }
}
