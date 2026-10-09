package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

/**
 * Handles: a class or id declared once and used by HTML, CSS and JS; the
 * value builders that replaced the last string values (3.0.2).
 */
public final class StylingHandles {
    private StylingHandles() {}

    public static Element render() {
        return since("v3.0.2", section(
            h3Title("Handles: one name for HTML, CSS and JS"),
            para("cls(\"card\") returns a Cls and id(\"x\") an Id: an element argument, an "
                + "immutable Selector and a JavaScript target. Declare it once as a static "
                + "field; the stylesheet and the script name the same thing the element "
                + "does, and a rename is a refactor instead of a search."),

            codeBlock("""
public class Card implements Template {
    static final Cls CARD = cls("card");
    static final Cls COPY = cls("copy");
    static final Id EDITOR = id("editor");

    @Override
    public Element render() {
        return article(CARD,
            button(COPY, "Copy"),
            textarea(EDITOR));
    }

    @Override
    public Stylesheet styles() {
        return stylesheet()
            .rule(CARD, style().padding(rem(1)).borderRadius(px(12)))
            .rule(COPY.hover(), style().color(hex("#4f46e5")))
            .rule(CARD.descendant(Selector.type("pre")), style().margin(zero))
            .rule(EDITOR.focus(), style().outline(none))
            .add(media().maxWidth(px(960)).rule(CARD, style().display(block)));
    }

    @Override
    public Optional<Action> scripts() {
        return Optional.of(actions().does(delegate(CARD, "click", COPY)
            .handler(callback("e", "t").does(copyFrom(Selector.type("pre")).trigger(v("t"))))));
    }
}"""),

            para("Every rule(...) takes a Selector as well as a String — on Stylesheet, "
                + "MediaQuery, ContainerQuery, Supports, Rule.of, startingStyle and scope. "
                + "Tag and universal selectors are Selector.type(\"pre\") and Selector.any(). "
                + "A multi-word cls(\"a b\") sets both classes; cls() with no name mints a "
                + "class scoped to the declaring class. A static component attaches its "
                + "rules with styled(sheet, element), as a Template's styles() does."),

            h3Title("Values that were strings"),
            para("Shadows, font stacks, calc, gradient stops and keyframe names have "
                + "builders, so no CSS is typed as text."),
            codeBlock("""
Keyframes shift = keyframes("shift").from(style().opacity(0)).to(style().opacity(1));

Style<?> card = style()
    .boxShadow(shadow(0, px(1), px(2), rgba(15, 23, 42, 0.04)),
               shadow(0, px(12), px(32), px(-16), rgba(79, 70, 229, 0.35)))
    .fontFamily(uiMonospace, font("SFMono-Regular"), font("Menlo"), monospace)
    .maxHeight(vh(100).minus(px(50)))
    .backgroundImage(radialGradient("circle", stop(hex("#e2e8f0"), px(1)), stop(transparent, px(1))))
    .whiteSpace(preWrap).overflowWrap(anywhere).textOverflow(ellipsis)
    .animation(shift, s(3), linear);

Stylesheet sheet = stylesheet().add(shift).rule(cls("card"), card);"""),

            docTip("Every CSS property has a camelCase setter and every keyword a constant; "
                + "prop(\"name\", \"value\") is never needed. The framework's own app is "
                + "built under a test that fails on any raw CSS or JS string.")
        ));
    }
}
