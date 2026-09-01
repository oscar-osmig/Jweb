package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

import java.util.Map;

/**
 * Base of every visible shape in a scene. Adds the material surface —
 * three.js {@code MeshStandardMaterial}, physically based, reacts to
 * lights — with the platform's own property names.
 *
 * @param <SELF> the concrete mesh type, so chained calls keep their exact type
 */
public abstract class MeshNode<SELF extends MeshNode<SELF>> extends ThreeNode<SELF> {

    private String color;
    private String emissive;
    private Double metalness;
    private Double roughness;
    private Double opacity;
    private boolean wireframe;
    private String texture;

    /** Material color — any CSS color string ({@code "#10b981"}, {@code "coral"}). */
    public SELF color(String color) {
        this.color = color;
        return self();
    }

    /** Material color from a typed CSS value ({@code hex(...)}, {@code hsl(...)}). */
    public SELF color(CSSValue color) {
        return color(color.css());
    }

    /** Self-illuminated glow color, independent of lighting. */
    public SELF emissive(String color) {
        this.emissive = color;
        return self();
    }

    /** Self-illuminated glow color from a typed CSS value. */
    public SELF emissive(CSSValue color) {
        return emissive(color.css());
    }

    /** How metallic the surface is, 0 (dielectric) to 1 (metal). three.js default: 0. */
    public SELF metalness(double metalness) {
        this.metalness = metalness;
        return self();
    }

    /** How rough the surface is, 0 (mirror) to 1 (diffuse). three.js default: 1. */
    public SELF roughness(double roughness) {
        this.roughness = roughness;
        return self();
    }

    /** Transparency, 0 (invisible) to 1 (opaque). */
    public SELF opacity(double opacity) {
        this.opacity = opacity;
        return self();
    }

    /** Renders the shape's edges only. */
    public SELF wireframe() {
        this.wireframe = true;
        return self();
    }

    /** An image applied as the material's texture map ({@code /assets/crate.png}). */
    public SELF texture(String url) {
        this.texture = url;
        return self();
    }

    @Override
    protected void fill(Map<String, Object> map) {
        if (color != null) map.put("color", color);
        if (emissive != null) map.put("emissive", emissive);
        if (metalness != null) map.put("metal", num(metalness));
        if (roughness != null) map.put("rough", num(roughness));
        if (opacity != null) map.put("opacity", num(opacity));
        if (wireframe) map.put("wire", true);
        if (texture != null) map.put("map", texture);
    }
}
