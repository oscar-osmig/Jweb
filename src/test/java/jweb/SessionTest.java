package jweb;

import com.osmig.Jweb.framework.server.CurrentRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    /** The gallery's Visit, minus the hand-rolled of(req). */
    public static class Visit {
        public boolean entered;
        public final Set<String> witnessed = new LinkedHashSet<>();
    }

    record Cart(int items) {}

    @AfterEach
    void cleanup() {
        CurrentRequest.clear();
    }

    @Test
    void typedValueIsCreatedOnFirstUseAndSharedAfter() {
        MockSession http = new MockSession();
        Request req = MockRequest.get("/x").session(http).build();

        Visit first = Session.of(Visit.class, req);
        first.entered = true;
        first.witnessed.add("tide");

        Visit again = Session.of(Visit.class, req);
        assertSame(first, again);
        assertTrue(again.entered);
        assertEquals(Set.of("tide"), again.witnessed);
        assertTrue(Session.of(req).has(Visit.class));
    }

    @Test
    void typedGetIsNullSafeAndTypeSafe() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        Session session = Session.of(req);

        assertNull(session.get(Visit.class));
        assertEquals(Optional.empty(), session.find(Cart.class));

        session.put(new Cart(2));
        assertEquals(new Cart(2), session.get(Cart.class));
        assertEquals(2, session.find(Cart.class).orElseThrow().items());

        session.remove(Cart.class);
        assertNull(session.get(Cart.class));
    }

    @Test
    void factoryFormBuildsTheFirstValue() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        Cart cart = Session.of(Cart.class, req, () -> new Cart(5));
        assertEquals(5, cart.items());
        assertEquals(5, Session.of(Cart.class, req, () -> new Cart(99)).items(), "factory only runs once");
    }

    @Test
    void keyedValuesRoundTrip() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        Session session = Session.of(req);

        assertNull(session.get("theme", String.class));
        assertEquals("light", session.get("theme", String.class, "light"));
        session.put("theme", "dark").put("count", 3);
        assertEquals("dark", session.get("theme", String.class));
        assertEquals(3, session.get("count", Integer.class));
        assertNull(session.get("count", String.class), "wrong type reads as absent");
        assertTrue(session.has("theme"));
        session.put("theme", null);
        assertFalse(session.has("theme"));
    }

    @Test
    void readsNeverCreateASession() {
        Request req = MockRequest.get("/x").build();   // no session yet
        Session session = Session.of(req);
        assertNull(session.get("anything"));
        assertNull(session.get(Visit.class));
        assertFalse(session.exists());
        assertNull(session.id());

        session.put("k", "v");
        assertTrue(session.exists());
        assertNotNull(session.id());
    }

    @Test
    void flashIsReadOnce() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        Session session = Session.of(req);

        assertNull(session.flash("notice"));
        session.flash("notice", "Saved!");
        assertTrue(session.hasFlash("notice"));
        String first = session.flash("notice");
        assertEquals("Saved!", first);
        assertNull(session.flash("notice"), "consumed");
        assertFalse(session.hasFlash("notice"));
    }

    @Test
    void endForgetsEverything() {
        MockSession http = new MockSession();
        Request req = MockRequest.get("/x").session(http).build();
        Session session = Session.of(req);
        session.put("k", "v");
        session.end();
        assertTrue(http.isInvalidated());
        assertNull(session.get("k"), "an invalidated session reads as empty");
    }

    @Test
    void templateSessionReadsTheRequestInFlight() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        Session.of(req).put("theme", "dark");
        Template page = new Template() {
            @Override public Element render() {
                return p("theme=" + session().get("theme", String.class));
            }
        };

        CurrentRequest.set(req);
        assertEquals("<p>theme=dark</p>", page.toHtml());

        CurrentRequest.clear();
        IllegalStateException e = assertThrows(IllegalStateException.class, page::toHtml);
        assertTrue(e.getMessage().contains("request in flight"), e.getMessage());
    }

    @Test
    void sessionCurrentMatchesTemplateSession() {
        Request req = MockRequest.get("/x").session(new MockSession()).build();
        CurrentRequest.set(req);
        Session.current().put("a", 1);
        assertEquals(1, Session.of(req).get("a", Integer.class));
    }
}
