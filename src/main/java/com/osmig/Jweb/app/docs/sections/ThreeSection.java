package com.osmig.Jweb.app.docs.sections;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class ThreeSection {
    private ThreeSection() {}

    public static Element render() {
        return section(
            docTitle("3D Scenes"),
            para("Declarative 3D rendered with three.js, declared the way you declare " +
                 "HTML. The runtime owns the ceremony — renderer setup, sizing, the " +
                 "render loop, shadow maps, and disposal — so a complete animated scene " +
                 "is a few lines of Java. See /demo/three for it running live."),

            codeBlock("""
                    import static jweb.Three.*;

                    div(class_("hero"),
                        scene(style().height(px(420)),
                            camera().position(0, 1.5, 4).orbit(),
                            directionalLight().position(3, 5, 2),
                            box().color("#10b981").spin()
                        )
                    )"""),
            docTip("three.js loads lazily — only pages containing a scene fetch the " +
                   "bundle (~170KB gzipped, immutable-cached, served from your jar). " +
                   "Pages without scenes ship zero extra bytes."),

            docSubtitle("Shapes & Materials"),
            para("box, sphere, plane, cylinder, cone and torus, with three.js's " +
                 "physically-based standard material under the platform's own names. " +
                 "All angles in the DSL are degrees."),
            codeBlock("""
                    sphere(0.9)
                        .position(0, 0.9, 0)
                        .color("#f59e0b")
                        .metalness(0.7).roughness(0.25)
                        .texture("/assets/crate.png")"""),

            docSubtitle("Lights & Shadows"),
            para("directionalLight, ambientLight, pointLight and hemisphereLight. A " +
                 "scene with no lights gets a soft default so nothing renders black. " +
                 "Shadows — normally a five-place chore — are one call:"),
            codeBlock("""
                    directionalLight(1.2).position(5, 8, 4).shadows()"""),

            docSubtitle("Camera & Atmosphere"),
            codeBlock("""
                    camera().position(6, 4, 8).lookAt(0, 1, 0).autoRotate(),
                    background("#0f172a"),
                    fog("#0f172a", 10, 30),
                    grid()   // placement aid while composing"""),

            docSubtitle("Animation"),
            para("A render loop runs only when something animates; still scenes render " +
                 "on demand and cost zero CPU."),
            codeBlock("""
                    box().spin(45)                       // degrees per second
                    model("/assets/drone.glb").float_()  // gentle hover"""),

            docSubtitle("Groups & Models"),
            codeBlock("""
                    group(
                        cylinder(0.1, 2).color("#8899aa"),
                        cone(0.4, 0.6).position(0, 1.3, 0).color("#cc4444")
                    ).position(-2, 0, 0).spin(15),

                    model("/assets/rocket.glb").scale(0.5)   // plain .glb/.gltf"""),

            docSubtitle("Clickable Objects"),
            para("Clicks raycast into the scene and dispatch through the same pipelines " +
                 "as element events — swap a server-rendered fragment, or run a server " +
                 "handler over the live WebSocket. The event's value() carries the " +
                 "node's name."),
            codeBlock("""
                    sphere().name("product")
                        .clickSwap("/api/product/42", "#detail-panel")

                    box().name("die")
                        .onClick(e -> rolls.set(rolls.get() + 1))"""),

            docSubtitle("Escape Hatch"),
            para("The DSL covers scenes, not shaders. Give a scene an id and the live " +
                 "three.js objects are exposed to scripts:"),
            codeBlock("""
                    // scene(...).id("hero") then, from any script:
                    // JWebThree.get('hero') -> {scene, camera, renderer, controls, objects}
                    var hero = JWebThree.get('hero');
                    hero.objects.moon.material.emissiveIntensity = 2;"""),
            para("The full guide — sizing, performance notes, limitations — lives in " +
                 "readme/three-dsl.md.")
        );
    }
}
