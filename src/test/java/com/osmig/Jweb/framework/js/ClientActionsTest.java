package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.security.CspNonce;
import com.osmig.Jweb.framework.state.StateManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The server side of the CSP-safe Actions-DSL contract: content-hash ids,
 * per-context registration and exactly-once draining, parser-safe emission.
 */
class ClientActionsTest {

    @AfterEach
    void cleanup() {
        StateManager.clearContext();
        CspNonce.clear();
    }

    @Test
    void noRenderContextMeansInlineFallback() {
        assertNull(ClientActions.register("location.reload()"),
            "without a page there is nowhere to deliver definitions");
    }

    @Test
    void runtimeDisabledMeansInlineFallback() {
        StateManager.createContext();
        JWebRuntime.setEnabled(false);
        try {
            assertNull(ClientActions.register("location.reload()"),
                "no runtime, no delegation — inline is all that can work");
        } finally {
            JWebRuntime.setEnabled(true);
        }
    }

    @Test
    void idsAreContentHashesDedupedAcrossContexts() {
        StateManager.createContext();
        String id1 = ClientActions.register("location.reload()");
        String id2 = ClientActions.register("location.reload()");
        String other = ClientActions.register("history.back()");
        assertNotNull(id1);
        assertTrue(id1.matches("a[0-9a-f]{10}"), id1);
        assertEquals(id1, id2, "identical JS shares one definition");
        assertNotEquals(id1, other);
        StateManager.clearContext();

        // A different context (a swap fragment, say) mints the same id for
        // the same JS — redefining it in the page is a no-op by construction
        StateManager.createContext();
        assertEquals(id1, ClientActions.register("location.reload()"));
    }

    @Test
    void drainDeliversEachDefinitionExactlyOnce() {
        var context = StateManager.createContext();
        ClientActions.register("a()");
        String first = ClientActions.drainJs(context);
        assertNotNull(first);
        assertTrue(first.contains("a()"), first);
        assertNull(ClientActions.drainJs(context), "second drain has nothing new");

        ClientActions.register("b()");
        ClientActions.register("a()");   // already delivered
        String second = ClientActions.drainJs(context);
        assertNotNull(second);
        assertTrue(second.contains("b()"), second);
        assertFalse(second.contains("a()"), "must not re-deliver: " + second);
    }

    @Test
    void definitionsMirrorInlineHandlerSemantics() {
        var context = StateManager.createContext();
        String id = ClientActions.register("$_('panel').style.display=''");
        String js = ClientActions.drainJs(context);
        assertTrue(js.startsWith("(function(){var A=window.__JWEB_ACTIONS__=window.__JWEB_ACTIONS__||{};"), js);
        // event under both its historical names, this bound by the dispatcher
        assertTrue(js.contains("A." + id + "=function(event){var e=event;$_('panel')"), js);
        // the DSL helpers ride along when referenced — attribute actions no
        // longer depend on some page script having defined $_ globally
        assertTrue(js.contains("const $_=id=>document.getElementById(id)"), js);
    }

    @Test
    void helpersOnlyIncludedWhenReferenced() {
        var context = StateManager.createContext();
        ClientActions.register("location.reload()");
        String js = ClientActions.drainJs(context);
        assertFalse(js.contains("getElementById"), js);
    }

    @Test
    void emittedJsCannotTerminateItsScriptElement() {
        var context = StateManager.createContext();
        // esc() leaves '<' alone, so user text could otherwise break out
        ClientActions.register(Actions.setText("x", "</script><b>owned</b>").build());
        String js = ClientActions.drainJs(context);
        assertFalse(js.toLowerCase().contains("</script"), js);
        assertTrue(js.contains("<\\/script"), js);
    }

    @Test
    void neutralizationLeavesComparisonsAlone() {
        var context = StateManager.createContext();
        ClientActions.register("if(x<5)y()");
        String js = ClientActions.drainJs(context);
        assertTrue(js.contains("if(x<5)y()"), "JS is not JSON — '<' must survive: " + js);
    }

    @Test
    void scriptTagIsNonceStampedAndMarkedForSwapExecution() {
        var context = StateManager.createContext();
        ClientActions.register("a()");
        CspNonce.set("t3stN0nce");
        String tag = ClientActions.drainScriptTag(context);
        assertTrue(tag.startsWith("<script nonce=\"t3stN0nce\" data-jweb-act>"), tag);
        assertTrue(tag.endsWith("</script>"), tag);

        assertEquals("", ClientActions.drainScriptTag(context), "nothing pending renders nothing");
        assertEquals("", ClientActions.drainScriptTag(null));
    }
}
