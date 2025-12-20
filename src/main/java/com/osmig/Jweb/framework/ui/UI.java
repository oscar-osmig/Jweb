package com.osmig.Jweb.framework.ui;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.styles.CSSValue;

import java.util.function.Consumer;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;

/**
 * Pre-styled reusable UI components.
 *
 * <p>Example usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.ui.UI.*;
 *
 * // Buttons
 * primaryButton("Submit", e -> handleSubmit())
 * secondaryButton("Cancel", e -> cancel())
 * dangerButton("Delete", e -> confirmDelete())
 *
 * // Badges and tags
 * badge("New", Badge.SUCCESS)
 * tag("JavaScript")
 *
 * // Alerts
 * alert("Operation successful!", Alert.SUCCESS)
 * alert("Something went wrong", Alert.ERROR)
 *
 * // Cards
 * card("Card Title", p("Card content here..."))
 *
 * // Avatars
 * avatar("John Doe")
 * avatarImage("/img/user.jpg", "John Doe")
 * </pre>
 */
public final class UI {

    private UI() {}

    // ==================== Buttons ====================

    /**
     * Creates a primary button (filled, accent color).
     */
    public static Element primaryButton(String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(hex("#6366f1"))
                .color(white)
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "none")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
                .transition(() -> "background-color 0.2s")
            .onClick(onClick),
            text(btnText));
    }

    /**
     * Creates a secondary button (outlined).
     */
    public static Element secondaryButton(String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(() -> "transparent")
                .color(hex("#6366f1"))
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "1px solid #6366f1")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
                .transition(() -> "all 0.2s")
            .onClick(onClick),
            text(btnText));
    }

    /**
     * Creates a danger button (red).
     */
    public static Element dangerButton(String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(hex("#ef4444"))
                .color(white)
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "none")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
                .transition(() -> "background-color 0.2s")
            .onClick(onClick),
            text(btnText));
    }

    /**
     * Creates a ghost button (minimal styling).
     */
    public static Element ghostButton(String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(() -> "transparent")
                .color(hex("#374151"))
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "none")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
                .transition(() -> "background-color 0.2s")
            .onClick(onClick),
            text(btnText));
    }

    /**
     * Creates a link-styled button.
     */
    public static Element linkButton(String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(() -> "transparent")
                .color(hex("#6366f1"))
                .padding(zero)
                .prop("border", "none")
                .fontSize(rem(0.875))
                .clickable()
                .textDecoration(() -> "underline")
            .onClick(onClick),
            text(btnText));
    }

    /**
     * Creates a button with icon and text.
     */
    public static Element iconButton(String icon, String btnText, Consumer<Event> onClick) {
        return button(attrs().style()
                .backgroundColor(hex("#6366f1"))
                .color(white)
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "none")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
                .flex()
                .alignItems(() -> "center")
                .gap(rem(0.5))
            .onClick(onClick),
            span(text(icon)),
            span(text(btnText)));
    }

    // ==================== Badge ====================

    public enum Badge {
        DEFAULT("#6b7280", "#f3f4f6"),
        PRIMARY("#4f46e5", "#eef2ff"),
        SUCCESS("#16a34a", "#dcfce7"),
        WARNING("#ca8a04", "#fef9c3"),
        ERROR("#dc2626", "#fee2e2"),
        INFO("#0891b2", "#cffafe");

        final String textColor;
        final String bgColor;

        Badge(String textColor, String bgColor) {
            this.textColor = textColor;
            this.bgColor = bgColor;
        }
    }

    /**
     * Creates a badge with variant.
     */
    public static Element badge(String text, Badge variant) {
        return span(attrs().style()
                .backgroundColor(hex(variant.bgColor))
                .color(hex(variant.textColor))
                .padding(rem(0.125), rem(0.5))
                .rounded(px(9999))
                .fontSize(rem(0.75))
                .fontWeight(500),
            text(text));
    }

    /**
     * Creates a default badge.
     */
    public static Element badge(String text) {
        return badge(text, Badge.DEFAULT);
    }

    // ==================== Tag ====================

    /**
     * Creates a removable tag.
     */
    public static Element tag(String tagText, Consumer<Event> onRemove) {
        return span(attrs().style()
                .backgroundColor(hex("#e5e7eb"))
                .color(hex("#374151"))
                .padding(rem(0.25), rem(0.75))
                .rounded(px(4))
                .fontSize(rem(0.875))
                .flex()
                .alignItems(() -> "center")
                .gap(rem(0.5)),
            text(tagText),
            onRemove != null ? span(attrs().style()
                    .clickable()
                    .prop("opacity", "0.6")
                .onClick(onRemove),
                text("\u00D7")) : null);
    }

    /**
     * Creates a simple tag (not removable).
     */
    public static Element tag(String tagText) {
        return tag(tagText, null);
    }

    // ==================== Alert ====================

    public enum Alert {
        INFO("#0891b2", "#ecfeff", "#06b6d4"),
        SUCCESS("#16a34a", "#f0fdf4", "#22c55e"),
        WARNING("#ca8a04", "#fffbeb", "#eab308"),
        ERROR("#dc2626", "#fef2f2", "#ef4444");

        final String textColor;
        final String bgColor;
        final String borderColor;

        Alert(String textColor, String bgColor, String borderColor) {
            this.textColor = textColor;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
        }
    }

    /**
     * Creates an alert message.
     */
    public static Element alert(String message, Alert variant) {
        return div(attrs().style()
                .backgroundColor(hex(variant.bgColor))
                .color(hex(variant.textColor))
                .prop("border-left", "4px solid " + variant.borderColor)
                .padding(rem(1))
                .rounded(px(4))
                .fontSize(rem(0.875)),
            text(message));
    }

    /**
     * Creates an alert with title and message.
     */
    public static Element alert(String alertTitle, String message, Alert variant) {
        return div(attrs().style()
                .backgroundColor(hex(variant.bgColor))
                .color(hex(variant.textColor))
                .prop("border-left", "4px solid " + variant.borderColor)
                .padding(rem(1))
                .rounded(px(4)),
            strong(attrs().style().fontSize(rem(0.875)).display(() -> "block").marginBottom(rem(0.25)),
                text(alertTitle)),
            span(attrs().style().fontSize(rem(0.875)),
                text(message)));
    }

    /**
     * Creates an info alert.
     */
    public static Element infoAlert(String message) {
        return alert(message, Alert.INFO);
    }

    /**
     * Creates a success alert.
     */
    public static Element successAlert(String message) {
        return alert(message, Alert.SUCCESS);
    }

    /**
     * Creates a warning alert.
     */
    public static Element warningAlert(String message) {
        return alert(message, Alert.WARNING);
    }

    /**
     * Creates an error alert.
     */
    public static Element errorAlert(String message) {
        return alert(message, Alert.ERROR);
    }

    // ==================== Card ====================

    /**
     * Creates a card with title and content.
     */
    public static Element card(String cardTitle, Object... content) {
        return div(attrs().style()
                .backgroundColor(white)
                .rounded(px(8))
                .boxShadow("0 1px 3px rgba(0,0,0,0.1)")
                .overflow(() -> "hidden"),
            div(attrs().style()
                    .padding(rem(1.5))
                    .prop("border-bottom", "1px solid #e5e7eb"),
                h3(attrs().style()
                        .fontSize(rem(1.125))
                        .fontWeight(600)
                        .margin(zero),
                    text(cardTitle))),
            div(attrs().style().padding(rem(1.5)),
                content));
    }

    /**
     * Creates a simple card (no title).
     */
    public static Element card(Object... content) {
        return div(attrs().style()
                .backgroundColor(white)
                .rounded(px(8))
                .boxShadow("0 1px 3px rgba(0,0,0,0.1)")
                .padding(rem(1.5)),
            content);
    }

    /**
     * Creates a card with header, body, and footer.
     */
    public static Element card(Element header, Element body, Element footer) {
        return div(attrs().style()
                .backgroundColor(white)
                .rounded(px(8))
                .boxShadow("0 1px 3px rgba(0,0,0,0.1)")
                .overflow(() -> "hidden"),
            div(attrs().style()
                    .padding(rem(1))
                    .prop("border-bottom", "1px solid #e5e7eb"),
                header),
            div(attrs().style().padding(rem(1.5)),
                body),
            div(attrs().style()
                    .padding(rem(1))
                    .prop("border-top", "1px solid #e5e7eb")
                    .backgroundColor(hex("#f9fafb")),
                footer));
    }

    // ==================== Avatar ====================

    /**
     * Creates an avatar with initials.
     */
    public static Element avatar(String name) {
        return avatar(name, px(40));
    }

    /**
     * Creates an avatar with initials and custom size.
     */
    public static Element avatar(String name, CSSValue size) {
        String initials = getInitials(name);
        return div(attrs().style()
                .width(size)
                .height(size)
                .rounded(px(9999))
                .backgroundColor(hex("#6366f1"))
                .color(white)
                .flexCenter()
                .fontSize(rem(0.875))
                .fontWeight(600)
            .title(name),
            text(initials));
    }

    /**
     * Creates an avatar with image.
     */
    public static Element avatarImage(String src, String alt) {
        return avatarImage(src, alt, px(40));
    }

    /**
     * Creates an avatar with image and custom size.
     */
    public static Element avatarImage(String src, String alt, CSSValue size) {
        return img(attrs().style()
                .width(size)
                .height(size)
                .rounded(px(9999))
                .prop("object-fit", "cover")
            .src(src).alt(alt));
    }

    private static String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ==================== Progress ====================

    /**
     * Creates a progress bar.
     */
    public static Element progressBar(int percent) {
        return progressBar(percent, hex("#6366f1"));
    }

    /**
     * Creates a progress bar with custom color.
     */
    public static Element progressBar(int progressPercent, CSSValue color) {
        int clamped = Math.max(0, Math.min(100, progressPercent));
        return div(attrs().style()
                .backgroundColor(hex("#e5e7eb"))
                .rounded(px(9999))
                .height(px(8))
                .overflow(() -> "hidden"),
            div(attrs().style()
                .backgroundColor(color)
                .height(percent(100))
                .width(percent(clamped))
                .transition(() -> "width 0.3s ease")));
    }

    // ==================== Spinner ====================

    /**
     * Creates a loading spinner.
     */
    public static Element spinner() {
        return spinner(px(24));
    }

    /**
     * Creates a loading spinner with custom size.
     */
    public static Element spinner(CSSValue size) {
        return div(attrs().style()
                .width(size)
                .height(size)
                .prop("border", "2px solid #e5e7eb")
                .prop("border-top", "2px solid #6366f1")
                .rounded(px(9999))
                .prop("animation", "spin 1s linear infinite"));
    }

    // ==================== Tooltip (wrapper) ====================

    /**
     * Creates a tooltip wrapper.
     * Note: Requires CSS for :hover to show tooltip.
     */
    public static Element tooltip(String text, Object... content) {
        return span(attrs().style()
                .position(() -> "relative")
                .display(() -> "inline-block")
            .title(text),
            content);
    }

    // ==================== Divider ====================

    /**
     * Creates a horizontal divider with optional text.
     */
    public static Element divider(String dividerText) {
        return div(attrs().style()
                .flex()
                .alignItems(() -> "center")
                .gap(rem(1)),
            div(attrs().style().flexGrow(1).height(px(1)).backgroundColor(hex("#e5e7eb"))),
            span(attrs().style()
                    .color(hex("#6b7280"))
                    .fontSize(rem(0.875)),
                text(dividerText)),
            div(attrs().style().flexGrow(1).height(px(1)).backgroundColor(hex("#e5e7eb"))));
    }

    /**
     * Creates a simple horizontal divider.
     */
    public static Element divider() {
        return hr(attrs().style()
                .prop("border", "none")
                .prop("border-top", "1px solid #e5e7eb")
                .prop("margin", rem(1).css() + " " + zero.css())
            .done());
    }

    // ==================== Skeleton ====================

    /**
     * Creates a skeleton loading placeholder.
     */
    public static Element skeleton(CSSValue skWidth, CSSValue skHeight) {
        return div(attrs().style()
                .width(skWidth)
                .height(skHeight)
                .backgroundColor(hex("#e5e7eb"))
                .rounded(px(4))
                .prop("animation", "pulse 2s ease-in-out infinite"));
    }

    /**
     * Creates a text skeleton line.
     */
    public static Element skeletonText() {
        return skeleton(percent(100), rem(1));
    }

    /**
     * Creates a circular skeleton (for avatars).
     */
    public static Element skeletonCircle(CSSValue size) {
        return div(attrs().style()
                .width(size)
                .height(size)
                .backgroundColor(hex("#e5e7eb"))
                .rounded(px(9999))
                .prop("animation", "pulse 2s ease-in-out infinite"));
    }

    // ==================== Empty State ====================

    /**
     * Creates an empty state placeholder.
     */
    public static Element emptyState(String emptyTitle, String description) {
        return div(attrs().style()
                .textCenter()
                .padding(rem(3)),
            div(attrs().style()
                    .fontSize(rem(3))
                    .marginBottom(rem(1)),
                text("\uD83D\uDCED")), // mailbox emoji
            h3(attrs().style()
                    .fontSize(rem(1.125))
                    .fontWeight(600)
                    .color(hex("#374151"))
                    .margin(zero)
                    .marginBottom(rem(0.5)),
                text(emptyTitle)),
            p(attrs().style()
                    .color(hex("#6b7280"))
                    .fontSize(rem(0.875))
                    .margin(zero),
                text(description)));
    }

    /**
     * Creates an empty state with action button.
     */
    public static Element emptyState(String emptyTitle, String description, String buttonText, Consumer<Event> onClick) {
        return div(attrs().style()
                .textCenter()
                .padding(rem(3)),
            div(attrs().style()
                    .fontSize(rem(3))
                    .marginBottom(rem(1)),
                text("\uD83D\uDCED")),
            h3(attrs().style()
                    .fontSize(rem(1.125))
                    .fontWeight(600)
                    .color(hex("#374151"))
                    .margin(zero)
                    .marginBottom(rem(0.5)),
                text(emptyTitle)),
            p(attrs().style()
                    .color(hex("#6b7280"))
                    .fontSize(rem(0.875))
                    .margin(zero)
                    .marginBottom(rem(1.5)),
                text(description)),
            primaryButton(buttonText, onClick));
    }

    // ==================== Breadcrumb ====================

    /**
     * Creates a breadcrumb navigation.
     */
    public static Element breadcrumb(String... items) {
        Element[] elements = new Element[items.length * 2 - 1];
        for (int i = 0; i < items.length; i++) {
            boolean isLast = i == items.length - 1;
            elements[i * 2] = span(attrs().style()
                    .color(isLast ? hex("#374151") : hex("#6b7280"))
                    .fontSize(rem(0.875))
                    .fontWeight(isLast ? 500 : 400),
                text(items[i]));
            if (!isLast) {
                elements[i * 2 + 1] = span(attrs().style()
                        .color(hex("#9ca3af"))
                        .margin(zero, rem(0.5)),
                    text("/"));
            }
        }
        return nav(attrs().style()
                .flex()
                .alignItems(() -> "center"),
            elements);
    }

    // ==================== Kbd (Keyboard) ====================

    /**
     * Creates a keyboard key display.
     */
    public static Element kbd(String key) {
        return span(attrs().style()
                .backgroundColor(hex("#f3f4f6"))
                .prop("border", "1px solid #d1d5db")
                .prop("border-bottom", "2px solid #d1d5db")
                .rounded(px(4))
                .padding(rem(0.125), rem(0.375))
                .fontSize(rem(0.75))
                .fontFamily("monospace"),
            text(key));
    }

    // ==================== Code ====================

    /**
     * Creates an inline code element.
     */
    public static Element inlineCode(String codeStr) {
        return span(attrs().style()
                .backgroundColor(hex("#f3f4f6"))
                .color(hex("#e11d48"))
                .padding(rem(0.125), rem(0.375))
                .rounded(px(4))
                .fontSize(rem(0.875))
                .fontFamily("monospace"),
            text(codeStr));
    }

    /**
     * Creates a code block.
     */
    public static Element codeBlock(String codeStr) {
        return pre(attrs().style()
                .backgroundColor(hex("#1f2937"))
                .color(hex("#f9fafb"))
                .padding(rem(1))
                .rounded(px(8))
                .overflow(() -> "auto")
                .fontSize(rem(0.875))
                .fontFamily("monospace")
                .lineHeight(1.5),
            code(text(codeStr)));
    }
}
