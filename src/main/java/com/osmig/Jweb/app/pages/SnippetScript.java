package com.osmig.Jweb.app.pages;

import jweb.Action;
import jweb.css.Selector;

import static jweb.Js.*;

/**
 * Click-to-copy on every snippet card's code pane, in the DSL. Delegated to
 * {@link SnippetsPage#LAYOUT}, which the gallery and the admin review page
 * share.
 *
 * <p>{@code copyFrom(...)} carries the whole behavior — the nearest
 * {@code <pre>} around the clicked button, the {@code execCommand} fallback
 * for contexts without {@code navigator.clipboard}, and the temporary
 * "Copied!" label with its {@link SnippetCard#COPIED} class.</p>
 *
 * <p>{@code copyFrom} itself only takes a String selector — no
 * {@code Selector} overload exists yet (framework gap), so the {@code <pre>}
 * target is built once here with {@code Selector.type("pre").build()}.</p>
 */
public final class SnippetScript {
    private SnippetScript() {}

    public static Action build() {
        return actions()
            .does(guard("__snippetsInit").does(
                delegate(SnippetsPage.LAYOUT, "click", SnippetCard.COPY).handler(
                    callback("e", "t").does(
                        copyFrom(Selector.type("pre").build()).trigger(v("t"))
                            .feedback("Copied!", 1600)
                            .failText("Copy failed")
                            .feedbackClass(SnippetCard.COPIED.name())))));
    }
}
