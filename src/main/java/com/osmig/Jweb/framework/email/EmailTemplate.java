package com.osmig.Jweb.framework.email;

import com.osmig.Jweb.framework.core.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.Styles.style;

/**
 * Email template builder using JWeb Element DSL.
 *
 * <p>Usage:</p>
 * <pre>
 * // Simple template
 * Email email = EmailTemplate.create()
 *     .to("user@example.com")
 *     .subject("Welcome!")
 *     .body(
 *         h1("Welcome to Our App!"),
 *         p("Hi " + user.getName() + ","),
 *         p("Thank you for signing up."),
 *         button("Get Started").attr("href", "https://app.example.com")
 *     )
 *     .build();
 *
 * // With prebuilt layout
 * Email email = EmailTemplate.create()
 *     .to("user@example.com")
 *     .subject("Your Order #" + order.getId())
 *     .layout(EmailTemplate.Layout.BASIC)
 *     .header(
 *         img().src("https://example.com/logo.png").attr("width", "150")
 *     )
 *     .body(
 *         h2("Order Confirmed"),
 *         p("Thank you for your order!"),
 *         orderTable(order)
 *     )
 *     .footer(
 *         p("Questions? Reply to this email."),
 *         p(small("© 2024 Your Company"))
 *     )
 *     .build();
 *
 * // Custom styling
 * Email email = EmailTemplate.create()
 *     .to("user@example.com")
 *     .subject("Reset Password")
 *     .primaryColor("#4F46E5")
 *     .body(
 *         h1("Reset Your Password"),
 *         p("Click the button below to reset your password:"),
 *         EmailTemplate.button("Reset Password", resetUrl)
 *     )
 *     .build();
 * </pre>
 */
public class EmailTemplate {

    // ==================== Quick Templates ====================

    /**
     * Creates a welcome email.
     */
    public static Email welcome(String to, String userName, String appName, String loginUrl) {
        return create()
            .to(to)
            .subject("Welcome to " + appName + "!")
            .body(
                h1("Welcome, " + userName + "!"),
                p("Thank you for joining " + appName + ". We're excited to have you on board."),
                p("Click the button below to get started:"),
                button("Get Started", loginUrl)
            )
            .build();
    }

    /**
     * Creates a password reset email.
     */
    public static Email passwordReset(String to, String resetUrl, int expiresInMinutes) {
        return create()
            .to(to)
            .subject("Reset Your Password")
            .body(
                h1("Password Reset Request"),
                p("We received a request to reset your password."),
                p("Click the button below to create a new password:"),
                button("Reset Password", resetUrl),
                p(small("This link expires in " + expiresInMinutes + " minutes.")),
                p(small("If you didn't request this, you can safely ignore this email."))
            )
            .build();
    }

    /**
     * Creates an email verification email.
     */
    public static Email verifyEmail(String to, String verifyUrl) {
        return create()
            .to(to)
            .subject("Verify Your Email")
            .body(
                h1("Verify Your Email Address"),
                p("Please verify your email address to complete your registration."),
                button("Verify Email", verifyUrl),
                p(small("If you didn't create an account, you can ignore this email."))
            )
            .build();
    }

    // ==================== Builder ====================

    /**
     * Creates a new email template builder.
     */
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private final Email.Builder emailBuilder = Email.create();
        private Element headerContent;
        private Element bodyContent;
        private Element footerContent;
        private Layout layout = Layout.BASIC;
        private String primaryColor = "#4F46E5";
        private String backgroundColor = "#f4f4f5";
        private String textColor = "#18181b";
        private String fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif";

        Builder() {}

        // ========== Email Properties ==========

        public Builder to(String... addresses) {
            emailBuilder.to(addresses);
            return this;
        }

        public Builder cc(String... addresses) {
            emailBuilder.cc(addresses);
            return this;
        }

        public Builder bcc(String... addresses) {
            emailBuilder.bcc(addresses);
            return this;
        }

        public Builder from(String address) {
            emailBuilder.from(address);
            return this;
        }

        public Builder replyTo(String address) {
            emailBuilder.replyTo(address);
            return this;
        }

        public Builder subject(String subject) {
            emailBuilder.subject(subject);
            return this;
        }

        // ========== Template Properties ==========

        public Builder layout(Layout layout) {
            this.layout = layout;
            return this;
        }

        public Builder primaryColor(String color) {
            this.primaryColor = color;
            return this;
        }

        public Builder backgroundColor(String color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder textColor(String color) {
            this.textColor = color;
            return this;
        }

        public Builder fontFamily(String font) {
            this.fontFamily = font;
            return this;
        }

        // ========== Content ==========

        public Builder header(Element... elements) {
            this.headerContent = div(elements);
            return this;
        }

        public Builder body(Element... elements) {
            this.bodyContent = div(elements);
            return this;
        }

        public Builder footer(Element... elements) {
            this.footerContent = div(elements);
            return this;
        }

        /**
         * Sets body content using a function that receives template vars.
         */
        public Builder body(Function<Map<String, Object>, Element> bodyFn, Map<String, Object> vars) {
            this.bodyContent = bodyFn.apply(vars);
            return this;
        }

        // ========== Build ==========

        public Email build() {
            String html = renderHtml();
            String text = renderPlainText();

            emailBuilder.html(html);
            if (text != null && !text.isEmpty()) {
                emailBuilder.text(text);
            }

            return emailBuilder.build();
        }

        private String renderHtml() {
            Element content = layout.render(this);
            return "<!DOCTYPE html>\n" + content.toHtml();
        }

        private String renderPlainText() {
            // Extract text from body for plain-text fallback
            if (bodyContent == null) return null;
            return stripHtml(bodyContent.toHtml());
        }

        private String stripHtml(String html) {
            return html
                .replaceAll("<br\\s*/?>", "\n")
                .replaceAll("<p[^>]*>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        }
    }

    // ==================== Layouts ====================

    public enum Layout {
        /**
         * No layout, just the body content.
         */
        NONE {
            @Override
            Element render(Builder builder) {
                return html(
                    head(
                        meta().attr("charset", "UTF-8"),
                        meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                        inlineStyles(builder)
                    ),
                    body(builder.bodyContent != null ? builder.bodyContent : div())
                );
            }
        },

        /**
         * Basic centered layout with header and footer.
         */
        BASIC {
            @Override
            Element render(Builder builder) {
                return html(
                    head(
                        meta().attr("charset", "UTF-8"),
                        meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                        inlineStyles(builder)
                    ),
                    body(
                        div().style(style()
                            .unsafeProp("max-width", "600px")
                            .unsafeProp("margin", "0 auto")
                            .unsafeProp("padding", "20px")
                            .unsafeProp("font-family", builder.fontFamily)
                            .unsafeProp("color", builder.textColor)
                        ).children(
                            // Header
                            builder.headerContent != null
                                ? div().style(style()
                                    .unsafeProp("text-align", "center")
                                    .unsafeProp("padding", "20px 0")
                                    .unsafeProp("border-bottom", "1px solid #e5e5e5")
                                ).children(builder.headerContent)
                                : null,

                            // Body
                            div().style(style()
                                .unsafeProp("padding", "30px 0")
                            ).children(
                                builder.bodyContent != null ? builder.bodyContent : div()
                            ),

                            // Footer
                            builder.footerContent != null
                                ? div().style(style()
                                    .unsafeProp("text-align", "center")
                                    .unsafeProp("padding", "20px 0")
                                    .unsafeProp("border-top", "1px solid #e5e5e5")
                                    .unsafeProp("font-size", "12px")
                                    .unsafeProp("color", "#71717a")
                                ).children(builder.footerContent)
                                : null
                        )
                    ).style(style()
                        .unsafeProp("background-color", builder.backgroundColor)
                        .unsafeProp("padding", "40px 20px")
                    )
                );
            }
        },

        /**
         * Card-style layout with shadow.
         */
        CARD {
            @Override
            Element render(Builder builder) {
                return html(
                    head(
                        meta().attr("charset", "UTF-8"),
                        meta().attr("name", "viewport").attr("content", "width=device-width, initial-scale=1.0"),
                        inlineStyles(builder)
                    ),
                    body(
                        div().style(style()
                            .unsafeProp("max-width", "600px")
                            .unsafeProp("margin", "40px auto")
                            .unsafeProp("background", "white")
                            .unsafeProp("border-radius", "8px")
                            .unsafeProp("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                            .unsafeProp("overflow", "hidden")
                            .unsafeProp("font-family", builder.fontFamily)
                            .unsafeProp("color", builder.textColor)
                        ).children(
                            // Header with brand color
                            builder.headerContent != null
                                ? div().style(style()
                                    .unsafeProp("background", builder.primaryColor)
                                    .unsafeProp("color", "white")
                                    .unsafeProp("padding", "30px")
                                    .unsafeProp("text-align", "center")
                                ).children(builder.headerContent)
                                : div().style(style()
                                    .unsafeProp("height", "8px")
                                    .unsafeProp("background", builder.primaryColor)
                                ),

                            // Body
                            div().style(style()
                                .unsafeProp("padding", "30px")
                            ).children(
                                builder.bodyContent != null ? builder.bodyContent : div()
                            ),

                            // Footer
                            builder.footerContent != null
                                ? div().style(style()
                                    .unsafeProp("background", "#f9fafb")
                                    .unsafeProp("padding", "20px 30px")
                                    .unsafeProp("font-size", "12px")
                                    .unsafeProp("color", "#71717a")
                                    .unsafeProp("text-align", "center")
                                ).children(builder.footerContent)
                                : null
                        )
                    ).style(style()
                        .unsafeProp("background-color", builder.backgroundColor)
                        .unsafeProp("padding", "0")
                        .unsafeProp("margin", "0")
                    )
                );
            }
        };

        abstract Element render(Builder builder);

        Element inlineStyles(Builder builder) {
            return com.osmig.Jweb.framework.elements.Elements.style(String.format("""
                body { margin: 0; padding: 0; }
                h1 { margin: 0 0 16px; font-size: 24px; }
                h2 { margin: 0 0 12px; font-size: 20px; }
                p { margin: 0 0 16px; line-height: 1.6; }
                a { color: %s; }
                img { max-width: 100%%; height: auto; }
                """, builder.primaryColor));
        }
    }

    // ==================== Helper Elements ====================

    /**
     * Creates a styled email button.
     */
    public static Element button(String text, String href) {
        return button(text, href, "#4F46E5");
    }

    /**
     * Creates a styled email button with custom color.
     */
    public static Element button(String text, String href, String color) {
        return a(text)
            .attr("href", href)
            .style(style()
                .unsafeProp("display", "inline-block")
                .unsafeProp("padding", "14px 28px")
                .unsafeProp("background-color", color)
                .unsafeProp("color", "white")
                .unsafeProp("text-decoration", "none")
                .unsafeProp("border-radius", "6px")
                .unsafeProp("font-weight", "600")
                .unsafeProp("font-size", "16px")
                .unsafeProp("margin", "16px 0")
            );
    }

    /**
     * Creates a styled secondary button.
     */
    public static Element secondaryButton(String text, String href) {
        return a(text)
            .attr("href", href)
            .style(style()
                .unsafeProp("display", "inline-block")
                .unsafeProp("padding", "12px 24px")
                .unsafeProp("background-color", "transparent")
                .unsafeProp("color", "#4F46E5")
                .unsafeProp("text-decoration", "none")
                .unsafeProp("border", "2px solid #4F46E5")
                .unsafeProp("border-radius", "6px")
                .unsafeProp("font-weight", "600")
                .unsafeProp("margin", "16px 0")
            );
    }

    /**
     * Creates a divider line.
     */
    public static Element divider() {
        return hr().style(style()
            .unsafeProp("border", "none")
            .unsafeProp("border-top", "1px solid #e5e5e5")
            .unsafeProp("margin", "24px 0")
        );
    }

    /**
     * Creates a callout/alert box.
     */
    public static Element callout(Element... content) {
        return callout("#fef3c7", "#92400e", content);
    }

    /**
     * Creates a callout with custom colors.
     */
    public static Element callout(String bgColor, String textColor, Element... content) {
        return div(content).style(style()
            .unsafeProp("background-color", bgColor)
            .unsafeProp("color", textColor)
            .unsafeProp("padding", "16px")
            .unsafeProp("border-radius", "6px")
            .unsafeProp("margin", "16px 0")
        );
    }

    /**
     * Creates a simple table for data display.
     */
    public static Element dataTable(Map<String, String> data) {
        var rows = data.entrySet().stream()
            .map(e -> tr(
                td(strong(e.getKey())).style(style().unsafeProp("padding", "8px 16px 8px 0").unsafeProp("text-align", "left")),
                td(e.getValue()).style(style().unsafeProp("padding", "8px 0"))
            ))
            .toArray(Element[]::new);

        return table(rows).style(style()
            .unsafeProp("width", "100%")
            .unsafeProp("border-collapse", "collapse")
            .unsafeProp("margin", "16px 0")
        );
    }
}
