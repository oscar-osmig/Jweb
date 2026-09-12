package jweb.js;

import org.junit.jupiter.api.Test;

import static jweb.Css.*;
import static jweb.El.*;
import static jweb.Js.*;
import static jweb.Three.*;
import static jweb.js.JSAnimation.*;
import static jweb.js.JSClipboard.*;
import static jweb.js.JSFormData.*;
import static jweb.js.JSHistory.*;
import static jweb.js.JSIndexedDB.*;
import static jweb.js.JSMedia.*;
import static jweb.js.JSObservers.*;
import static jweb.js.JSStorage.*;
import static jweb.js.JSUrl.*;
import static jweb.js.JSWebAnimations.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A browser-API module's own static import sits next to the four DSL
 * wildcards. {@code jweb.Js} used to re-export ten of these modules, which
 * turned the documented {@code import static jweb.js.JSStorage.*} beside
 * {@code import static jweb.Js.*} into an "ambiguous reference" for every
 * shared name — Java has no way to tell two identical statics apart. The
 * re-exports are gone; this file imports all ten modules together with the
 * four wildcards and calls one static from each, so the pairing cannot
 * silently break again.
 */
class ModuleImportsCoexistTest {

    @Test
    void everyModuleImportSitsBesideTheFourWildcards() {
        Object clip = writeText("hi");                        // JSClipboard
        Object watch = intersection();                        // JSObservers
        Object store = local();                               // JSStorage
        Object history = replaceState("/x");                  // JSHistory
        Object params = queryParamsObject();                  // JSUrl
        Object fd = formData();                               // JSFormData
        Object frames = raf(callback("t").log(v("t")));       // JSAnimation
        Object frame = keyframe(0.5);                         // JSWebAnimations
        Object level = volume(v("player"));                   // JSMedia
        Object db = openDB("app", 1);                         // JSIndexedDB

        // and the four wildcards still resolve to what they always did
        Object page = div(cls("x"), style().padding(px(4)), "hi");
        Object action = toggle("panel");
        Object node = box();

        assertNotNull(clip); assertNotNull(watch); assertNotNull(store);
        assertNotNull(history); assertNotNull(params); assertNotNull(fd);
        assertNotNull(frames); assertNotNull(frame); assertNotNull(level);
        assertNotNull(db); assertNotNull(page); assertNotNull(action); assertNotNull(node);
        assertTrue(local().get("k").js().contains("localStorage"), local().get("k").js());
    }
}
