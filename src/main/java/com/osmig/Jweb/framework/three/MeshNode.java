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
    private Double glass;
    private String hoverColor;
    private String hoverEmissive;

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

    /**
     * Clear glass: light passes through and refracts (three.js
     * {@code MeshPhysicalMaterial} transmission). {@code .color(...)} tints
     * it; {@code .roughness(...)} frosts it (default 0.05, near-clear).
     */
    public SELF glass() {
        return glass(1);
    }

    /** Glass with the given transmission, 0 (opaque) to 1 (fully clear). */
    public SELF glass(double transmission) {
        if (transmission < 0 || transmission > 1) {
            throw new IllegalArgumentException("glass() transmission must be within 0–1 — got " + transmission);
        }
        this.glass = transmission;
        return self();
    }

    /**
     * Applies a {@link Material} preset: every property the preset set is
     * copied onto this shape; explicit calls made later still override.
     *
     * <pre>{@code
     * var brass = material().color("#A07C4B").metalness(0.85).roughness(0.35);
     * box().material(brass)
     * }</pre>
     */
    public SELF material(Material preset) {
        if (preset == null) throw new IllegalArgumentException("material(preset): preset is null");
        if (preset.color != null) this.color = preset.color;
        if (preset.emissive != null) this.emissive = preset.emissive;
        if (preset.metalness != null) this.metalness = preset.metalness;
        if (preset.roughness != null) this.roughness = preset.roughness;
        if (preset.opacity != null) this.opacity = preset.opacity;
        if (preset.wireframe) this.wireframe = true;
        if (preset.texture != null) this.texture = preset.texture;
        if (preset.glass != null) this.glass = preset.glass;
        return self();
    }

    /** Material color while the pointer is over the shape (raycast, client-side). */
    public SELF hoverColor(String color) {
        this.hoverColor = color;
        return self();
    }

    /** Hover color from a typed CSS value. */
    public SELF hoverColor(CSSValue color) {
        return hoverColor(color.css());
    }

    /** Emissive glow while the pointer is over the shape — reads as a highlight. */
    public SELF hoverEmissive(String color) {
        this.hoverEmissive = color;
        return self();
    }

    /** Hover glow from a typed CSS value. */
    public SELF hoverEmissive(CSSValue color) {
        return hoverEmissive(color.css());
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
        if (glass != null) map.put("glass", num(glass));
        if (hoverColor != null) map.put("hovColor", hoverColor);
        if (hoverEmissive != null) map.put("hovEmissive", hoverEmissive);
    }
}
