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

    /**
     * A ground surface lying flat — y up, centered on its position, no
     * rotation needed — with optional seeded hills:
     *
     * <pre>{@code
     * terrain(60, 60).hills(1.5).color("#C9B58A")   // low dunes
     * }</pre>
     */
    public static Terrain terrain(double width, double depth) {
        return new Terrain(width, depth);
    }

    // ==================== Materials ====================

    /**
     * A reusable material preset — declare the surface once, apply it with
     * {@code .material(preset)} on any shape:
     *
     * <pre>{@code
     * var brass = material().color("#A07C4B").metalness(0.85).roughness(0.35);
     * box().material(brass)
     * }</pre>
     */
    public static Material material() {
        return new Material();
    }

    // ==================== Curves ====================

    /**
     * A thin, unlit polyline through x,y,z points — outlines, guides,
     * constellations, the pencil line of a diagram. ({@code wire}, because
     * SVG owns {@code line}.) WebGL lines are one pixel wide; for thickness
     * use {@link #tube}.
     *
     * <pre>{@code
     * wire(-2, 0, 0,  0, 1.4, 0,  2, 0, 0).color("#fde68a").draw(1200)
     * }</pre>
     */
    public static Line wire(double... points) {
        return new Line(points);
    }

    /**
     * A rectangular profile ({@code width × height}) swept along a smooth
     * curve through x,y,z points — moldings, ribs, rails, gutters:
     *
     * <pre>{@code
     * sweep(0.12, 0.06,  -3, 0, 0,  0, 2.2, 0,  3, 0, 0)   // one vault rib
     * }</pre>
     */
    public static Sweep sweep(double width, double height, double... points) {
        return new Sweep(width, height, points);
    }

    /**
     * A round tube swept along a smooth curve through x,y,z points —
     * pipes, vines, cables, ribs and rails without segmenting the curve
     * by hand:
     *
     * <pre>{@code
     * tube(0.05, -2, 0, 0,  0, 1.4, 0,  2, 0, 0)   // one smooth rib
     * }</pre>
     */
    public static Tube tube(double radius, double... points) {
        return new Tube(radius, points);
    }

    /**
     * A partial ring — an arch that stops where you say. Sweep is in
     * degrees, counter-clockwise from the ring's +x; rotate the node to
     * hang it. {@code arc(1.5, 0.08, 180)} is a doorway arch.
     */
    public static Arc arc(double radius, double tube, double sweepDeg) {
        return new Arc(radius, tube, sweepDeg);
    }

    /**
     * A surface of revolution from radius,height pairs, bottom-up — pots,
     * columns, domes and bells from their silhouette:
     *
     * <pre>{@code
     * lathe(0, 0,  0.5, 0,  0.35, 0.8,  0.55, 1.1,  0.3, 1.3)
     * }</pre>
     */
    public static Lathe lathe(double... radiusHeightPairs) {
        return new Lathe(radiusHeightPairs);
    }

    // ==================== Particles ====================

    /**
     * A cloud of point particles — one node, one draw call. Dust is
     * {@code particles(120).size(0.03).spread(8, 5, 20).drift()}; rain is
     * {@code particles(400).size(0.02).spread(12, 8, 12).fall(3)}.
     */
    public static Particles particles(int count) {
        return new Particles(count);
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

    /** A group built from any collection — no {@code toArray} ceremony. */
    public static Group group(Iterable<? extends ThreeNode<?>> children) {
        Group g = new Group();
        for (ThreeNode<?> child : children) g.add(child);
        return g;
    }

    /**
     * The node, or nothing: {@code when(aligned, whale())} adds the whale
     * only when the condition holds. Null nodes vanish from {@code scene},
     * {@code group} and {@code repeat} alike, so branches never need a
     * placeholder.
     */
    public static ThreeNode<?> when(boolean condition, ThreeNode<?> node) {
        return condition ? node : null;
    }

    /** Like {@link #when(boolean, ThreeNode)}, but only builds the node if needed. */
    public static ThreeNode<?> when(boolean condition,
                                    java.util.function.Supplier<? extends ThreeNode<?>> node) {
        return condition ? node.get() : null;
    }

    /**
     * A group of {@code count} nodes built by index — colonnades, coffers,
     * chains, stakes:
     *
     * <pre>{@code
     * repeat(10, i -> cylinder(0.08, 1.5).position(-2.7 + i * 0.6, 0.75, 0))
     * }</pre>
     *
     * <p>Return {@code null} from the builder to skip an index.</p>
     */
    public static Group repeat(int count, java.util.function.IntFunction<? extends ThreeNode<?>> node) {
        Group g = new Group();
        for (int i = 0; i < count; i++) g.add(node.apply(i));
        return g;
    }

    /** A glTF model loaded from a URL — plain {@code .glb}/{@code .gltf}. */
    public static Model model(String url) {
        return new Model(url);
    }

    // ==================== Places that react ====================

    /**
     * An invisible floor region that reacts when the camera walks in or out
     * — {@code zone(-1.6, 14.9, 1.6, 16).link("/")} is the door home,
     * {@code zone(...).onEnter(e -> ...)} a trigger. Edges are x and z on
     * the ground; see {@link Zone}. Pair with {@code .near(distance)} on any
     * node for distance-based reactions and {@code .solid()} for collisions.
     */
    public static Zone zone(double minX, double minZ, double maxX, double maxZ) {
        return new Zone(minX, minZ, maxX, maxZ);
    }

    // ==================== Sound ====================

    /**
     * A sound in the scene: {@code sound(url).loop().volume(0.4)} is
     * ambience; give it a {@code .position(...)} and it plays from there,
     * louder as the camera nears. Starts on the visitor's first gesture, as
     * browsers require. See {@link Sound}.
     */
    public static Sound sound(String url) {
        return new Sound(url);
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

    /**
     * A cone of light aimed at a target — stage lamp, desk light, street
     * light. Placed at {@code (2, 4, 2)} and aimed straight down unless told
     * otherwise: {@code spotLight(40).position(0, 6, 0).angle(25).shadows()}.
     */
    public static SpotLight spotLight() {
        return new SpotLight();
    }

    /** A cone of light with the given strength (a real lamp a few units away wants 20–60). */
    public static SpotLight spotLight(double intensity) {
        return new SpotLight().intensity(intensity);
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

    /**
     * Cinematic tone mapping (ACES filmic): highlights roll off instead of
     * clipping, and lit materials gain depth. One line, whole-scene:
     *
     * <pre>{@code
     * scene(toneMapped(), ...)
     * }</pre>
     */
    public static SceneSetting toneMapped() {
        return new SceneSetting("tone");
    }

    /** Tone mapping with an exposure dial — above 1 brightens, below darkens. */
    public static SceneSetting toneMapped(double exposure) {
        return new SceneSetting("tone").put("exposure", ThreeNode.num(exposure));
    }

    /**
     * Makes bright emissive surfaces actually glow — an HDR bloom pass over
     * the whole scene, composited before tone mapping (which it implies).
     * The scene must still read without it; bloom is the halo, not the lamp.
     *
     * <pre>{@code
     * scene(bloom(),
     *     sphere().color("#8FD5B5").emissive("#5FA98A"), ...)
     * }</pre>
     */
    public static SceneSetting bloom() {
        return bloom(0.7);
    }

    /** Bloom with the given strength (0.7 default; beyond ~1.5 the halo takes over). */
    public static SceneSetting bloom(double strength) {
        return new SceneSetting("bloom").put("strength", ThreeNode.num(strength));
    }

    /**
     * Fully dialed bloom: {@code radius} spreads the halo (0–1), and only
     * pixels brighter than {@code threshold} (0–1) bloom at all — raise it
     * and the glow stays on the truly luminous.
     */
    public static SceneSetting bloom(double strength, double radius, double threshold) {
        return new SceneSetting("bloom").put("strength", ThreeNode.num(strength))
            .put("radius", ThreeNode.num(radius)).put("threshold", ThreeNode.num(threshold));
    }

    // ==================== Live updates ====================

    /**
     * Updates named nodes in a <em>live</em> scene from a server event
     * handler — no page reload, no scene rebuild, the visitor doesn't move:
     *
     * <pre>{@code
     * box().name("vane").onClick(e ->
     *     Three.patch("world")
     *          .node("vane").rotation(0, 24, 0).tween(600)
     *          .node("veil").emissive("#1E5D50"))
     * }</pre>
     *
     * <p>The patch rides the WebSocket answer to the event that's already in
     * flight, so it works from {@code onClick(Consumer)} handlers (and any
     * other server-side event handler). Outside one there is no live page to
     * deliver to — the patch is dropped with a warning. Targets are nodes
     * carrying {@code .name(...)}; {@code .camera()} moves the camera.</p>
     *
     * @param sceneId the scene element's {@code id}
     */
    public static ThreePatch patch(String sceneId) {
        return new ThreePatch(sceneId);
    }
}
