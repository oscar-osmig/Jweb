package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A live update to a rendered scene, built inside a server event handler via
 * {@link Three#patch(String)} and delivered on the WebSocket answer already
 * in flight — the scene mutates in place, the visitor doesn't move:
 *
 * <pre>{@code
 * Three.patch("world")
 *      .node("vane").rotation(0, 24, 0).tween(600)
 *      .node("veil").emissive("#1E5D50")
 *      .camera().lookAt(0, 3, -6).tween(1200)
 * }</pre>
 *
 * <p>Targets are nodes that carry {@code .name(...)} in the scene. Building
 * the patch queues it; there is nothing to send. {@code tween(ms)} eases the
 * <em>current</em> target's changes over that many milliseconds (position,
 * rotation, scale, colors, opacity); without it they apply instantly.
 * Angles are degrees, like everywhere in the DSL.</p>
 */
public final class ThreePatch {

    private final String sceneId;
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private Map<String, Object> camera;
    private Map<String, Object> current;

    ThreePatch(String sceneId) {
        this.sceneId = sceneId;
        if (!ThreePatchQueue.offer(this)) {
            Log.warn("Three.patch(\"{}\") outside a live event handler — "
                + "there is no page in flight to deliver it to; dropped", sceneId);
        }
    }

    /** Starts (or reopens) the patch for the named node. */
    public ThreePatch node(String name) {
        current = new LinkedHashMap<>();
        current.put("name", name);
        nodes.add(current);
        return this;
    }

    /** Starts the patch for the scene's camera. */
    public ThreePatch camera() {
        if (camera == null) camera = new LinkedHashMap<>();
        current = camera;
        return this;
    }

    /** Moves the target to the position, in scene units. */
    public ThreePatch position(double x, double y, double z) {
        return put("pos", ThreeNode.vec(new double[]{x, y, z}));
    }

    /** Rotates the target to the given angles, in degrees. */
    public ThreePatch rotation(double xDeg, double yDeg, double zDeg) {
        return put("rot", ThreeNode.vec(new double[]{xDeg, yDeg, zDeg}));
    }

    /** Uniform scale factor. */
    public ThreePatch scale(double factor) {
        return scale(factor, factor, factor);
    }

    /** Per-axis scale factors. */
    public ThreePatch scale(double x, double y, double z) {
        return put("scl", ThreeNode.vec(new double[]{x, y, z}));
    }

    /** Material color (meshes) or light color. */
    public ThreePatch color(String color) {
        return put("color", color);
    }

    /** Material or light color from the CSS DSL's palette. */
    public ThreePatch color(jweb.CSSValue color) {
        return color(color.css());
    }

    /** Emissive (self-lit) color — the glow channel. */
    public ThreePatch emissive(String color) {
        return put("emissive", color);
    }

    /** Emissive color from a typed CSS value. */
    public ThreePatch emissive(jweb.CSSValue color) {
        return emissive(color.css());
    }

    /** Opacity 0–1 (mesh materials). */
    public ThreePatch opacity(double opacity) {
        return put("opacity", ThreeNode.num(opacity));
    }

    /** Light intensity (lights only). */
    public ThreePatch intensity(double intensity) {
        return put("intensity", ThreeNode.num(intensity));
    }

    /** Shows or hides the target. */
    public ThreePatch visible(boolean visible) {
        return put("visible", visible);
    }

    /** Where the camera looks (camera target only). */
    public ThreePatch lookAt(double x, double y, double z) {
        return put("look", ThreeNode.vec(new double[]{x, y, z}));
    }

    /** Eases the current target's changes over the given milliseconds. */
    public ThreePatch tween(int millis) {
        return put("tween", millis);
    }

    private ThreePatch put(String key, Object value) {
        if (current == null) {
            throw new IllegalStateException(
                "Three.patch: call .node(\"<name>\") or .camera() before setting properties");
        }
        current.put(key, value);
        return this;
    }

    // ==================== Delivery (framework-internal) ====================

    /** The target scene element's id. */
    public String sceneId() {
        return sceneId;
    }

    /** The node updates, serialized. Empty if only the camera moved. */
    public List<Map<String, Object>> nodeMaps() {
        return nodes;
    }

    /** The camera update, serialized — or null if untouched. */
    public Map<String, Object> cameraMap() {
        return camera;
    }
}
