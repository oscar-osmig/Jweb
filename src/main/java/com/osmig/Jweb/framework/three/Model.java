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

    Model(String url) {
        this.url = url;
    }

    @Override
    protected String type() {
        return "model";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("url", url);
    }
}
