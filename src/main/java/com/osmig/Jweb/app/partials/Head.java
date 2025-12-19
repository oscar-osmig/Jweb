package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.js.JWebRuntime;
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
        return styles(
            // Reset
            rule(all().or(all().before()).or(all().after()))
                .boxSizing(borderBox)
                .margin(zero)
                .padding(zero),
            rule(tag("html"))
                .fontSize(px(16))
                .webkitFontSmoothing(antialiased)
                .scrollBehavior(smooth),
            rule(tag("body"))
                .fontFamily(FONT_FAMILY)
                .lineHeight(1.6)
                .color(TEXT)
                .backgroundColor(BG)
                .minHeight(vh(100))
                .display(flex)
                .flexDirection(column),

            // Links
            rule(tag("a"))
                .color(PRIMARY)
                .textDecoration(none)
                .transition(propColor, s(0.2), ease),
            rule(tag("a").hover())
                .color(PRIMARY_DARK),

            // Images
            rule(tag("img"))
                .maxWidth(percent(100))
                .height(auto),

            // Card hover effects
            rule(cls("card"))
                .transition(transitions(
                    trans(propTransform, s(0.3), ease),
                    trans(propBoxShadow, s(0.3), ease)
                )),
            rule(cls("card").hover())
                .transform(translateY(px(-4)))
                .boxShadow(px(0), px(8), px(24), SHADOW_HOVER),

            // Button hover effects
            rule(cls("btn").hover())
                .transform(translateY(px(-2)))
                .boxShadow(px(0), px(4), px(12), SHADOW),

            // Nav link hover
            rule(cls("navbar").descendant(tag("a").hover()))
                .background(rgba(255, 255, 255, 0.15))
                .textDecoration(none),

            // Focus styles for accessibility
            rule(select().pseudo("focus-visible"))
                .outline(px(2), solid, PRIMARY)
                .outlineOffset(px(2)),

            // Form input focus
            rule(tag("input").focus().or(tag("textarea").focus()))
                .borderColor(PRIMARY)
                .boxShadow(px(0), px(0), px(0), px(3), rgba(102, 126, 234, 0.2)),

            // Animate elements on page load
            rule(cls("fade-in"))
                .animation(anim("fadeIn"), s(0.5), easeOut)
                .animationFillMode(forwards),
            rule(cls("slide-up"))
                .animation(anim("slideUp"), s(0.6), easeOut)
                .animationFillMode(forwards),
            rule(cls("scale-in"))
                .animation(anim("scaleIn"), s(0.4), easeOut)
                .animationFillMode(forwards),

            // Staggered animation delays
            rule(cls("delay-1")).animationDelay(s(0.1)).opacity(0),
            rule(cls("delay-2")).animationDelay(s(0.2)).opacity(0),
            rule(cls("delay-3")).animationDelay(s(0.3)).opacity(0),
            rule(cls("delay-4")).animationDelay(s(0.4)).opacity(0),

            // Feature icon animation on hover
            rule(cls("feature-icon"))
                .transition(propTransform, s(0.3), ease),
            rule(cls("feature-icon").hover())
                .transform(scale(1.1)),

            // Gradient text
            rule(cls("gradient-text"))
                .background(linearGradient("135deg", PRIMARY, SECONDARY))
                .webkitBackgroundClip(text)
                .webkitTextFillColor(transparent)
                .backgroundClip(text),

            // Animated gradient background
            rule(cls("gradient-bg"))
                .background(linearGradient("-45deg", hex("#667eea"), hex("#764ba2"), hex("#f093fb"), hex("#f5576c")))
                .backgroundSize(percent(400), percent(400))
                .animation(anim("gradientShift"), s(15), ease)
                .animationIterationCount(infinite)
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
