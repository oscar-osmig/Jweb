package jweb.state;

import com.osmig.Jweb.framework.state.LiveRegion;
import com.osmig.Jweb.framework.state.RenderableComponent;
import com.osmig.Jweb.framework.state.StateManager;
import jweb.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static jweb.El.*;
import static jweb.State.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The live-region protocol: what {@code live(...)} renders, what a state
 * change re-renders, and what the {@code bind*} arguments emit. Both
 * {@code jweb.El.*} and {@code jweb.State.*} are imported here on purpose —
 * they must coexist without an ambiguous {@code bind}.
 */
class LiveRegionTest {

    @AfterEach
    void cleanup() {
        StateManager.StateContext context = StateManager.getContext();
        if (context != null) context.clearContext();
        StateManager.clearContext();
    }

    // ==================== bind renders the value ====================

    @Test
    void bindRendersTheValueWhenItIsTheOnlyContent() {
        State<Integer> clicks = new State<>("s1", 3);
        assertEquals("<span data-state-bind=\"s1\">3</span>", span(bind(clicks)).toHtml());
        assertEquals("<p>Clicks: <span data-state-bind=\"s1\">3</span></p>",
            p("Clicks: ", span(bind(clicks))).toHtml());
    }

    @Test
    void bindKeepsTheExplicitTwoArgFormWithoutDoubling() {
        State<Integer> clicks = new State<>("s1", 3);
        assertEquals("<span data-state-bind=\"s1\">3</span>", span(bind(clicks), clicks.get()).toHtml());
        assertEquals("<span data-state-bind=\"s1\">Total: 3</span>", span(bind(clicks), "Total: " + clicks.get()).toHtml());
    }

    @Test
    void bindOfNullRendersEmptyText() {
        State<String> name = new State<>("s2", null);
        assertEquals("<span data-state-bind=\"s2\"></span>", span(bind(name)).toHtml());
    }

    @Test
    void bindInputWritesTheValueAndChecksBooleans() {
        State<String> name = new State<>("s3", "Ada");
        State<Boolean> on = new State<>("s4", true);
        String text = input(type("text"), bindInput(name)).toHtml();
        assertTrue(text.contains("value=\"Ada\""), text);
        assertTrue(text.contains("data-state-input=\"true\""), text);
        String box = input(type("checkbox"), bindInput(on)).toHtml();
        assertTrue(box.contains(" checked"), box);
    }

    // ==================== bindAttr / bindClass ====================

    @Test
    void bindAttrEmitsTheAttributeWhileTruthy() {
        State<Boolean> busy = new State<>("s5", true);
        String html = button(bindAttr(busy, "disabled"), "Save").toHtml();
        assertTrue(html.contains("data-state-attr=\"disabled=s5\""), html);
        assertTrue(html.contains(" disabled"), html);

        busy.set(false);
        String idle = button(bindAttr(busy, "disabled"), "Save").toHtml();
        assertFalse(idle.contains(" disabled"), idle);
        assertTrue(idle.contains("data-state-attr=\"disabled=s5\""), idle);

        State<Integer> count = new State<>("s6", 0);
        assertFalse(div(bindAttr(count, "hidden")).toHtml().contains(" hidden"));
        State<String> q = new State<>("s7", "java");
        assertTrue(input(bindAttr(q, "value")).toHtml().contains("value=\"java\""));
    }

    @Test
    void bindClassEmitsTheClassWhileTruthy() {
        State<Boolean> active = new State<>("s8", true);
        String html = li(bindClass(active, "on"), "Home").toHtml();
        assertTrue(html.contains("data-state-class=\"on=s8\""), html);
        assertTrue(html.contains("class=\"on\""), html);
        active.set(false);
        String off = li(bindClass(active, "on"), "Home").toHtml();
        assertFalse(off.contains(" class=\""), off);
        assertTrue(off.contains("data-state-class=\"on=s8\""), off);
    }

    // ==================== live regions ====================

    @Test
    void liveRendersTheBodyWithADataLiveAttribute() {
        StateManager.StateContext context = StateManager.createContext();
        State<List<String>> items = useState(new ArrayList<>(List.of("a", "b")));

        Element region = live(items, list -> ul(each(list, i -> li(i))));
        String html = region.toHtml();

        assertEquals("<ul data-live=\"live_1\"><li>a</li><li>b</li></ul>", html);
        assertEquals(1, context.getComponents().size());
        RenderableComponent component = context.getComponent("live_1");
        assertEquals(Set.of(items.getId()), component.dependsOn());
    }

    @Test
    void liveReRendersFromTheNewValueAsAPatch() {
        StateManager.StateContext context = StateManager.createContext();
        State<List<String>> items = useState(new ArrayList<>(List.of("a")));
        live(items, list -> ul(each(list, i -> li(i))));

        // A handler mutates the list in place — update() must still count as a change
        items.update(l -> { l.add("b"); return l; });
        assertTrue(context.getChangedStates().contains(items), "in-place update is a change");

        String patch = context.getComponent("live_1").render();
        assertEquals("<ul data-live=\"live_1\"><li>a</li><li>b</li></ul>", patch);
    }

    @Test
    void liveOnlyReRendersForItsOwnStates() {
        StateManager.createContext();
        State<Integer> a = useState(1);
        State<Integer> b = useState(2);
        RenderableComponent onA = (RenderableComponent) live(a, v -> p("A" + v));
        RenderableComponent onBoth = (RenderableComponent) live(a, b, (x, y) -> p(x + y));
        RenderableComponent onAny = (RenderableComponent) live(() -> p("any"));

        assertTrue(onA.affectedBy(Set.of(a.getId())));
        assertFalse(onA.affectedBy(Set.of(b.getId())));
        assertTrue(onBoth.affectedBy(Set.of(b.getId())));
        assertTrue(onAny.affectedBy(Set.of("state_999")));
    }

    @Test
    void liveWrapsANonElementBodyInASpan() {
        StateManager.createContext();
        State<String> name = useState("Ada");
        String html = live(name, n -> text("Hi " + n)).toHtml();
        assertEquals("<span data-live=\"live_1\">Hi Ada</span>", html);
    }

    @Test
    void liveConditionalSwapsTheRootElement() {
        StateManager.StateContext context = StateManager.createContext();
        State<Boolean> in = useState(false);
        live(in, logged -> logged ? span("Hi") : a(href("/login"), "Sign in"));

        assertTrue(context.getComponent("live_1").render().startsWith("<a "));
        in.set(true);
        assertEquals("<span data-live=\"live_1\">Hi</span>", context.getComponent("live_1").render());
    }

    @Test
    void liveOutsideAContextStillRendersOnce() {
        State<Integer> n = new State<>("sx", 7);
        String html = live(n, v -> p("n=" + v)).toHtml();
        assertTrue(html.contains("data-live=\"live_x"), html);
        assertTrue(html.endsWith("n=7</p>"), html);
    }

    @Test
    @SuppressWarnings("deprecation")
    void useComponentIsAnAliasOfLiveWithAnId() {
        StateManager.StateContext context = StateManager.createContext();
        State<Integer> count = useState(1);
        var element = useComponent("counter", () -> p("Count: " + count.get()));

        assertEquals("<div id=\"counter\" data-live=\"counter\"><p>Count: 1</p></div>", element.toHtml());
        count.set(2);
        assertTrue(context.getComponent("counter").render().contains("Count: 2"));
        assertNull(context.getComponent("counter").dependsOn(), "legacy regions re-render on any change");
    }

    @Test
    void mutateNotifiesWithoutReturningTheList() {
        StateManager.StateContext context = StateManager.createContext();
        State<List<String>> items = useState(new ArrayList<>());
        items.mutate(l -> l.add("x"));
        assertEquals(List.of("x"), items.get());
        assertTrue(context.getChangedStates().contains(items));
    }

    @Test
    void liveIdsAreScopedToTheContext() {
        StateManager.createContext();
        State<Integer> a = useState(1);
        assertTrue(live(a, v -> p(v)).toHtml().contains("live_1"));
        assertTrue(live(a, v -> p(v)).toHtml().contains("live_2"));
        LiveRegion direct = LiveRegion.of(() -> p("x"), a);
        assertEquals("live_3", direct.id());
    }
}
