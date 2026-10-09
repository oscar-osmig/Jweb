package com.osmig.Jweb.app.docs;

import jweb.Action;
import jweb.css.Selector;

import static jweb.Js.*;

/**
 * Click-to-copy for docs code blocks. Delegated to .docs-layout so the buttons
 * keep working after client-side navigation swaps .docs-content.
 *
 * <p>{@code copyFrom("pre")} carries the whole behavior — the nearest
 * {@code <pre>} around the clicked button, the {@code execCommand} fallback for
 * contexts without {@code navigator.clipboard}, and the temporary "Copied!"
 * label with its {@code .copied} class.</p>
 */
final class CodeCopyScript {
    private CodeCopyScript() {}

    static Action build() {
        return actions()
            .does(guard("__codeCopyInit").does(
                delegate(DocsPage.DOCS_LAYOUT, "click", DocComponents.COPY_BTN).handler(
                    callback("e", "t").does(
                        copyFrom(Selector.type("pre").build()).trigger(v("t"))
                            .feedback("Copied!", 1600)
                            .failText("Copy failed")
                            .feedbackClass(DocComponents.COPIED.name())))));
    }
}
