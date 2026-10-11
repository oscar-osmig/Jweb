package jweb;

import com.osmig.Jweb.framework.state.StateManager;
import jweb.state.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static jweb.El.*;
import static jweb.State.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The event loop without a browser: render a page, fire the server handler an
 * element carries, read the DOM patches back — through the same dispatch the
 * WebSocket handler uses.
 */
class RenderedPageTest {

    static final Id INC = id("inc");

    static class CounterPage implements Template {
        State<Integer> count;

        @Override
        public Element render() {
            count = useState(0);
            return div(
                live(count, c -> p("Count: " + c)),
                button(INC, onClick(e -> count.set(count.get() + 1)), "+1"),
                button(id("reset"), onClick(e -> count.set(0)), "Reset"),
                button(id("noop"), onClick(e -> {}), "Nothing"));
        }
    }

    @AfterEach
    void cleanup() {
        StateManager.clearContext();
    }

    @Test
    void rendersWithTheHandlerAttributesABrowserWouldSee() {
        try (RenderedPage page = JWebTest.render(new CounterPage())) {
            String html = page.html();
            assertTrue(html.contains("Count: 0"), html);
            assertTrue(html.contains("data-live=\"live_1\""), html);
            assertTrue(html.contains("id=\"inc\" data-jweb-onclick=\"h_"), html);
            assertTrue(page.contextId().startsWith("ctx_"));
            assertNotNull(StateManager.getContextById(page.contextId()), "context stays registered");
        }
    }

    @Test
    void clickRunsTheHandlerAndPatchesTheRegion() {
        try (RenderedPage page = JWebTest.render(new CounterPage())) {
            RenderedPage.Result r = page.click(INC);

            assertTrue(r.ok(), r.error());
            assertFalse(r.defaultPrevented());
            assertEquals(1, r.states().size(), "one state changed");
            assertEquals(1, r.states().values().iterator().next());
            assertEquals("<p data-live=\"live_1\">Count: 1</p>", r.patch("live_1"));
            assertEquals(r.states(), page.states(), "the page remembers what the server pushed");

            String html = page.html();
            assertTrue(html.contains("Count: 1"), html);
            assertFalse(html.contains("Count: 0"), html);
            assertTrue(html.contains("id=\"inc\""), "the rest of the page is untouched");

            page.click("inc");
            assertTrue(page.html().contains("Count: 2"));
            assertTrue(page.region("live_1").contains("Count: 2"));

            RenderedPage.Result reset = page.click("reset");
            assertEquals(0, reset.states().values().iterator().next());
            assertTrue(page.html().contains("Count: 0"));
        }
    }

    @Test
    void aHandlerThatChangesNothingSendsNoPatch() {
        try (RenderedPage page = JWebTest.render(new CounterPage())) {
            RenderedPage.Result r = page.click("noop");
            assertTrue(r.ok());
            assertTrue(r.patches().isEmpty());
            assertTrue(r.states().isEmpty());
            assertEquals(1, r.messages().size(), "just the ack: " + r.messages());
        }
    }

    @Test
    void inputAndSubmitCarryTheirPayload() {
        class FormPage implements Template {
            State<String> name;
            State<String> email;

            @Override
            public Element render() {
                name = useState("");
                email = useState("");
                return form(id("f"),
                    onSubmit(e -> { e.preventDefault(); email.set(e.formData().get("email")); }),
                    input(id("name"), type("text"), onInput(e -> name.set(e.value()))),
                    live(name, n -> p("Hi " + n)),
                    live(email, m -> p("Sent to " + m)));
            }
        }
        try (RenderedPage page = JWebTest.render(new FormPage())) {
            page.input("name", "Ada");
            assertTrue(page.html().contains("Hi Ada"), page.html());

            RenderedPage.Result r = page.submit("f", Map.of("email", "ada@example.com"));
            assertTrue(r.ok());
            assertTrue(r.defaultPrevented(), "the handler called preventDefault");
            assertTrue(page.html().contains("Sent to ada@example.com"), page.html());
            assertTrue(page.html().contains("Hi Ada"), "earlier patches stay applied");
        }
    }

    @Test
    void checkKeydownAndCustomEventsReachTheHandler() {
        class Page implements Template {
            State<String> seen;

            @Override
            public Element render() {
                seen = useState("");
                return div(
                    input(id("agree"), type("checkbox"), onChange(e -> seen.set("checked=" + e.checked()))),
                    input(id("editor"), onKeyDown(e -> seen.set("key=" + e.key()))),
                    button(id("tile"), onClick(e -> seen.set("row=" + e.data("row") + " ctrl=" + e.ctrlKey()))),
                    live(seen, s -> span(s)));
            }
        }
        try (RenderedPage page = JWebTest.render(new Page())) {
            page.check("agree", true);
            assertTrue(page.html().contains("checked=true"), page.html());
            page.keydown("editor", "Enter");
            assertTrue(page.html().contains("key=Enter"), page.html());
            page.fire("click", "tile", e -> e.data("row", "7").ctrlKey(true));
            assertTrue(page.html().contains("row=7 ctrl=true"), page.html());
        }
    }

    @Test
    void addressingMistakesAreLoud() {
        try (RenderedPage page = JWebTest.render(new CounterPage())) {
            IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, () -> page.click("nope"));
            assertTrue(missing.getMessage().contains("\"nope\""), missing.getMessage());

            IllegalStateException wrongType = assertThrows(IllegalStateException.class, () -> page.input("inc", "x"));
            assertTrue(wrongType.getMessage().contains("\"input\""), wrongType.getMessage());
            assertTrue(wrongType.getMessage().contains("click"), "names what it does handle: " + wrongType.getMessage());
        }
    }

    @Test
    void aClientOnlyHandlerIsNotAServerHandler() {
        try (RenderedPage page = RenderedPage.of(() -> div(button(id("b"), onClick(Js.reload()), "Reload")))) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> page.click("b"));
            assertTrue(e.getMessage().contains("Actions-DSL"), e.getMessage());
        }
    }

    @Test
    void afterCloseTheContextIsExpiredLikeTheReaperWouldLeaveIt() {
        RenderedPage page = JWebTest.render(new CounterPage());
        String contextId = page.contextId();
        page.close();
        assertNull(StateManager.getContextById(contextId));

        RenderedPage.Result r = page.click("inc");
        assertFalse(r.ok());
        assertTrue(r.error().contains("expired"), r.error());
        assertTrue(r.patches().isEmpty());
        page.close(); // idempotent
    }

    @Test
    void aComponentRendersAsALambdaInsideTheContext() {
        int[] hits = {0};
        // A Template is just render(): the lambda runs inside the page's context,
        // so the handler registers there (an element built outside would not)
        try (RenderedPage page = RenderedPage.of(() -> div(button(id("b"), onClick(e -> hits[0]++), "x")))) {
            assertTrue(page.click("b").ok());
            assertTrue(page.click("b").ok());
            assertEquals(2, hits[0]);
        }
    }

    @Test
    void lifecycleHooksSeeTheRequest() {
        class Page implements Template {
            String before, after;

            @Override
            public void beforeRender(Request request) { before = request.path(); }

            @Override
            public void afterRender(Request request) { after = request.method(); }

            @Override
            public Element render() { return p("hello"); }
        }
        Page template = new Page();
        try (RenderedPage page = JWebTest.render(template, MockRequest.get("/hello"))) {
            assertEquals("/hello", template.before);
            assertEquals("GET", template.after);
            assertTrue(page.html().contains("hello"));
        }
    }

    @Test
    void styleTagCarriesThePageStylesheet() {
        class Styled implements Template {
            final Cls BOX = cls("box");

            @Override
            public jweb.css.Stylesheet styles() {
                return jweb.Css.stylesheet().rule(BOX, jweb.Css.style().padding(jweb.Css.px(4)));
            }

            @Override
            public Element render() { return div(BOX, "x"); }
        }
        try (RenderedPage page = JWebTest.render(new Styled())) {
            String style = page.styleTag();
            assertTrue(style.contains(".box"), style);
            assertTrue(style.contains("padding"), style);
        }
    }
}
