package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.events.EventRegistry;
import com.osmig.Jweb.framework.js.JWebRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static jweb.Css.hex;
import static jweb.Three.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The Three DSL: scene-graph serialization, the scene element, events, and
 * the served assets. Angles are degrees end-to-end in the DSL — the client
 * runtime owns the radian conversion.
 */
class ThreeDslTest {

    // ==================== Shape serialization ====================

    @Test
    void boxSerializesOnlyWhatWasSet() {
        Map<String, Object> m = box().toMap();
        assertEquals("box", m.get("t"));
        assertEquals(1, m.size(), "unset properties must be omitted: " + m);
    }

    @Test
    void boxSerializesAllProperties() {
        Map<String, Object> m = box(2)
            .position(1, 2, 3)
            .rotation(0, 45, 0)
            .scale(2)
            .color("#10b981")
            .metalness(0.5)
            .roughness(0.25)
            .spin(10, 20, 30)
            .name("cube")
            .toMap();
        assertEquals("box", m.get("t"));
        assertEquals("cube", m.get("name"));
        assertEquals(List.of(2L, 2L, 2L), m.get("size"));
        assertEquals(List.of(1L, 2L, 3L), m.get("pos"));
        assertEquals(List.of(0L, 45L, 0L), m.get("rot"));
        assertEquals(List.of(2L, 2L, 2L), m.get("scl"));
        assertEquals("#10b981", m.get("color"));
        assertEquals(0.5, m.get("metal"));
        assertEquals(0.25, m.get("rough"));
        assertEquals(List.of(10L, 20L, 30L), m.get("spin"));
    }

    @Test
    void everyShapeSerializesItsDimensions() {
        assertEquals(1.5, sphere(1.5).toMap().get("radius"));
        assertEquals(List.of(4L, 3L), plane(4, 3).toMap().get("size"));

        Map<String, Object> cyl = cylinder(0.5, 2).toMap();
        assertEquals(0.5, cyl.get("radius"));
        assertEquals(2L, cyl.get("height"));
        assertEquals(List.of(0.2, 0.8), cylinder().radii(0.2, 0.8).toMap().get("radii"));

        Map<String, Object> cone = cone(1, 2).toMap();
        assertEquals("cone", cone.get("t"));
        assertEquals(1L, cone.get("radius"));
        assertEquals(2L, cone.get("height"));

        Map<String, Object> torus = torus(1.2, 0.3).toMap();
        assertEquals(1.2, torus.get("radius"));
        assertEquals(0.3, torus.get("tube"));
    }

    @Test
    void wholeNumbersSerializeWithoutDecimalPoint() {
        Map<String, Object> m = box().position(1.0, 0.5, -2.0).toMap();
        assertEquals(List.of(1L, 0.5, -2L), m.get("pos"));
    }

    // ==================== Materials ====================

    @Test
    void materialSurfaceSerializes() {
        Map<String, Object> m = sphere()
            .emissive("#ff2200")
            .opacity(0.5)
            .wireframe()
            .texture("/assets/crate.png")
            .toMap();
        assertEquals("#ff2200", m.get("emissive"));
        assertEquals(0.5, m.get("opacity"));
        assertEquals(true, m.get("wire"));
        assertEquals("/assets/crate.png", m.get("map"));
    }

    @Test
    void typedCssColorsWorkEverywhere() {
        assertEquals("#10b981", box().color(hex("#10b981")).toMap().get("color"));
        assertEquals("#101418", background(hex("#101418")).toMap().get("color"));
        assertEquals("#ffffff", directionalLight().color(hex("#ffffff")).toMap().get("color"));
    }

    // ==================== Animation presets ====================

    @Test
    void spinDefaultsToAPleasantRate() {
        assertEquals(List.of(20L, 30L, 0L), box().spin().toMap().get("spin"));
        assertEquals(List.of(0L, 45L, 0L), box().spin(45).toMap().get("spin"));
    }

    @Test
    void floatSerializesAmplitudeAndSpeed() {
        assertEquals(List.of(0.25, 0.4), box().float_().toMap().get("float"));
        assertEquals(List.of(0.5, 1L), model("/a.glb").float_(0.5, 1).toMap().get("float"));
    }

    // ==================== Composition ====================

    @Test
    void groupNestsChildrenAndSharesTransforms() {
        Map<String, Object> m = group(box().name("a"), sphere().name("b"))
            .position(2, 0, 0).spin(15).toMap();
        assertEquals("group", m.get("t"));
        assertEquals(List.of(2L, 0L, 0L), m.get("pos"));
        assertEquals(List.of(0L, 15L, 0L), m.get("spin"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) m.get("children");
        assertEquals(2, children.size());
        assertEquals("a", children.get(0).get("name"));
        assertEquals("sphere", children.get(1).get("t"));
    }

    @Test
    void modelSerializesItsUrl() {
        Map<String, Object> m = model("/assets/rocket.glb").scale(0.5).toMap();
        assertEquals("model", m.get("t"));
        assertEquals("/assets/rocket.glb", m.get("url"));
        assertEquals(List.of(0.5, 0.5, 0.5), m.get("scl"));
    }

    // ==================== Camera ====================

    @Test
    void cameraSerializesFovLookAndOrbit() {
        Map<String, Object> m = camera().fov(60).near(0.5).far(100)
            .position(0, 1.5, 4).lookAt(0, 1, 0).orbit().toMap();
        assertEquals("camera", m.get("t"));
        assertEquals(60L, m.get("fov"));
        assertEquals(0.5, m.get("near"));
        assertEquals(100L, m.get("far"));
        assertEquals(List.of(0L, 1.5, 4L), m.get("pos"));
        assertEquals(List.of(0L, 1L, 0L), m.get("look"));
        assertEquals(true, m.get("orbit"));
    }

    @Test
    void autoRotateImpliesOrbit() {
        Map<String, Object> m = camera().autoRotate().toMap();
        assertEquals(true, m.get("orbit"));
        assertEquals(2L, m.get("auto"));
    }

    // ==================== Lights ====================

    @Test
    void lightsSerializeIntensityAndColor() {
        Map<String, Object> dir = directionalLight(0.8).color("#fff8e7")
            .position(3, 5, 2).toMap();
        assertEquals("dirLight", dir.get("t"));
        assertEquals(0.8, dir.get("intensity"));
        assertEquals("#fff8e7", dir.get("color"));
        assertEquals(List.of(3L, 5L, 2L), dir.get("pos"));

        assertEquals("ambLight", ambientLight().toMap().get("t"));
        assertEquals("pointLight", pointLight(2).toMap().get("t"));

        Map<String, Object> hemi = hemisphereLight("#bde0fe", "#3a5a40").toMap();
        assertEquals("hemiLight", hemi.get("t"));
        assertEquals("#bde0fe", hemi.get("sky"));
        assertEquals("#3a5a40", hemi.get("ground"));
    }

    @Test
    void shadowsFlagSerializesOnShadowCastingLights() {
        assertEquals(true, directionalLight().shadows().toMap().get("shadows"));
        assertEquals(true, pointLight().shadows().toMap().get("shadows"));
        assertNull(directionalLight().toMap().get("shadows"));
    }

    // ==================== Scene settings ====================

    @Test
    void sceneSettingsSerialize() {
        assertEquals(Map.of("t", "bg", "color", "#101418"), background("#101418").toMap());

        Map<String, Object> fog = fog("#101418", 5, 30).toMap();
        assertEquals("fog", fog.get("t"));
        assertEquals(5L, fog.get("near"));
        assertEquals(30L, fog.get("far"));

        Map<String, Object> grid = grid(20, 40).toMap();
        assertEquals("grid", grid.get("t"));
        assertEquals(20L, grid.get("size"));
        assertEquals(40, grid.get("divisions"));
    }

    // ==================== Events ====================

    @Test
    void onClickRegistersARealHandlerAndSerializesItsId() {
        boolean[] called = {false};
        Map<String, Object> m = box().name("die").onClick(e -> called[0] = true).toMap();
        String id = (String) m.get("click");
        assertNotNull(id, "click handler id must serialize");
        assertTrue(id.startsWith("h_"), "unguessable registry id expected: " + id);
        // the id resolves in the same registry the element DSL uses
        assertNotNull(EventRegistry.get(id), "handler must be registered globally outside a render");
        EventRegistry.get(id).handle(null);
        assertTrue(called[0], "dispatching through the registry must reach the lambda");
    }

    @Test
    void clickSwapSerializesUrlAndTarget() {
        Map<String, Object> m = sphere().clickSwap("/api/info", "#panel").toMap();
        assertEquals(Map.of("url", "/api/info", "target", "#panel"), m.get("swap"));
    }

    // ==================== The scene element ====================

    @Test
    void sceneRendersDivWithSerializedGraph() {
        String html = scene(box().color("#10b981")).toHtml();
        assertTrue(html.startsWith("<div "), html);
        assertTrue(html.contains("data-three="), html);
        assertTrue(html.contains("&quot;t&quot;:&quot;box&quot;"), html);
        assertTrue(html.contains("&quot;v&quot;:1"), html);
    }

    @Test
    void sceneNodesAreNotRenderedAsChildren() {
        String html = scene(box(), camera()).toHtml();
        assertFalse(html.contains("Box"), "a ThreeNode must never render as text: " + html);
        assertTrue(html.endsWith("></div>"), "scene div must have no children: " + html);
    }

    @Test
    void sceneAcceptsAttributeItemsAndHtmlChildren() {
        Tag t = scene(jweb.El.attr("class", "hero"), box(),
            jweb.El.p("fallback")).id("demo");
        String html = t.toHtml();
        assertTrue(html.contains("class=\"hero\""), html);
        assertTrue(html.contains("id=\"demo\""), html);
        assertTrue(html.contains("<p>fallback</p>"), html);
    }

    @Test
    void sceneFlattensGroupsOfNodes() {
        List<Box> row = List.of(box().name("a"), box().name("b"));
        String html = scene(row).toHtml();
        assertTrue(html.contains("&quot;name&quot;:&quot;a&quot;"), html);
        assertTrue(html.contains("&quot;name&quot;:&quot;b&quot;"), html);
    }

    @Test
    void nodeOrderIsPreserved() {
        String html = scene(camera(), directionalLight(), box()).toHtml();
        int cam = html.indexOf("camera");
        int light = html.indexOf("dirLight");
        int mesh = html.indexOf("box");
        assertTrue(cam < light && light < mesh, "declaration order must survive: " + html);
    }

    // ==================== Served assets ====================

    @Test
    void bundleShipsInsideTheJarWithItsAddons() {
        byte[] bundle = ThreeAssets.bundleBytes();
        assertTrue(bundle.length > 100_000, "bundle suspiciously small: " + bundle.length);
        String head = new String(bundle, 0, 2_000);
        assertTrue(head.contains("three.js " + ThreeAssets.THREE_VERSION),
            "bundle banner must match ThreeAssets.THREE_VERSION — rebuild tools/three-bundle "
            + "or bump the constant; they cache-bust together");
        String all = new String(bundle);
        assertTrue(all.contains("OrbitControls"), "OrbitControls missing from bundle");
        assertTrue(all.contains("GLTFLoader"), "GLTFLoader missing from bundle");
    }

    @Test
    void bundleVersionIsContentAware() {
        String v = ThreeAssets.bundleVersion();
        assertTrue(v.startsWith(ThreeAssets.THREE_VERSION + "-"),
            "bundle version must be three version + content hash: " + v);
        assertTrue(v.length() > ThreeAssets.THREE_VERSION.length() + 1,
            "content hash missing — a rebuilt bundle would never bust caches: " + v);
    }

    @Test
    void interpreterHandlesTheSceneContract() {
        String js = ThreeRuntime.getScript();
        for (String needle : new String[]{"data-three", "box", "sphere", "plane", "cylinder",
                "cone", "torus", "group", "model", "GLTFLoader", "dirLight", "pointLight",
                "ambLight", "hemiLight", "bg", "fog", "grid", "shadowMap", "Raycaster",
                "autoRotate", "float", "OrbitControls", "ResizeObserver", "dispose",
                "JWebThree", "JWeb.call", "JWeb.swap"}) {
            assertTrue(js.contains(needle), "interpreter lost handling for: " + needle);
        }
        assertEquals(ThreeRuntime.version(), ThreeRuntime.version(), "version must be stable");
    }

    @Test
    void runtimeStubResolvesAssetVersions() {
        String runtime = JWebRuntime.getScript();
        assertFalse(runtime.contains("__THREE_BUNDLE_V__"), "unresolved bundle version");
        assertFalse(runtime.contains("__THREE_RUNTIME_V__"), "unresolved runtime version");
        assertTrue(runtime.contains("/jweb/three-bundle.js?v=" + ThreeAssets.bundleVersion()));
        assertTrue(runtime.contains("/jweb/three-runtime.js?v=" + ThreeRuntime.version()));
        assertTrue(runtime.contains("data-three"), "lazy-load stub missing");
    }

    // ==================== Expansion: shapes ====================

    @Test
    void expansionShapesSerializeTheirDimensions() {
        Map<String, Object> cap = capsule(0.4, 1.5).toMap();
        assertEquals("capsule", cap.get("t"));
        assertEquals(0.4, cap.get("radius"));
        assertEquals(1.5, cap.get("length"));

        assertEquals("disc", disc().toMap().get("t"));
        assertEquals(3L, disc(3).toMap().get("radius"));

        Map<String, Object> rn = ring(0.8, 1).toMap();
        assertEquals("ring", rn.get("t"));
        assertEquals(List.of(0.8, 1L), rn.get("radii"));

        Map<String, Object> knot = torusKnot(1.2, 0.3).toMap();
        assertEquals("knot", knot.get("t"));
        assertEquals(1.2, knot.get("radius"));
        assertEquals(0.3, knot.get("tube"));

        assertEquals("tetra", tetrahedron().toMap().get("t"));
        assertEquals("octa", octahedron(2).toMap().get("t"));
        assertEquals(2L, octahedron(2).toMap().get("radius"));
        assertEquals("dodeca", dodecahedron().toMap().get("t"));
        assertEquals("icosa", icosahedron(1.5).toMap().get("t"));
        // polyhedra carry the full mesh surface
        assertEquals(true, icosahedron().wireframe().toMap().get("wire"));
    }

    // ==================== Expansion: billboards ====================

    @Test
    void billboardSerializesTextStyling() {
        Map<String, Object> m = billboard("Sun")
            .color("#fde68a").background("rgba(15,23,42,0.85)").size(0.8)
            .position(0, 1.6, 0).toMap();
        assertEquals("label", m.get("t"));
        assertEquals("Sun", m.get("text"));
        assertEquals("#fde68a", m.get("color"));
        assertEquals("rgba(15,23,42,0.85)", m.get("bg"));
        assertEquals(0.8, m.get("size"));
        assertEquals(List.of(0L, 1.6, 0L), m.get("pos"));
    }

    @Test
    void spriteSerializesUrlAndSize() {
        Map<String, Object> m = sprite("/assets/pin.png").size(0.6).toMap();
        assertEquals("sprite", m.get("t"));
        assertEquals("/assets/pin.png", m.get("url"));
        assertEquals(0.6, m.get("size"));
    }

    // ==================== Expansion: hover, actions, animation ====================

    @Test
    void hoverEffectsSerialize() {
        assertEquals(1.15, box().hoverScale(1.15).toMap().get("hovScale"));
        // hoverScale lives on ThreeNode — groups and models get it too
        assertEquals(1.2, group(box()).hoverScale(1.2).toMap().get("hovScale"));
        Map<String, Object> m = sphere()
            .hoverColor("#f472b6").hoverEmissive(hex("#331122")).toMap();
        assertEquals("#f472b6", m.get("hovColor"));
        assertEquals("#331122", m.get("hovEmissive"));
    }

    @Test
    void actionsClickRegistersThroughTheRenderContext() {
        var context = com.osmig.Jweb.framework.state.StateManager.createContext();
        try {
            Map<String, Object> m = sphere()
                .onClick(com.osmig.Jweb.framework.js.Actions.show("info-panel")).toMap();
            String id = (String) m.get("clickAct");
            assertNotNull(id, "action id expected in the graph");
            assertTrue(id.matches("a[0-9a-f]{10}"), id);
            String defs = com.osmig.Jweb.framework.js.ClientActions.drainJs(context);
            assertNotNull(defs);
            assertTrue(defs.contains("info-panel"), defs);
        } finally {
            com.osmig.Jweb.framework.state.StateManager.clearContext();
        }
        // outside a render context there is nothing to deliver — no id
        assertNull(sphere()
            .onClick(com.osmig.Jweb.framework.js.Actions.show("x")).toMap().get("clickAct"));
    }

    @Test
    void modelAnimationSerializes() {
        assertEquals(true, model("/a.glb").animate().toMap().get("anim"));
        assertEquals("Walk", model("/a.glb").animate("Walk").toMap().get("anim"));
        assertNull(model("/a.glb").toMap().get("anim"));
    }

    // ==================== Expansion: environment ====================

    @Test
    void environmentAndSkySerialize() {
        Map<String, Object> env = environment("/assets/studio.jpg").toMap();
        assertEquals("env", env.get("t"));
        assertEquals("/assets/studio.jpg", env.get("url"));
        assertNull(env.get("bg"), "environment() must not set the background");

        Map<String, Object> sk = sky("/assets/dusk.jpg").toMap();
        assertEquals("env", sk.get("t"));
        assertEquals(true, sk.get("bg"));
    }

    @Test
    void interpreterHandlesTheExpansionContract() {
        String js = ThreeRuntime.getScript();
        for (String needle : new String[]{"CapsuleGeometry", "CircleGeometry", "RingGeometry",
                "TorusKnotGeometry", "TetrahedronGeometry", "OctahedronGeometry",
                "DodecahedronGeometry", "IcosahedronGeometry", "CanvasTexture",
                "SpriteMaterial", "EquirectangularReflectionMapping", "AnimationMixer",
                "IntersectionObserver", "JWeb.runAction", "hovScale", "hovColor",
                "hovEmissive", "pointerleave", "clips"}) {
            assertTrue(js.contains(needle), "interpreter lost handling for: " + needle);
        }
        // flat shapes render both faces, like plane always has
        assertTrue(js.contains("n.t==='plane'||n.t==='disc'||n.t==='ring'"),
            "disc/ring must be double-sided");
    }

    // ==================== 2.2.3: walk mode & orbit limits ====================

    @Test
    void walkCameraSerializes() {
        Map<String, Object> m = camera().position(0, 2, 6).walk(1.7).bounds(-8, -8, 8, 8).toMap();
        assertEquals(List.of(1.7), m.get("walk"));
        assertEquals(List.of(-8L, -8L, 8L, 8L), m.get("bounds"));

        Map<String, Object> speeds = camera().walk(1.6, 2.5, 6).toMap();
        assertEquals(List.of(1.6, 2.5, 6L), speeds.get("walk"));

        assertEquals(1L, camera().sway().toMap().get("sway"));
        assertEquals(1.5, camera().sway(1.5).toMap().get("sway"));
    }

    @Test
    void orbitLimitsSerializeAndImplyOrbit() {
        Map<String, Object> m = camera().noZoom().noPan().distance(2, 12).polar(20, 90).toMap();
        assertEquals(true, m.get("orbit"), "limits imply orbit — they configure it");
        assertEquals(true, m.get("noZoom"));
        assertEquals(true, m.get("noPan"));
        assertEquals(List.of(2L, 12L), m.get("dist"));
        assertEquals(List.of(20L, 90L), m.get("polar"));
    }

    // ==================== 2.2.3: curves ====================

    @Test
    void curvedShapesSerialize() {
        Map<String, Object> t = tube(0.05, -2, 0, 0, 0, 1.4, 0, 2, 0, 0).closed().toMap();
        assertEquals("tube", t.get("t"));
        assertEquals(0.05, t.get("radius"));
        assertEquals(List.of(-2L, 0L, 0L, 0L, 1.4, 0L, 2L, 0L, 0L), t.get("pts"));
        assertEquals(true, t.get("closed"));

        Map<String, Object> a = arc(1.5, 0.08, 180).toMap();
        assertEquals("arc", a.get("t"));
        assertEquals(1.5, a.get("radius"));
        assertEquals(0.08, a.get("tube"));
        assertEquals(180L, a.get("sweep"));

        Map<String, Object> l = lathe(0, 0, 0.5, 0, 0.3, 1.2).segments(48).toMap();
        assertEquals("lathe", l.get("t"));
        assertEquals(List.of(0L, 0L, 0.5, 0L, 0.3, 1.2), l.get("profile"));
        assertEquals(48, l.get("seg"));
    }

    @Test
    void curvedShapesRejectMalformedGeometry() {
        assertThrows(IllegalArgumentException.class, () -> tube(0.1, 1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> tube(0.1, 1, 2, 3, 4));
        assertThrows(IllegalArgumentException.class, () -> lathe(0.5, 0));
        assertThrows(IllegalArgumentException.class, () -> lathe(0, 0, 1, 2, 3));
    }

    // ==================== 2.2.3: particles ====================

    @Test
    void particlesSerialize() {
        Map<String, Object> m = particles(120).color("#E7D6B1").size(0.03)
            .spread(8, 5, 20).drift().opacity(0.8).seed(7).toMap();
        assertEquals("particles", m.get("t"));
        assertEquals(120, m.get("count"));
        assertEquals("#E7D6B1", m.get("color"));
        assertEquals(0.03, m.get("size"));
        assertEquals(List.of(8L, 5L, 20L), m.get("spread"));
        assertEquals(1L, m.get("drift"));
        assertEquals(0.8, m.get("opacity"));
        assertEquals(7, m.get("seed"));

        assertEquals(3L, particles(400).fall(3).toMap().get("fall"));
        assertThrows(IllegalArgumentException.class, () -> particles(0));
    }

    // ==================== 2.2.3: composition ergonomics ====================

    @Test
    void whenAndRepeatAndListGroupsCompose() {
        assertNull(when(false, box()), "false must yield nothing, not a placeholder");
        assertNotNull(when(true, box()));
        assertNull(when(false, () -> { throw new AssertionError("must not build"); }));

        Map<String, Object> g = group(List.of(box(), sphere())).toMap();
        assertEquals(2, ((List<?>) g.get("children")).size());

        Map<String, Object> r = repeat(4, i -> i == 2 ? null : box().position(i, 0, 0)).toMap();
        assertEquals(3, ((List<?>) r.get("children")).size(), "null indices are skipped");

        // a null branch vanishes from the scene graph entirely
        String html = scene(when(false, box()), sphere()).toHtml();
        assertTrue(html.contains("&quot;t&quot;:&quot;sphere&quot;"), html);
        assertFalse(html.contains("&quot;t&quot;:&quot;box&quot;"), html);
    }

    // ==================== 2.2.3: tone, bloom, mirror ====================

    @Test
    void toneBloomAndMirrorSerialize() {
        assertEquals("tone", toneMapped().toMap().get("t"));
        assertEquals(1.2, toneMapped(1.2).toMap().get("exposure"));

        Map<String, Object> b = bloom().toMap();
        assertEquals("bloom", b.get("t"));
        assertEquals(0.7, b.get("strength"));

        Map<String, Object> full = bloom(1.1, 0.5, 0.9).toMap();
        assertEquals(1.1, full.get("strength"));
        assertEquals(0.5, full.get("radius"));
        assertEquals(0.9, full.get("threshold"));

        Map<String, Object> m = plane(20, 20).mirror().color("#889199").toMap();
        assertEquals(true, m.get("mirror"));
        assertEquals("#889199", m.get("color"));
    }

    // ==================== 2.2.3: live patches ====================

    @Test
    void patchBuildsDegreesAndNarrowedNumbers() {
        ThreePatchQueue.open();
        try {
            Three.patch("world")
                 .node("vane").rotation(0, 24, 0).tween(600)
                 .node("veil").emissive("#1E5D50").opacity(0.5)
                 .camera().position(0, 2, 6).lookAt(0, 3, -6);
            var queued = ThreePatchQueue.drain();
            assertEquals(1, queued.size());
            ThreePatch p = queued.get(0);
            assertEquals("world", p.sceneId());
            assertEquals(2, p.nodeMaps().size());

            Map<String, Object> vane = p.nodeMaps().get(0);
            assertEquals("vane", vane.get("name"));
            assertEquals(List.of(0L, 24L, 0L), vane.get("rot"));
            assertEquals(600, vane.get("tween"));

            Map<String, Object> veil = p.nodeMaps().get(1);
            assertEquals("#1E5D50", veil.get("emissive"));
            assertEquals(0.5, veil.get("opacity"));

            assertEquals(List.of(0L, 2L, 6L), p.cameraMap().get("pos"));
            assertEquals(List.of(0L, 3L, -6L), p.cameraMap().get("look"));
        } finally {
            ThreePatchQueue.close();
        }
    }

    @Test
    void patchOutsideAHandlerIsDroppedNotThrown() {
        ThreePatchQueue.close();   // make sure no window is open
        Three.patch("world").node("vane").rotation(0, 10, 0);   // warns, doesn't throw
        ThreePatchQueue.open();
        assertTrue(ThreePatchQueue.drain().isEmpty(),
            "a patch born outside the window must not leak into the next one");
    }

    @Test
    void patchPropertiesNeedATargetFirst() {
        ThreePatchQueue.open();
        try {
            assertThrows(IllegalStateException.class,
                () -> Three.patch("world").rotation(0, 10, 0));
        } finally {
            ThreePatchQueue.close();
        }
    }

    // ==================== 2.2.3: interpreter & bundle contract ====================

    @Test
    void interpreterHandlesTheWalkAndPatchContract() {
        String js = ThreeRuntime.getScript();
        for (String needle : new String[]{"TubeGeometry", "CatmullRomCurve3", "LatheGeometry",
                "PointsMaterial", "BufferAttribute", "Reflector", "EffectComposer",
                "UnrealBloomPass", "OutputPass", "ACESFilmicToneMapping",
                "data-three-walk", "three-walking", "jweb:three-walk",
                "prefers-reduced-motion", "applyPatch", "ready:", "THREE:THREE",
                "setWalk", "minPolarAngle", "minDistance", "enableZoom", "enablePan"}) {
            assertTrue(js.contains(needle), "interpreter lost handling for: " + needle);
        }
    }

    @Test
    void runtimeDispatchesLivePatches() {
        String runtime = JWebRuntime.getScript();
        assertTrue(runtime.contains("threePatch"), "runtime must route threePatch messages");
        assertTrue(runtime.contains("JWebThree.applyPatch"),
            "threePatch must hand off to the three runtime");
    }

    @Test
    void bundleShipsThePostProcessingAddons() {
        String all = new String(ThreeAssets.bundleBytes());
        for (String needle : new String[]{"EffectComposer", "UnrealBloomPass", "OutputPass",
                "Reflector"}) {
            assertTrue(all.contains(needle), needle + " missing from bundle — re-run "
                + "tools/three-bundle/build.sh");
        }
    }

    // ==================== 2.3: wires, sweeps, terrain ====================

    @Test
    void wireSerializesPointsAndStyling() {
        Map<String, Object> m = wire(-2, 0, 0, 0, 1.4, 0, 2, 0, 0)
            .color("#fde68a").opacity(0.7).closed().dashed(0.2, 0.1).draw(1200, 300).toMap();
        assertEquals("line", m.get("t"));
        assertEquals(List.of(-2L, 0L, 0L, 0L, 1.4, 0L, 2L, 0L, 0L), m.get("pts"));
        assertEquals("#fde68a", m.get("color"));
        assertEquals(0.7, m.get("opacity"));
        assertEquals(true, m.get("closed"));
        assertEquals(List.of(0.2, 0.1), m.get("dash"));
        assertEquals(List.of(1200, 300), m.get("draw"));
        assertEquals(List.of(800, 0), wire(0, 0, 0, 1, 1, 1).draw(800).toMap().get("draw"));
        assertEquals(2, wire(0, 0, 0, 1, 1, 1).toMap().size(), "only t and pts when nothing is set");
        assertEquals("#fde68a", wire(0, 0, 0, 1, 1, 1).color(hex("#fde68a")).toMap().get("color"));
    }

    @Test
    void wireRejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> wire(1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> wire(1, 2, 3, 4, 5, 6, 7));
        assertThrows(IllegalArgumentException.class, () -> wire(0, 0, 0, 1, 1, 1).dashed(0, 1));
        assertThrows(IllegalArgumentException.class, () -> wire(0, 0, 0, 1, 1, 1).draw(0));
        assertThrows(IllegalArgumentException.class, () -> wire(0, 0, 0, 1, 1, 1).draw(500, -1));
    }

    @Test
    void sweepSerializesProfileAndCurve() {
        Map<String, Object> m = sweep(0.12, 0.06, -3, 0, 0, 0, 2.2, 0, 3, 0, 0).closed().steps(64)
            .color("#A07C4B").metalness(0.85).toMap();
        assertEquals("sweep", m.get("t"));
        assertEquals(List.of(0.12, 0.06), m.get("profile"));
        assertEquals(List.of(-3L, 0L, 0L, 0L, 2.2, 0L, 3L, 0L, 0L), m.get("pts"));
        assertEquals(true, m.get("closed"));
        assertEquals(64, m.get("steps"));
        assertEquals(0.85, m.get("metal"), "a sweep carries the full mesh surface");
        assertNull(sweep(0.1, 0.1, 0, 0, 0, 1, 1, 1).toMap().get("steps"), "default steps stay client-side");
        assertThrows(IllegalArgumentException.class, () -> sweep(0, 0.1, 0, 0, 0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> sweep(0.1, 0.1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> sweep(0.1, 0.1, 0, 0, 0, 1, 1, 1).steps(0));
    }

    @Test
    void terrainSerializesSizeHillsSeedAndDetail() {
        Map<String, Object> flat = terrain(60, 40).toMap();
        assertEquals("terrain", flat.get("t"));
        assertEquals(List.of(60L, 40L), flat.get("size"));
        assertEquals(2, flat.size(), "a bare terrain is a flat plane: " + flat);

        Map<String, Object> m = terrain(60, 60).hills(1.5).seed(4).detail(128).color("#5B7F4A").toMap();
        assertEquals(List.of(1.5), m.get("hills"), "feature scale defaults client-side to width/4");
        assertEquals(4, m.get("seed"));
        assertEquals(128, m.get("seg"));
        assertEquals("#5B7F4A", m.get("color"));

        assertEquals(List.of(3L, 12L), terrain(60, 60).hills(3, 12).toMap().get("hills"));
        assertEquals(8, terrain(10, 10).detail(2).toMap().get("seg"), "detail clamps low");
        assertEquals(256, terrain(10, 10).detail(999).toMap().get("seg"), "detail clamps high");
        assertThrows(IllegalArgumentException.class, () -> terrain(0, 10));
        assertThrows(IllegalArgumentException.class, () -> terrain(10, 10).hills(-1));
        assertThrows(IllegalArgumentException.class, () -> terrain(10, 10).hills(1, 0));
    }

    // ==================== 2.3: spot lights ====================

    @Test
    void spotLightSerializesConeAndTarget() {
        Map<String, Object> m = spotLight(40).color("#fff4e0").position(0, 6, 0)
            .target(0, 1, 0).angle(25).penumbra(0.4).shadows().toMap();
        assertEquals("spotLight", m.get("t"));
        assertEquals(40L, m.get("intensity"));
        assertEquals("#fff4e0", m.get("color"));
        assertEquals(List.of(0L, 6L, 0L), m.get("pos"));
        assertEquals(List.of(0L, 1L, 0L), m.get("target"));
        assertEquals(25L, m.get("angle"));
        assertEquals(0.4, m.get("penumbra"));
        assertEquals(true, m.get("shadows"));
        assertEquals(1, spotLight().toMap().size(), "defaults stay client-side");
        assertEquals("#fde68a", spotLight().color(hex("#fde68a")).toMap().get("color"));
        assertThrows(IllegalArgumentException.class, () -> spotLight().angle(0));
        assertThrows(IllegalArgumentException.class, () -> spotLight().angle(91));
        assertThrows(IllegalArgumentException.class, () -> spotLight().penumbra(1.5));
    }

    // ==================== 2.3: materials ====================

    @Test
    void glassSerializesTransmission() {
        assertEquals(1L, box().glass().toMap().get("glass"));
        assertEquals(0.6, sphere().glass(0.6).toMap().get("glass"));
        assertNull(box().toMap().get("glass"));
        assertThrows(IllegalArgumentException.class, () -> box().glass(1.2));
    }

    @Test
    void materialPresetCopiesOnlyWhatItSet() {
        Material brass = material().color("#A07C4B").metalness(0.85).roughness(0.35);
        Map<String, Object> m = box().material(brass).toMap();
        assertEquals("#A07C4B", m.get("color"));
        assertEquals(0.85, m.get("metal"));
        assertEquals(0.35, m.get("rough"));
        assertNull(m.get("opacity"));
        assertNull(m.get("wire"));
        assertNull(m.get("glass"));

        Map<String, Object> full = sphere().material(material().emissive(hex("#331100")).opacity(0.5)
            .wireframe().texture("/a.png").glass(0.8)).toMap();
        assertEquals("#331100", full.get("emissive"));
        assertEquals(0.5, full.get("opacity"));
        assertEquals(true, full.get("wire"));
        assertEquals("/a.png", full.get("map"));
        assertEquals(0.8, full.get("glass"));

        // one preset serves any number of shapes; each keeps its own identity
        assertEquals("#A07C4B", cylinder().material(brass).toMap().get("color"));
        assertEquals("cylinder", cylinder().material(brass).toMap().get("t"));
    }

    @Test
    void materialPresetOverrideOrderIsCallOrder() {
        Material brass = material().color("#A07C4B").roughness(0.35);
        assertEquals(0.1, box().material(brass).roughness(0.1).toMap().get("rough"), "a later call wins");
        assertEquals(0.35, box().roughness(0.1).material(brass).toMap().get("rough"), "a later preset wins");
        assertEquals("#fff", box().material(brass).color("#fff").toMap().get("color"));
        // a field the preset never set can't clobber an explicit one
        assertEquals(0.9, box().metalness(0.9).material(brass).toMap().get("metal"));
        assertThrows(IllegalArgumentException.class, () -> box().material(null));
        assertThrows(IllegalArgumentException.class, () -> material().glass(-0.1));
    }

    // ==================== 2.3: mirrors, instancing, particle palettes ====================

    @Test
    void mirrorStrengthSerializesAndKeepsTheBooleanForm() {
        assertEquals(true, plane(20, 20).mirror().toMap().get("mirror"), "the no-arg form stays true");
        assertEquals(true, plane(20, 20).mirror(1).toMap().get("mirror"));
        assertEquals(0.4, plane(20, 20).mirror(0.4).toMap().get("mirror"));
        assertEquals(0L, plane(20, 20).mirror(0).toMap().get("mirror"));
        assertThrows(IllegalArgumentException.class, () -> plane().mirror(1.5));
        assertThrows(IllegalArgumentException.class, () -> plane().mirror(-0.1));
    }

    @Test
    void instancedGroupSerializesTheFlag() {
        Map<String, Object> m = repeat(3, i -> box(0.3).position(i, 0, 0)).instanced().spin(10).toMap();
        assertEquals("group", m.get("t"));
        assertEquals(true, m.get("inst"));
        assertEquals(3, ((List<?>) m.get("children")).size());
        assertEquals(List.of(0L, 10L, 0L), m.get("spin"), "the group itself still animates");
        assertNull(group(box()).toMap().get("inst"));
    }

    @Test
    void particlePaletteSerializes() {
        Map<String, Object> m = particles(200).colors("#ff7a00", "#ffb347", "#ff3d00").toMap();
        assertEquals(List.of("#ff7a00", "#ffb347", "#ff3d00"), m.get("colors"));
        assertEquals(List.of("#ff7a00", "#ffb347"),
            particles(10).colors(hex("#ff7a00"), hex("#ffb347")).toMap().get("colors"));
        assertNull(particles(10).toMap().get("colors"));
        assertEquals(List.of("#fff"), particles(10).colors("#fff").toMap().get("colors"));
        // colors() with no arguments is a compile error by design (a required first color);
        // blank or null entries fail loudly instead of painting particles black
        assertThrows(IllegalArgumentException.class, () -> particles(10).colors("#fff", " "));
        assertThrows(IllegalArgumentException.class, () -> particles(10).colors((String) null));
        assertThrows(IllegalArgumentException.class, () -> particles(10).colors((jweb.CSSValue) null));
    }

    // ==================== 2.3: motion presets ====================

    @Test
    void motionPresetsSerialize() {
        assertEquals(List.of(0.08, 0.5), box().pulse().toMap().get("pulse"));
        assertEquals(List.of(0.1, 0.7), box().pulse(0.1, 0.7).toMap().get("pulse"));
        assertEquals(0.6, torus().glow().toMap().get("glow"));
        assertEquals(0.5, torus().glow(0.5).toMap().get("glow"));
        assertEquals(List.of(600, 0), box().appear(600).toMap().get("appear"));
        assertEquals(List.of(500, 200), box().appear(500, 200).toMap().get("appear"));
        assertEquals(220, box().delay(220).toMap().get("delay"));
        // presets live on ThreeNode: groups, models and billboards take them too
        assertEquals(List.of(0.08, 0.5), group(box()).pulse().toMap().get("pulse"));
        assertEquals(List.of(400, 0), billboard("Hi").appear(400).toMap().get("appear"));
        assertEquals(List.of(300, 0), model("/a.glb").appear(300).toMap().get("appear"));
        assertNull(box().toMap().get("delay"));
    }

    @Test
    void followSerializesAClosedPath() {
        Map<String, Object> m = cone(0.2, 0.6).rotation(90, 0, 0)
            .follow(4, 3, 2, -2, 4, 2.5, -1, 3, 3, 0).toMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> f = (Map<String, Object>) m.get("follow");
        assertEquals(4L, f.get("sec"));
        assertEquals(List.of(3L, 2L, -2L, 4L, 2.5, -1L, 3L, 3L, 0L), f.get("pts"));
        assertEquals(List.of(90L, 0L, 0L), m.get("rot"), "the declared rotation rides along as the offset");
    }

    @Test
    void motionPresetsRejectNonsense() {
        assertThrows(IllegalArgumentException.class, () -> box().pulse(0, 1));
        assertThrows(IllegalArgumentException.class, () -> box().pulse(0.1, 0));
        assertThrows(IllegalArgumentException.class, () -> box().glow(0));
        assertThrows(IllegalArgumentException.class, () -> box().appear(0));
        assertThrows(IllegalArgumentException.class, () -> box().appear(100, -1));
        assertThrows(IllegalArgumentException.class, () -> box().delay(-5));
        assertThrows(IllegalArgumentException.class, () -> box().follow(0, 0, 0, 0, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> box().follow(4, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> box().follow(4, 0, 0, 0, 1, 1));
    }

    // ==================== 2.3: structural patches ====================

    @Test
    void patchAddsRemovesAndReplacesNodes() {
        ThreePatchQueue.open();
        try {
            Three.patch("hall")
                 .add(sphere(0.3).name("orb"), box().name("crate"))
                 .addTo("shelf", cone().name("hat"))
                 .remove("old", "older")
                 .replace("lamp", pointLight(2).name("lamp2"))
                 .replace("vase", lathe(0, 0, 0.5, 0, 0.3, 1))
                 .node("orb").emissive("#5FA98A");
            ThreePatch p = ThreePatchQueue.drain().get(0);

            List<Map<String, Object>> adds = p.addMaps();
            assertEquals(5, adds.size());
            assertEquals(Map.of("t", "sphere", "name", "orb", "radius", 0.3), adds.get(0).get("node"));
            assertNull(adds.get(0).get("into"), "root additions carry no parent");
            assertEquals("crate", ((Map<?, ?>) adds.get(1).get("node")).get("name"));
            assertEquals("shelf", adds.get(2).get("into"));
            assertEquals("hat", ((Map<?, ?>) adds.get(2).get("node")).get("name"));
            assertEquals("lamp", adds.get(3).get("replaces"));
            assertEquals("lamp2", ((Map<?, ?>) adds.get(3).get("node")).get("name"), "an explicit name is kept");
            assertEquals("vase", ((Map<?, ?>) adds.get(4).get("node")).get("name"),
                "an unnamed replacement inherits the replaced name");
            assertEquals(List.of("old", "older", "lamp", "vase"), p.removeNames(),
                "replace removes first; removes always precede adds on the client");
            // node patches still ride alongside, and may target an addition
            assertEquals(1, p.nodeMaps().size());
            assertEquals("orb", p.nodeMaps().get(0).get("name"));
        } finally {
            ThreePatchQueue.close();
        }
    }

    @Test
    void structuralPatchVerbsValidateAndCloseTheCurrentTarget() {
        ThreePatchQueue.open();
        try {
            assertThrows(IllegalStateException.class,
                () -> Three.patch("hall").node("a").add(box()).color("#fff"),
                "after a structural verb, properties need a fresh .node()/.camera()");
            assertThrows(IllegalArgumentException.class, () -> Three.patch("hall").addTo(" ", box()));
            assertThrows(IllegalArgumentException.class, () -> Three.patch("hall").replace("x", null));
            assertThrows(IllegalArgumentException.class, () -> Three.patch("hall").replace("", box()));
            Three.patch("hall").add((ThreeNode<?>) null).remove((String) null, "");
            var all = ThreePatchQueue.drain();
            ThreePatch p = all.get(all.size() - 1);
            assertTrue(p.addMaps().isEmpty(), "nulls are skipped, like everywhere in the DSL");
            assertTrue(p.removeNames().isEmpty(), "blank names are skipped");
        } finally {
            ThreePatchQueue.close();
        }
    }

    @Test
    void patchResponseCarriesStructureAndStaysCompatible() {
        var full = new com.osmig.Jweb.framework.websocket.WebSocketMessage.ThreePatchResponse(
            "hall", List.of(), null, List.of(Map.of("node", Map.of("t", "box"))), List.of("old"));
        String json = com.osmig.Jweb.framework.util.Json.stringify(full);
        assertTrue(json.contains("\"type\":\"threePatch\""), json);
        assertTrue(json.contains("\"add\":[{\"node\":{\"t\":\"box\"}}]"), json);
        assertTrue(json.contains("\"remove\":[\"old\"]"), json);
        assertFalse(json.contains("\"nodes\""), "an empty node list is omitted: " + json);

        var legacy = new com.osmig.Jweb.framework.websocket.WebSocketMessage.ThreePatchResponse(
            "hall", List.of(Map.of("name", "a", "visible", true)), null);
        String old = com.osmig.Jweb.framework.util.Json.stringify(legacy);
        assertFalse(old.contains("\"add\""), "no structure, no keys: " + old);
        assertFalse(old.contains("\"remove\""), old);
    }

    // ==================== 2.3: interpreter contract ====================

    @Test
    void interpreterHandlesTheSurfaceAndMotionContract() {
        String js = ThreeRuntime.getScript();
        for (String needle : new String[]{"ExtrudeGeometry", "LineDashedMaterial", "LineBasicMaterial",
                "computeLineDistances", "SpotLight", "MeshPhysicalMaterial", "transmission",
                "InstancedMesh", "setColorAt", "vertexColors", "emissiveIntensity", "getPointAt",
                "n.t==='sweep'", "n.t==='terrain'", "n.t==='line'", "n.t==='spotLight'",
                "n.inst", "n.glass", "n.pulse", "n.glow", "n.appear", "n.delay", "n.follow",
                "n.colors", "n.draw", "n.dash", "n.mirror===true", "msg.remove", "msg.add",
                "entry.into", "entry.replaces", "jwebTarget", "isInstancedMesh"}) {
            assertTrue(js.contains(needle), "interpreter lost handling for: " + needle);
        }
    }
}
