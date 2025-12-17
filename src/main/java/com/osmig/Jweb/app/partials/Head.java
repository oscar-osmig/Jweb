package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.Keyframes.*;
import static com.osmig.Jweb.framework.styles.MediaQuery.*;
import static com.osmig.Jweb.app.Theme.*;

import com.osmig.Jweb.framework.styles.Style;

/**
 * HTML head partial with meta tags, animations, and responsive styles.
 */
public class Head implements Template {

    private final String pageTitle;

    public Head(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public static Head create(String pageTitle) {
        return new Head(pageTitle);
    }

    @Override
    public Element render() {
        return head(
            meta(attr("charset", "UTF-8")),
            meta(name("viewport"), attr("content", "width=device-width, initial-scale=1.0")),
            title(pageTitle),
            style(globalStyles() + keyframeAnimations() + mediaQueryStyles())
        );
    }

    private String globalStyles() {
        return """
            *, *::before, *::after {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            html {
                font-size: 16px;
                -webkit-font-smoothing: antialiased;
                scroll-behavior: smooth;
            }
            body {
                font-family: %s;
                line-height: 1.6;
                color: %s;
                background-color: %s;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }
            a {
                color: %s;
                text-decoration: none;
                transition: color 0.2s ease;
            }
            a:hover {
                color: %s;
            }
            img {
                max-width: 100%%;
                height: auto;
            }
            /* Card hover effects */
            .card {
                transition: transform 0.3s ease, box-shadow 0.3s ease;
            }
            .card:hover {
                transform: translateY(-4px);
                box-shadow: 0 8px 24px %s;
            }
            /* Button hover effects */
            .btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px %s;
            }
            /* Nav link hover */
            .navbar a:hover {
                background: rgba(255,255,255,0.15);
                text-decoration: none;
            }
            /* Focus styles for accessibility */
            :focus-visible {
                outline: 2px solid %s;
                outline-offset: 2px;
            }
            /* Form input focus */
            input:focus, textarea:focus {
                border-color: %s !important;
                box-shadow: 0 0 0 3px %s;
            }
            /* Animate elements on page load */
            .fade-in {
                animation: fadeIn 0.5s ease-out forwards;
            }
            .slide-up {
                animation: slideUp 0.6s ease-out forwards;
            }
            .scale-in {
                animation: scaleIn 0.4s ease-out forwards;
            }
            /* Staggered animation delays */
            .delay-1 { animation-delay: 0.1s; opacity: 0; }
            .delay-2 { animation-delay: 0.2s; opacity: 0; }
            .delay-3 { animation-delay: 0.3s; opacity: 0; }
            .delay-4 { animation-delay: 0.4s; opacity: 0; }
            /* Feature icon animation on hover */
            .feature-icon {
                transition: transform 0.3s ease;
            }
            .feature-icon:hover {
                transform: scale(1.1);
            }
            /* Gradient text */
            .gradient-text {
                background: linear-gradient(135deg, %s, %s);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
            }
            /* Animated gradient background */
            .gradient-bg {
                background: linear-gradient(-45deg, %s, %s, %s, %s);
                background-size: 400%% 400%%;
                animation: gradientShift 15s ease infinite;
            }
            """.formatted(
                FONT_FAMILY,
                TEXT.css(),
                BG.css(),
                PRIMARY.css(),
                PRIMARY_DARK.css(),
                SHADOW_HOVER.css(),
                SHADOW.css(),
                PRIMARY.css(),
                PRIMARY.css(),
                rgba(102, 126, 234, 0.2).css(),
                PRIMARY.css(),
                SECONDARY.css(),
                hex("#667eea").css(),
                hex("#764ba2").css(),
                hex("#f093fb").css(),
                hex("#f5576c").css()
            );
    }

    private String keyframeAnimations() {
        return """

            """ + fadeIn().build() + """

            """ + keyframes("slideUp")
                    .from(new Style().opacity(0).transform(translateY(px(20))))
                    .to(new Style().opacity(1).transform(translateY(zero)))
                    .build() + """

            """ + keyframes("scaleIn")
                    .from(new Style().opacity(0).transform(scale(0.95)))
                    .to(new Style().opacity(1).transform(scale(1)))
                    .build() + """

            """ + pulse().build() + """

            """ + keyframes("gradientShift")
                    .at(0, new Style().prop("background-position", "0% 50%"))
                    .at(50, new Style().prop("background-position", "100% 50%"))
                    .at(100, new Style().prop("background-position", "0% 50%"))
                    .build() + """

            """ + keyframes("float")
                    .at(0, new Style().transform(translateY(zero)))
                    .at(50, new Style().transform(translateY(px(-10))))
                    .at(100, new Style().transform(translateY(zero)))
                    .build() + """

            """;
    }

    private String mediaQueryStyles() {
        return """

            """ + mobile()
                    .rule(".container", new Style().padding(SPACE_SM, SPACE_MD))
                    .rule(".navbar", new Style().padding(SPACE_SM, SPACE_MD).gap(SPACE_SM))
                    .rule("h1", new Style().fontSize(FONT_2XL))
                    .rule("h2", new Style().fontSize(FONT_XL))
                    .build() + """

            """ + md()
                    .rule(".container", new Style().padding(SPACE_MD, SPACE_LG))
                    .build() + """

            """ + lg()
                    .rule(".container", new Style().padding(SPACE_MD, SPACE_XL))
                    .build() + """

            """ + media().prefersReducedMotion()
                    .rule("*, *::before, *::after", new Style()
                        .animationDuration(ms(0))
                        .transitionDuration(ms(0)))
                    .build() + """

            """ + media().prefersDark()
                    .rule("body", new Style().backgroundColor(BG_DARK).color(hex("#e2e8f0")))
                    .rule(".card", new Style().backgroundColor(hex("#374151")))
                    .rule(".footer", new Style().backgroundColor(hex("#1f2937")))
                    .rule("input, textarea", new Style()
                        .backgroundColor(hex("#374151"))
                        .borderColor(hex("#4b5563"))
                        .color(hex("#e2e8f0")))
                    .build() + """

            """;
    }
}
