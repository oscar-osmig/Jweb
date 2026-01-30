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
        return div(attrs().style()
                .flex(num(1)).padding(SP_8)
                .maxWidth(px(900)).margin(zero, auto).width(percent(100))
            .done(),
            topBar(),
            messagesGrid()
        );
    }

    private Element topBar() {
        return div(attrs().style()
                .display(flex).justifyContent(spaceBetween).alignItems(center)
                .marginBottom(SP_8)
            .done(),
            div(
                h1(attrs().style().fontSize(TEXT_3XL).fontWeight(700).color(TEXT).done(),
                    text("Messages")),
                p(attrs().style().fontSize(TEXT_SM).color(TEXT_LIGHT).marginTop(SP_1).done(),
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
                .prop("transition", "color 0.2s")
            .done(),
            // Logout door icon (SVG)
            svg(attrs().viewBox("0 0 24 24").width("24").height("24")
                    .fill("none").stroke("currentColor")
                    .set("stroke-width", "2").set("stroke-linecap", "round")
                    .set("stroke-linejoin", "round"),
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
        return div(attrs().style()
                .display(grid).gridTemplateColumns(repeat(1, fr(1))).gap(SP_6)
            .done(),
            each(messages, this::messageCard)
        );
    }

    private Element emptyState() {
        return div(attrs().style()
                .textAlign(center).padding(rem(4))
                .color(TEXT_LIGHT).fontSize(TEXT_LG)
            .done(),
            p(text("No messages yet")),
            p(attrs().style().fontSize(TEXT_SM).marginTop(SP_2).done(),
                text("Contact form submissions will appear here"))
        );
    }

    private Element messageCard(Doc msg) {
        return div(attrs().style()
                .position(relative)
                .backgroundColor(white)
                .borderRadius(ROUNDED_LG)
                .overflow(hidden)
            .done(),
            // Gradient border
            gradientBorder(),
            // Card content
            div(attrs().style()
                    .position(relative).zIndex(1).padding(SP_6)
                .done(),
                // Header row: name + date
                div(attrs().style()
                        .display(flex).justifyContent(spaceBetween).alignItems(center)
                        .marginBottom(SP_3)
                    .done(),
                    span(attrs().style()
                            .fontSize(TEXT_LG).fontWeight(600).color(TEXT)
                        .done(),
                        text(msg.getString("name"))),
                    span(attrs().style()
                            .fontSize(TEXT_SM).color(TEXT_LIGHT)
                        .done(),
                        text(formatDate(msg.get("createdAt"))))
                ),
                // Email
                p(attrs().style()
                        .fontSize(TEXT_SM).color(PRIMARY).marginBottom(SP_3)
                    .done(),
                    text(msg.getString("email"))),
                // Message body
                p(attrs().style()
                        .fontSize(TEXT_BASE).color(TEXT).lineHeight(1.7)
                    .done(),
                    text(msg.getString("message")))
            )
        );
    }

    private Element gradientBorder() {
        return div(attrs().style()
            .position(absolute).top(zero).left(zero).right(zero).bottom(zero)
            .borderRadius(ROUNDED_LG)
            .padding(px(2))
            .background(linearGradient("90deg",
                hex("#6366f1"), hex("#8b5cf6"), hex("#a855f7"),
                hex("#ec4899"), hex("#8b5cf6"), hex("#6366f1")))
            .backgroundSize(() -> "300% 100%")
            .animation(anim("gradientShift"), s(3), linear, s(0), infinite)
            .prop("-webkit-mask", "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)")
            .prop("mask", "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)")
            .prop("-webkit-mask-composite", "xor")
            .prop("mask-composite", "exclude")
            .zIndex(0)
        .done());
    }

    private String formatDate(Object dateObj) {
        if (dateObj instanceof Date date) {
            return new SimpleDateFormat("MMM d, yyyy 'at' h:mm a").format(date);
        }
        return dateObj != null ? dateObj.toString() : "";
    }
}
