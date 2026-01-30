package com.osmig.Jweb.app.pages.admin;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.app.forms.FormComponents.*;

/** Admin login page with gradient-bordered card. */
public class AdminLoginPage implements Template {
    private final String error;

    public AdminLoginPage() { this.error = null; }
    public AdminLoginPage(String error) { this.error = error; }

    @Override
    public Element render() {
        return div(attrs().style()
                .display(flex).justifyContent(center).alignItems(center)
                .flex(num(1)).padding(SP_8)
            .done(),
            loginCard()
        );
    }

    private Element loginCard() {
        return div(attrs().style()
                .position(relative)
                .width(px(400))
                .backgroundColor(white)
                .borderRadius(ROUNDED_LG)
                .overflow(hidden)
            .done(),
            // Gradient border (same technique as homepage feature cards)
            gradientBorder(),
            // Card content
            div(attrs().style()
                    .position(relative).zIndex(1).padding(SP_8)
                .done(),
                h2(attrs().style()
                        .fontSize(TEXT_2XL).fontWeight(700).color(TEXT)
                        .textAlign(center).marginBottom(SP_2)
                    .done(),
                    text("Admin Login")),
                p(attrs().style()
                        .fontSize(TEXT_SM).color(TEXT_LIGHT)
                        .textAlign(center).marginBottom(SP_6)
                    .done(),
                    text("Enter your credentials to access the dashboard")),
                errorMessage(),
                form(attrs().action("/only-admin/log/in").method("post").style()
                        .display(flex).flexDirection(column).gap(SP_4)
                    .done(),
                    field("Email", "email", "email", "admin@example.com"),
                    field("Admin Token", "token", "password", "Enter admin token"),
                    gradientSubmitButton("Sign In")
                )
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

    private Element gradientSubmitButton(String label) {
        return button(attrs().type("submit").style()
            .width(percent(100)).padding(SP_3)
            .background(linearGradient("90deg",
                hex("#6366f1"), hex("#8b5cf6"), hex("#a855f7"),
                hex("#ec4899"), hex("#8b5cf6"), hex("#6366f1")))
            .backgroundSize(() -> "300% 100%")
            .animation(anim("gradientShift"), s(3), linear, s(0), infinite)
            .color(white).border(none).borderRadius(ROUNDED)
            .fontSize(TEXT_BASE).fontWeight(600).cursor(pointer)
        .done(), text(label));
    }

    private Element errorMessage() {
        if (error == null) return text("");
        return div(attrs().style()
                .padding(SP_3).borderRadius(ROUNDED).marginBottom(SP_4)
                .backgroundColor(hex("#fee2e2")).color(hex("#991b1b"))
                .fontSize(TEXT_SM).textAlign(center)
            .done(),
            text(error)
        );
    }
}
