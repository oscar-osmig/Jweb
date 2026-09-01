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
                    + "offscreen."))
        );
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
