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
 *
 * <p>The scene's structure changes the same way: {@link #add}, {@link #addTo},
 * {@link #remove} and {@link #replace} build or dispose whole nodes in place.
 * Removes always apply before additions, and property patches after both —
 * so one patch can add a named node and tween it.</p>
 */
public final class ThreePatch {

    private final String sceneId;
    private final List<Map<String, Object>> nodes = new ArrayList<>();
    private final List<Map<String, Object>> adds = new ArrayList<>();
    private final List<String> removes = new ArrayList<>();
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

    /** Volume 0–1 (sounds); eases with {@link #tween}. */
    public ThreePatch volume(double volume) {
        return put("vol", ThreeNode.num(volume));
    }

    /** Starts a sound from the beginning (once the visitor's first gesture has unlocked audio). */
    public ThreePatch play() {
        return put("play", true);
    }

    /** Stops a sound. */
    public ThreePatch stop() {
        return put("play", false);
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

    // ==================== Structure ====================

    /**
     * Adds nodes to the live scene's root — built exactly as they would be
     * in {@code scene(...)}, presets, clicks and all. Give them a
     * {@code .name(...)} to patch or remove them later.
     *
     * <pre>{@code
     * Three.patch("hall").add(sphere(0.3).name("orb").emissive("#5FA98A").appear(400))
     * }</pre>
     */
    public ThreePatch add(ThreeNode<?>... nodes) {
        for (ThreeNode<?> node : nodes) {
            if (node != null) adds.add(entry(null, null, node.toMap()));
        }
        current = null;
        return this;
    }

    /** Adds nodes inside the named group, sharing its transform and presets. */
    public ThreePatch addTo(String groupName, ThreeNode<?>... nodes) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("addTo(groupName, ...): the group name is blank");
        }
        for (ThreeNode<?> node : nodes) {
            if (node != null) adds.add(entry(groupName, null, node.toMap()));
        }
        current = null;
        return this;
    }

    /** Removes the named nodes (and everything under them), releasing their GPU resources. */
    public ThreePatch remove(String... names) {
        for (String name : names) {
            if (name != null && !name.isBlank()) removes.add(name);
        }
        current = null;
        return this;
    }

    /**
     * Swaps the named node for a new one in the same parent. The new node
     * inherits the old name unless it carries its own, so a later
     * {@code replace} or {@code node(...)} finds it again.
     */
    public ThreePatch replace(String name, ThreeNode<?> node) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("replace(name, node): the name is blank");
        }
        if (node == null) throw new IllegalArgumentException("replace(name, node): the node is null");
        Map<String, Object> map = node.toMap();
        if (map.get("name") == null) map.put("name", name);
        removes.add(name);
        adds.add(entry(null, name, map));
        current = null;
        return this;
    }

    private static Map<String, Object> entry(String into, String replaces, Map<String, Object> node) {
        Map<String, Object> e = new LinkedHashMap<>();
        if (into != null) e.put("into", into);
        if (replaces != null) e.put("replaces", replaces);
        e.put("node", node);
        return e;
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

    /**
     * Nodes to build into the live scene, in order: each entry is
     * {@code {node: {...}}} plus {@code into: groupName} or
     * {@code replaces: oldName} for the parent. Empty if none.
     */
    public List<Map<String, Object>> addMaps() {
        return adds;
    }

    /** Names of nodes to remove — applied before any additions. Empty if none. */
    public List<String> removeNames() {
        return removes;
    }
}
