package com.osmig.Jweb.framework.three;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A group of nodes sharing one transform (three.js {@code Group}) — position,
 * rotation, scale and animation presets apply to all children together:
 *
 * <pre>{@code
 * group(
 *     box().color("#6366f1"),
 *     sphere().radius(0.4).position(0, 1, 0)
 * ).position(2, 0, 0).spin(15)
 * }</pre>
 */
public class Group extends ThreeNode<Group> {

    private final List<ThreeNode<?>> children = new ArrayList<>();
    private boolean instanced;

    Group(ThreeNode<?>... children) {
        for (ThreeNode<?> child : children) {
            if (child != null) this.children.add(child);
        }
    }

    /** Adds more children to the group. */
    public Group add(ThreeNode<?>... children) {
        for (ThreeNode<?> child : children) {
            if (child != null) this.children.add(child);
        }
        return this;
    }

    /**
     * Draws the group's shapes as GPU instances — one draw call per distinct
     * geometry-and-material, whatever the count. A colonnade of 400 columns
     * or a field of 2,000 stones costs a handful of calls:
     *
     * <pre>{@code
     * repeat(400, i -> cylinder(0.1, 2).color("#bbb").position(...)).instanced()
     * }</pre>
     *
     * <p>Members give up individuality: they can't be clicked, hovered,
     * named or animated on their own (a member carrying {@code spin},
     * {@code float_}, {@code onClick}, hover or {@code name} falls back to a
     * normal mesh, with a console warning). Per-member {@code color} is
     * fine. The group itself still moves, spins and takes clicks as a unit.</p>
     */
    public Group instanced() {
        this.instanced = true;
        return this;
    }

    @Override
    protected String type() {
        return "group";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        List<Map<String, Object>> serialized = new ArrayList<>(children.size());
        for (ThreeNode<?> child : children) serialized.add(child.toMap());
        map.put("children", serialized);
        if (instanced) map.put("inst", true);
    }
}
