package com.osmig.Jweb.framework.three;

import java.util.Map;

/**
 * An image that always faces the camera (a three.js {@code Sprite}) — icons,
 * markers, particles. Hidden until the image loads, then sized to
 * {@link #size(double)} scene units wide with the image's own aspect ratio.
 *
 * <pre>{@code
 * sprite("/assets/pin.png").position(2, 1, 0).size(0.6)
 * sprite("/assets/star.png").clickSwap("/api/star-info", "#panel")
 * }</pre>
 */
public class Sprite extends ThreeNode<Sprite> {

    private final String url;
    private Double size;

    Sprite(String url) {
        this.url = url;
    }

    /** The sprite's width in scene units (height keeps the image aspect). Default 1. */
    public Sprite size(double width) {
        this.size = width;
        return this;
    }

    @Override
    protected String type() {
        return "sprite";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        map.put("url", url);
        if (size != null) map.put("size", num(size));
    }
}
