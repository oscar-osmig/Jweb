package com.osmig.Jweb.app.pages.admin;

import com.osmig.Jweb.app.pages.SnippetCard;
import com.osmig.Jweb.app.pages.SnippetScript;
import com.osmig.Jweb.app.pages.SnippetsPage;
import jweb.Action;
import jweb.Cls;
import jweb.Csrf;
import jweb.Doc;
import jweb.Element;
import jweb.Id;
import jweb.Template;

import java.util.List;
import java.util.Optional;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * The review queue. Every submission is shown exactly as it would appear in
 * the public gallery — code, notes and the rendered preview — so approval is
 * a judgement on the real thing. Approve and reject are POSTs carrying the
 * CSRF token; the same actions are open to scripts through
 * {@code /api/v1/admin/snippets}.
 */
public class AdminSnippetsPage implements Template {
    private static final Id NOTICE = id("admin-notice");

    private final List<Doc> pending;
    private final List<Doc> approved;
    private final String notice;

    public AdminSnippetsPage(List<Doc> pending, List<Doc> approved, String notice) {
        this.pending = pending;
        this.approved = approved;
        this.notice = notice;
    }

    @Override
    public String pageTitle() {
        return "Snippets - Admin";
    }

    @Override
    public Optional<Action> scripts() {
        return Optional.of(SnippetScript.build());
    }

    @Override
    public Element render() {
        return div(SnippetsPage.LAYOUT, style()
                .flex(1).padding(SP_8, GUTTER)
                .maxWidth(px(1400)).margin(zero, auto).width(percent(100)),
            AdminBar.render("Snippets",
                p(style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_1),
                    count(pending.size(), "awaiting review"), " · ",
                    count(approved.size(), "published")),
                "snippets"),
            noticeMessage(),
            queue(),
            published()
        );
    }

    /** A one-shot session flash ("Snippet published."). */
    private Element noticeMessage() {
        return when(notice != null, () -> div(NOTICE, style()
                .padding(SP_3).borderRadius(ROUNDED).marginBottom(SP_6)
                .backgroundColor(hex("#dcfce7")).color(hex("#166534"))
                .fontSize(TEXT_SM),
            notice));
    }

    private Element queue() {
        return section(style().marginBottom(SP_12),
            heading("Awaiting review", pending.size()),
            pending.isEmpty()
                ? empty("Nothing to review", "New submissions from /snippets land here.")
                : div(SnippetsPage.LIST,
                    each(pending, s -> SnippetCard.render(s, approveForm(s), rejectForm(s))))
        );
    }

    private Element published() {
        return section(
            heading("Published", approved.size()),
            approved.isEmpty()
                ? empty("Nothing published yet", "Approved snippets show on /snippets, newest first.")
                : div(SnippetsPage.LIST,
                    each(approved, s -> SnippetCard.render(s, unpublishForm(s))))
        );
    }

    private Element heading(String title, int n) {
        return div(row().alignItems(center).gap(SP_3).marginBottom(SP_4),
            h2(style().fontSize(TEXT_XL).fontWeight(700).color(TEXT), title),
            span(style()
                    .fontSize(TEXT_SM).fontWeight(600).color(PRIMARY)
                    .backgroundColor(hex("#eef2ff"))
                    .padding(px(2), SP_2).borderRadius(px(999)),
                String.valueOf(n))
        );
    }

    private Element empty(String title, String hint) {
        return div(style()
                .textAlign(center).padding(SP_8).color(TEXT_LIGHT)
                .border(px(1), dashed, BORDER).borderRadius(ROUNDED_LG),
            p(style().fontSize(TEXT_LG), title),
            p(style().fontSize(TEXT_SM).marginTop(SP_2), hint)
        );
    }

    private static String count(int n, String what) {
        return n + " " + what;
    }

    // ==================== actions ====================

    private static final Cls BTN = cls("snip-btn");
    private static final Cls BTN_APPROVE = cls("snip-btn-approve");
    private static final Cls BTN_REJECT = cls("snip-btn-reject");

    private Element approveForm(Doc s) {
        return actionForm("/only-admin/snippets/" + s.getId() + "/approve", BTN_APPROVE, "Approve");
    }

    private Element rejectForm(Doc s) {
        return actionForm("/only-admin/snippets/" + s.getId() + "/delete", BTN_REJECT, "Reject");
    }

    private Element unpublishForm(Doc s) {
        return actionForm("/only-admin/snippets/" + s.getId() + "/delete", BTN_REJECT, "Unpublish");
    }

    // Each action is a POST with the CSRF token so a cross-site link can't trigger it
    private Element actionForm(String url, Cls variant, String label) {
        return form(action(url), method("post"), style().margin(zero),
            Csrf.tokenField(),
            button(BTN, variant, attrs().type("submit"), label)
        );
    }

    // ==================== styles ====================

    @Override
    public jweb.css.Stylesheet styles() {
        return stylesheet()
            .rule(SnippetsPage.LIST, style().display(grid).gap(SP_6))
            .rule(BTN, style()
                .padding(SP_2, SP_4).borderRadius(ROUNDED)
                .fontSize(TEXT_SM).fontWeight(600).cursor(pointer)
                .border(px(1), solid, transparent))
            .rule(BTN_APPROVE, style().apply(brandFlow()).color(white))
            .rule(BTN_REJECT, style()
                .backgroundColor(white).color(hex("#b91c1c")).borderColor(hex("#fecaca")))
            .rule(BTN_REJECT.hover(), style().backgroundColor(hex("#fef2f2")));
    }
}
