package com.osmig.Jweb.app.pages.admin;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.db.mongo.Doc;
import com.osmig.Jweb.framework.template.Template;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSGrid.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Admin messages dashboard showing contact form submissions. */
public class AdminMessagesPage implements Template {
    private final List<Doc> messages;

    public AdminMessagesPage(List<Doc> messages) {
        this.messages = messages;
    }

    @Override
    public Element render() {
        return div(style()
                .flex(1).padding(SP_8)
                .maxWidth(px(900)).margin(zero, auto).width(percent(100)),
            topBar(),
            messagesGrid()
        );
    }

    private Element topBar() {
        return div(style()
                .display(flex).justifyContent(spaceBetween).alignItems(center)
                .marginBottom(SP_8),
            div(
                h1(style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT),
                    text("Messages")),
                p(style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_1),
                    text(messages.size() + " submission" + (messages.size() != 1 ? "s" : "")))
            ),
            logoutButton()
        );
    }

    private Element logoutButton() {
        return a(attrs().href("/only-admin/logout").title("Logout").style()
                .display(flex).alignItems(center).justifyContent(center)
                .width(px(40)).height(px(40))
                .borderRadius(ROUNDED).color(TEXT_LIGHT)
                .textDecoration(none)
                .transitionColors(s(0.2))
            .done(),
            // Logout door icon (SVG)
            svg(attrs().viewBox(0, 0, 24, 24).width(24).height(24).lineIcon(2),
                path(attrs().d("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4")),
                polyline(attrs().points("16 17 21 12 16 7")),
                line(attrs().x1("21").y1("12").x2("9").y2("12"))
            )
        );
    }

    private Element messagesGrid() {
        if (messages.isEmpty()) {
            return emptyState();
        }
        return div(style()
                .display(grid).gridTemplateColumns(repeat(1, fr(1))).gap(SP_6),
            each(messages, this::messageCard)
        );
    }

    private Element emptyState() {
        return div(style()
                .textAlign(center).padding(rem(4))
                .color(TEXT_LIGHT).fontSize(TEXT_LG),
            p(text("No messages yet")),
            p(style().fontSize(TEXT_SM).marginTop(SP_2),
                text("Contact form submissions will appear here"))
        );
    }

    private Element messageCard(Doc msg) {
        return div(style()
                .position(relative)
                .backgroundColor(white)
                .borderRadius(ROUNDED_LG)
                .overflow(hidden),
            // Gradient border
            brandBorder(ROUNDED_LG),
            // Card content
            div(style()
                    .position(relative).zIndex(1).padding(SP_6),
                // Header row: name + date
                div(style()
                        .display(flex).justifyContent(spaceBetween).alignItems(center)
                        .marginBottom(SP_3),
                    span(style()
                            .fontSize(TEXT_LG).fontWeight(600).color(TEXT),
                        text(msg.getString("name"))),
                    span(style()
                            .fontSize(TEXT_SM).color(TEXT_LIGHT),
                        text(formatDate(msg.get("createdAt"))))
                ),
                // Email
                p(style()
                        .fontSize(TEXT_SM).color(PRIMARY).marginBottom(SP_3),
                    text(msg.getString("email"))),
                // Message body
                p(style()
                        .fontSize(TEXT_BASE).color(TEXT).lineHeight(1.7),
                    text(msg.getString("message")))
            )
        );
    }

    private String formatDate(Object dateObj) {
        if (dateObj instanceof Date date) {
            return new SimpleDateFormat("MMM d, yyyy 'at' h:mm a").format(date);
        }
        return dateObj != null ? dateObj.toString() : "";
    }
}
