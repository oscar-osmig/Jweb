package com.osmig.Jweb.framework.three;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base of every node in a {@link Three#scene} graph — meshes, lights and the
 * camera all share this surface, so builder chains never dead-end.
 *
 * <p>All angles in the Three DSL are <b>degrees</b> (matching the CSS DSL);
 * the client runtime converts to radians. Only values you set are serialized,
 * so scene descriptions stay compact and three.js defaults apply otherwise.</p>
 *
 * @param <SELF> the concrete node type, so chained calls keep their exact type
 */
public abstract class ThreeNode<SELF extends ThreeNode<SELF>> {

    private String name;
    private double[] position;
    private double[] rotation;
    private double[] scale;

    /** The node's type tag in the serialized scene graph (e.g. {@code "box"}). */
    protected abstract String type();

    /** Subclasses add their own properties to the serialized form here. */
    protected abstract void fill(Map<String, Object> map);

    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }

    /** Names the object so scripts can reach it via {@code JWebThree.get(id).objects[name]}. */
    public SELF name(String name) {
        this.name = name;
        return self();
    }

    /** Position in scene units: x right, y up, z toward the viewer. */
    public SELF position(double x, double y, double z) {
        this.position = new double[]{x, y, z};
        return self();
    }

    /** Rotation around each axis, in degrees. */
    public SELF rotation(double xDeg, double yDeg, double zDeg) {
        this.rotation = new double[]{xDeg, yDeg, zDeg};
        return self();
    }

    /** Uniform scale factor. */
    public SELF scale(double factor) {
        return scale(factor, factor, factor);
    }

    /** Per-axis scale factors. */
    public SELF scale(double x, double y, double z) {
        this.scale = new double[]{x, y, z};
        return self();
    }

    /** Serializes this node (and only the properties that were set). */
    public final Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("t", type());
        if (name != null) map.put("name", name);
        if (position != null) map.put("pos", vec(position));
        if (rotation != null) map.put("rot", vec(rotation));
        if (scale != null) map.put("scl", vec(scale));
        fill(map);
        return map;
    }

    /** Serializes a vector, using whole numbers where possible ({@code 1} not {@code 1.0}). */
    static List<Number> vec(double[] v) {
        List<Number> list = new ArrayList<>(v.length);
        for (double d : v) list.add(num(d));
        return list;
    }

    /** Narrows a whole-valued double to a long so JSON stays compact. */
    static Number num(double d) {
        // if/return, not a ternary — the conditional operator would promote
        // the long branch back to double (JLS 15.25) and undo the narrowing
        if (d == Math.rint(d) && !Double.isInfinite(d)) return (long) d;
        return d;
    }
}
