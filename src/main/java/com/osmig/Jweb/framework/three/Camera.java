package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * The scene's perspective camera. A scene without one gets a camera at
 * {@code (0, 0, 5)} looking at the origin. Declaring more than one camera
 * is not supported — the runtime uses the first.
 *
 * <pre>{@code
 * camera().position(0, 1.5, 4).orbit()
 * }</pre>
 */
public class Camera extends ThreeNode<Camera> {

    private Double fov;
    private Double near;
    private Double far;
    private double[] lookAt;
    private boolean orbit;
    private Double autoRotate;
    private double[] walk;
    private double[] bounds;
    private Double sway;
    private boolean noZoom;
    private boolean noPan;
    private double[] distance;
    private double[] polar;

    /** Vertical field of view in degrees. three.js default: 50. */
    public Camera fov(double degrees) {
        this.fov = degrees;
        return this;
    }

    /** Nearest visible distance. Default: 0.1. */
    public Camera near(double near) {
        this.near = near;
        return this;
    }

    /** Farthest visible distance. Default: 2000. */
    public Camera far(double far) {
        this.far = far;
        return this;
    }

    /** Point the camera looks at. Default: the origin. */
    public Camera lookAt(double x, double y, double z) {
        this.lookAt = new double[]{x, y, z};
        return this;
    }

    /** Lets the viewer rotate/zoom the camera by dragging (OrbitControls). */
    public Camera orbit() {
        this.orbit = true;
        return this;
    }

    /** Slowly circles the scene on its own; dragging still works. */
    public Camera autoRotate() {
        return autoRotate(2);
    }

    /** Circles the scene at the given speed (2 ≈ one lap every 30s). */
    public Camera autoRotate(double speed) {
        this.orbit = true;
        this.autoRotate = speed;
        return this;
    }

    // ==================== Walk mode ====================

    /**
     * First-person walk mode at the given eye height: W A S D / arrow keys
     * move, dragging looks, Shift runs, Esc steps back out to the framed
     * view. The scene starts framed (position/lookAt as declared); any
     * element with {@code data-three-walk="<scene id>"} toggles walking:
     *
     * <pre>{@code
     * scene(camera().position(0, 2, 6).lookAt(0, 2, 0).walk(1.7)
     *           .bounds(-8, -8, 8, 8), ...).id("hall")
     * button(attrs().data("three-walk", "hall"), text("Walk here"))
     * }</pre>
     *
     * <p>While walking, {@code three-walking} is set as a class on the scene
     * element, the toggle element(s) and {@code <body>}, and the scene
     * element dispatches a bubbling {@code jweb:three-walk} event with
     * {@code detail.walking}. Head-bob is skipped for visitors who prefer
     * reduced motion.</p>
     */
    public Camera walk(double eyeHeight) {
        this.walk = new double[]{eyeHeight};
        return this;
    }

    /** Walk mode with explicit speeds, in scene units per second. */
    public Camera walk(double eyeHeight, double speed, double runSpeed) {
        this.walk = new double[]{eyeHeight, speed, runSpeed};
        return this;
    }

    /**
     * Keeps a walking camera inside the rectangle {@code x in [minX, maxX]},
     * {@code z in [minZ, maxZ]} — the simple honest collision model for a
     * walled room. Only meaningful with {@link #walk}.
     */
    public Camera bounds(double minX, double minZ, double maxX, double maxZ) {
        this.bounds = new double[]{minX, minZ, maxX, maxZ};
        return this;
    }

    /**
     * A gentle idle drift of the framed camera — the room breathes instead
     * of standing frozen. Suppressed for visitors who prefer reduced motion.
     */
    public Camera sway() {
        return sway(1);
    }

    /** Idle drift scaled by {@code amount} (1 = subtle; 2 = twice the drift). */
    public Camera sway(double amount) {
        this.sway = amount;
        return this;
    }

    // ==================== Orbit limits ====================

    /** Orbit without zooming — the scroll wheel stays with the page. */
    public Camera noZoom() {
        this.orbit = true;
        this.noZoom = true;
        return this;
    }

    /** Orbit without panning — the subject stays centered. */
    public Camera noPan() {
        this.orbit = true;
        this.noPan = true;
        return this;
    }

    /** Clamps orbit zoom between the two camera-to-target distances. */
    public Camera distance(double min, double max) {
        this.orbit = true;
        this.distance = new double[]{min, max};
        return this;
    }

    /**
     * Clamps the orbit's vertical swing, in degrees from straight overhead:
     * {@code polar(30, 90)} allows from 30° below the zenith down to the
     * horizon, and never under the floor.
     */
    public Camera polar(double minDeg, double maxDeg) {
        this.orbit = true;
        this.polar = new double[]{minDeg, maxDeg};
        return this;
    }

    @Override
    protected String type() {
        return "camera";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (fov != null) map.put("fov", num(fov));
        if (near != null) map.put("near", num(near));
        if (far != null) map.put("far", num(far));
        if (lookAt != null) map.put("look", vec(lookAt));
        if (orbit) map.put("orbit", true);
        if (autoRotate != null) map.put("auto", num(autoRotate));
        if (walk != null) map.put("walk", vec(walk));
        if (bounds != null) map.put("bounds", vec(bounds));
        if (sway != null) map.put("sway", num(sway));
        if (noZoom) map.put("noZoom", true);
        if (noPan) map.put("noPan", true);
        if (distance != null) map.put("dist", vec(distance));
        if (polar != null) map.put("polar", vec(polar));
    }
}
