package com.osmig.Jweb.app.pages.admin;

import com.osmig.Jweb.app.forms.AdminLogin;
import jweb.CsrfToken;
import jweb.Element;
import jweb.Form;
import jweb.Template;

import static jweb.El.*;
import static jweb.Css.*;
import static com.osmig.Jweb.app.layout.Theme.*;

/** Admin login page with gradient-bordered card. */
public class AdminLoginPage implements Template {
    private final String error;
    private final String notice;
    private final Form.Bound<AdminLogin> submitted;
    private final CsrfToken csrfToken;

    public AdminLoginPage(CsrfToken csrfToken) { this(null, null, null, csrfToken); }
    public AdminLoginPage(String error, CsrfToken csrfToken) { this(error, null, null, csrfToken); }
    public AdminLoginPage(String error, String notice, CsrfToken csrfToken) { this(error, notice, null, csrfToken); }
    /** After a submit: the bound form re-renders with the values typed and any field errors. */
    public AdminLoginPage(String error, Form.Bound<AdminLogin> submitted, CsrfToken csrfToken) {
        this(error, null, submitted, csrfToken);
    }
    public AdminLoginPage(String error, String notice, Form.Bound<AdminLogin> submitted, CsrfToken csrfToken) {
        this.error = error;
        this.notice = notice;
        this.submitted = submitted;
        this.csrfToken = csrfToken;
    }

    @Override
    public Element render() {
        return div(center()
                .flex(1).padding(SP_8),
            loginCard()
        );
    }

    private Element loginForm() {
        Form<AdminLogin> form = form(AdminLogin.class)
            .cls("admin-login-form")
            .action("/only-admin/log/in")
            .csrf(csrfToken)
            .field("email", f -> f.placeholder("admin@example.com"))
            .field("token", f -> f.placeholder("Enter admin token"))
            .submit("Sign In");
        return submitted == null ? form : form.errors(submitted);
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
                    "Admin Login"),
                p(style()
                        .fontSize(TEXT_SM).color(TEXT_LIGHT)
                        .textAlign(center).marginBottom(SP_6),
                    "Enter your credentials to access the dashboard"),
                noticeMessage(),
                errorMessage(),
                loginForm()
            )
        );
    }

    private Element errorMessage() {
        return when(error != null, () -> div(style()
                .padding(SP_3).borderRadius(ROUNDED).marginBottom(SP_4)
                .backgroundColor(hex("#fee2e2")).color(hex("#991b1b"))
                .fontSize(TEXT_SM).textAlign(center),
            error));
    }

    /** A one-shot session flash ("You have been signed out."). */
    private Element noticeMessage() {
        return when(notice != null, () -> div(id("login-notice"), style()
                .padding(SP_3).borderRadius(ROUNDED).marginBottom(SP_4)
                .backgroundColor(hex("#dcfce7")).color(hex("#166534"))
                .fontSize(TEXT_SM).textAlign(center),
            notice));
    }
}
