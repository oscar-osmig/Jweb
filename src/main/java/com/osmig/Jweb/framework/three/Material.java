package com.osmig.Jweb.framework.three;

import jweb.CSSValue;

/**
 * A reusable material preset — declare a surface once, apply it to any
 * number of shapes with {@code .material(preset)}. Pure Java: nothing is
 * serialized until it lands on a mesh, and only the properties the preset
 * set are copied, so explicit calls before or after still override:
 *
 * <pre>{@code
 * var brass = material().color("#A07C4B").metalness(0.85).roughness(0.35);
 * var frosted = material().glass(0.9).roughness(0.4);
 *
 * box().material(brass)
 * sphere().material(brass).roughness(0.1)     // brass, but polished
 * cylinder().material(frosted)
 * }</pre>
 */
public final class Material {

    String color;
    String emissive;
    Double metalness;
    Double roughness;
    Double opacity;
    boolean wireframe;
    String texture;
    Double glass;

    Material() {}

    /** Material color — any CSS color string. */
    public Material color(String color) {
        this.color = color;
        return this;
    }

    /** Material color from a typed CSS value. */
    public Material color(CSSValue color) {
        return color(color.css());
    }

    /** Self-illuminated glow color. */
    public Material emissive(String color) {
        this.emissive = color;
        return this;
    }

    /** Glow color from a typed CSS value. */
    public Material emissive(CSSValue color) {
        return emissive(color.css());
    }

    /** 0 (dielectric) to 1 (metal). */
    public Material metalness(double metalness) {
        this.metalness = metalness;
        return this;
    }

    /** 0 (mirror) to 1 (diffuse). */
    public Material roughness(double roughness) {
        this.roughness = roughness;
        return this;
    }

    /** 0 (invisible) to 1 (opaque). */
    public Material opacity(double opacity) {
        this.opacity = opacity;
        return this;
    }

    /** Edges only. */
    public Material wireframe() {
        this.wireframe = true;
        return this;
    }

    /** An image as the material's texture map. */
    public Material texture(String url) {
        this.texture = url;
        return this;
    }

    /** Clear glass — see {@link MeshNode#glass()}. */
    public Material glass() {
        return glass(1);
    }

    /** Glass with the given transmission, 0–1 — see {@link MeshNode#glass(double)}. */
    public Material glass(double transmission) {
        if (transmission < 0 || transmission > 1) {
            throw new IllegalArgumentException("glass() transmission must be within 0–1 — got " + transmission);
        }
        this.glass = transmission;
        return this;
    }
}
