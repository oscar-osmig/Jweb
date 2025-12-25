package com.osmig.Jweb.app.pages.tryit;

import com.osmig.Jweb.framework.core.Element;

import com.osmig.Jweb.framework.elements.TextElement;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.pages.tryit.TryItTheme.*;

/** Shared UI components for Admin page. */
public final class AdminComponents {
    private AdminComponents() {}

    public static Element tabBtn(String id, String label, boolean active) {
        return button(attrs().id("tab-" + id).data("tab", id).class_("tab-btn").style()
            .padding(rem(0.75), rem(1.5)).border(none)
            .backgroundColor(active ? white : transparent).color(active ? INDIGO : GRAY_600)
            .fontWeight(500).cursor(pointer).borderBottom(px(2), solid, active ? INDIGO : transparent)
            .marginBottom(px(-1))
        .done(), text(label));
    }

    public static Element loginModal() {
        return div(attrs().id("login-overlay").style().position(fixed).top(zero).left(zero)
            .width(percent(100)).height(percent(100)).backgroundColor(rgba(0, 0, 0, 0.5))
            .display(flex).alignItems(center).justifyContent(center).zIndex(1000).done(),
            modalCard("Admin Login",
                form(attrs().id("login-form").style().display(flex).flexDirection(column).gap(rem(1)).done(),
                    field("Admin Email", "admin-email", "email", "admin@example.com"),
                    field("Secret Key", "admin-key", "password", "Enter secret key..."),
                    errorBox("login-error"),
                    submitBtn("Login", INDIGO)
                )
            )
        );
    }

    public static Element actionModal() {
        return div(attrs().id("modal-overlay").style().position(fixed).top(zero).left(zero)
            .width(percent(100)).height(percent(100)).backgroundColor(rgba(0, 0, 0, 0.5))
            .display(none).alignItems(center).justifyContent(center).zIndex(1001).done(),
            div(attrs().style().backgroundColor(white).borderRadius(px(16)).padding(rem(2))
                .maxWidth(px(500)).width(percent(90)).boxShadow("0 20px 60px rgba(0,0,0,0.3)").position(relative).done(),
                closeBtn(),
                div(attrs().id("modal-body").data("modal-body", ""))
            )
        );
    }

    private static Element modalCard(String title, Element content) {
        return div(attrs().style().backgroundColor(white).borderRadius(px(16)).padding(rem(2))
            .maxWidth(px(400)).width(percent(90)).boxShadow("0 20px 60px rgba(0,0,0,0.3)").done(),
            h2(attrs().style().fontSize(rem(1.25)).fontWeight(600).color(GRAY_900).margin(zero)
                .marginBottom(rem(1)).textAlign(center).done(), text(title)),
            content
        );
    }

    private static Element errorBox(String id) {
        return div(attrs().id(id).style().padding(rem(0.6)).borderRadius(px(6))
            .fontSize(rem(0.8)).backgroundColor(hex("#fee2e2")).color(hex("#991b1b")).display(none).done());
    }

    private static Element closeBtn() {
        return button(attrs().id("modal-close").style().position(absolute).top(rem(1)).right(rem(1))
            .backgroundColor(transparent).border(none).fontSize(rem(1.5)).cursor(pointer)
            .color(GRAY_500).lineHeight(num(1)).padding(rem(0.25)).done(), TextElement.raw("&times;"));
    }
}
