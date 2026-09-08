package com.osmig.Jweb.framework.routing;

import jweb.BindException;
import jweb.JWeb;
import jweb.JWebTest;
import jweb.MockRequest;
import jweb.Request;
import jweb.Response;
import jweb.api.Length;
import jweb.api.Pattern;
import jweb.api.Range;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RecordBinderTest {

    enum Setting { EBB, SLACK, FLOOD }

    record Vane(@Range(min = 1, max = 3) int n, Setting set) {}

    record Pref(Optional<Setting> sound, Optional<Boolean> motion, boolean contrast) {}

    record Everything(String s, int i, long l, double d, boolean b, UUID id,
                      Optional<Integer> maybe, List<String> tags) {}

    record Note(@Length(min = 1, max = 5) String text, @Pattern(value = "[a-z-]+", message = "slug: lowercase and dashes only") String slug) {}

    record Checked(int n) {
        Checked {
            if (n % 2 != 0) throw new IllegalArgumentException("n must be even");
        }
    }

    record Unsupported(java.time.LocalDate when) {}

    private static Request req(String... kv) {
        MockRequest mock = MockRequest.get("/act");
        for (int i = 0; i < kv.length; i += 2) mock.queryParam(kv[i], kv[i + 1]);
        return mock.build();
    }

    // ==================== happy path ====================

    @Test
    void bindsEnumsByNameCaseInsensitive() {
        Vane vane = RecordBinder.bind(Vane.class, req("n", "2", "set", "flood"));
        assertEquals(2, vane.n());
        assertEquals(Setting.FLOOD, vane.set());
    }

    @Test
    void bindsEveryScalarType() {
        UUID id = UUID.randomUUID();
        Everything e = RecordBinder.bind(Everything.class, MockRequest.get("/x")
            .queryParam("s", "hi").queryParam("i", "7").queryParam("l", "9000000000")
            .queryParam("d", "1.5").queryParam("b", "on").queryParam("id", id.toString())
            .queryParams("tags", "a", "b").build());
        assertEquals("hi", e.s());
        assertEquals(7, e.i());
        assertEquals(9_000_000_000L, e.l());
        assertEquals(1.5, e.d());
        assertTrue(e.b());
        assertEquals(id, e.id());
        assertEquals(Optional.empty(), e.maybe());
        assertEquals(List.of("a", "b"), e.tags());
    }

    @Test
    void optionalIsEmptyWhenAbsentOrBlankAndPrimitiveBooleanDefaultsToFalse() {
        Pref none = RecordBinder.bind(Pref.class, req());
        assertEquals(Optional.empty(), none.sound());
        assertEquals(Optional.empty(), none.motion());
        assertFalse(none.contrast());

        Pref some = RecordBinder.bind(Pref.class, req("sound", "ebb", "motion", "", "contrast", "true"));
        assertEquals(Optional.of(Setting.EBB), some.sound());
        assertEquals(Optional.empty(), some.motion(), "blank reads as absent");
        assertTrue(some.contrast());
    }

    @Test
    void optionalWithABadValueIsStillAnError() {
        BindException e = assertThrows(BindException.class,
            () -> RecordBinder.bind(Pref.class, req("motion", "maybe")));
        assertEquals("motion", e.field());
        assertEquals("motion must be true or false", e.getMessage());
    }

    @Test
    void requestBindIsTheSameBinder() {
        Vane vane = req("n", "1", "set", "ebb").bind(Vane.class);
        assertEquals(Setting.EBB, vane.set());
    }

    // ==================== each failure ====================

    @Test
    void missingRequiredComponent() {
        BindException e = assertThrows(BindException.class, () -> RecordBinder.bind(Vane.class, req("set", "ebb")));
        assertEquals("n", e.field());
        assertEquals("n is required", e.getMessage());
    }

    @Test
    void malformedNumber() {
        BindException e = assertThrows(BindException.class, () -> RecordBinder.bind(Vane.class, req("n", "two", "set", "ebb")));
        assertEquals("n must be a whole number", e.getMessage());
    }

    @Test
    void outOfRange() {
        BindException e = assertThrows(BindException.class, () -> RecordBinder.bind(Vane.class, req("n", "4", "set", "ebb")));
        assertEquals("n must be between 1 and 3", e.getMessage());
    }

    @Test
    void unknownEnumConstant() {
        BindException e = assertThrows(BindException.class, () -> RecordBinder.bind(Vane.class, req("n", "1", "set", "tsunami")));
        assertEquals("set must be one of ebb, slack, flood", e.getMessage());
    }

    @Test
    void lengthAndPattern() {
        assertEquals("text must be between 1 and 5 characters",
            assertThrows(BindException.class, () -> RecordBinder.bind(Note.class, req("text", "toolong", "slug", "ok"))).getMessage());
        assertEquals("slug: lowercase and dashes only",
            assertThrows(BindException.class, () -> RecordBinder.bind(Note.class, req("text", "ok", "slug", "Nope!"))).getMessage());
        assertEquals("ok", RecordBinder.bind(Note.class, req("text", "ok", "slug", "fine-slug")).text());
    }

    @Test
    void compactConstructorValidationBecomesABindError() {
        BindException e = assertThrows(BindException.class, () -> RecordBinder.bind(Checked.class, req("n", "3")));
        assertEquals("n must be even", e.getMessage());
        assertEquals(4, RecordBinder.bind(Checked.class, req("n", "4")).n());
    }

    @Test
    void unsupportedComponentTypeFailsAtRegistration() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RecordBinder.check(Unsupported.class));
        assertTrue(e.getMessage().contains("LocalDate"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> RecordBinder.check(String.class));
    }

    // ==================== through the app ====================

    @Test
    void actionRouteBindsAndAnswersBindFailuresWith400() {
        JWeb app = JWeb.create().action("/act/vane", Vane.class,
            (req, vane) -> Response.redirect("/tide").anchor(vane.set() == Setting.FLOOD ? "content" : "chamber"));

        var ok = JWebTest.test(app, MockRequest.get("/act/vane").queryParam("n", "1").queryParam("set", "flood"));
        assertEquals(302, ok.getStatus());

        var posted = JWebTest.test(app, MockRequest.post("/act/vane").queryParam("n", "1").queryParam("set", "ebb"));
        assertEquals(302, posted.getStatus(), "an action answers POST too");

        var bad = JWebTest.test(app, MockRequest.get("/act/vane").queryParam("n", "9").queryParam("set", "ebb")
            .header("Accept", "application/json"));
        assertEquals(400, bad.getStatus());
        assertTrue(bad.bodyContains("n must be between 1 and 3"), bad.getBody());

        var html = JWebTest.test(app, MockRequest.get("/act/vane").queryParam("set", "ebb"));
        assertEquals(400, html.getStatus());
        assertTrue(html.bodyContains("n is required"), html.getBody());
        assertTrue(html.bodyContains("<html"), "browsers get a page, not JSON");
    }

    @Test
    void typedGetAndPostRegisterOneMethodEach() {
        JWeb app = JWeb.create()
            .get("/view", Pref.class, (req, pref) -> "get:" + pref.contrast())
            .post("/save", Pref.class, (req, pref) -> "post:" + pref.contrast());

        assertEquals("get:true", JWebTest.test(app, MockRequest.get("/view").queryParam("contrast", "1")).getBody());
        assertEquals(405, JWebTest.test(app, MockRequest.post("/view")).getStatus());
        assertEquals("post:false", JWebTest.test(app, MockRequest.post("/save")).getBody());
    }
}
