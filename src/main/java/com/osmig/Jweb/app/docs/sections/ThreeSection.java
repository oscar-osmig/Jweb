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

            since("v2.2.0",
                docSubtitle("More Shapes & Billboards"),
                para("torusKnot(), capsule(), disc(), ring() and the four platonic " +
                     "solids join the geometry set. Two nodes always face the camera: " +
                     "billboard(text) renders crisp text with no font file, sprite(url) " +
                     "shows an image."),
                codeBlock("""
                        torusKnot(1, 0.28).metalness(0.6).spin(),
                        icosahedron(0.7).wireframe().color("#22d3ee"),
                        billboard("Sun").background("rgba(15,23,42,0.85)")
                            .size(0.5).position(0, 1.8, 0),
                        sprite("/assets/pin.png").size(0.6)"""),

                docSubtitle("Hover & Client-Side Clicks"),
                para("Hover effects are declared on the shape and run entirely in the " +
                     "browser — applied on raycast enter, restored on leave. onClick " +
                     "also takes an Actions-DSL handler: dispatched client-side, " +
                     "CSP-safe, no server round-trip."),
                codeBlock("""
                        torusKnot()
                            .hoverScale(1.1)              // grows while hovered
                            .hoverEmissive("#4c1d95")     // glow highlight
                            .onClick(toggle("info-panel"))"""),

                docSubtitle("Environment & Model Animation"),
                para("An equirectangular panorama lights the scene — sky(url) also " +
                     "shows it as the backdrop. Models that ship animation clips play " +
                     "them with .animate(); animated scenes pause their render loop " +
                     "whenever they scroll offscreen."),
                codeBlock("""
                        sky("/assets/dusk.jpg"),
                        sphere().metalness(1).roughness(0.05),   // mirrors the sky
                        model("/assets/robot.glb").animate("Walk")""")
            ),

            since("v2.2.3",
                docSubtitle("Walk Mode"),
                para("camera().walk(eyeHeight) turns a scene into a place: W A S D and " +
                     "arrows move, dragging looks, Shift runs, Esc steps back out to " +
                     "the framed view. bounds(...) fences the floor, sway() gives the " +
                     "framed camera a gentle idle drift (skipped under reduced motion), " +
                     "and the toggle is any element carrying data-three-walk — no " +
                     "script. While walking, the scene, the toggle and <body> carry a " +
                     "three-walking class."),
                codeBlock("""
                        scene(camera().position(0, 2, 7).lookAt(0, 2, 0)
                                  .walk(1.7).bounds(-8, -8, 8, 8).sway(),
                            ...).id("hall"),

                        button(data("three-walk", "hall"),
                            "Walk here")"""),

                docSubtitle("Live Patches"),
                para("Three.patch updates named nodes in the rendered scene from a " +
                     "server event handler — riding the WebSocket answer already in " +
                     "flight. No reload, no rebuild; the visitor doesn't move. " +
                     "tween(ms) eases the change; .camera() glides the framing."),
                codeBlock("""
                        sphere().name("lantern").onClick(e ->
                            Three.patch("hall")
                                 .node("lantern").emissive("#22d3ee").tween(500)
                                 .node("key-light").intensity(1.6).tween(500)
                                 .camera().lookAt(0, 2, -6).tween(1200))"""),

                docSubtitle("Curves, Particles & Repetition"),
                para("tube() sweeps a smooth curve through points — vines, pipes, rails " +
                     "— arc() is a partial ring for archways, lathe() spins a profile " +
                     "into pots, columns and domes. particles(n) is a whole drifting or " +
                     "falling cloud in one node and one draw call. when() and repeat() " +
                     "keep conditional and repeated structure declarative."),
                codeBlock("""
                        tube(0.05, -2, 0, 0,  0, 1.4, 0,  2, 0, 0),
                        arc(1.6, 0.09, 180).position(0, 2.1, -4.5),
                        lathe(0, 0,  0.5, 0,  0.35, 0.8,  0.3, 1.3),
                        particles(140).size(0.02).spread(6, 3, 10).drift(),
                        particles(400).size(0.02).spread(12, 8, 12).fall(3),
                        when(doorOpen, group(...)),
                        repeat(10, i -> cylinder(0.08, 1.5)
                            .position(-2.7 + i * 0.6, 0.75, 0))"""),

                docSubtitle("Glow, Tone & Mirrors"),
                para("bloom() makes bright emissive surfaces actually glow — an HDR " +
                     "pass composited before tone mapping, which it implies. " +
                     "toneMapped() alone applies the cinematic ACES curve. " +
                     "plane().mirror() is a real-time reflection — a polished floor " +
                     "under a translucent overlay reads as satin."),
                codeBlock("""
                        scene(bloom(),
                            sphere().emissive("#5FA98A"),        // now it glows
                            plane(20, 20).flat()
                                .mirror().color("#4a443e"))"""),

                docSubtitle("Scripting Interop"),
                para("For scripts that do reach into three.js: JWebThree.ready(id, cb) " +
                     "runs when the scene exists (no polling), and JWebThree.THREE is " +
                     "the bundled module itself — construct vectors and materials " +
                     "without prototype tricks."),
                codeBlock("""
                        JWebThree.ready('hall', h => {
                            const v = new JWebThree.THREE.Vector3(0, 1, 0);
                            h.objects.moon.position.add(v);
                        });""")
            ),

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
