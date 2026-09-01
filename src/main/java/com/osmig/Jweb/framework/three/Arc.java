package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A partial torus — an arch, a rib, a hoop that doesn't close (three.js
 * {@code TorusGeometry} with a sweep angle). The sweep starts at the ring's
 * +x and runs counter-clockwise; rotate the node to hang it the way you
 * want. A round doorway arch is:
 *
 * <pre>{@code
 * arc(1.5, 0.08, 180)          // half a ring, 1.5 radius, 0.08 thick
 *     .position(0, 2.2, 0)     // spring line
 * }</pre>
 *
 * <p>Squash it into an elliptical vault with {@code .scale(1, 0.7, 1)}.</p>
 */
public class Arc extends MeshNode<Arc> {

    private final double radius;
    private final double tube;
    private final double sweepDeg;

    Arc(double radius, double tube, double sweepDeg) {
        this.radius = radius;
        this.tube = tube;
        this.sweepDeg = sweepDeg;
    }

    @Override
    protected String type() {
        return "arc";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("radius", num(radius));
        map.put("tube", num(tube));
        map.put("sweep", num(sweepDeg));
        super.fill(map);
    }
}
