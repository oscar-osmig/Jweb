package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.js.JSWebAnimations;
import org.junit.jupiter.api.Test;

import static jweb.Js.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the JS-module pass: the bugs that shipped silently broken, the
 * platform-name renames, and the {@code toVal()} expression contract.
 */
class JsModuleSimplificationTest {

    @Test
    void reconnectingSocketReattachesItsHandlers() {
        String js = jweb.js.JSWebSocket.webSocket("wss://example.test")
            .onMessage(callback("e").call("handle", v("e")))
            .autoReconnect(1000)
            .build("sock")
            .js();

        // The reconnect re-runs the same connect function, so the replacement
        // socket is handed the handlers again instead of coming back deaf.
        assertTrue(js.contains("function _connect_sock()"), js);
        assertTrue(js.contains("setTimeout(_connect_sock,1000)"), js);

        // The handler binding must live INSIDE connect(), not beside it.
        int connectBody = js.indexOf("function _connect_sock(){");
        int handler = js.indexOf(".onmessage=");
        int reconnect = js.indexOf("setTimeout(_connect_sock");
        assertTrue(connectBody < handler && handler < reconnect,
            "onmessage must be bound inside the connect function: " + js);
    }

    @Test
    void toValIsAnExpressionNotAStatement() {
        String ws = jweb.js.JSWebSocket.webSocket("wss://example.test")
            .onMessage(callback("e").call("handle", v("e")))
            .toVal().js();
        assertFalse(ws.startsWith("var "), "toVal must not emit a statement: " + ws);
        assertTrue(ws.startsWith("(function(){"), ws);
        assertTrue(ws.endsWith("})()"), ws);
    }

    @Test
    void pushStateTakesPlatformArgumentOrder() {
        // Platform: history.pushState(state, title, url)
        assertEquals("history.pushState({page:'home'},'','/home')",
            jweb.js.JSHistory.pushState(obj("page", "home"), "/home").js());
        assertEquals("history.pushState(null,'','/home')",
            jweb.js.JSHistory.pushState("/home").js());
    }

    @Test
    void transitionUsesCssNameInTheShorthandAndDomKeyOnTheStyle() {
        String js = jweb.js.JSAnimation.transition(byId("box"), "backgroundColor", "red", "blue", 500).js();
        // The shorthand is CSS, so it needs the hyphenated name...
        assertTrue(js.contains(".style.transition='background-color 500ms'"), js);
        // ...while the style assignment is JS, so it needs the camelCase key.
        assertTrue(js.contains(".style.backgroundColor='red'"), js);
        assertTrue(js.contains(".style.backgroundColor='blue'"), js);

        // Either spelling may be passed in.
        assertEquals(js, jweb.js.JSAnimation.transition(byId("box"), "background-color", "red", "blue", 500).js());
    }

    @Test
    void animationWithoutKeyframesFailsLoudly() {
        var builder = JSWebAnimations.animate(byId("box")).duration(300);
        IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(e.getMessage().contains("keyframes"), e.getMessage());
    }

    @Test
    void platformNamesExistAlongsideTheOldOnes() {
        assertEquals("Date.now()", jweb.js.JSDate.dateNow().js());
        assertEquals("new Date()", jweb.js.JSDate.newDate().js());
        assertEquals(jweb.js.JSCrypto.randomValues(v("buf")).js(),
                     jweb.js.JSCrypto.getRandomValues(v("buf")).js());
        assertEquals(jweb.js.JSStorage.local().get("k").js(),
                     jweb.js.JSStorage.local().getItem("k").js());
    }

    @Test
    void currentUrlNoLongerMeansTwoThings() {
        assertEquals("location.href", jweb.js.JSHistory.currentUrl().js());
        assertEquals("new URL(location.href)", jweb.js.JSUrl.currentUrlObject().js());
    }
}
