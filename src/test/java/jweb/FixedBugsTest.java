package jweb;

import com.osmig.Jweb.framework.server.RequestIdFilter;
import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.vdom.HtmlNesting;
import jweb.state.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static jweb.El.*;
import static jweb.State.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The bugs the October 2026 review of the unreleased framework code turned
 * up, each pinned where it was found so it stays fixed: the stored-file
 * response, the SQL table helper, the jobs store, the test page, the mail
 * envelope, the request id, the HTML content model and the plural rules.
 */
class FixedBugsTest {

    @BeforeEach
    void fresh() {
        Storage.reset();
    }

    @AfterEach
    void done() {
        Storage.reset();
        StateManager.clearContext();
        Jobs.store(new Jobs.Memory());
    }

    // ==================== Storage ====================

    @Test
    void anUploadsTypeIsTheExtensionsWhenKnownAndNeverAMalformedClaim() {
        assertEquals("image/png", Storage.uploadType("avatars/x.png", "text/html"), "the browser's claim loses to the extension");
        assertEquals("text/plain", Storage.uploadType("notes/readme", "text/plain; charset=utf-8"), "a well-formed type is kept for an extensionless key");
        assertEquals("application/octet-stream", Storage.uploadType("blob", "garbage"), "no slash: not a media type");
        assertEquals("application/octet-stream", Storage.uploadType("blob", null));

        UploadedFile upload = new UploadedFile(new MockMultipartFile("f", "evil.png", "text/html", new byte[]{1, 2, 3}));
        assertEquals("image/png", Storage.put("up/evil.png", upload).contentType());
    }

    @Test
    void storedActiveContentIsServedSandboxedAndNothingIsSniffed() {
        Storage.put("docs/page.html", "<script>alert(1)</script>".getBytes(), "text/html");
        Storage.put("docs/pic.png", new byte[]{1, 2, 3}, "image/png");
        RouteHandler serve = Storage.serve();

        ResponseEntity<?> html = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/page.html").build());
        assertEquals("nosniff", html.getHeaders().getFirst("X-Content-Type-Options"));
        assertEquals("sandbox", html.getHeaders().getFirst("Content-Security-Policy"), "HTML somebody uploaded runs in an opaque origin");

        ResponseEntity<?> png = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/pic.png").build());
        assertEquals("nosniff", png.getHeaders().getFirst("X-Content-Type-Options"));
        assertNull(png.getHeaders().getFirst("Content-Security-Policy"), "an image needs no sandbox");

        assertTrue(Storage.isActive("image/svg+xml"));
        assertTrue(Storage.isActive("Text/HTML; charset=utf-8"));
        assertFalse(Storage.isActive("application/pdf"));
    }

    @Test
    void ifModifiedSinceIsAnsweredHereWithA304() {
        Storage.put("docs/a.txt", "a".getBytes(), "text/plain");
        Instant modified = Storage.get("docs/a.txt").orElseThrow().modified();
        String later = DateTimeFormatter.RFC_1123_DATE_TIME.format(modified.plusSeconds(1).atOffset(ZoneOffset.UTC));
        String earlier = DateTimeFormatter.RFC_1123_DATE_TIME.format(modified.minusSeconds(5).atOffset(ZoneOffset.UTC));
        RouteHandler serve = Storage.serve();

        ResponseEntity<?> same = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/a.txt").header("If-Modified-Since", later).build());
        assertEquals(304, same.getStatusCode().value());
        ResponseEntity<?> changed = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/a.txt").header("If-Modified-Since", earlier).build());
        assertEquals(200, changed.getStatusCode().value());
        ResponseEntity<?> junk = (ResponseEntity<?>) serve.handle(MockRequest.get("/files/docs/a.txt").header("If-Modified-Since", "yesterday").build());
        assertEquals(200, junk.getStatusCode().value(), "an unparseable date is ignored");
    }

    @Test
    void aDownloadNameCannotBreakOutOfItsHeader() {
        assertEquals("report", Storage.asciiFilename("report\\"), "a trailing backslash would escape the closing quote");
        assertEquals("ab", Storage.asciiFilename("a\r\nb"));
        assertEquals("r_sum_.pdf", Storage.asciiFilename("résumé.pdf"));
    }

    // ==================== Sql ====================

    record Later(Long id, String name) {}

    @Test
    void aTableCanBeMadeBeforeTheDatabaseIsConnected() {
        // Routes.configure runs before the datasource is bound; the table binds on first use
        assertDoesNotThrow(() -> Sql.table("later", Later.class));
        assertEquals("later", Sql.table("later", Later.class).name());
    }

    @Test
    void searchTextIsMatchedLiterallyNotAsALikePattern() {
        assertEquals("%50!%!_x%", Sql.Table.likeContains("50%_x"));
        assertEquals("%a!!b%", Sql.Table.likeContains("a!b"));
        assertEquals("%plain%", Sql.Table.likeContains("plain"));
    }

    // ==================== Jobs ====================

    @Test
    void aLateResultFromAReleasedRunIsDiscarded() {
        Jobs.Memory store = new Jobs.Memory();
        Jobs.Job job = store.add(new Jobs.Job("j1", "slow", null, Jobs.Status.PENDING, 0, 4,
            Instant.now(), Instant.now(), null, null, null, null));
        Jobs.Job claimedByA = store.claim("worker-a", Instant.now()).orElseThrow();
        // A looks dead; the job is released and B takes it
        store.update(claimedByA.with(Jobs.Status.RUNNING, 0, job.runAt(), Instant.now().minus(Duration.ofHours(1)), "worker-a", null));
        assertEquals(1, store.releaseStale(Instant.now().minus(Duration.ofMinutes(10))));
        Jobs.Job released = store.find("j1").orElseThrow();
        assertEquals(Jobs.Status.PENDING, released.status());
        assertEquals(1, released.attempts(), "the run that never reported back counts");
        store.claim("worker-b", Instant.now()).orElseThrow();

        assertFalse(store.finish(claimedByA.with(Jobs.Status.DONE, 1, job.runAt(), Instant.now(), "worker-a", null), "worker-a"),
            "A finishing late must not overwrite B's run");
        assertEquals(Jobs.Status.RUNNING, store.find("j1").orElseThrow().status());
        assertEquals("worker-b", store.find("j1").orElseThrow().lockedBy());
    }

    @Test
    void aJobThatKeepsTakingItsWorkerDownIsDeadLetteredNotRerunForever() {
        Jobs.Memory store = new Jobs.Memory();
        store.add(new Jobs.Job("j2", "poison", null, Jobs.Status.PENDING, 0, 2,
            Instant.now(), Instant.now(), null, null, null, null));
        Instant longAgo = Instant.now().minus(Duration.ofHours(1));
        for (int round = 1; round <= 2; round++) {
            Jobs.Job claimed = store.claim("w", Instant.now()).orElseThrow();
            store.update(claimed.with(Jobs.Status.RUNNING, claimed.attempts(), claimed.runAt(), longAgo, "w", null));
            assertEquals(1, store.releaseStale(Instant.now().minus(Duration.ofMinutes(10))));
        }
        Jobs.Job dead = store.find("j2").orElseThrow();
        assertEquals(Jobs.Status.FAILED, dead.status());
        assertEquals(2, dead.attempts());
        assertTrue(dead.error().startsWith("Released"), dead.error());
    }

    // ==================== RenderedPage ====================

    static class Reveal implements Template {
        final AtomicInteger inner = new AtomicInteger();
        State<Boolean> open;

        @Override
        public Element render() {
            open = useState(false);
            return div(
                button(id("open"), onClick(e -> open.set(true)), "Open"),
                live(open, o -> o
                    ? div(button(id("inner"), onClick(e -> inner.incrementAndGet()), "Inside"))
                    : p("closed")));
        }
    }

    @Test
    void anElementARerenderAddedCanBeClicked() {
        Reveal reveal = new Reveal();
        try (RenderedPage page = JWebTest.render(reveal)) {
            assertThrows(IllegalArgumentException.class, () -> page.click("inner"), "not rendered yet");
            assertTrue(page.click("open").ok());
            assertTrue(page.html().contains("id=\"inner\""), page.html());
            assertTrue(page.click("inner").ok(), "the button lives only in the patched region");
            assertEquals(1, reveal.inner.get());
        }
    }

    @Test
    void keydownCarriesTheKeyCodeABrowserSends() {
        assertEquals(13, RenderedPage.keyCodeOf("Enter"));
        assertEquals(27, RenderedPage.keyCodeOf("Escape"));
        assertEquals(65, RenderedPage.keyCodeOf("a"));
        assertEquals(-1, RenderedPage.keyCodeOf("Shift"));
        Map<String, String> attributes = RenderedPage.tagAttributes(
            "<div><button id=\"x\" data-jweb-onclick=\"h_1\" title=\"a &quot;b&quot;\">x</button></div>", "x");
        assertNotNull(attributes);
        assertEquals("h_1", attributes.get("data-jweb-onclick"));
        assertEquals("a \"b\"", attributes.get("title"));
        assertNull(RenderedPage.tagAttributes("<p id=\"y\">y</p>", "x"));
    }

    // ==================== Mail ====================

    @Test
    void anEnvelopeAddressCannotSmuggleSmtpCommands() {
        assertEquals("ada@example.com", Mail.address("Ada <ada@example.com>"));
        assertThrows(Mail.MailException.class, () -> Mail.address("a@evil.com>\r\nDATA\r\nFrom: ceo@bank.com"));
        assertThrows(Mail.MailException.class, () -> Mail.address("two words@example.com"));
        assertThrows(Mail.MailException.class, () -> Mail.address("no-at-sign"));
        assertEquals("a@b", Mail.address("<a@b> <c@d>"), "the first bracketed address is the one; the rest never reaches the envelope");
    }

    // ==================== Request id ====================

    @Test
    void theErrorDispatchKeepsTheIdTheFailingRequestAlreadyHad() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/boom");
        request.setAttribute(Request.ID_ATTRIBUTE, "first-pass-id");
        request.addHeader(Request.ID_HEADER, "caller-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new RequestIdFilter().doFilter(request, response, new MockFilterChain());
        assertEquals("first-pass-id", request.getAttribute(Request.ID_ATTRIBUTE));
        assertEquals("first-pass-id", response.getHeader(Request.ID_HEADER));
    }

    // ==================== HtmlNesting ====================

    @Test
    void validNestingIsNotReported() {
        assertNull(HtmlNesting.problem("label", "a"), "I agree to the <a>terms</a>");
        assertNotNull(HtmlNesting.problem("label", "label"));
        assertNotNull(HtmlNesting.problem("label", "button"));
        assertNull(HtmlNesting.problem("dt", "div"), "dt takes flow content");
        assertNotNull(HtmlNesting.problem("dt", "h2"), "but no headings");
        assertNull(HtmlNesting.problem("legend", "h2"), "a legend may hold a heading");
        assertNotNull(HtmlNesting.problem("legend", "div"));
        assertNull(HtmlNesting.problem("g", "title"), "an SVG accessible name");
        assertNull(HtmlNesting.problem("path", "title"));
        assertNotNull(HtmlNesting.problem("button", "title"));
        assertNotNull(HtmlNesting.problem("p", "div"), "the classic is still caught");
    }

    // ==================== Plural ====================

    @Test
    void romanianFewCoversEveryNumberEndingInOneToNineteen() {
        assertEquals(Plural.Category.ONE, Plural.of("ro", 1));
        assertEquals(Plural.Category.FEW, Plural.of("ro", 2));
        assertEquals(Plural.Category.FEW, Plural.of("ro", 19));
        assertEquals(Plural.Category.OTHER, Plural.of("ro", 20));
        assertEquals(Plural.Category.FEW, Plural.of("ro", 101), "101 ends in 01");
        assertEquals(Plural.Category.FEW, Plural.of("ro", 1001));
        assertEquals(Plural.Category.OTHER, Plural.of("ro", 100));
    }

    // ==================== Redis URL ====================

    @Test
    void theRedisDatabaseInTheUrlIsHonoured() {
        assertEquals(2, Redis.Endpoint.parse("redis://localhost:6379/2").database());
        assertEquals(0, Redis.Endpoint.parse("redis://localhost/0").database());
        assertEquals(0, Redis.Endpoint.parse("rediss://user:secret@host:6380").database());
        assertThrows(IllegalArgumentException.class, () -> Redis.Endpoint.parse("redis://localhost/two"));
    }
}
