package com.osmig.Jweb.framework.dsl;

import org.junit.jupiter.api.Test;

import static jweb.Js.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A realistic script written entirely as one builder chain — if any step
 * returned a type that could not continue, this would not compile.
 */
class JsBuilderStyleTest {

    @Test
    void aWholeScriptIsOneChain() {
        String js = script()
            .const_("count", 0)
            .add(func("render", "items")
                .let_("total", v("items").length())
                .if_(v("total").eq(0), return_())
                .forOf("item", v("items"),
                    call("console.log", v("item")))
                .return_(v("total")))
            .build();

        assertEquals("const count=0;"
            + "function render(items){"
            + "let total=items.length;"
            + "if((total===0)){return;}"
            + "for(const item of items){console.log(item);}"
            + "return total;}", js);
    }

    @Test
    void functionsRenderAsBothExpressionAndDeclaration() {
        // Both terminals are reachable from outside the package.
        Func fn = callback("e").call("handle", v("e"));
        assertEquals("function(e){handle(e);}", fn.toExpr());
        assertTrue(func("named").return_(1).toDecl().startsWith("function named()"));
    }

    @Test
    void domChainsKeepTheirTypeAndEndInAnExpression() {
        El el = byId("panel");
        String js = el.addClass("open").setText("Ready").js();
        assertEquals("document.getElementById('panel').classList.add('open')"
            + ".textContent='Ready'", js);
    }
}
