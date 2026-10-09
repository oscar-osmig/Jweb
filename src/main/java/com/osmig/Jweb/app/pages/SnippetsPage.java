package com.osmig.Jweb.app.pages;

import jweb.Action;
import jweb.Cls;
import jweb.Doc;
import jweb.Element;
import jweb.Template;

import java.util.List;
import java.util.Optional;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * The gallery: every approved snippet as a {@link SnippetCard}, newest first.
 * Snippets are sent in from the Sandbox's "Add snippet" form and appear here
 * once an admin approves them — this page only shows, it never takes input.
 */
public class SnippetsPage implements Template {

    public static final Cls LAYOUT = cls("snippets-layout");
    public static final Cls LIST = cls("snip-list");

    private final List<Doc> snippets;

    public SnippetsPage(List<Doc> snippets) {
        this.snippets = snippets;
    }

    @Override
    public String pageTitle() {
        return "Snippets - JWeb";
    }

    @Override
    public Optional<Action> scripts() {
        return Optional.of(SnippetScript.build());
    }

    @Override
    public Element render() {
        return div(LAYOUT, style()
                .padding(clamp(rem(2), vw(6), rem(3)), GUTTER)
                .maxWidth(px(1400)).margin(zero, auto).width(percent(100)),
            hero(),
            gallery()
        );
    }

    private Element hero() {
        int n = snippets.size();
        return section(style().marginBottom(SP_8),
            h1(style()
                    .fontSize(clamp(rem(1.9), vw(5), TEXT_4XL)).fontWeight(800).color(TEXT)
                    .textWrap("balance"),
                "Snippets"),
            p(style()
                    .fontSize(TEXT_LG).color(TEXT_LIGHT).lineHeight(1.6)
                    .maxWidth(px(640)).marginTop(SP_3),
                "Small pieces of JWeb, sent in from the Sandbox and published after review. "
                    + "Each one is the code and what it renders to."),
            p(style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_2),
                n + (n == 1 ? " snippet" : " snippets") + " published · ",
                a(href("/sandbox"), style().color(PRIMARY).fontWeight(600),
                    "add yours in the Sandbox →"))
        );
    }

    private Element gallery() {
        if (snippets.isEmpty()) {
            return section(style()
                    .textAlign(center).padding(SP_12, SP_4).color(TEXT_LIGHT)
                    .border(px(1), dashed, BORDER).borderRadius(ROUNDED_LG),
                p(style().fontSize(TEXT_LG), "No snippets yet"),
                p(style().fontSize(TEXT_SM).marginTop(SP_2),
                    "Open the Sandbox, write something, and press Add snippet.")
            );
        }
        return section(LIST, each(snippets, s -> SnippetCard.render(s)));
    }

    // ==================== styles ====================

    @Override
    public jweb.css.Stylesheet styles() {
        return stylesheet()
            .rule(LIST, style().display(grid).gap(SP_6));
    }
}
