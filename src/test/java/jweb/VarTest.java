package jweb;

import org.junit.jupiter.api.Test;

import static jweb.Js.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A JavaScript local as a handle: named once, declared, assigned and read
 * through the handle, and a callback parameter by handle too.
 */
class VarTest {

    @Test
    void aHandleIsItsNameAndAValue() {
        Var q = local("q");
        assertEquals("q", q.name());
        assertEquals("q", q.js(), "reads as the bare identifier");
        assertEquals("(q+1)", q.plus(1).js(), "a Var is a Val");
        assertEquals("q.length", q.length().js());
        assertEquals(local("q"), q);
        assertNotEquals(local("r"), q);
    }

    @Test
    void declarationsAndAssignmentsGoThroughTheHandle() {
        Var q = local("q");
        assertEquals("let q='a'", q.let(str("a")).js());
        assertEquals("const q=3", q.const_(3).js());
        assertEquals("q=(q+1)", q.set(q.plus(1)).js());
        assertEquals("q++", q.increment().js());
        assertEquals("q--", q.decrement().js());
    }

    @Test
    void callbacksTakeHandlesAsParameters() {
        Var e = local("e"), t = local("t");
        String js = callback(e, t).does(e.dot("preventDefault").call("")).toExpr();
        assertTrue(js.startsWith("function(e,t){"), js);
        assertTrue(js.contains("e.preventDefault"), js);

        String named = func("onKey", e).does(eventKey(e).eq("Escape")).toDecl();
        assertTrue(named.startsWith("function onKey(e){"), named);
        assertTrue(named.contains("(e.key===\"Escape\")") || named.contains("(e.key==='Escape')"), named);

        String declared = callback().let(e, str("x")).const_(t, 2).toExpr();
        assertTrue(declared.contains("let e='x';"), declared);
        assertTrue(declared.contains("const t=2;"), declared);
    }

    @Test
    void onlyIdentifiersAreAccepted() {
        assertThrows(IllegalArgumentException.class, () -> local("a b"));
        assertThrows(IllegalArgumentException.class, () -> local("1x"));
        assertThrows(IllegalArgumentException.class, () -> local(""));
        assertThrows(IllegalArgumentException.class, () -> local(null));
        assertEquals("_ok$1", local("_ok$1").name());
    }
}
