package com.osmig.Jweb.framework.three;

import com.osmig.Jweb.framework.elements.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static jweb.Three.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The walker's body, places that react, and sound — the serialized contract
 * between the Java DSL and the client runtime.
 */
class ThreeBodyDslTest {

    // ==================== The walker's body ====================

    @Test
    void bodyVerbsSerializeUnderTheCameraNode() {
        Map<String, Object> m = camera().walk(1.6, 3, 5)
            .ground(false).fly(6.5).clickToMove().autoStart().radius(0.4)
            .spawn(0, 12, 180).pointerLock().touch().gamepad()
            .toMap();
        assertEquals(List.of(1.6, 3L, 5L), m.get("walk"));
        assertEquals(false, m.get("ground"));
        assertEquals(6.5, m.get("fly"));
        assertEquals(true, m.get("clickMove"));
        assertEquals(true, m.get("autoStart"));
        assertEquals(0.4, m.get("radius"));
        assertEquals(List.of(0L, 12L, 180L), m.get("spawn"));
        assertEquals(true, m.get("plock"));
        assertEquals(true, m.get("touch"));
        assertEquals(true, m.get("gamepad"));
    }

    @Test
    void groundOnIsTheDefaultAndIsNotSerialized() {
        assertFalse(camera().walk(1.7).toMap().containsKey("ground"));
        assertFalse(camera().walk(1.7).ground(true).toMap().containsKey("ground"));
    }

    @Test
    void bodyVerbsImplyWalkModeAtTheDefaultEyeHeight() {
        assertEquals(List.of(1.7), camera().fly(4).toMap().get("walk"));
        assertEquals(List.of(1.7), camera().clickToMove().toMap().get("walk"));
        assertEquals(List.of(1.7), camera().footsteps().toMap().get("walk"));
        // an explicit walk() is never overridden
        assertEquals(List.of(1.5), camera().walk(1.5).fly(4).toMap().get("walk"));
    }

    @Test
    void footstepsSerializeSynthOrClip() {
        assertEquals(Map.of(), camera().footsteps().toMap().get("steps"));
        assertEquals(Map.of("url", "/a/step.mp3", "vol", 0.5),
            camera().footsteps("/a/step.mp3").toMap().get("steps"));
        assertEquals(Map.of("url", "/a/step.mp3", "vol", 0.2),
            camera().footsteps("/a/step.mp3", 0.2).toMap().get("steps"));
    }

    // ==================== Places that react ====================

    @Test
    void solidIsAFootprintOrACylinder() {
        assertEquals(true, box().solid().toMap().get("solid"));
        assertEquals(0.6, cylinder(0.5, 3).solid(0.6).toMap().get("solid"));
        assertEquals(1L, group(box(), sphere()).solid(1).toMap().get("solid"));
        assertFalse(box().toMap().containsKey("solid"));
    }

    @Test
    void linkSerializesOnAnyNode() {
        assertEquals("/worlds/tide", plane(2, 3).link("/worlds/tide").toMap().get("link"));
        assertEquals("/", group(box()).link("/").toMap().get("link"));
    }

    @Test
    void nearAndItsHandlersSerialize() {
        Map<String, Object> plain = sphere().name("veil").near(3).toMap();
        assertEquals(3L, plain.get("near"));
        assertFalse(plain.containsKey("nearH"));

        Map<String, Object> handled = sphere().name("veil")
            .onNear(2.5, e -> {})
            .onFar(e -> {})
            .toMap();
        assertEquals(2.5, handled.get("near"));
        assertNotNull(handled.get("nearH"));
        assertNotNull(handled.get("farH"));
        assertNotEquals(handled.get("nearH"), handled.get("farH"));
    }

    @Test
    void onFarWithoutNearGetsTheDefaultDistance() {
        Map<String, Object> m = box().name("lamp").onFar(e -> {}).toMap();
        assertEquals(3L, m.get("near"));
        assertNotNull(m.get("farH"));
    }

    @Test
    void zoneSerializesNormalizedEdgesAndHandlers() {
        Map<String, Object> m = zone(1.6, 16, -1.6, 14.9).name("door").link("/")
            .onEnter(e -> {}).onLeave(e -> {})
            .toMap();
        assertEquals("zone", m.get("t"));
        assertEquals("door", m.get("name"));
        assertEquals(List.of(-1.6, 14.9, 1.6, 16L), m.get("box"));
        assertEquals("/", m.get("link"));
        assertNotNull(m.get("enterH"));
        assertNotNull(m.get("leaveH"));
    }

    @Test
    void zoneIsASceneNodeLikeAnyOther() {
        Tag t = scene(camera().walk(1.7), zone(-1, -1, 1, 1).name("z"));
        assertTrue(t.toHtml().contains("zone"), t.toHtml());
    }

    // ==================== Sound ====================

    @Test
    void soundSerializesItsOptions() {
        Map<String, Object> ambient = sound("/audio/sea.mp3").loop().volume(0.4).toMap();
        assertEquals("sound", ambient.get("t"));
        assertEquals("/audio/sea.mp3", ambient.get("url"));
        assertEquals(true, ambient.get("loop"));
        assertEquals(0.4, ambient.get("vol"));
        assertFalse(ambient.containsKey("pos"), "no position: a global sound");

        Map<String, Object> placed = sound("/audio/fountain.mp3").position(4, 1, -6).range(3)
            .name("fountain").paused().toMap();
        assertEquals(List.of(4L, 1L, -6L), placed.get("pos"));
        assertEquals(3L, placed.get("ref"));
        assertEquals(true, placed.get("paused"));
        assertEquals("fountain", placed.get("name"));
    }

    @Test
    void soundNeedsAUrl() {
        assertThrows(IllegalArgumentException.class, () -> sound(" "));
    }

    @Test
    void patchesControlSounds() {
        ThreePatchQueue.open();
        try {
            ThreePatch p = patch("world").node("bell").play()
                .node("sea").volume(0.1).tween(800)
                .node("bell").stop();
            List<Map<String, Object>> nodes = p.nodeMaps();
            assertEquals(Map.of("name", "bell", "play", true), nodes.get(0));
            assertEquals(Map.of("name", "sea", "vol", 0.1, "tween", 800), nodes.get(1));
            assertEquals(Map.of("name", "bell", "play", false), nodes.get(2));
        } finally {
            ThreePatchQueue.close();
        }
    }
}
