package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.js.JWebRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static jweb.Three.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The Three DSL vertical slice: scene-graph serialization, the scene element,
 * and the served assets. Angles are degrees end-to-end in the DSL — the
 * client runtime owns the radian conversion.
 */
class ThreeDslTest {

    // ==================== Node serialization ====================

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
    void wholeNumbersSerializeWithoutDecimalPoint() {
        Map<String, Object> m = box().position(1.0, 0.5, -2.0).toMap();
        assertEquals(List.of(1L, 0.5, -2L), m.get("pos"));
    }

    @Test
    void spinDefaultsToAPleasantRate() {
        assertEquals(List.of(20L, 30L, 0L), box().spin().toMap().get("spin"));
        assertEquals(List.of(0L, 45L, 0L), box().spin(45).toMap().get("spin"));
    }

    @Test
    void cameraSerializesFovLookAndOrbit() {
        Map<String, Object> m = camera().fov(60).position(0, 1.5, 4)
            .lookAt(0, 1, 0).orbit().toMap();
        assertEquals("camera", m.get("t"));
        assertEquals(60L, m.get("fov"));
        assertEquals(List.of(0L, 1.5, 4L), m.get("pos"));
        assertEquals(List.of(0L, 1L, 0L), m.get("look"));
        assertEquals(true, m.get("orbit"));
    }

    @Test
    void lightsSerializeIntensityAndColor() {
        Map<String, Object> dir = directionalLight(0.8).color("#fff8e7")
            .position(3, 5, 2).toMap();
        assertEquals("dirLight", dir.get("t"));
        assertEquals(0.8, dir.get("intensity"));
        assertEquals("#fff8e7", dir.get("color"));
        assertEquals(List.of(3L, 5L, 2L), dir.get("pos"));

        assertEquals("ambLight", ambientLight().toMap().get("t"));
    }

    // ==================== The scene element ====================

    @Test
    void sceneRendersDivWithSerializedGraph() {
        String html = scene(box().color("#10b981")).toHtml();
        assertTrue(html.startsWith("<div "), html);
        assertTrue(html.contains("data-three="), html);
        // the JSON is attribute-escaped by the normal pipeline
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
    void bundleShipsInsideTheJarAndCarriesOrbitControls() {
        byte[] bundle = ThreeAssets.bundleBytes();
        assertTrue(bundle.length > 100_000, "bundle suspiciously small: " + bundle.length);
        String head = new String(bundle, 0, 2_000);
        assertTrue(head.contains("three.js " + ThreeAssets.THREE_VERSION),
            "bundle banner must match ThreeAssets.THREE_VERSION — rebuild tools/three-bundle "
            + "or bump the constant; they cache-bust together");
        String all = new String(bundle);
        assertTrue(all.contains("OrbitControls"), "OrbitControls missing from bundle");
    }

    @Test
    void interpreterHandlesTheSceneContract() {
        String js = ThreeRuntime.getScript();
        for (String needle : new String[]{"data-three", "box", "dirLight", "ambLight",
                "OrbitControls", "ResizeObserver", "dispose", "JWebThree"}) {
            assertTrue(js.contains(needle), "interpreter lost handling for: " + needle);
        }
        assertEquals(ThreeRuntime.version(), ThreeRuntime.version(), "version must be stable");
    }

    @Test
    void runtimeStubResolvesAssetVersions() {
        String runtime = JWebRuntime.getScript();
        assertFalse(runtime.contains("__THREE_BUNDLE_V__"), "unresolved bundle version");
        assertFalse(runtime.contains("__THREE_RUNTIME_V__"), "unresolved runtime version");
        assertTrue(runtime.contains("/jweb/three-bundle.js?v=" + ThreeAssets.THREE_VERSION));
        assertTrue(runtime.contains("/jweb/three-runtime.js?v=" + ThreeRuntime.version()));
        assertTrue(runtime.contains("data-three"), "lazy-load stub missing");
    }
}
