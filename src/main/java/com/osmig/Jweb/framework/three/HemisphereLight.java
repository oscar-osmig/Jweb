package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * Soft sky-and-ground illumination (three.js {@code HemisphereLight}) — white
 * from above, dark gray from below unless colored. The pleasant no-setup
 * light; it is also what a scene gets when it declares no lights at all.
 */
public class HemisphereLight extends ThreeNode<HemisphereLight> {

    private String sky;
    private String ground;
    private Double intensity;

    /** The color shining down from above. Default: white. */
    public HemisphereLight sky(String color) {
        this.sky = color;
        return this;
    }

    /** The color shining down from above, from a typed CSS value. */
    public HemisphereLight sky(CSSValue color) {
        return sky(color.css());
    }

    /** The color reflected up from below. Default: dark gray. */
    public HemisphereLight ground(String color) {
        this.ground = color;
        return this;
    }

    /** The color reflected up from below, from a typed CSS value. */
    public HemisphereLight ground(CSSValue color) {
        return ground(color.css());
    }

    /** Light strength. three.js default: 1. */
    public HemisphereLight intensity(double intensity) {
        this.intensity = intensity;
        return this;
    }

    @Override
    protected String type() {
        return "hemiLight";
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (sky != null) map.put("sky", sky);
        if (ground != null) map.put("ground", ground);
        if (intensity != null) map.put("intensity", num(intensity));
    }
}
