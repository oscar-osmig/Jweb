package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A surface of revolution: a 2D profile of radius,height pairs spun around
 * the y axis (three.js {@code LatheGeometry}) — pots, vases, columns, domes
 * and bells from the silhouette alone:
 *
 * <pre>{@code
 * lathe(0.0, 0,      // closed at the base
 *       0.5, 0,      // out to the foot
 *       0.35, 0.8,   // waist
 *       0.55, 1.1,   // shoulder
 *       0.3, 1.3)    // lip
 * }</pre>
 *
 * <p>The profile runs bottom-up; start or end a pair at radius 0 to close
 * that end. Rendered double-sided, so open vessels read from inside too.</p>
 */
public class Lathe extends MeshNode<Lathe> {

    private final double[] profile;
    private Integer segments;

    Lathe(double[] profile) {
        if (profile.length < 4 || profile.length % 2 != 0) {
            throw new IllegalArgumentException(
                "lathe() needs radius,height pairs for at least 2 points — got "
                + profile.length + " values");
        }
        this.profile = profile;
    }

    /** Radial resolution (default 32). */
    public Lathe segments(int segments) {
        this.segments = segments;
        return this;
    }

    @Override
    protected String type() {
        return "lathe";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("profile", vec(profile));
        if (segments != null) map.put("seg", segments);
        super.fill(map);
    }
}
