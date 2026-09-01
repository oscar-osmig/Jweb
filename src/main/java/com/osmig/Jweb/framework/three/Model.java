package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * A glTF model loaded from a URL (three.js {@code GLTFLoader}) — the standard
 * format every 3D tool exports. Plain {@code .glb}/{@code .gltf} only; Draco-
 * compressed files need a decoder JWeb does not ship.
 *
 * <p>Transforms, animation presets and click handlers apply immediately (they
 * attach to a wrapper the model loads into), so a scene behaves the same
 * before and after the file arrives.</p>
 *
 * <pre>{@code
 * model("/assets/rocket.glb").scale(0.5).float_()
 * }</pre>
 */
public class Model extends ThreeNode<Model> {

    private final String url;
    private Object animation;

    Model(String url) {
        this.url = url;
    }

    /**
     * Plays every animation clip the file ships — the usual case for models
     * exported with a single baked animation.
     *
     * <pre>{@code
     * model("/assets/robot.glb").animate()
     * }</pre>
     */
    public Model animate() {
        this.animation = Boolean.TRUE;
        return this;
    }

    /** Plays only the named clip (as exported — check the file's clip names). */
    public Model animate(String clipName) {
        this.animation = clipName;
        return this;
    }

    @Override
    protected String type() {
        return "model";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("url", url);
        if (animation != null) map.put("anim", animation);
    }
}
