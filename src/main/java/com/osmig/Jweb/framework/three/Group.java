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

    @Override
    protected String type() {
        return "group";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        List<Map<String, Object>> serialized = new ArrayList<>(children.size());
        for (ThreeNode<?> child : children) serialized.add(child.toMap());
        map.put("children", serialized);
    }
}
