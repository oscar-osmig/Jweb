package com.osmig.Jweb.app.pages;

import jweb.Element;

import static jweb.El.*;
import static jweb.Css.*;
import static jweb.Three.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Three DSL demo: declarative 3D scenes, zero handwritten JavaScript. */
public final class ThreeDemoPage {

    private ThreeDemoPage() {}

    public static Element content() {
        return div(style().maxWidth(px(700)).margin(zero, auto)
                .padding(clamp(rem(2), vw(6), rem(3)), GUTTER),
            h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT),
                text("3D scenes in pure Java")),
            p(style().marginTop(SP_2).color(TEXT_LIGHT),
                text("Declared like any other element — three.js loads lazily, and "
                    + "the runtime owns the render loop, shadows, sizing and cleanup. "
                    + "Drag to orbit. Click a shape.")),

            scene(style().marginTop(SP_6).height(px(420)).borderRadius(ROUNDED),
                background("#0f172a"),
                fog("#0f172a", 10, 30),
                camera().position(6, 4, 8).lookAt(0, 0.8, 0).autoRotate(1),
                directionalLight(1.2).position(5, 8, 4).shadows(),
                ambientLight(0.35),
                plane(30, 30).rotation(-90, 0, 0).color("#1e293b").roughness(0.9),
                group(
                    torus(1, 0.35).color("#8b5cf6").position(-2.4, 1.2, 0)
                        .rotation(90, 0, 0).spin(0, 0, 25).name("torus")
                        .clickSwap("/demo/three/pick?shape=torus", "#pick-panel"),
                    sphere(0.9).color("#f59e0b").metalness(0.7).roughness(0.25)
                        .position(0, 0.9, 0).name("sphere")
                        .clickSwap("/demo/three/pick?shape=sphere", "#pick-panel"),
                    cone(0.8, 1.6).color("#10b981").position(2.4, 0.8, 0)
                        .float_(0.3, 0.5).name("cone")
                        .clickSwap("/demo/three/pick?shape=cone", "#pick-panel")
                ).name("stage")
            ).id("showcase"),

            div(style().marginTop(SP_3), pickFragment(null)).id("pick-panel"),

            p(style().marginTop(SP_6).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("Every shape above is a builder chain: shadows are one call on the "
                    + "light, the camera circles on its own, the cone hovers with "
                    + "float_(), and clicking raycasts into the same swap pipeline "
                    + "as any link — the panel below the scene is server-rendered.")),

            scene(style().marginTop(SP_6).height(px(320)).borderRadius(ROUNDED),
                camera().position(2.5, 2, 4).orbit(),
                directionalLight().position(3, 5, 2),
                ambientLight(0.3),
                grid(),
                box().color("#10b981").position(0, 1, 0).spin().name("cube")
            ).id("classic"),

            p(style().marginTop(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("The classic: box().color(\"#10b981\").spin() over a grid() — "
                    + "the render loop only runs because something animates.")),

            h2(style().marginTop(SP_6).fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                text("Hover, billboards and client-side clicks")),
            scene(style().marginTop(SP_2).height(px(380)).borderRadius(ROUNDED),
                background("#0b1120"),
                camera().position(0, 2.2, 7).lookAt(0, 1, 0).orbit(),
                directionalLight(1.1).position(4, 6, 3),
                ambientLight(0.35),
                disc(6).rotation(-90, 0, 0).color("#111c33").roughness(0.95),
                torusKnot(1, 0.28).color("#a855f7").metalness(0.6).roughness(0.2)
                    .position(0, 1.4, 0).spin(0, 25, 0).name("knot")
                    .hoverScale(1.1).hoverEmissive("#4c1d95")
                    .onClick(jweb.Actions.toggle("knot-caption")),
                billboard("torusKnot() — click it").size(0.45).position(0, 3.2, 0)
                    .background("rgba(15,23,42,0.85)").color("#e2e8f0"),
                icosahedron(0.7).wireframe().color("#22d3ee")
                    .position(-2.6, 1, 0).spin(0, 40, 0).hoverScale(1.2),
                capsule(0.4, 0.8).color("#fb7185").position(2.6, 1.1, 0)
                    .float_(0.2, 0.5).hoverColor("#f43f5e"),
                ring(0.75, 0.95).color("#38bdf8").position(-2.6, 1, 0).spin(0, 0, 40)
            ).id("playground"),

            p(attrs().id("knot-caption")
                    .style().display(none).marginTop(SP_2).padding(SP_3).borderRadius(ROUNDED)
                        .backgroundColor(hex("#f5f3ff")).color(hex("#5b21b6")).done(),
                text("Clicked! That ran an Actions-DSL handler — toggle(\"knot-caption\") — "
                    + "raycast in the scene, dispatched client-side, no server involved. "
                    + "The grow-on-hover is hoverScale(1.1), the glow hoverEmissive(...).")),

            p(style().marginTop(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("New surface: torusKnot(), icosahedron().wireframe(), capsule(), "
                    + "ring(), a disc() floor and a billboard(\"...\") label that always "
                    + "faces the camera. Hover effects and the click are declared on the "
                    + "shapes — and the render loop pauses whenever the scene scrolls "
                    + "offscreen.")),

            h2(style().marginTop(SP_6).fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                text("Walk through it — and patch it live")),
            scene(style().marginTop(SP_2).height(px(460)).borderRadius(ROUNDED),
                background("#0c0a09"),
                fog("#0c0a09", 14, 34),
                bloom(0.8, 0.4, 0.8),
                camera().position(0, 1.9, 7.2).lookAt(0, 1.8, -2.5).fov(56)
                    .walk(1.6).bounds(-3.3, -5.6, 3.3, 6.6).sway(),
                hemisphereLight("#8a7a68", "#151210"),
                pointLight(1.2).color("#ffd9a0").position(0, 3.4, 1),
                pointLight(0.8).color("#ffe0b0").position(0, 3.2, -4.5),
                // the polished floor: a real mirror under a satin veil
                plane(8, 14).rotation(-90, 0, 0).mirror().color("#4a443e"),
                plane(8, 14).rotation(-90, 0, 0).color("#191512").roughness(0.4)
                    .opacity(0.72).position(0, 0.01, 0),
                // walls and the far portal
                plane(8.2, 4.6).position(0, 2.3, -7).color("#25211c").roughness(1),
                plane(14, 4.6).rotation(0, 90, 0).position(-4, 2.3, 0).color("#2a2521").roughness(1),
                plane(14, 4.6).rotation(0, -90, 0).position(4, 2.3, 0).color("#2a2521").roughness(1),
                // one arc() is the whole archway; two lathe() profiles are the columns
                arc(1.6, 0.09, 180).color("#8a6f4d").metalness(0.8).roughness(0.4)
                    .position(0, 2.1, -4.5),
                lathe(0.34, 0, 0.26, 0.12, 0.2, 0.3, 0.2, 2.0, 0.3, 2.15).segments(24)
                    .color("#4a4038").roughness(0.9).position(-1.6, 0, -4.5),
                lathe(0.34, 0, 0.26, 0.12, 0.2, 0.3, 0.2, 2.0, 0.3, 2.15).segments(24)
                    .color("#4a4038").roughness(0.9).position(1.6, 0, -4.5),
                // one tube() is the whole vine over the arch
                tube(0.035, -1.7, 2.0, -4.4, -0.8, 3.05, -4.6, 0.6, 3.1, -4.4, 1.7, 2.2, -4.5)
                    .color("#3d5c3a").roughness(1),
                // the lantern: click it and the server re-colors it over the socket
                cylinder(0.05, 1.1).color("#4a4038").position(0, 0.55, -2.2),
                sphere(0.24).color("#ffd9a0").emissive("#e8b36b").roughness(0.6)
                    .position(0, 1.35, -2.2).name("lantern").hoverScale(1.15)
                    .onClick(ThreeDemoPage::relightLantern),
                pointLight(1.1).color("#e8b36b").position(0, 1.6, -2.2).name("lantern-light"),
                billboard("the lantern listens — click it").size(0.32)
                    .position(0, 2.15, -2.2).background("rgba(12,10,9,0.8)").color("#e7d6b1"),
                // dust holding the light: one node, one draw call
                particles(140).color("#e7d6b1").size(0.02).spread(6, 3.4, 10)
                    .position(0, 1.9, 0).drift().opacity(0.75)
            ).id("walkable"),

            div(style().marginTop(SP_3).display(flex).gap(SP_2).flexWrap(wrap),
                button(attrs().data("three-walk", "walkable")
                        .style().padding(SP_2, SP_3).borderRadius(ROUNDED)
                        .border(px(1), solid, hex("#d6d3d1")).cursor(pointer).done(),
                    text("🚶 Walk here — W A S D, drag to look, Esc to step out")),
                button(attrs()
                        .onClick(e -> patch("walkable").camera()
                            .position(0, 2.2, -0.6).lookAt(0, 2.1, -4.5).tween(1200))
                        .style().padding(SP_2, SP_3).borderRadius(ROUNDED)
                        .border(px(1), solid, hex("#d6d3d1")).cursor(pointer).done(),
                    text("Glide to the arch")),
                button(attrs()
                        .onClick(e -> patch("walkable").camera()
                            .position(0, 1.9, 7.2).lookAt(0, 1.8, -2.5).tween(1200))
                        .style().padding(SP_2, SP_3).borderRadius(ROUNDED)
                        .border(px(1), solid, hex("#d6d3d1")).cursor(pointer).done(),
                    text("Back to the door"))),

            p(style().marginTop(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("camera().walk(1.6).bounds(...) hands you your feet — the button is "
                    + "just data-three-walk=\"walkable\", no script. The floor is "
                    + "plane().mirror() under a satin overlay, the archway is one arc(), "
                    + "the columns are lathe() profiles, the vine is one tube(), the dust "
                    + "is particles(140).drift(), and bloom() makes the lantern's emissive "
                    + "actually glow. Clicking the lantern runs a server handler that "
                    + "answers with Three.patch — watch it re-light in place, mid-walk, "
                    + "no reload. The camera buttons are server patches too."))
        );
    }

    private static final java.util.concurrent.atomic.AtomicBoolean LANTERN_COOL =
        new java.util.concurrent.atomic.AtomicBoolean(false);

    /** The lantern's server-side click: re-light the live scene over the socket. */
    private static void relightLantern(com.osmig.Jweb.framework.events.Event e) {
        boolean cool = !LANTERN_COOL.get();
        LANTERN_COOL.set(cool);
        patch("walkable")
            .node("lantern").emissive(cool ? "#22d3ee" : "#e8b36b")
                .color(cool ? "#a5f3fc" : "#ffd9a0").tween(500)
            .node("lantern-light").color(cool ? "#22d3ee" : "#e8b36b")
                .intensity(cool ? 1.6 : 1.1).tween(500);
    }

    /** The fragment swapped in when a shape is clicked (also the initial hint). */
    public static Element pickFragment(String shape) {
        String title;
        String detail;
        if ("torus".equals(shape)) {
            title = "torus(1, 0.35)";
            detail = "Spinning on its z axis at 25°/s. Purple #8b5cf6, default material.";
        } else if ("sphere".equals(shape)) {
            title = "sphere(0.9)";
            detail = "Polished metal: metalness(0.7).roughness(0.25). Amber #f59e0b.";
        } else if ("cone".equals(shape)) {
            title = "cone(0.8, 1.6)";
            detail = "Hovering with float_(0.3, 0.5). Emerald #10b981.";
        } else {
            title = "Click a shape";
            detail = "The click raycasts into the scene and swaps this panel from the server.";
        }
        return div(style().padding(SP_3).borderRadius(ROUNDED)
                .backgroundColor(hex("#f8fafc")).border(px(1), solid, hex("#e2e8f0")),
            h2(style().fontSize(TEXT_LG).fontWeight(600).color(TEXT), text(title)),
            p(style().marginTop(SP_1).color(TEXT_LIGHT), text(detail))
        );
    }
}
