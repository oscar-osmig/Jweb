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
}
