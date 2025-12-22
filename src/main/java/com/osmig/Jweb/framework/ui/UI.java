package com.osmig.Jweb.framework.ui;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.events.Event;
import com.osmig.Jweb.framework.styles.CSSValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

    // ==================== Modal ====================

    /**
     * Creates a modal dialog builder.
     *
     * <p>Example:</p>
     * <pre>
     * Modal.create("confirm-modal")
     *     .title("Confirm Action")
     *     .body(p("Are you sure you want to proceed?"))
     *     .footer(
     *         secondaryButton("Cancel", e -> Modal.close("confirm-modal")),
     *         primaryButton("Confirm", e -> handleConfirm())
     *     )
     *     .build()
     * </pre>
     */
    public static class Modal {
        private final String id;
        private String title;
        private Element body;
        private Element[] footer;
        private boolean closable = true;
        private CSSValue width = rem(28);
        private boolean closeOnOverlay = true;

        private Modal(String id) {
            this.id = id;
        }

        public static Modal create(String id) {
            return new Modal(id);
        }

        public Modal title(String title) { this.title = title; return this; }
        public Modal body(Element body) { this.body = body; return this; }
        public Modal body(Object... content) { this.body = div(content); return this; }
        public Modal footer(Element... footer) { this.footer = footer; return this; }
        public Modal closable(boolean closable) { this.closable = closable; return this; }
        public Modal width(CSSValue width) { this.width = width; return this; }
        public Modal closeOnOverlay(boolean close) { this.closeOnOverlay = close; return this; }

        public Element build() {
            List<Element> headerElements = new ArrayList<>();
            if (title != null) {
                headerElements.add(h3(attrs().style()
                        .fontSize(rem(1.125))
                        .fontWeight(600)
                        .margin(zero),
                    text(title)));
            }
            if (closable) {
                headerElements.add(button(attrs().style()
                        .backgroundColor(() -> "transparent")
                        .prop("border", "none")
                        .fontSize(rem(1.5))
                        .clickable()
                        .color(hex("#6b7280"))
                    .data("modal-close", id),
                    text("\u00D7")));
            }

            List<Element> content = new ArrayList<>();

            // Header
            if (!headerElements.isEmpty()) {
                content.add(div(attrs().style()
                        .flex()
                        .justifyContent(() -> "space-between")
                        .alignItems(() -> "center")
                        .padding(rem(1), rem(1.5))
                        .prop("border-bottom", "1px solid #e5e7eb"),
                    headerElements.toArray()));
            }

            // Body
            if (body != null) {
                content.add(div(attrs().style().padding(rem(1.5)), body));
            }

            // Footer
            if (footer != null && footer.length > 0) {
                content.add(div(attrs().style()
                        .flex()
                        .justifyContent(() -> "flex-end")
                        .gap(rem(0.5))
                        .padding(rem(1), rem(1.5))
                        .prop("border-top", "1px solid #e5e7eb")
                        .backgroundColor(hex("#f9fafb")),
                    footer));
            }

            // Modal container
            Element modal = div(attrs().style()
                    .backgroundColor(white)
                    .rounded(px(8))
                    .boxShadow("0 25px 50px -12px rgba(0,0,0,0.25)")
                    .width(width)
                    .maxWidth(percent(90))
                    .maxHeight(() -> "90vh")
                    .overflow(() -> "auto")
                .data("modal-content", id),
                content.toArray());

            // Overlay
            return div(attrs().style()
                    .position(() -> "fixed")
                    .prop("inset", "0")
                    .backgroundColor(() -> "rgba(0,0,0,0.5)")
                    .flexCenter()
                    .prop("z-index", "1000")
                    .display(() -> "none")
                .id(id)
                .data("modal-overlay", closeOnOverlay ? "close" : ""),
                modal);
        }

        /**
         * Returns JavaScript to open a modal.
         */
        public static String openJs(String id) {
            return "document.getElementById('" + id + "').style.display='flex'";
        }

        /**
         * Returns JavaScript to close a modal.
         */
        public static String closeJs(String id) {
            return "document.getElementById('" + id + "').style.display='none'";
        }
    }

    /**
     * CSS and JavaScript required for modals.
     * Include this once in your page.
     */
    public static Element modalScript() {
        return inlineScript(
            "document.addEventListener('click',function(e){" +
            "if(e.target.dataset.modalClose){" +
            "document.getElementById(e.target.dataset.modalClose).style.display='none'}" +
            "if(e.target.dataset.modalOverlay==='close'){" +
            "e.target.style.display='none'}" +
            "if(e.target.dataset.modalOpen){" +
            "document.getElementById(e.target.dataset.modalOpen).style.display='flex'}" +
            "});"
        );
    }

    /**
     * Creates a button that opens a modal.
     */
    public static Element modalTrigger(String modalId, String buttonText) {
        return button(attrs().style()
                .backgroundColor(hex("#6366f1"))
                .color(white)
                .padding(rem(0.5), rem(1))
                .rounded(px(6))
                .prop("border", "none")
                .fontSize(rem(0.875))
                .fontWeight(500)
                .clickable()
            .data("modal-open", modalId),
            text(buttonText));
    }

    // ==================== Tabs ====================

    /**
     * Creates a tabbed interface builder.
     *
     * <p>Example:</p>
     * <pre>
     * Tabs.create("settings-tabs")
     *     .tab("general", "General", generalContent())
     *     .tab("security", "Security", securityContent())
     *     .tab("billing", "Billing", billingContent())
     *     .build()
     * </pre>
     */
    public static class Tabs {
        private final String id;
        private final List<Tab> tabs = new ArrayList<>();
        private String defaultTab;
        private TabStyle style = TabStyle.LINE;

        private Tabs(String id) {
            this.id = id;
        }

        public static Tabs create(String id) {
            return new Tabs(id);
        }

        public Tabs tab(String tabId, String tabLabel, Element content) {
            tabs.add(new Tab(tabId, tabLabel, content, null));
            if (defaultTab == null) defaultTab = tabId;
            return this;
        }

        public Tabs tab(String tabId, String tabLabel, Object... content) {
            tabs.add(new Tab(tabId, tabLabel, div(content), null));
            if (defaultTab == null) defaultTab = tabId;
            return this;
        }

        public Tabs tab(String tabId, String tabLabel, String icon, Element content) {
            tabs.add(new Tab(tabId, tabLabel, content, icon));
            if (defaultTab == null) defaultTab = tabId;
            return this;
        }

        public Tabs defaultTab(String tabId) { this.defaultTab = tabId; return this; }
        public Tabs style(TabStyle style) { this.style = style; return this; }

        public enum TabStyle { LINE, PILLS, BOXED }

        public Element build() {
            List<Element> tabButtons = new ArrayList<>();
            List<Element> tabPanels = new ArrayList<>();

            for (Tab tab : tabs) {
                boolean isDefault = tab.id.equals(defaultTab);

                // Tab button
                Element btn;
                if (style == TabStyle.PILLS) {
                    btn = button(attrs().style()
                            .backgroundColor(isDefault ? hex("#6366f1") : () -> "transparent")
                            .color(isDefault ? white : hex("#6b7280"))
                            .padding(rem(0.5), rem(1))
                            .rounded(px(6))
                            .prop("border", "none")
                            .fontSize(rem(0.875))
                            .fontWeight(500)
                            .clickable()
                            .transition(() -> "all 0.2s")
                        .data("tab", tab.id)
                        .data("tab-group", id),
                        tab.icon != null ? span(attrs().style().marginRight(rem(0.5)), text(tab.icon)) : null,
                        text(tab.label));
                } else if (style == TabStyle.BOXED) {
                    btn = button(attrs().style()
                            .backgroundColor(isDefault ? white : hex("#f3f4f6"))
                            .color(isDefault ? hex("#6366f1") : hex("#6b7280"))
                            .padding(rem(0.75), rem(1.25))
                            .prop("border", isDefault ? "1px solid #e5e7eb" : "1px solid transparent")
                            .prop("border-bottom", isDefault ? "1px solid white" : "1px solid #e5e7eb")
                            .prop("margin-bottom", "-1px")
                            .fontSize(rem(0.875))
                            .fontWeight(500)
                            .clickable()
                        .data("tab", tab.id)
                        .data("tab-group", id),
                        tab.icon != null ? span(attrs().style().marginRight(rem(0.5)), text(tab.icon)) : null,
                        text(tab.label));
                } else { // LINE (default)
                    btn = button(attrs().style()
                            .backgroundColor(() -> "transparent")
                            .color(isDefault ? hex("#6366f1") : hex("#6b7280"))
                            .padding(rem(0.75), rem(1))
                            .prop("border", "none")
                            .prop("border-bottom", isDefault ? "2px solid #6366f1" : "2px solid transparent")
                            .fontSize(rem(0.875))
                            .fontWeight(isDefault ? 600 : 500)
                            .clickable()
                            .transition(() -> "all 0.2s")
                        .data("tab", tab.id)
                        .data("tab-group", id),
                        tab.icon != null ? span(attrs().style().marginRight(rem(0.5)), text(tab.icon)) : null,
                        text(tab.label));
                }
                tabButtons.add(btn);

                // Tab panel
                tabPanels.add(div(attrs().style()
                        .display(isDefault ? () -> "block" : () -> "none")
                        .padding(rem(1))
                    .data("tab-panel", tab.id)
                    .data("tab-group", id),
                    tab.content));
            }

            // Tab list
            Element tabList = div(attrs().style()
                    .flex()
                    .gap(style == TabStyle.LINE ? zero : rem(0.5))
                    .prop("border-bottom", style == TabStyle.LINE ? "1px solid #e5e7eb" : "none"),
                tabButtons.toArray());

            return div(attrs().id(id),
                tabList,
                div(tabPanels.toArray()));
        }

        private record Tab(String id, String label, Element content, String icon) {}
    }

    /**
     * JavaScript required for tabs.
     * Include this once in your page.
     */
    public static Element tabsScript() {
        return inlineScript(
            "document.addEventListener('click',function(e){" +
            "var tab=e.target.dataset.tab,group=e.target.dataset.tabGroup;" +
            "if(!tab||!group)return;" +
            "document.querySelectorAll('[data-tab-group=\"'+group+'\"][data-tab]').forEach(function(btn){" +
            "var isActive=btn.dataset.tab===tab;" +
            "btn.style.borderBottomColor=isActive?'#6366f1':'transparent';" +
            "btn.style.color=isActive?'#6366f1':'#6b7280';" +
            "btn.style.fontWeight=isActive?'600':'500';" +
            "});" +
            "document.querySelectorAll('[data-tab-group=\"'+group+'\"][data-tab-panel]').forEach(function(p){" +
            "p.style.display=p.dataset.tabPanel===tab?'block':'none';" +
            "});" +
            "});"
        );
    }

    // ==================== Dropdown ====================

    /**
     * Creates a dropdown menu builder.
     *
     * <p>Example:</p>
     * <pre>
     * Dropdown.create("user-menu")
     *     .trigger(avatar("John Doe"))
     *     .item("Profile", e -> goToProfile())
     *     .item("Settings", e -> goToSettings())
     *     .divider()
     *     .item("Sign Out", e -> signOut())
     *     .build()
     * </pre>
     */
    public static class Dropdown {
        private final String id;
        private Element trigger;
        private final List<DropdownItem> items = new ArrayList<>();
        private Position position = Position.BOTTOM_LEFT;
        private CSSValue width = rem(12);

        private Dropdown(String id) {
            this.id = id;
        }

        public static Dropdown create(String id) {
            return new Dropdown(id);
        }

        public Dropdown trigger(Element element) { this.trigger = element; return this; }
        public Dropdown trigger(String buttonText) {
            this.trigger = button(attrs().style()
                    .backgroundColor(white)
                    .color(hex("#374151"))
                    .padding(rem(0.5), rem(1))
                    .rounded(px(6))
                    .prop("border", "1px solid #d1d5db")
                    .fontSize(rem(0.875))
                    .clickable()
                    .flex()
                    .alignItems(() -> "center")
                    .gap(rem(0.5)),
                text(buttonText),
                span(text("\u25BC"))); // down arrow
            return this;
        }

        public Dropdown item(String label, Consumer<Event> onClick) {
            items.add(new DropdownItem(label, onClick, null, false, false));
            return this;
        }

        public Dropdown item(String icon, String label, Consumer<Event> onClick) {
            items.add(new DropdownItem(label, onClick, icon, false, false));
            return this;
        }

        public Dropdown itemDisabled(String label) {
            items.add(new DropdownItem(label, null, null, true, false));
            return this;
        }

        public Dropdown divider() {
            items.add(new DropdownItem(null, null, null, false, true));
            return this;
        }

        public Dropdown position(Position pos) { this.position = pos; return this; }
        public Dropdown width(CSSValue width) { this.width = width; return this; }

        public enum Position { BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT }

        public Element build() {
            List<Element> menuItems = new ArrayList<>();
            for (DropdownItem item : items) {
                if (item.isDivider) {
                    menuItems.add(hr(attrs().style()
                        .prop("border", "none")
                        .prop("border-top", "1px solid #e5e7eb")
                        .margin(rem(0.25), zero)
                        .done()));
                } else {
                    Element menuItem = button(attrs().style()
                            .display(() -> "flex")
                            .width(percent(100))
                            .alignItems(() -> "center")
                            .gap(rem(0.5))
                            .backgroundColor(() -> "transparent")
                            .color(item.disabled ? hex("#9ca3af") : hex("#374151"))
                            .padding(rem(0.5), rem(0.75))
                            .prop("border", "none")
                            .textAlign(() -> "left")
                            .fontSize(rem(0.875))
                            .clickable()
                            .transition(() -> "background-color 0.15s")
                        .data("dropdown-item", id)
                        .onClick(item.onClick),
                        item.icon != null ? span(text(item.icon)) : null,
                        text(item.label));
                    menuItems.add(menuItem);
                }
            }

            // Calculate position styles
            String topBottom = position == Position.TOP_LEFT || position == Position.TOP_RIGHT
                ? "bottom:100%" : "top:100%";
            String leftRight = position == Position.BOTTOM_RIGHT || position == Position.TOP_RIGHT
                ? "right:0" : "left:0";

            Element menu = div(attrs().style()
                    .position(() -> "absolute")
                    .prop(topBottom.split(":")[0], topBottom.split(":")[1])
                    .prop(leftRight.split(":")[0], leftRight.split(":")[1])
                    .marginTop(rem(0.25))
                    .backgroundColor(white)
                    .rounded(px(6))
                    .boxShadow("0 10px 15px -3px rgba(0,0,0,0.1)")
                    .prop("border", "1px solid #e5e7eb")
                    .width(width)
                    .padding(rem(0.25))
                    .prop("z-index", "50")
                    .display(() -> "none")
                .data("dropdown-menu", id),
                menuItems.toArray());

            Element triggerWrapper = div(attrs()
                .data("dropdown-trigger", id),
                trigger);

            return div(attrs().style()
                    .position(() -> "relative")
                    .display(() -> "inline-block")
                .id(id),
                triggerWrapper,
                menu);
        }

        private record DropdownItem(String label, Consumer<Event> onClick, String icon,
                                   boolean disabled, boolean isDivider) {}
    }

    /**
     * JavaScript required for dropdowns.
     * Include this once in your page.
     */
    public static Element dropdownScript() {
        return inlineScript(
            "document.addEventListener('click',function(e){" +
            "var trigger=e.target.closest('[data-dropdown-trigger]');" +
            "if(trigger){" +
            "var id=trigger.dataset.dropdownTrigger;" +
            "var menu=document.querySelector('[data-dropdown-menu=\"'+id+'\"]');" +
            "menu.style.display=menu.style.display==='none'?'block':'none';" +
            "return}" +
            "if(e.target.dataset.dropdownItem){" +
            "document.querySelector('[data-dropdown-menu=\"'+e.target.dataset.dropdownItem+'\"]').style.display='none';" +
            "return}" +
            "document.querySelectorAll('[data-dropdown-menu]').forEach(function(m){m.style.display='none'});" +
            "});"
        );
    }

    // ==================== Accordion ====================

    /**
     * Creates an accordion (collapsible sections) builder.
     *
     * <p>Example:</p>
     * <pre>
     * Accordion.create("faq")
     *     .item("What is JWeb?", p("JWeb is a Java web framework..."))
     *     .item("How do I install it?", p("Add the dependency to your pom.xml..."))
     *     .item("Is it open source?", p("Yes, JWeb is MIT licensed."))
     *     .allowMultiple(false)
     *     .build()
     * </pre>
     */
    public static class Accordion {
        private final String id;
        private final List<AccordionItem> items = new ArrayList<>();
        private boolean allowMultiple = false;
        private String defaultOpen;

        private Accordion(String id) {
            this.id = id;
        }

        public static Accordion create(String id) {
            return new Accordion(id);
        }

        public Accordion item(String title, Element content) {
            String itemId = id + "-" + items.size();
            items.add(new AccordionItem(itemId, title, content));
            return this;
        }

        public Accordion item(String title, Object... content) {
            String itemId = id + "-" + items.size();
            items.add(new AccordionItem(itemId, title, div(content)));
            return this;
        }

        public Accordion allowMultiple(boolean allow) { this.allowMultiple = allow; return this; }
        public Accordion defaultOpen(int index) {
            this.defaultOpen = id + "-" + index;
            return this;
        }

        public Element build() {
            List<Element> sections = new ArrayList<>();

            for (AccordionItem item : items) {
                boolean isOpen = item.id.equals(defaultOpen);

                Element header = button(attrs().style()
                        .display(() -> "flex")
                        .width(percent(100))
                        .justifyContent(() -> "space-between")
                        .alignItems(() -> "center")
                        .backgroundColor(hex("#f9fafb"))
                        .color(hex("#374151"))
                        .padding(rem(1))
                        .prop("border", "none")
                        .prop("border-bottom", "1px solid #e5e7eb")
                        .fontSize(rem(0.9375))
                        .fontWeight(500)
                        .textAlign(() -> "left")
                        .clickable()
                    .data("accordion-trigger", item.id)
                    .data("accordion-group", id)
                    .data("accordion-multiple", String.valueOf(allowMultiple)),
                    text(item.title),
                    span(attrs().style()
                            .transition(() -> "transform 0.2s")
                            .prop("transform", isOpen ? "rotate(180deg)" : "rotate(0)")
                        .data("accordion-icon", item.id),
                        text("\u25BC")));

                Element content = div(attrs().style()
                        .display(isOpen ? () -> "block" : () -> "none")
                        .padding(rem(1))
                        .backgroundColor(white)
                        .prop("border-bottom", "1px solid #e5e7eb")
                    .data("accordion-content", item.id),
                    item.content);

                sections.add(div(header, content));
            }

            return div(attrs().style()
                    .prop("border", "1px solid #e5e7eb")
                    .rounded(px(8))
                    .overflow(() -> "hidden")
                .id(id),
                sections.toArray());
        }

        private record AccordionItem(String id, String title, Element content) {}
    }

    /**
     * JavaScript required for accordions.
     * Include this once in your page.
     */
    public static Element accordionScript() {
        return inlineScript(
            "document.addEventListener('click',function(e){" +
            "var trigger=e.target.closest('[data-accordion-trigger]');" +
            "if(!trigger)return;" +
            "var id=trigger.dataset.accordionTrigger;" +
            "var group=trigger.dataset.accordionGroup;" +
            "var multiple=trigger.dataset.accordionMultiple==='true';" +
            "var content=document.querySelector('[data-accordion-content=\"'+id+'\"]');" +
            "var icon=document.querySelector('[data-accordion-icon=\"'+id+'\"]');" +
            "var isOpen=content.style.display!=='none';" +
            "if(!multiple){" +
            "document.querySelectorAll('[data-accordion-content]').forEach(function(c){" +
            "if(c.dataset.accordionContent.startsWith(group+'-'))c.style.display='none'});" +
            "document.querySelectorAll('[data-accordion-icon]').forEach(function(i){" +
            "if(i.dataset.accordionIcon.startsWith(group+'-'))i.style.transform='rotate(0)'});}" +
            "content.style.display=isOpen?'none':'block';" +
            "icon.style.transform=isOpen?'rotate(0)':'rotate(180deg)';" +
            "});"
        );
    }

    // ==================== Navigation ====================

    /**
     * Creates a navigation bar builder.
     *
     * <p>Example:</p>
     * <pre>
     * Nav.create()
     *     .brand("JWeb", "/")
     *     .link("Home", "/")
     *     .link("Docs", "/docs")
     *     .link("Examples", "/examples")
     *     .right(
     *         secondaryButton("Sign In", e -> signIn()),
     *         primaryButton("Get Started", e -> getStarted())
     *     )
     *     .build()
     * </pre>
     */
    public static class Nav {
        private Element brand;
        private final List<NavLink> links = new ArrayList<>();
        private Element[] rightElements;
        private boolean sticky = false;
        private CSSValue height = px(64);

        private Nav() {}

        public static Nav create() {
            return new Nav();
        }

        public Nav brand(String name, String href) {
            this.brand = a(attrs().style()
                    .fontSize(rem(1.25))
                    .fontWeight(700)
                    .color(hex("#111827"))
                    .textDecoration(() -> "none")
                .href(href),
                text(name));
            return this;
        }

        public Nav brand(Element element) {
            this.brand = element;
            return this;
        }

        public Nav link(String label, String href) {
            links.add(new NavLink(label, href, false));
            return this;
        }

        public Nav link(String label, String href, boolean active) {
            links.add(new NavLink(label, href, active));
            return this;
        }

        public Nav right(Element... elements) {
            this.rightElements = elements;
            return this;
        }

        public Nav sticky() { this.sticky = true; return this; }
        public Nav height(CSSValue height) { this.height = height; return this; }

        public Element build() {
            // Links
            List<Element> navLinks = new ArrayList<>();
            for (NavLink link : links) {
                navLinks.add(a(attrs().style()
                        .color(link.active ? hex("#6366f1") : hex("#6b7280"))
                        .fontSize(rem(0.875))
                        .fontWeight(link.active ? 600 : 500)
                        .textDecoration(() -> "none")
                        .padding(rem(0.5), rem(0.75))
                        .transition(() -> "color 0.15s")
                    .href(link.href),
                    text(link.label)));
            }

            // Main nav content
            Element leftSection = div(attrs().style()
                    .flex()
                    .alignItems(() -> "center")
                    .gap(rem(2)),
                brand,
                navLinks.isEmpty() ? null : nav(attrs().style()
                        .flex()
                        .gap(rem(0.5)),
                    navLinks.toArray()));

            Element rightSection = rightElements != null && rightElements.length > 0
                ? div(attrs().style()
                        .flex()
                        .alignItems(() -> "center")
                        .gap(rem(0.75)),
                    rightElements)
                : null;

            return header(attrs().style()
                    .backgroundColor(white)
                    .prop("border-bottom", "1px solid #e5e7eb")
                    .height(height)
                    .position(sticky ? () -> "sticky" : () -> "relative")
                    .prop("top", sticky ? "0" : "")
                    .prop("z-index", sticky ? "100" : ""),
                div(attrs().style()
                        .maxWidth(px(1280))
                        .margin(zero, () -> "auto")
                        .padding(zero, rem(1.5))
                        .height(percent(100))
                        .flex()
                        .justifyContent(() -> "space-between")
                        .alignItems(() -> "center"),
                    leftSection,
                    rightSection));
        }

        private record NavLink(String label, String href, boolean active) {}
    }

    /**
     * Creates a sidebar navigation builder.
     *
     * <p>Example:</p>
     * <pre>
     * Sidebar.create()
     *     .header(h3(text("Admin Panel")))
     *     .section("Main",
     *         sidebarLink("Dashboard", "/admin", true),
     *         sidebarLink("Users", "/admin/users"),
     *         sidebarLink("Settings", "/admin/settings"))
     *     .section("Reports",
     *         sidebarLink("Analytics", "/admin/analytics"),
     *         sidebarLink("Logs", "/admin/logs"))
     *     .footer(p(text("v1.0.0")))
     *     .build()
     * </pre>
     */
    public static class Sidebar {
        private Element header;
        private final List<SidebarSection> sections = new ArrayList<>();
        private Element footer;
        private CSSValue width = rem(16);

        private Sidebar() {}

        public static Sidebar create() {
            return new Sidebar();
        }

        public Sidebar header(Element element) { this.header = element; return this; }
        public Sidebar footer(Element element) { this.footer = element; return this; }
        public Sidebar width(CSSValue width) { this.width = width; return this; }

        public Sidebar section(String title, Element... items) {
            sections.add(new SidebarSection(title, items));
            return this;
        }

        public Element build() {
            List<Element> content = new ArrayList<>();

            // Header
            if (header != null) {
                content.add(div(attrs().style()
                        .padding(rem(1.25))
                        .prop("border-bottom", "1px solid #e5e7eb"),
                    header));
            }

            // Sections
            for (SidebarSection section : sections) {
                List<Element> sectionContent = new ArrayList<>();
                sectionContent.add(div(attrs().style()
                        .fontSize(rem(0.75))
                        .fontWeight(600)
                        .color(hex("#6b7280"))
                        .textTransform(() -> "uppercase")
                        .letterSpacing(() -> "0.05em")
                        .padding(rem(0.75), rem(1.25)),
                    text(section.title)));

                for (Element item : section.items) {
                    sectionContent.add(item);
                }

                content.add(div(attrs().style().paddingBottom(rem(1)),
                    sectionContent.toArray()));
            }

            // Footer
            if (footer != null) {
                content.add(div(attrs().style()
                        .prop("margin-top", "auto")
                        .padding(rem(1))
                        .prop("border-top", "1px solid #e5e7eb")
                        .fontSize(rem(0.75))
                        .color(hex("#9ca3af")),
                    footer));
            }

            return aside(attrs().style()
                    .width(width)
                    .height(() -> "100vh")
                    .backgroundColor(white)
                    .prop("border-right", "1px solid #e5e7eb")
                    .flex()
                    .flexDirection(() -> "column")
                    .overflow(() -> "auto"),
                content.toArray());
        }

        private record SidebarSection(String title, Element[] items) {}
    }

    /**
     * Creates a sidebar link.
     */
    public static Element sidebarLink(String label, String href) {
        return sidebarLink(label, href, false);
    }

    /**
     * Creates a sidebar link with active state.
     */
    public static Element sidebarLink(String label, String href, boolean active) {
        return a(attrs().style()
                .display(() -> "block")
                .color(active ? hex("#6366f1") : hex("#374151"))
                .backgroundColor(active ? hex("#eef2ff") : () -> "transparent")
                .fontSize(rem(0.875))
                .fontWeight(active ? 500 : 400)
                .padding(rem(0.5), rem(1.25))
                .textDecoration(() -> "none")
                .transition(() -> "all 0.15s")
            .href(href),
            text(label));
    }

    /**
     * Creates a sidebar link with icon.
     */
    public static Element sidebarLink(String icon, String label, String href, boolean active) {
        return a(attrs().style()
                .display(() -> "flex")
                .alignItems(() -> "center")
                .gap(rem(0.75))
                .color(active ? hex("#6366f1") : hex("#374151"))
                .backgroundColor(active ? hex("#eef2ff") : () -> "transparent")
                .fontSize(rem(0.875))
                .fontWeight(active ? 500 : 400)
                .padding(rem(0.5), rem(1.25))
                .textDecoration(() -> "none")
                .transition(() -> "all 0.15s")
            .href(href),
            span(text(icon)),
            text(label));
    }

    // ==================== DataTable ====================

    /**
     * Creates a data table builder.
     *
     * <p>Example:</p>
     * <pre>
     * DataTable.create()
     *     .column("Name", user -> text(user.getName()))
     *     .column("Email", user -> text(user.getEmail()))
     *     .column("Status", user -> badge(user.getStatus(), Badge.SUCCESS))
     *     .column("Actions", user -> div(
     *         ghostButton("Edit", e -> edit(user)),
     *         dangerButton("Delete", e -> delete(user))))
     *     .data(users)
     *     .striped()
     *     .hoverable()
     *     .build()
     * </pre>
     */
    public static class DataTable<T> {
        private final List<Column<T>> columns = new ArrayList<>();
        private List<T> data = new ArrayList<>();
        private boolean striped = false;
        private boolean hoverable = false;
        private boolean bordered = false;
        private boolean compact = false;
        private String emptyMessage = "No data available";
        private Element emptyState;

        private DataTable() {}

        public static <T> DataTable<T> create() {
            return new DataTable<>();
        }

        public static <T> DataTable<T> create(Class<T> type) {
            return new DataTable<>();
        }

        public DataTable<T> column(String header, Function<T, Element> renderer) {
            columns.add(new Column<>(header, renderer, null, true));
            return this;
        }

        public DataTable<T> column(String header, Function<T, Element> renderer, CSSValue width) {
            columns.add(new Column<>(header, renderer, width, true));
            return this;
        }

        public DataTable<T> columnSortable(String header, Function<T, Element> renderer) {
            columns.add(new Column<>(header, renderer, null, true));
            return this;
        }

        public DataTable<T> data(List<T> data) { this.data = data; return this; }
        public DataTable<T> striped() { this.striped = true; return this; }
        public DataTable<T> hoverable() { this.hoverable = true; return this; }
        public DataTable<T> bordered() { this.bordered = true; return this; }
        public DataTable<T> compact() { this.compact = true; return this; }
        public DataTable<T> emptyMessage(String message) { this.emptyMessage = message; return this; }
        public DataTable<T> emptyState(Element element) { this.emptyState = element; return this; }

        public Element build() {
            // Header
            List<Element> headerCells = new ArrayList<>();
            for (Column<T> col : columns) {
                Element headerCell = th(attrs().style()
                        .textAlign(() -> "left")
                        .padding(compact ? rem(0.5) : rem(0.75), rem(1))
                        .fontSize(rem(0.75))
                        .fontWeight(600)
                        .color(hex("#6b7280"))
                        .textTransform(() -> "uppercase")
                        .letterSpacing(() -> "0.05em")
                        .backgroundColor(hex("#f9fafb"))
                        .prop("border-bottom", "1px solid #e5e7eb")
                        .width(col.width != null ? col.width : () -> "auto"),
                    text(col.header));
                headerCells.add(headerCell);
            }
            Element headerRow = tr(headerCells.toArray());

            // Body
            List<Element> bodyRows = new ArrayList<>();
            if (data.isEmpty()) {
                Element emptyCell = td(attrs()
                        .set("colspan", String.valueOf(columns.size()))
                        .style()
                        .textCenter()
                        .padding(rem(3))
                        .color(hex("#6b7280")),
                    emptyState != null ? emptyState : text(emptyMessage));
                bodyRows.add(tr(emptyCell));
            } else {
                int rowIndex = 0;
                for (T item : data) {
                    List<Element> cells = new ArrayList<>();
                    for (Column<T> col : columns) {
                        Element cell = td(attrs().style()
                                .padding(compact ? rem(0.5) : rem(0.75), rem(1))
                                .prop("border-bottom", "1px solid #e5e7eb")
                                .fontSize(rem(0.875)),
                            col.renderer.apply(item));
                        cells.add(cell);
                    }

                    final int idx = rowIndex;
                    Element row = tr(attrs().style()
                            .backgroundColor(striped && idx % 2 == 1 ? hex("#f9fafb") : white)
                            .transition(hoverable ? () -> "background-color 0.15s" : () -> "none"),
                        cells.toArray());
                    bodyRows.add(row);
                    rowIndex++;
                }
            }

            return div(attrs().style()
                    .overflow(() -> "auto")
                    .rounded(px(8))
                    .prop("border", bordered ? "1px solid #e5e7eb" : "none"),
                table(attrs().style()
                        .width(percent(100))
                        .prop("border-collapse", "collapse"),
                    thead(headerRow),
                    tbody(bodyRows.toArray())));
        }

        private record Column<T>(String header, Function<T, Element> renderer,
                                 CSSValue width, boolean sortable) {}
    }

    // ==================== Pagination ====================

    /**
     * Creates a pagination component.
     *
     * <p>Example:</p>
     * <pre>
     * pagination(1, 10, page -> loadPage(page))
     * </pre>
     */
    public static Element pagination(int currentPage, int totalPages, Consumer<Integer> onPageChange) {
        List<Element> items = new ArrayList<>();

        // Previous button
        items.add(button(attrs().style()
                .backgroundColor(currentPage == 1 ? hex("#f3f4f6") : white)
                .color(currentPage == 1 ? hex("#9ca3af") : hex("#374151"))
                .padding(rem(0.5), rem(0.75))
                .prop("border", "1px solid #d1d5db")
                .rounded(px(6))
                .fontSize(rem(0.875))
                .clickable()
            .disabled(currentPage == 1)
            .onClick(e -> onPageChange.accept(currentPage - 1)),
            text("\u2190 Prev")));

        // Page numbers
        int start = Math.max(1, currentPage - 2);
        int end = Math.min(totalPages, currentPage + 2);

        if (start > 1) {
            items.add(pageButton(1, currentPage, onPageChange));
            if (start > 2) {
                items.add(span(attrs().style()
                        .padding(rem(0.5))
                        .color(hex("#6b7280")),
                    text("...")));
            }
        }

        for (int i = start; i <= end; i++) {
            items.add(pageButton(i, currentPage, onPageChange));
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                items.add(span(attrs().style()
                        .padding(rem(0.5))
                        .color(hex("#6b7280")),
                    text("...")));
            }
            items.add(pageButton(totalPages, currentPage, onPageChange));
        }

        // Next button
        items.add(button(attrs().style()
                .backgroundColor(currentPage == totalPages ? hex("#f3f4f6") : white)
                .color(currentPage == totalPages ? hex("#9ca3af") : hex("#374151"))
                .padding(rem(0.5), rem(0.75))
                .prop("border", "1px solid #d1d5db")
                .rounded(px(6))
                .fontSize(rem(0.875))
                .clickable()
            .disabled(currentPage == totalPages)
            .onClick(e -> onPageChange.accept(currentPage + 1)),
            text("Next \u2192")));

        return nav(attrs().style()
                .flex()
                .alignItems(() -> "center")
                .gap(rem(0.25)),
            items.toArray());
    }

    private static Element pageButton(int page, int currentPage, Consumer<Integer> onPageChange) {
        boolean isCurrent = page == currentPage;
        return button(attrs().style()
                .backgroundColor(isCurrent ? hex("#6366f1") : white)
                .color(isCurrent ? white : hex("#374151"))
                .padding(rem(0.5), rem(0.75))
                .prop("border", isCurrent ? "1px solid #6366f1" : "1px solid #d1d5db")
                .rounded(px(6))
                .fontSize(rem(0.875))
                .fontWeight(isCurrent ? 600 : 400)
                .minWidth(rem(2.5))
                .clickable()
            .onClick(e -> onPageChange.accept(page)),
            text(String.valueOf(page)));
    }

    // ==================== Combined UI Scripts ====================

    /**
     * Returns all UI component scripts combined.
     * Include this once at the end of your body tag.
     */
    public static Element uiScripts() {
        return fragment(
            modalScript(),
            tabsScript(),
            dropdownScript(),
            accordionScript()
        );
    }
}
