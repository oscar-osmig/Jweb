package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.util.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Declarative 3D scenes rendered with three.js — the scene graph is declared
 * the way the HTML DSL declares the DOM, and the client runtime owns all of
 * three.js's ceremony: renderer setup, sizing, the render loop and disposal.
 *
 * <pre>{@code
 * import static jweb.Three.*;
 *
 * div(class_("hero"),
 *     scene(style().height(px(420)),
 *         camera().position(0, 1.5, 4).orbit(),
 *         directionalLight().position(3, 5, 2),
 *         box().color("#10b981").spin()
 *     )
 * )
 * }</pre>
 *
 * <p>{@code scene(...)} returns a regular {@link Tag}, so every HTML
 * attribute chains on it ({@code .id("hero")}, {@code .class_(...)}). Give it
 * a height via style or class; three.js loads lazily, only on pages that
 * contain a scene. Scenes render on demand and only run an animation loop
 * when something animates. An {@code .id(...)} additionally exposes the live
 * three.js objects to scripts at {@code JWebThree.get('<id>')} —
 * {@code {scene, camera, renderer, objects}} — the escape hatch into the full
 * three.js API.</p>
 */
public class Three {

    protected Three() {}

    /**
     * A 3D scene. Mix {@link ThreeNode} items (meshes, lights, camera) with
     * regular attribute items ({@code style()}, {@code attrs()}, {@code id()})
     * for the container element, in any order — same rules as every element.
     */
    public static Tag scene(Object... items) {
        List<ThreeNode<?>> nodes = new ArrayList<>();
        List<Object> rest = new ArrayList<>();
        partition(items, nodes, rest);

        List<Map<String, Object>> serialized = new ArrayList<>(nodes.size());
        for (ThreeNode<?> node : nodes) serialized.add(node.toMap());
        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("v", 1);
        graph.put("nodes", serialized);

        return Tag.create("div", rest.toArray())
                .set("data-three", Json.stringify(graph));
    }

    /** Splits scene items from container items, descending into groups like the element DSL. */
    private static void partition(Object[] items, List<ThreeNode<?>> nodes, List<Object> rest) {
        for (Object item : items) {
            if (item == null) continue;
            if (item instanceof ThreeNode<?> node) {
                nodes.add(node);
            } else if (item instanceof Object[] array) {
                partition(array, nodes, rest);
            } else if (item instanceof Iterable<?> iterable) {
                List<Object> group = new ArrayList<>();
                for (Object o : iterable) group.add(o);
                partition(group.toArray(), nodes, rest);
            } else {
                rest.add(item);
            }
        }
    }

    /** A unit cube. Size it with {@code box(2)} / {@code box(4, 0.2, 4)} or {@code .size(...)}. */
    public static Box box() {
        return new Box();
    }

    /** A cube with the given side length. */
    public static Box box(double side) {
        return new Box().size(side);
    }

    /** A box with the given width (x), height (y) and depth (z). */
    public static Box box(double width, double height, double depth) {
        return new Box().size(width, height, depth);
    }

    /** The scene's camera. Without one: positioned at (0, 0, 5), looking at the origin. */
    public static Camera camera() {
        return new Camera();
    }

    /** Sun-like light shining from its position toward the origin. */
    public static DirectionalLight directionalLight() {
        return new DirectionalLight();
    }

    /** Sun-like light with the given strength. */
    public static DirectionalLight directionalLight(double intensity) {
        return new DirectionalLight().intensity(intensity);
    }

    /** Even, directionless illumination. */
    public static AmbientLight ambientLight() {
        return new AmbientLight();
    }

    /** Even, directionless illumination with the given strength. */
    public static AmbientLight ambientLight(double intensity) {
        return new AmbientLight().intensity(intensity);
    }
}
