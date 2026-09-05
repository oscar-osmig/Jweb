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

    @Test
    void canvasPropertiesReadAndWriteUnderOneName() {
        // ctx.fillStyle is a property: one argument reads it, two write it.
        assertEquals("c.fillStyle", jweb.js.JSCanvas.fillStyle(v("c")).js());
        assertEquals("c.fillStyle='#f00'", jweb.js.JSCanvas.fillStyle(v("c"), "#f00").js());
        assertEquals(jweb.js.JSCanvas.setFillStyle(v("c"), "#f00").js(),
                     jweb.js.JSCanvas.fillStyle(v("c"), "#f00").js());
    }

    @Test
    void chainableContextDoesNotThreadCtxThroughEveryCall() {
        String js = jweb.js.JSCanvas.ctx2d(byId("chart"))
            .fillStyle("#4f46e5")
            .fillRect(10, 10, 100, 50)
            .stroke()
            .build().js();
        assertEquals("document.getElementById('chart').getContext('2d').fillStyle='#4f46e5';"
            + "document.getElementById('chart').getContext('2d').fillRect(10,10,100,50);"
            + "document.getElementById('chart').getContext('2d').stroke()", js);
    }

    @Test
    void mediaPropertiesShareOneName() {
        assertEquals(jweb.js.JSMedia.setVolume(v("audio"), 0.5).js(),
                     jweb.js.JSMedia.volume(v("audio"), 0.5).js());
    }

    @Test
    void webAudioSynthesisTierEmitsTheExactJs() {
        assertEquals("ctx.createBuffer(2,44100,44100)",
            jweb.js.JSMedia.createBuffer(v("ctx"), 2, 44100, 44100).js());
        assertEquals("ctx.createBufferSource()", jweb.js.JSMedia.createBufferSource(v("ctx")).js());

        assertEquals("gain.gain.setValueAtTime(0.0,0.0)",
            jweb.js.JSMedia.setValueAtTime(v("gain").dot("gain"), 0, 0).js());
        assertEquals("gain.gain.linearRampToValueAtTime(1.0,0.05)",
            jweb.js.JSMedia.linearRampToValueAtTime(v("gain").dot("gain"), 1, 0.05).js());
        assertEquals("gain.gain.exponentialRampToValueAtTime(1.0E-4,0.4)",
            jweb.js.JSMedia.exponentialRampToValueAtTime(v("gain").dot("gain"), 0.0001, 0.4).js());
    }

    @Test
    void audioContextShortNamesShareOneImplementationWithTheLongOnes() {
        assertEquals(jweb.js.JSMedia.resumeAudioContext(v("ctx")).js(),
                     jweb.js.JSMedia.resume(v("ctx")).js());
        assertEquals(jweb.js.JSMedia.audioContextState(v("ctx")).js(),
                     jweb.js.JSMedia.audioState(v("ctx")).js());
        assertEquals("ctx.state", jweb.js.JSMedia.audioState(v("ctx")).js());
    }
}
