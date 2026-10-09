package com.osmig.Jweb.app.pages.admin;

import jweb.Csrf;
import jweb.Element;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/**
 * The admin pages' shared top bar: the title with a line under it, the
 * section tabs, and the logout button.
 */
public final class AdminBar {
    private AdminBar() {}

    /**
     * @param title    the page heading
     * @param subtitle the line under it (counts, order links…)
     * @param active   which tab this page is: {@code messages} or {@code snippets}
     */
    public static Element render(String title, Element subtitle, String active) {
        return div(row().justifyContent(spaceBetween).alignItems(flexStart)
                .flexWrap(wrap).gap(SP_4).marginBottom(SP_8),
            div(
                h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT), title),
                subtitle
            ),
            div(cluster(SP_3).alignItems(center),
                tabs(active),
                logoutButton()
            )
        );
    }

    private static Element tabs(String active) {
        return nav(attrs().aria("label", "Admin sections").style(cluster(SP_1)),
            tab("messages", "Messages", "/only-admin/messages", active),
            tab("snippets", "Snippets", "/only-admin/snippets", active)
        );
    }

    private static Element tab(String key, String label, String url, String active) {
        boolean current = key.equals(active);
        return a(href(url), style()
                .padding(SP_1, SP_3).borderRadius(px(999))
                .fontSize(TEXT_SM).fontWeight(600).textDecoration(none)
                .color(current ? white : PRIMARY)
                .backgroundColor(current ? PRIMARY : hex("#eef2ff")),
            label);
    }

    // Logout is a POST (with CSRF token) so a cross-site link can't trigger it
    private static Element logoutButton() {
        return form(action("/only-admin/logout"), method("post"),
                style().margin(zero),
            Csrf.tokenField(),
            button(attrs().type("submit").title("Logout"),
                style()
                    .apply(center())
                    .width(px(40)).height(px(40))
                    .backgroundColor(transparent).border(none).cursor(pointer)
                    .borderRadius(ROUNDED).color(TEXT_LIGHT)
                    .transitionColors(s(0.2)),
                // Logout door icon (SVG)
                svg(attrs().viewBox(0, 0, 24, 24).width(24).height(24).lineIcon(2),
                    path(attrs().d("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4")),
                    polyline(attrs().points("16 17 21 12 16 7")),
                    line(attrs().x1("21").y1("12").x2("9").y2("12"))
                )
            )
        );
    }
}
