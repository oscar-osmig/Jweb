package jweb;

import com.osmig.Jweb.framework.state.StateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static jweb.El.*;
import static jweb.State.live;
import static jweb.State.useState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PubSub} on the in-memory backend: a message reaches a plain listener
 * typed, and reaches a rendered page as a DOM patch for the region its
 * handler changed — the server-initiated half of the event loop.
 */
class PubSubTest {

    record Post(String author, String text) {}

    /** A chat room: subscribes while rendering, so its list follows the topic. */
    static final class RoomPage implements Template {
        final String room;
        final List<Post> seen = new CopyOnWriteArrayList<>();

        RoomPage(String room) {
            this.room = room;
        }

        @Override
        public Element render() {
            jweb.state.State<List<Post>> posts = useState(new ArrayList<>());
            PubSub.subscribe("room:" + room, Post.class, post -> {
                seen.add(post);
                posts.mutate(list -> list.add(post));
            });
            return div(id("room"),
                live(posts, list -> ul(id("posts"), each(list, p -> li(p.author() + ": " + p.text())))));
        }
    }

    @BeforeEach
    @AfterEach
    void clean() {
        PubSub.reset();
        StateManager.clearContext();
    }

    @Test
    void aListenerOutsideARenderGetsTheMessageAsItsType() {
        List<Post> got = new CopyOnWriteArrayList<>();
        List<String> raw = new CopyOnWriteArrayList<>();
        try (PubSub.Subscription typed = PubSub.subscribe("news", Post.class, got::add);
             PubSub.Subscription text = PubSub.subscribe("news", raw::add)) {
            assertEquals(2, PubSub.subscribers("news"));
            PubSub.publish("news", new Post("ada", "hello"));
            PubSub.publish("other", new Post("x", "not for us"));
            PubSub.awaitDelivery();
            assertEquals(List.of(new Post("ada", "hello")), got);
            assertEquals(1, raw.size());
            assertTrue(raw.get(0).contains("\"author\":\"ada\""), raw.get(0));
            assertTrue(typed.active());
        }
        assertEquals(0, PubSub.subscribers("news"));
        PubSub.publish("news", new Post("ada", "after close"));
        PubSub.awaitDelivery();
        assertEquals(1, got.size(), "a closed subscription hears nothing");
    }

    @Test
    void aStringMessageArrivesAsAString() {
        List<String> got = new CopyOnWriteArrayList<>();
        try (PubSub.Subscription s = PubSub.subscribe("ping", String.class, got::add)) {
            PubSub.publish("ping", "hi there");
            PubSub.awaitDelivery();
            assertEquals(List.of("hi there"), got);
        }
        List<Map<String, Object>> maps = new CopyOnWriteArrayList<>();
        try (PubSub.Subscription s = PubSub.subscribe("map", Map.class, maps::add)) {
            PubSub.publish("map", Map.of("n", 1));
            PubSub.awaitDelivery();
            assertEquals(1, maps.get(0).get("n"));
        }
    }

    @Test
    void aPublishReachesTheSubscribedPageAsADomPatch() {
        RoomPage template = new RoomPage("42");
        try (RenderedPage page = JWebTest.render(template)) {
            assertEquals(1, PubSub.subscribers("room:42"));
            assertFalse(page.html().contains("<li>"));

            PubSub.publish("room:42", new Post("ada", "hello"));
            RenderedPage.Result push = page.awaitPush();

            assertEquals(1, push.patches().size(), "one live region re-rendered");
            assertTrue(page.html().contains("<li>ada: hello</li>"), page.html());
            assertEquals(List.of(new Post("ada", "hello")), template.seen);
            assertEquals(1, push.states().size(), "the posts state changed");

            // a second message appends; a message for another room is not seen
            PubSub.publish("room:43", new Post("bob", "elsewhere"));
            PubSub.publish("room:42", new Post("bob", "hi ada"));
            page.awaitPush();
            assertTrue(page.html().contains("<li>ada: hello</li><li>bob: hi ada</li>"), page.html());
            assertEquals(2, template.seen.size());
        }
        // closing the page ended its subscription
        assertEquals(0, PubSub.subscribers("room:42"));
    }

    @Test
    void twoPagesOnOneTopicEachGetTheirOwnPatch() {
        RoomPage a = new RoomPage("lobby");
        RoomPage b = new RoomPage("lobby");
        try (RenderedPage pageA = JWebTest.render(a); RenderedPage pageB = JWebTest.render(b)) {
            assertEquals(2, PubSub.subscribers("room:lobby"));
            PubSub.publish("room:lobby", new Post("ada", "everyone"));
            pageA.awaitPush();
            pageB.awaitPush();
            assertTrue(pageA.html().contains("ada: everyone"));
            assertTrue(pageB.html().contains("ada: everyone"));
        }
    }

    @Test
    void aHandlerThatThrowsDoesNotStopTheOthers() {
        List<String> got = new CopyOnWriteArrayList<>();
        try (PubSub.Subscription bad = PubSub.subscribe("t", String.class, s -> { throw new IllegalStateException("boom"); });
             PubSub.Subscription good = PubSub.subscribe("t", String.class, got::add)) {
            PubSub.publish("t", "x");
            PubSub.awaitDelivery();
            assertEquals(List.of("x"), got);
        }
    }

    @Test
    void aHandlerThatChangesNoRegionSendsNothing() {
        List<String> seen = new CopyOnWriteArrayList<>();
        Template quiet = () -> {
            PubSub.subscribe("quiet", String.class, seen::add);   // no state touched
            return p("static");
        };
        try (RenderedPage page = JWebTest.render(quiet)) {
            PubSub.publish("quiet", "msg");
            PubSub.awaitDelivery();
            assertEquals(List.of("msg"), seen, "the handler ran in the page's context");
            assertTrue(page.pushes().isEmpty(), "nothing to patch, nothing sent");
        }
    }

    @Test
    void blankTopicsAreRefusedAndCountsAreKept() {
        assertThrows(IllegalArgumentException.class, () -> PubSub.publish(" ", "x"));
        assertThrows(IllegalArgumentException.class, () -> PubSub.subscribe("", String.class, s -> {}));
        long before = PubSub.published();
        PubSub.publish("counted", "x");
        assertEquals(before + 1, PubSub.published());
        assertTrue(PubSub.backend() instanceof PubSub.Memory);
    }
}
