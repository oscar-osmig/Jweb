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
                text("Both scenes below are declared like any other element — "
                    + "three.js loads lazily and the runtime owns the render "
                    + "loop, sizing and cleanup. Drag to orbit.")),

            scene(style().marginTop(SP_6).height(px(360)).borderRadius(ROUNDED),
                camera().position(0, 1.5, 4).orbit(),
                directionalLight().position(3, 5, 2),
                ambientLight(0.3),
                box().color("#10b981").spin().name("cube")
            ).id("demo-spin"),

            p(style().marginTop(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("An animated scene: box().color(\"#10b981\").spin() — "
                    + "the render loop only runs because something animates.")),

            scene(style().marginTop(SP_6).height(px(360)).borderRadius(ROUNDED),
                camera().position(3, 2.5, 5).orbit(),
                directionalLight().position(5, 8, 3),
                box(4, 0.2, 4).color("#e2e8f0").position(0, -1.1, 0),
                box().color("#6366f1").rotation(0, 35, 0),
                box(0.6).color("#f59e0b").position(1.4, -0.7, 0.6)
            ).id("demo-static"),

            p(style().marginTop(SP_2).color(TEXT_LIGHT).fontSize(TEXT_SM),
                text("A still scene renders on demand — no animation loop, "
                    + "zero CPU until you drag or resize."))
        );
    }
}
