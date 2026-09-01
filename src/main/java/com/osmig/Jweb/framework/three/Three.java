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

    // ==================== Shapes ====================

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

    /** A unit sphere. */
    public static Sphere sphere() {
        return new Sphere();
    }

    /** A sphere with the given radius. */
    public static Sphere sphere(double radius) {
        return new Sphere().radius(radius);
    }

    /** A 1×1 flat rectangle, double-sided. Rotate -90° on x for a ground plane. */
    public static Plane plane() {
        return new Plane();
    }

    /** A flat rectangle with the given width and height, double-sided. */
    public static Plane plane(double width, double height) {
        return new Plane().size(width, height);
    }

    /** A cylinder with radius 1 and height 1. */
    public static Cylinder cylinder() {
        return new Cylinder();
    }

    /** A cylinder with the given radius and height. */
    public static Cylinder cylinder(double radius, double height) {
        return new Cylinder().radius(radius).height(height);
    }

    /** A cone with base radius 1 and height 1. */
    public static Cone cone() {
        return new Cone();
    }

    /** A cone with the given base radius and height. */
    public static Cone cone(double radius, double height) {
        return new Cone().radius(radius).height(height);
    }

    /** A ring with radius 1 and tube thickness 0.4. */
    public static Torus torus() {
        return new Torus();
    }

    /** A ring with the given radius and tube thickness. */
    public static Torus torus(double radius, double tube) {
        return new Torus().radius(radius).tube(tube);
    }

    /** A capsule — a cylinder with rounded caps. Radius 1, middle length 1. */
    public static Capsule capsule() {
        return new Capsule();
    }

    /** A capsule with the given radius and middle length. */
    public static Capsule capsule(double radius, double length) {
        return new Capsule().radius(radius).length(length);
    }

    /** A filled flat disc, double-sided. ({@code disc}, because SVG owns {@code circle}.) */
    public static Disc disc() {
        return new Disc();
    }

    /** A flat disc with the given radius. */
    public static Disc disc(double radius) {
        return new Disc().radius(radius);
    }

    /** A flat annulus, double-sided. Inner radius 0.5, outer radius 1. */
    public static Ring ring() {
        return new Ring();
    }

    /** A flat annulus with the given inner and outer radius. */
    public static Ring ring(double inner, double outer) {
        return new Ring().radii(inner, outer);
    }

    /** A torus knot — the classic 3D showpiece. Radius 1, tube 0.4. */
    public static TorusKnot torusKnot() {
        return new TorusKnot();
    }

    /** A torus knot with the given overall radius and tube thickness. */
    public static TorusKnot torusKnot(double radius, double tube) {
        return new TorusKnot().radius(radius).tube(tube);
    }

    /** A tetrahedron (4 faces), radius 1. */
    public static Polyhedron tetrahedron() {
        return new Polyhedron("tetra");
    }

    /** A tetrahedron with the given radius. */
    public static Polyhedron tetrahedron(double radius) {
        return new Polyhedron("tetra").radius(radius);
    }

    /** An octahedron (8 faces), radius 1. */
    public static Polyhedron octahedron() {
        return new Polyhedron("octa");
    }

    /** An octahedron with the given radius. */
    public static Polyhedron octahedron(double radius) {
        return new Polyhedron("octa").radius(radius);
    }

    /** A dodecahedron (12 faces), radius 1. */
    public static Polyhedron dodecahedron() {
        return new Polyhedron("dodeca");
    }

    /** A dodecahedron with the given radius. */
    public static Polyhedron dodecahedron(double radius) {
        return new Polyhedron("dodeca").radius(radius);
    }

    /** An icosahedron (20 faces), radius 1. */
    public static Polyhedron icosahedron() {
        return new Polyhedron("icosa");
    }

    /** An icosahedron with the given radius. */
    public static Polyhedron icosahedron(double radius) {
        return new Polyhedron("icosa").radius(radius);
    }

    // ==================== Billboards ====================

    /**
     * A text label that always faces the camera — canvas-rendered, no font
     * file. ({@code billboard}, because HTML owns {@code label}.)
     */
    public static Billboard billboard(String text) {
        return new Billboard(text);
    }

    /** An image that always faces the camera — icons, markers, particles. */
    public static Sprite sprite(String imageUrl) {
        return new Sprite(imageUrl);
    }

    // ==================== Composition ====================

    /** Nodes sharing one transform — move, rotate, scale or animate them together. */
    public static Group group(ThreeNode<?>... children) {
        return new Group(children);
    }

    /** A glTF model loaded from a URL — plain {@code .glb}/{@code .gltf}. */
    public static Model model(String url) {
        return new Model(url);
    }

    // ==================== Camera ====================

    /** The scene's camera. Without one: positioned at (0, 0, 5), looking at the origin. */
    public static Camera camera() {
        return new Camera();
    }

    // ==================== Lights ====================

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

    /** A bulb-like light radiating from its position in every direction. */
    public static PointLight pointLight() {
        return new PointLight();
    }

    /** A bulb-like light with the given strength. */
    public static PointLight pointLight(double intensity) {
        return new PointLight().intensity(intensity);
    }

    /** Soft sky-and-ground illumination — the pleasant no-setup light. */
    public static HemisphereLight hemisphereLight() {
        return new HemisphereLight();
    }

    /** Soft illumination with the given sky and ground colors. */
    public static HemisphereLight hemisphereLight(String sky, String ground) {
        return new HemisphereLight().sky(sky).ground(ground);
    }

    // ==================== Scene settings ====================

    /** A solid background color (scenes are transparent over the page by default). */
    public static SceneSetting background(String color) {
        return new SceneSetting("bg").put("color", color);
    }

    /** A solid background color from a typed CSS value. */
    public static SceneSetting background(jweb.CSSValue color) {
        return background(color.css());
    }

    /**
     * Distance fog: objects fade to the color between the near and far
     * distances. Pair it with a matching {@code background(...)} for the
     * classic infinite-depth look.
     */
    public static SceneSetting fog(String color, double near, double far) {
        return new SceneSetting("fog").put("color", color)
            .put("near", ThreeNode.num(near)).put("far", ThreeNode.num(far));
    }

    /** Distance fog from a typed CSS value. */
    public static SceneSetting fog(jweb.CSSValue color, double near, double far) {
        return fog(color.css(), near, far);
    }

    /** A 10×10 reference grid on the ground plane — handy while placing objects. */
    public static SceneSetting grid() {
        return new SceneSetting("grid");
    }

    /** A reference grid of the given size and number of divisions. */
    public static SceneSetting grid(double size, int divisions) {
        return new SceneSetting("grid")
            .put("size", ThreeNode.num(size)).put("divisions", divisions);
    }

    /**
     * An equirectangular panorama (a plain wide jpg/png) as the scene's
     * light environment — metallic and glossy materials pick up its
     * reflections. The image is not shown; pair with {@code background(...)}
     * or use {@link #sky(String)} to also see it.
     */
    public static SceneSetting environment(String panoramaUrl) {
        return new SceneSetting("env").put("url", panoramaUrl);
    }

    /**
     * An equirectangular panorama as the visible sky <em>and</em> the light
     * environment — the one-liner backdrop.
     *
     * <pre>{@code
     * scene(sky("/assets/dusk.jpg"),
     *     sphere().metalness(1).roughness(0.05))     // mirrors the sky
     * }</pre>
     */
    public static SceneSetting sky(String panoramaUrl) {
        return new SceneSetting("env").put("url", panoramaUrl).put("bg", true);
    }
}
