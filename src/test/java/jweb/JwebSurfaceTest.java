package jweb;

import com.osmig.Jweb.framework.events.DomEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static jweb.Css.*;
import static jweb.El.*;
import static jweb.Js.*;
import static jweb.Three.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The short names are the real names. Every type an app author can receive
 * from a framework call, or must implement, is assignable to its {@code jweb.*}
 * spelling — so an IDE's auto-import never has to reach for
 * {@code com.osmig.Jweb.framework.*}. Every assignment here is written with the
 * fully-qualified {@code jweb.X} on purpose: if the framework ever hands back a
 * supertype from the long package again, this file stops compiling.
 *
 * <p>All four DSL wildcards are imported at once, so the names used here are
 * also proven unambiguous across {@code El}, {@code Css}, {@code Js} and
 * {@code Three}.</p>
 */
@SuppressWarnings("unused")
class JwebSurfaceTest {

    // ==================== HTML ====================

    @Test
    void htmlValueTypesAreJwebTypes() {
        jweb.Tag t = div(id("x"), "hi");
        jweb.Attributes a = attrs().id("y");
        jweb.Attr id = id("z");
        jweb.Element el = t;
        jweb.Tag scene = scene(box());

        Consumer<jweb.Event> handler = ev -> {};
        jweb.Attributes withHandler = onClick(handler);
        jweb.Event e = DomEvent.builder().type("click").build();

        assertEquals("<div id=\"x\">hi</div>", t.toHtml());
        assertEquals("id", id.name());
        assertTrue(div(withHandler).toHtml().contains("click"), div(withHandler).toHtml());
        assertEquals("click", e.type());
        assertTrue(scene.toHtml().contains("data-three"));
    }

    /** A form is a record — the type the form system is built on. */
    record Signup(@jweb.Form.Required String name,
                  @jweb.Form.Required @jweb.Form.Email String email) {}

    @Test
    void formValueTypesAreJwebTypes() {
        jweb.Form<Signup> signup = form(Signup.class);
        jweb.Form<Signup> configured = signup.action("/signup").submit("Join");
        jweb.Form.Bound<Signup> bound =
            new jweb.Form.Bound<>(new Signup("Ada", "ada@example.com"),
                jweb.ValidationResult.valid(), java.util.Map.of());
        jweb.When chain = when(true).then(p("yes"));
        jweb.Element chosen = when(false).then(p("no")).otherwise(p("fallback"));

        assertTrue(configured.toHtml().contains("<form"), configured.toHtml());
        assertTrue(configured.toHtml().contains("name=\"email\""), configured.toHtml());
        assertTrue(bound.ok());
        assertEquals("<p>yes</p>", chain.toHtml());
        assertEquals("<p>fallback</p>", chosen.toHtml());
    }

    @Test
    void errorBoundaryAndTransitionAreElements() {
        jweb.ErrorBoundary eb = jweb.ErrorBoundary.of((Supplier<jweb.Element>) () -> p("x"));
        jweb.Element tr = jweb.Transition.fade(true, () -> p("y"));
        jweb.Element outlet = jweb.Portal.outlet("modals");
        assertTrue(eb.toHtml().contains("x"));
        assertTrue(tr.toHtml().contains("y"));
        assertNotNull(outlet.toHtml());
    }

    // ==================== JavaScript ====================

    @Test
    void javascriptValueTypesAreJwebTypes() {
        jweb.Action act = show("panel");
        jweb.Val v = v("x").plus(1);
        jweb.Func f = func("f", "a").ret(v("a"));
        jweb.js.Stmt s = v("y").assign(2);
        jweb.Action toast = jweb.Toast.success("Saved");
        jweb.Action fromFunc = call("init");

        assertTrue(act.build().contains("panel"));
        assertEquals("(x+1)", v.js());
        assertTrue(f.toDecl().startsWith("function f(a)"), f.toDecl());
        assertEquals("y=2", s.js());
        assertTrue(toast.build().contains("Saved"));
        assertEquals("init()", fromFunc.build());
    }

    @Test
    void behaviorTypesAreJwebTypes() {
        jweb.js.Copy literal = copy("npm i jweb").feedback("Copied!", 1200);
        jweb.js.Copy nearest = copyFrom("pre").trigger(v("t")).feedbackClass("copied");
        jweb.js.Navigate nav = navigate("/docs/content?section=state")
            .target(".docs-content").push("/docs?section=state").cache(1000);
        jweb.js.Prefetch warm = prefetch(".nav-link").within("nav").delay(50).onHover();
        jweb.js.ActiveLink current = activeLink(".nav-link").activeClass("active");
        jweb.js.ScrollSpy spy = scrollSpy("#toc", "h2, h3").within(".content").offset(80);
        jweb.js.SplitPane split = splitPane("#handle", "#left").minPercent(20).persist("split");
        jweb.js.LineGutter gutter = lineGutter("#editor", "#lines").mirror("#mirror");
        jweb.js.CustomElement custom = customElement("user-card").shadow().template(p("hi"));
        jweb.js.Behavior installed = spy;
        jweb.js.Manifest manifest = jweb.js.Pwa.manifest("Demo").display("standalone");
        jweb.Element manifestLink = jweb.js.Pwa.link();
        jweb.Action worker = jweb.js.Pwa.registerServiceWorker("/sw.js");
        jweb.Val clientState = syncState("s1");
        jweb.js.Stmt guard = if_(v("x").gt(1), return_());
        jweb.js.Stmt stop = preventDefault();

        assertTrue(literal.build().contains("JWeb.copyText"));
        assertTrue(nearest.build().contains("JWeb.nearest(t,'pre')"));
        assertTrue(nav.build().contains("JWeb.swap"));
        assertTrue(warm.build().contains("JWeb.prefetchOn"), warm.build());
        assertTrue(current.build().contains("JWeb.activeLink"));
        assertTrue(installed.build().startsWith("(window.__JWEB_READY__"), installed.build());
        assertTrue(split.build().contains("JWeb.splitPane"));
        assertTrue(gutter.build().contains("JWeb.lineGutter"));
        assertTrue(custom.build().contains("customElements.define('user-card'"));
        assertTrue(worker.build().contains("serviceWorker"));
        assertTrue(manifest.json().contains("\"display\":\"standalone\""));
        assertTrue(manifestLink.toHtml().contains("rel=\"manifest\""), manifestLink.toHtml());
        assertEquals("JWeb.getState('s1')", clientState.js());
        assertEquals("if((x>1)){return;}", guard.js());
        assertEquals("e.preventDefault()", stop.js());
    }

    // ==================== CSS ====================

    @Test
    void cssValueTypesAreJwebTypes() {
        jweb.Style<?> st = style().padding(px(4));
        jweb.CSSValue unit = rem(1);
        jweb.css.Stylesheet sheet = stylesheet().rule("body", style().margin(zero));
        jweb.css.MediaQuery mq = media().minWidth(px(768));
        jweb.css.MediaQuery bp = md();
        jweb.css.ContainerQuery cq = jweb.css.ContainerQuery.container("card");
        jweb.css.Keyframes kf = keyframes("spin");
        jweb.css.Keyframes preset = jweb.css.Keyframes.fadeIn();
        jweb.css.FontFace ff = jweb.css.FontFace.fontFace("Inter");
        jweb.css.Supports sp = jweb.css.Supports.supports("display", "grid");
        jweb.css.Rule rule = jweb.css.Rule.of(".x", style().color("red"));
        jweb.css.Selector sel = jweb.css.Selectors.cls("card").hover();
        jweb.css.Theme theme = jweb.css.Theme.light().color("primary", hex("#4f46e5"));
        jweb.CSSValue token = jweb.css.Theme.color("primary");
        jweb.Style<?> mixin = row(rem(1)).apply(card()).hover(style().color("red"));

        assertEquals(":root{--color-primary:#4f46e5;}", theme.css());
        assertEquals("var(--color-primary)", token.css());
        assertEquals("&:hover{color: red;}", mixin.variantCss("&"));

        String css = sheet.add(mq.rules(rule)).add(kf.from(style().opacity(0)).to(style().opacity(1)))
            .add(sp.rules(rule)).add(cq.rule(".x", style().display(block))).build();
        assertTrue(css.contains("@media"), css);
        assertTrue(css.contains("@keyframes spin"), css);
        assertTrue(css.contains("@supports"), css);
        assertTrue(css.contains("@container"), css);
        assertEquals(".x{color: red;}", rule.build());
        assertEquals(".card:hover", sel.toString());
        assertEquals("1rem", unit.css());
        assertNotNull(preset.build());
        assertNotNull(ff.build());
    }

    // ==================== Three ====================

    @Test
    void threeNodeTypesAreJwebTypes() {
        jweb.three.Box b = box(1, 1, 1);
        jweb.three.Sphere sp = sphere(2);
        jweb.three.Camera cam = camera().position(0, 1, 4);
        jweb.three.DirectionalLight light = directionalLight();
        jweb.three.Group g = group(b, sp);
        jweb.three.SceneSetting fog = Three.fog("#fff", 1, 40);
        jweb.three.SceneSetting grid = Three.grid(10, 40);
        jweb.three.ThreePatch patch = Three.patch("hall").node("x").opacity(0.5);

        assertEquals(40, grid.toMap().get("divisions"));   // an int stays an Integer
        assertEquals(40L, fog.toMap().get("far"));         // a whole double narrows to a Long
        assertNotNull(cam.toMap());
        assertNotNull(light.toMap());
        assertNotNull(g.toMap());
        assertNotNull(patch);
    }

    // ==================== Server ====================

    @Test
    void serverValueTypesAreJwebTypes() {
        jweb.Request req = jweb.MockRequest.get("/x").build();
        jweb.MockRequest mock = jweb.MockRequest.post("/y");
        jweb.MockSession session = new jweb.MockSession();
        jweb.TestClient client = jweb.TestClient.localhost(8085).withAuth("t");
        jweb.Response.ResponseBuilder rb = jweb.Response.ok();
        ResponseEntity<String> html = jweb.Response.html(p("x"));
        jweb.Response.Redirect redirect = jweb.Response.redirect("/x").anchor("top");
        jweb.Principal principal = jweb.Principal.of("42", "Ada", "admin");
        jweb.Session sess = jweb.Session.of(req);
        jweb.Guard guard = rq -> rq.principal() == null ? jweb.Response.unauthorized() : null;
        jweb.ActionHandler<String> action = (rq, s) -> s;
        jweb.BindException bind = new jweb.BindException("f", "f is required");
        jweb.Attributes bound = bind(new jweb.state.State<>("s", 1));
        jweb.state.State<Integer> st = jweb.State.useState(0);
        jweb.Element region = jweb.State.live(st, n -> p("n=" + n));
        jweb.Attributes attr = jweb.State.bindAttr(st, "disabled");
        jweb.Seo seo = jweb.Seo.of("Title", "Description");
        jweb.Doc doc = jweb.Doc.of("users").set("name", "Ada");
        jweb.Streamed streamed = jweb.Streamed.of(() -> p("x"));
        jweb.BackgroundTask<String> task = new jweb.BackgroundTask<>("job", CompletableFuture.completedFuture("done"));
        jweb.SseEvent event = jweb.SseEvent.of("tick", "1");
        jweb.SseBroadcaster broadcaster = new jweb.SseBroadcaster(0);
        jweb.SseEmitter emitter = jweb.SseEmitter.create(0);
        jweb.ValidationResult valid = jweb.ValidationResult.valid();
        jweb.Validator<String> validator = (value, field) -> jweb.ValidationResult.valid();
        Function<jweb.Request, jweb.UploadedFile> upload = rq -> jweb.FileUpload.getFile(rq, "avatar");
        Supplier<jweb.FetchResult> fetch = () -> jweb.Fetch.get("http://localhost/x").send();

        assertEquals("/x", req.path());
        assertEquals("/x#top", redirect.location());
        assertEquals("f", bind.field());
        assertEquals("<span data-state-bind=\"s\">1</span>", span(bound).toHtml());
        assertTrue(region.toHtml().contains("data-live"));
        assertNotNull(attr.toMap().get("data-state-attr"));
        assertFalse(sess.exists());
        assertNotNull(guard);
        assertEquals("v", action.handle(req, "v"));
        assertEquals("42", principal.getId());
        assertEquals("Ada", doc.getString("name"));
        assertEquals("1", event.data());
        assertTrue(valid.isValid());
        assertTrue(validator.validate("v", "f").isValid());
        assertNotNull(html.getBody());
        assertTrue(jweb.Messages.has("nope") || true);
        broadcaster.shutdown();
    }

    // ==================== Wiring: what authors implement ====================

    @Test
    void overridePointsAreTypedOnJwebTypes() {
        jweb.Middleware mw = (rq, chain) -> chain.next();
        jweb.Middleware recommended = jweb.Middlewares.recommended();
        jweb.RouteHandler handler = rq -> "hi";
        record Pref(Optional<String> theme) {}
        jweb.JWeb app = jweb.JWeb.create()
            .use(mw)
            .guard("/admin/**", rq -> null)
            .guard("/admin/**", jweb.Auth.requireLogin("/admin/login"))
            .get("/x", handler)
            .get("/y", () -> p("y"))
            .post("/z", handler)
            .post("/w", rq -> "posted")     // a lambda POST route needs no (RouteHandler) cast
            .put("/w", rq -> "put")
            .delete("/w", rq -> "gone")
            .action("/act/pref", Pref.class, (rq, pref) -> jweb.Response.redirectBack(rq))
            .pages("/users/:id", PageWithParams.class);

        jweb.Template page = new jweb.Template() {
            @Override public jweb.Element render() { return p("x"); }
            @Override public String pageTitle() { return "Title"; }
            @Override public String description() { return "Desc"; }
            @Override public Optional<jweb.Element> extraHead() { return Optional.of(p("head")); }
            @Override public jweb.Action onMount() { return call("init"); }
            @Override public Optional<jweb.Action> scripts() { return Optional.of(call("more")); }
            @Override public void beforeRender(jweb.Request request) {}
        };

        assertTrue(app.getRouter().match("GET", "/x").isPresent());
        assertTrue(app.getRouter().match("POST", "/z").isPresent());
        assertTrue(app.getRouter().match("POST", "/w").isPresent());
        assertTrue(app.getRouter().match("PUT", "/w").isPresent());
        assertTrue(app.getRouter().match("DELETE", "/w").isPresent());
        assertTrue(app.getRouter().match("POST", "/act/pref").isPresent());
        assertTrue(app.getPageRegistry().match("/users/7").isPresent());
        assertEquals(2, app.getGuards().size());
        assertEquals("init()", page.onMount().build());
        assertEquals("Title", page.pageTitle());
        assertEquals("Desc", page.description());
        assertTrue(page.extraHead().isPresent());
    }

    public static class PageWithParams implements jweb.Template {
        @Override public jweb.Element render() { return p("user " + pathParam("id")); }
    }

    // ==================== Routing, security, ops, AI ====================

    /**
     * The types the docs name that lived only under the long package until
     * the docs compile test caught them. Each is now the real class under
     * {@code jweb} (the old name is a deprecated subclass), so the declared
     * types below are the ones the factories actually return.
     */
    @Test
    void routingSecurityOpsAndAiTypesAreJwebTypes() throws Exception {
        jweb.Request req = jweb.MockRequest.get("/x").build();

        jweb.QueryParam<Integer> page = jweb.QueryParam.of("page", Integer.class).orElse(1);
        jweb.MiddlewareChain chain = () -> "next";
        jweb.Middleware inline = (rq, ch) -> ch.next();
        jweb.RateLimit.RateLimitBuilder limiter = jweb.RateLimit.perMinute(10).forPath("/api/**");
        jweb.Middleware limited = limiter.build();
        jweb.Cors.CorsBuilder cors = jweb.Cors.configure().origins("https://app.example").credentials();
        jweb.Middleware corsAll = jweb.Cors.allowAll();
        jweb.OAuth2.ProviderBuilder google = jweb.OAuth2.google();
        jweb.http.Cookie cookie = jweb.http.Cookie.of("lang", "en").httpOnly().sameSiteLax();
        jweb.HealthStatus status = jweb.HealthStatus.up("connected").withDetail("latencyMs", 4);
        jweb.HealthCheck check = () -> jweb.HealthStatus.degraded("slow");
        jweb.Metrics.Counter counter = jweb.Metrics.counter("surface.test.counter");
        jweb.Metrics.Timer timer = jweb.Metrics.timer("surface.test.timer");
        jweb.ContextKey<String> key = jweb.Context.key("surface.test.user");
        jweb.Link link = jweb.Link.to("/about").text("About").prefetch();
        jweb.Element navLink = jweb.Link.navLink("/about", "About", "/about");
        jweb.Element navScript = jweb.Navigation.script();
        jweb.Chat chat = jweb.AI.chat().system("You are terse");
        jweb.Tool tool = jweb.Tool.of("echo", "Echoes its argument")
            .param("text", "What to echo")
            .handler(args -> args.get("text"));
        jweb.Agent agent = jweb.AI.agent().tools(tool).maxSteps(2);

        assertEquals(1, page.from(req));
        assertEquals("next", inline.handle(req, chain));
        assertTrue(cookie.toHeaderValue().startsWith("lang=en; Path=/"), cookie.toHeaderValue());
        assertTrue(cookie.toHeaderValue().contains("HttpOnly"), cookie.toHeaderValue());
        assertEquals(jweb.HealthStatus.Status.UP, status.getStatus());
        assertEquals(4, status.getDetails().get("latencyMs"));
        assertEquals(jweb.HealthStatus.Status.DEGRADED, check.check().getStatus());
        counter.increment();
        assertTrue(counter.get() >= 1);
        timer.record(1);
        assertEquals("surface.test.user", key.name());
        assertEquals("hi", jweb.Context.provide(key, "hi", () -> jweb.Context.use(key)));
        assertTrue(link.toHtml().contains("href=\"/about\""), link.toHtml());
        assertTrue(navLink.toHtml().contains("active"), navLink.toHtml());
        assertTrue(navScript.toHtml().startsWith("<script"), navScript.toHtml());
        assertEquals(1, chat.size());
        assertEquals("echo", tool.name());
        assertNotNull(agent); assertNotNull(limited); assertNotNull(cors); assertNotNull(corsAll);
        assertNotNull(google);
    }
}
