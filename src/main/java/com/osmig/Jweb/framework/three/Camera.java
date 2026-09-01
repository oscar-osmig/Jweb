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
    private double[] lookAt;
    private boolean orbit;

    /** Vertical field of view in degrees. three.js default: 50. */
    public Camera fov(double degrees) {
        this.fov = degrees;
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

    @Override
    protected String type() {
        return "camera";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (fov != null) map.put("fov", num(fov));
        if (lookAt != null) map.put("look", vec(lookAt));
        if (orbit) map.put("orbit", true);
    }
}
