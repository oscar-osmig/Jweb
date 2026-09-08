package com.osmig.Jweb.app.layout;

import com.osmig.Jweb.framework.state.StateManager;
import com.osmig.Jweb.framework.styles.PageStyles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static jweb.El.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The framework's own app is the proof: its layout's stylesheet — tokens,
 * reset and shell — is collected by the render, once, with nothing left in the
 * body.
 */
class AppStylesheetTest {

    @AfterEach
    void cleanup() {
        StateManager.clearContext();
    }

    @Test
    void theLayoutContributesTheTokensAndTheResetOnce() {
        var context = StateManager.createContext();
        String html = new Layout("Home", p("hi")).toHtml();

        assertFalse(html.contains("<style"), "no stylesheet left in the markup: " + html);

        String css = PageStyles.drainCss(context);
        assertNotNull(css);
        assertTrue(css.contains("--color-primary:#4f46e5;"), css);
        assertTrue(css.contains("--space-4:1rem;"), css);
        assertTrue(css.contains("--radius-lg:12px;"), css);
        assertTrue(css.contains("box-sizing: border-box;"), css);
        assertTrue(css.contains("@keyframes gradientShift"), css);
        assertEquals(1, css.split(":root\\{--color-primary", -1).length - 1,
            "the token block is emitted once");
    }

    @Test
    void theAppsTokenConstantsAreVarReferences() {
        assertEquals("var(--color-primary)", Theme.PRIMARY.css());
        assertEquals("var(--space-4)", Theme.SP_4.css());
        assertEquals("var(--radius-md)", Theme.ROUNDED.css());
        assertEquals("var(--text-3xl)", Theme.TEXT_3XL.css());
        assertEquals("var(--gradient-brand)", Theme.BRAND_GRADIENT.css());
        assertTrue(Theme.brandFlow().build().contains("var(--gradient-brand)"));
    }
}
