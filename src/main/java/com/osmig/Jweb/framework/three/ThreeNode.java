package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.events.EventRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base of every node in a {@link Three#scene} graph — meshes, groups, models,
 * lights and the camera all share this surface, so builder chains never
 * dead-end.
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
    private double[] spin;
    private double[] floating;
    private Double hoverScale;
    private String clickHandlerId;
    private String clickActionId;
    private String[] clickSwap;

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

    // ==================== Animation presets ====================

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

    /** Gentle vertical hover: ±0.25 units, one cycle every 2.5 seconds. */
    public SELF float_() {
        return float_(0.25, 0.4);
    }

    /** Vertical hover of the given amplitude (scene units) at the default pace. */
    public SELF float_(double amplitude) {
        return float_(amplitude, 0.4);
    }

    /** Vertical hover: amplitude in scene units, speed in cycles per second. */
    public SELF float_(double amplitude, double cyclesPerSec) {
        this.floating = new double[]{amplitude, cyclesPerSec};
        return self();
    }

    /**
     * Grows the object while the pointer is over it (raycast, entirely
     * client-side) — instant affordance for interactive scenes.
     *
     * <pre>{@code
     * box().hoverScale(1.15).onClick(e -> ...)
     * }</pre>
     *
     * @param factor the scale multiplier while hovered (e.g. 1.15)
     */
    public SELF hoverScale(double factor) {
        this.hoverScale = factor;
        return self();
    }

    // ==================== Events ====================

    /**
     * Runs a server-side handler when the object is clicked — the same
     * event pipeline as {@code attrs().onClick(...)} on any element, reached
     * by raycasting. {@code event.value()} carries the node's {@code name}.
     *
     * <pre>{@code
     * box().name("die").onClick(e -> rolls.set(rolls.get() + 1))
     * }</pre>
     */
    public SELF onClick(Consumer<Event> handler) {
        this.clickHandlerId = EventRegistry.register("click", handler).getId();
        return self();
    }

    /**
     * Runs a client-side Actions-DSL handler when the object is clicked —
     * no server round-trip, and CSP-safe like {@code attrs().onClick(Action)}:
     * the JS ships in the page's nonce-stamped definitions script and the
     * interpreter dispatches it by id after the raycast.
     *
     * <pre>{@code
     * sphere().onClick(jweb.Actions.toggle("info-panel"))
     * }</pre>
     *
     * <p>Requires a page render (a scene has no inline fallback to offer);
     * outside one the handler is dropped with nothing to deliver it.</p>
     */
    public SELF onClick(com.osmig.Jweb.framework.js.Actions.Action action) {
        this.clickActionId = com.osmig.Jweb.framework.js.ClientActions.register(action.inline());
        return self();
    }

    /**
     * Fetches a fragment and swaps it into a target when the object is
     * clicked — the 3D counterpart of {@code attrs().swap(url, target)}.
     *
     * <pre>{@code
     * model("/assets/rocket.glb").clickSwap("/api/rocket-details", "#panel")
     * }</pre>
     */
    public SELF clickSwap(String url, String targetSelector) {
        this.clickSwap = new String[]{url, targetSelector};
        return self();
    }

    // ==================== Serialization ====================

    /** Serializes this node (and only the properties that were set). */
    public final Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("t", type());
        if (name != null) map.put("name", name);
        if (position != null) map.put("pos", vec(position));
        if (rotation != null) map.put("rot", vec(rotation));
        if (scale != null) map.put("scl", vec(scale));
        if (spin != null) map.put("spin", vec(spin));
        if (floating != null) map.put("float", vec(floating));
        if (hoverScale != null) map.put("hovScale", num(hoverScale));
        if (clickHandlerId != null) map.put("click", clickHandlerId);
        if (clickActionId != null) map.put("clickAct", clickActionId);
        if (clickSwap != null) map.put("swap", Map.of("url", clickSwap[0], "target", clickSwap[1]));
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
