package com.osmig.Jweb.framework.dsl;

import org.junit.jupiter.api.Test;

import static jweb.Js.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the JS-core simplification pass: keyword-consistent {@code return_},
 * one-call loop bodies, the short {@code v()}/{@code byId()} names, and the
 * closure-based debounce/throttle.
 */
class JsCoreSimplificationTest {

    @Test
    void return_MatchesTheKeywordConvention() {
        assertEquals("return", return_().js());
        assertEquals("return 5", return_(5).js());
        assertEquals("function f(){return 'hi';}",
            func("f").return_(str("hi")).toDecl());
    }

    @Test
    void vAndByIdAreShortNamesForTheSameThing() {
        assertEquals("count", v("count").js());
        assertEquals(variable("count").js(), v("count").js());
        assertEquals("document.getElementById('main')", byId("main").js());
        assertEquals(getElem("main").js(), byId("main").js());
    }

    @Test
    void loopBodiesInlineWithNoEndCall() {
        String forLoop = func("f").for_("i", 0, v("n"), call("log", v("i"))).toDecl();
        assertEquals("function f(){for(let i=0;i<n;i++){log(i);}}", forLoop);

        String forOf = func("f").forOf("item", v("items"), call("draw", v("item"))).toDecl();
        assertEquals("function f(){for(const item of items){draw(item);}}", forOf);

        String forIn = func("f").forIn("k", v("obj"), call("log", v("k"))).toDecl();
        assertEquals("function f(){for(const k in obj){log(k);}}", forIn);

        String whileLoop = func("f").while_(v("go"), "i++").toDecl();
        assertEquals("function f(){while(go){i++;}}", whileLoop);

        String doWhile = func("f").doWhile(v("go"), "i++").toDecl();
        assertEquals("function f(){do{i++;}while(go);}", doWhile);
    }

    @Test
    void oneCallLoopMatchesTheBuilderForm() {
        String builder = func("f").for_("i", 0, v("n")).body(call("log", v("i"))).endFor().toDecl();
        String inline = func("f").for_("i", 0, v("n"), call("log", v("i"))).toDecl();
        assertEquals(builder, inline);
    }

    @Test
    void tryCatchAndSwitchCloseThemselves() {
        String tryCatch = func("f")
            .try_().body(call("risky"))
            .catch_("e", call("report", v("e")))
            .toDecl();
        assertEquals("function f(){try{risky();}catch(e){report(e);}}", tryCatch);

        String sw = func("f")
            .switch_(v("action"))
                .case_("add", call("add"), "break")
                .default_(call("noop"))
            .toDecl();
        assertEquals("function f(){switch(action){case 'add':add();break;default:noop();}}", sw);
    }

    @Test
    void debounceAndThrottleNeedNoTimerVariable() {
        String debounced = debounce(300, callback().call("search")).js();
        assertTrue(debounced.startsWith("(function(){let _t;return function(){"), debounced);
        assertTrue(debounced.contains("clearTimeout(_t)"), debounced);
        assertTrue(debounced.contains("300"), debounced);

        String throttled = throttle(100, callback().call("onScroll")).js();
        assertTrue(throttled.startsWith("(function(){let _last=0;return function(){"), throttled);
        assertTrue(throttled.contains("Date.now()"), throttled);
        assertTrue(throttled.contains("100"), throttled);
    }

    @Test
    void sliceStrAndSliceEmitTheSameCall() {
        assertEquals(v("s").slice(0, 3).js(), v("s").sliceStr(0, 3).js());
        assertEquals(v("s").slice(2).js(), v("s").sliceStr(2).js());
    }

    @Test
    void indexOfIsOverloadedOnArgumentType() {
        assertEquals("arr.indexOf('x')", v("arr").indexOf("x").js());
        assertEquals("arr.indexOf(needle)", v("arr").indexOf(v("needle")).js());
    }
}
