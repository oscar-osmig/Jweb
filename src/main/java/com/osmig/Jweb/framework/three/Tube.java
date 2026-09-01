package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A round tube swept along a smooth curve through the given points
 * (three.js {@code TubeGeometry} over a Catmull-Rom spline) — pipes, vines,
 * ribs, cables and railings without hand-segmenting the curve:
 *
 * <pre>{@code
 * tube(0.05,
 *     -2, 0, 0,
 *      0, 1.4, 0,
 *      2, 0, 0)          // one smooth rib through three points
 * }</pre>
 *
 * <p>Points are x,y,z triples in scene units; the curve passes through every
 * one. {@link #closed()} joins the last point back to the first.</p>
 */
public class Tube extends MeshNode<Tube> {

    private final double radius;
    private final double[] points;
    private boolean closed;

    Tube(double radius, double[] points) {
        if (points.length < 6 || points.length % 3 != 0) {
            throw new IllegalArgumentException(
                "tube() needs x,y,z triples for at least 2 points — got "
                + points.length + " values");
        }
        this.radius = radius;
        this.points = points;
    }

    /** Joins the curve into a loop (last point back to the first). */
    public Tube closed() {
        this.closed = true;
        return this;
    }

    @Override
    protected String type() {
        return "tube";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("radius", num(radius));
        map.put("pts", vec(points));
        if (closed) map.put("closed", true);
        super.fill(map);
    }
}
