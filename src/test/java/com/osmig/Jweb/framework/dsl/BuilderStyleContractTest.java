package com.osmig.Jweb.framework.dsl;

import com.osmig.Jweb.framework.styles.CSS;
import org.junit.jupiter.api.Test;

import static jweb.Css.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The builder contract the DSL promises: a chain keeps its exact type, so it
 * never dead-ends, and mixing typed values with plain CSS strings is free.
 */
class BuilderStyleContractTest {

    @Test
    void aStyleChainKeepsItsConcreteTypeThroughout() {
        // No casts anywhere: the self-typed generic has to carry StyleBuilder
        // through every call, including the String overloads added this pass.
        CSS.StyleBuilder chained = style()
            .display(flex)
            .cursor("pointer")
            .padding(rem(1))
            .margin("0 auto")
            .gridTemplateColumns("repeat(3, 1fr)")
            .backgroundColor(hex("#fff"));

        assertEquals("display: flex; cursor: pointer; padding: 1rem; margin: 0 auto; "
            + "grid-template-columns: repeat(3, 1fr); background-color: #fff;", chained.build());
    }

    @Test
    void typedAndStringValuesAreInterchangeable() {
        assertEquals(style().display(flex).build(), style().display("flex").build());
        assertEquals(style().cursor(pointer).build(), style().cursor("pointer").build());
        assertEquals(style().padding(px(10)).build(), style().padding("10px").build());
    }

    @Test
    void everyBuilderTerminatesWithBuild() {
        // One terminal name across the CSS DSL, whatever the builder.
        assertNotNull(style().color(red).build());
        assertNotNull(media().maxWidth(px(768)).rule(".a", style().color(red)).build());
        assertNotNull(keyframes("fade").from(style().opacity(0)).to(style().opacity(1)).build());
        assertNotNull(stylesheet().rule(".a", style().color(red)).build());
    }

    @Test
    void aPlainJwebStyleArgumentIsTreatedAsAStyleNotAChild() {
        // jweb.Style is the canonical class; framework.styles.Style is a deprecated
        // shell over it. The element argument check used to name only the shell, so a
        // plain jweb.Style fell through to the child path.
        jweb.Style<?> plain = new jweb.Style<>().color("red");
        assertEquals("<div style=\"color: red;\"></div>",
            jweb.El.div(plain).toVNode().toHtml());
    }
}
