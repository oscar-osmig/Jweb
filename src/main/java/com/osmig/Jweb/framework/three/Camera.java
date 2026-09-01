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
    }
}
