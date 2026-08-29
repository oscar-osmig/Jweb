package com.osmig.Jweb.app.pages.admin;

import jweb.Element;
import jweb.Csrf;
import jweb.CsrfToken;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import static com.osmig.Jweb.app.forms.FormComponents.*;

/** Admin login page with gradient-bordered card. */
public class AdminLoginPage implements Template {
    private final String error;
    private final CsrfToken csrfToken;

    public AdminLoginPage(CsrfToken csrfToken) { this(null, csrfToken); }
    public AdminLoginPage(String error, CsrfToken csrfToken) {
        this.error = error;
        this.csrfToken = csrfToken;
    }

    @Override
    public Element render() {
        return div(style()
                .display(flex).justifyContent(center).alignItems(center)
                .flex(1).padding(SP_8),
            loginCard()
        );
    }

    private Element loginCard() {
        return div(style()
                .position(relative)
                .width(px(400))
                .backgroundColor(white)
                .borderRadius(ROUNDED_LG)
                .overflow(hidden),
            // Gradient border (same technique as homepage feature cards)
            brandBorder(ROUNDED_LG),
            // Card content
            div(style()
                    .position(relative).zIndex(1).padding(SP_8),
                h2(style()
                        .fontSize(TEXT_2XL).fontWeight(700).color(TEXT)
                        .textAlign(center).marginBottom(SP_2),
                    text("Admin Login")),
                p(style()
                        .fontSize(TEXT_SM).color(TEXT_LIGHT)
                        .textAlign(center).marginBottom(SP_6),
                    text("Enter your credentials to access the dashboard")),
                errorMessage(),
                form(attrs().action("/only-admin/log/in").method("post").style()
                        .display(flex).flexDirection(column).gap(SP_4)
                    .done(),
                    Csrf.tokenField(csrfToken),
                    field("Email", "email", "email", "admin@example.com"),
                    field("Admin Token", "token", "password", "Enter admin token"),
                    gradientSubmitButton("Sign In")
                )
            )
        );
    }

    private Element gradientSubmitButton(String label) {
        return button(attrs().type("submit").style()
            .width(percent(100)).padding(SP_3)
            .apply(brandFlow())
            .color(white).border(none).borderRadius(ROUNDED)
            .fontSize(TEXT_BASE).fontWeight(600).cursor(pointer)
        .done(), text(label));
    }

    private Element errorMessage() {
        if (error == null) return text("");
        return div(style()
                .padding(SP_3).borderRadius(ROUNDED).marginBottom(SP_4)
                .backgroundColor(hex("#fee2e2")).color(hex("#991b1b"))
                .fontSize(TEXT_SM).textAlign(center),
            text(error)
        );
    }
}
