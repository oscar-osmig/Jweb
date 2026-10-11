package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.server.JWebAssetsController;
import com.osmig.Jweb.framework.util.Json;
import com.osmig.Jweb.framework.websocket.WebSocketMessage.InitStateResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The connection protocol the runtime owes the user: a page whose render
 * context the server no longer holds says so (on the first click, or at once
 * when the socket reconnects and the server answers {@code known:false}), a
 * socket that will not come back says so, and both are configurable.
 */
class JWebRuntimeConnectionTest {

    @AfterEach
    void resetDefaults() {
        JWebRuntime.setExpiredAction(JWebRuntime.ExpiredAction.BANNER);
        JWebRuntime.setReconnectAttempts(8);
    }

    @Test
    void expiredContextReachesTheUserNotJustTheConsole() {
        String script = JWebRuntime.getScript();
        // the server's error code is handled, not logged
        assertTrue(script.contains("if(msg.code==='context_expired'){this.onExpired();break;}"), "error branch");
        // the notice is a real element with a reload affordance
        assertTrue(script.contains("notice:function(kind){"), "notice helper");
        assertTrue(script.contains("This page has expired. Reload to continue."), "expired text");
        assertTrue(script.contains("Connection to the server was lost. Reload to continue."), "lost text");
        assertTrue(script.contains("btn.addEventListener('click',function(){location.reload();});"), "reload button");
        assertTrue(script.contains("el.setAttribute('role','alert');"), "announced to assistive tech");
        // themeable the same way showMessage is
        assertTrue(script.contains("var(--jweb-notice-bg,"), "theme variable");
        assertTrue(script.contains("el.className='jweb-notice';"), "class hook");
        // page scripts can react
        assertTrue(script.contains("new CustomEvent('jweb:expired')"), "jweb:expired event");
    }

    @Test
    void reconnectAnnouncesExpiryBeforeTheFirstDroppedClick() {
        String script = JWebRuntime.getScript();
        assertTrue(script.contains("if(msg.known===false&&this.data&&this.data.contextId&&this.hasServerBindings())this.onExpired();"),
            "initState known:false → expired, only when the page has server-bound elements");
        assertTrue(script.contains("hasServerBindings:function(){"), "binding probe");
        assertTrue(script.contains("indexOf('data-jweb-on')===0"), "probe covers server handlers");
        assertTrue(script.contains("[data-live],[data-state],[data-state-bind],[data-state-input],[data-state-attr],[data-state-class]"),
            "probe covers live regions and bindings");
    }

    @Test
    void eventsFiredWhileReconnectingAreHeldNotDropped() {
        String script = JWebRuntime.getScript();
        assertTrue(script.contains("if(this.pending.length<20)this.pending.push(eventData);"), "bounded queue");
        assertTrue(script.contains("queued.forEach(function(d){self.ws.send(JSON.stringify(d));});"), "flushed on open");
        assertFalse(script.contains("console.warn('[JWeb] Not connected');"), "the old silent drop is gone");
    }

    @Test
    void backoffIsExponentialCappedAndRetriesOnOnlineOrVisible() {
        String script = JWebRuntime.getScript();
        assertTrue(script.contains("Math.min(this.reconnectDelay*Math.pow(2,this.reconnectAttempts-1),30000)"), "1s,2s,4s… ≤30s");
        assertTrue(script.contains("this.notice('lost');"), "give-up is visible");
        assertTrue(script.contains("window.addEventListener('online',retry);"), "online retries");
        assertTrue(script.contains("if(document.visibilityState==='visible')retry();"), "foreground retries");
        assertTrue(script.contains("this.initConnectionWatch();"), "installed at init");
    }

    @Test
    void settingsAreBakedIntoTheScriptAndChangeItsVersion() {
        String defaults = JWebRuntime.getScript();
        assertTrue(defaults.contains("maxReconnectAttempts:8,"), "default attempts");
        assertTrue(defaults.contains("expiredAction:'banner',"), "default action");
        String v1 = JWebAssetsController.versionOf(defaults);

        JWebRuntime.setExpiredAction(JWebRuntime.ExpiredAction.RELOAD);
        JWebRuntime.setReconnectAttempts(3);
        String tuned = JWebRuntime.getScript();
        assertTrue(tuned.contains("maxReconnectAttempts:3,"), tuned.substring(0, 300));
        assertTrue(tuned.contains("expiredAction:'reload',"));
        assertFalse(tuned.contains("__RECONNECT_ATTEMPTS__") || tuned.contains("__EXPIRED_ACTION__"), "placeholders resolved");
        assertNotEquals(v1, JWebAssetsController.versionOf(tuned), "browsers must not keep the old script");
    }

    @Test
    void expiredActionParsesLeniently() {
        assertEquals(JWebRuntime.ExpiredAction.BANNER, JWebRuntime.ExpiredAction.parse(null));
        assertEquals(JWebRuntime.ExpiredAction.BANNER, JWebRuntime.ExpiredAction.parse(" "));
        assertEquals(JWebRuntime.ExpiredAction.RELOAD, JWebRuntime.ExpiredAction.parse("Reload"));
        assertEquals(JWebRuntime.ExpiredAction.SILENT, JWebRuntime.ExpiredAction.parse(" silent "));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> JWebRuntime.ExpiredAction.parse("toast"));
        assertTrue(e.getMessage().contains("jweb.runtime.expired"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> JWebRuntime.setReconnectAttempts(-1));
    }

    @Test
    void initStateCarriesWhetherTheServerKnowsTheContext() {
        String unknown = Json.stringify(new InitStateResponse(List.of(), false));
        assertTrue(unknown.contains("\"known\":false"), unknown);
        String known = Json.stringify(new InitStateResponse(List.of(), true));
        assertTrue(known.contains("\"known\":true"), known);
        String noContextNamed = Json.stringify(new InitStateResponse(List.of()));
        assertFalse(noContextNamed.contains("known"), "absent when the client named no context: " + noContextNamed);
    }
}
