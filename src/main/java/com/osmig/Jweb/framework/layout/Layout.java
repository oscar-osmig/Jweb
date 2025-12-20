package com.osmig.Jweb.framework.layout;

import com.osmig.Jweb.framework.attributes.Attributes;
import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.Tag;
import com.osmig.Jweb.framework.styles.CSSValue;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;

/**
 * Pre-built layout patterns for common UI structures.
 *
 * <p>Example - Page with header, main, footer:</p>
 * <pre>
 * Layout.page(
 *     Layout.header(new Nav()),
 *     Layout.main(
 *         Layout.container(
 *             h1("Welcome"),
 *             p("Content here...")
 *         )
 *     ),
 *     Layout.footer(text("© 2024"))
 * )
 * </pre>
 *
 * <p>Example - Two column layout:</p>
 * <pre>
 * Layout.columns(2, rem(2),
 *     div(p("Left column")),
 *     div(p("Right column"))
 * )
 * </pre>
 *
 * <p>Example - Sidebar layout:</p>
 * <pre>
 * Layout.sidebar(
 *     px(250),  // sidebar width
 *     div(text("Sidebar content")),
 *     div(text("Main content"))
 * )
 * </pre>
 *
 * <p>Example - Card grid:</p>
 * <pre>
 * Layout.grid(3, rem(1.5),
 *     card("Card 1", "Content..."),
 *     card("Card 2", "Content..."),
 *     card("Card 3", "Content...")
 * )
 * </pre>
 */
public final class Layout {

    private Layout() {}

    // ==================== Page Structure ====================

    /**
     * Creates a full-height page layout with flexbox column.
     * Children will stack vertically, with the last child (usually main) growing to fill space.
     */
    public static Element page(Object... children) {
        return div(attrs().style()
                .minHeight(vh(100))
                .flexCol(),
            children);
    }

    /**
     * Creates a header section with horizontal flex layout.
     */
    public static Element header(Object... children) {
        return header(attrs().style()
                .flexBetween()
                .padding(rem(1), rem(2)),
            children);
    }

    /**
     * Creates a main content section that grows to fill available space.
     */
    public static Element main(Object... children) {
        return main(attrs().style()
                .flexGrow(1)
                .padding(rem(2)),
            children);
    }

    /**
     * Creates a footer section.
     */
    public static Element footer(Object... children) {
        return footer(attrs().style()
                .padding(rem(1), rem(2))
                .textCenter(),
            children);
    }

    // ==================== Container ====================

    /**
     * Creates a centered container with max-width and horizontal padding.
     * Default max-width: 1200px
     */
    public static Element container(Object... children) {
        return container(px(1200), children);
    }

    /**
     * Creates a centered container with custom max-width.
     */
    public static Element container(CSSValue maxWidth, Object... children) {
        return div(attrs().style()
                .maxWidth(maxWidth)
                .centerX()
                .padding(zero, rem(1))
                .fullWidth(),
            children);
    }

    /**
     * Creates a narrow container (max-width: 800px).
     * Good for blog posts and readable content.
     */
    public static Element narrow(Object... children) {
        return container(px(800), children);
    }

    /**
     * Creates a wide container (max-width: 1400px).
     */
    public static Element wide(Object... children) {
        return container(px(1400), children);
    }

    // ==================== Flexbox Layouts ====================

    /**
     * Creates a horizontal flex container (row).
     */
    public static Element row(Object... children) {
        return div(attrs().style().flexRow(), children);
    }

    /**
     * Creates a horizontal flex container with gap.
     */
    public static Element row(CSSValue gap, Object... children) {
        return div(attrs().style().flexRow().gap(gap), children);
    }

    /**
     * Creates a vertical flex container (column).
     */
    public static Element column(Object... children) {
        return div(attrs().style().flexCol(), children);
    }

    /**
     * Creates a vertical flex container with gap.
     */
    public static Element column(CSSValue gap, Object... children) {
        return div(attrs().style().flexCol().gap(gap), children);
    }

    /**
     * Creates a flex container with centered content.
     */
    public static Element center(Object... children) {
        return div(attrs().style().flexCenter(), children);
    }

    /**
     * Creates a flex container with space-between alignment.
     */
    public static Element spaceBetween(Object... children) {
        return div(attrs().style().flexBetween(), children);
    }

    /**
     * Creates a flex container with wrapping items.
     */
    public static Element wrap(CSSValue gap, Object... children) {
        return div(attrs().style()
                .flex()
                .flexWrap(() -> "wrap")
                .gap(gap),
            children);
    }

    // ==================== Grid Layouts ====================

    /**
     * Creates a grid with equal columns.
     */
    public static Element grid(int columns, Object... children) {
        return div(attrs().style().grid(columns), children);
    }

    /**
     * Creates a grid with equal columns and gap.
     */
    public static Element grid(int columns, CSSValue gap, Object... children) {
        return div(attrs().style().grid(columns, gap), children);
    }

    /**
     * Creates an auto-fit grid with minimum column width.
     * Columns automatically adjust based on available space.
     */
    public static Element autoGrid(CSSValue minColumnWidth, CSSValue gap, Object... children) {
        return div(attrs().style()
                .display(() -> "grid")
                .prop("grid-template-columns", "repeat(auto-fit, minmax(" + minColumnWidth.css() + ", 1fr))")
                .gap(gap),
            children);
    }

    /**
     * Creates an auto-fill grid with minimum column width.
     * Creates empty columns when there's extra space.
     */
    public static Element autoFillGrid(CSSValue minColumnWidth, CSSValue gap, Object... children) {
        return div(attrs().style()
                .display(() -> "grid")
                .prop("grid-template-columns", "repeat(auto-fill, minmax(" + minColumnWidth.css() + ", 1fr))")
                .gap(gap),
            children);
    }

    // ==================== Multi-Column Layouts ====================

    /**
     * Creates equal-width columns.
     */
    public static Element columns(int count, Object... children) {
        return columns(count, rem(1), children);
    }

    /**
     * Creates equal-width columns with gap.
     */
    public static Element columns(int count, CSSValue gap, Object... children) {
        return div(attrs().style().grid(count, gap), children);
    }

    /**
     * Creates a two-column layout with custom ratio.
     * Example: columns("1fr", "2fr", ...) creates 1:2 ratio
     */
    public static Element columns(String leftWidth, String rightWidth, CSSValue gap, Object... children) {
        return div(attrs().style()
                .display(() -> "grid")
                .prop("grid-template-columns", leftWidth + " " + rightWidth)
                .gap(gap),
            children);
    }

    // ==================== Sidebar Layout ====================

    /**
     * Creates a sidebar layout (sidebar on left, main content on right).
     */
    public static Element sidebar(CSSValue sidebarWidth, Element sidebar, Element content) {
        return div(attrs().style()
                .display(() -> "grid")
                .prop("grid-template-columns", sidebarWidth.css() + " 1fr")
                .gap(rem(2)),
            sidebar, content);
    }

    /**
     * Creates a sidebar layout with the sidebar on the right.
     */
    public static Element sidebarRight(CSSValue sidebarWidth, Element content, Element sidebar) {
        return div(attrs().style()
                .display(() -> "grid")
                .prop("grid-template-columns", "1fr " + sidebarWidth.css())
                .gap(rem(2)),
            content, sidebar);
    }

    // ==================== Stack ====================

    /**
     * Creates a vertical stack with consistent spacing.
     */
    public static Element stack(CSSValue gap, Object... children) {
        return div(attrs().style().flexCol().gap(gap), children);
    }

    /**
     * Creates a vertical stack with default spacing (1rem).
     */
    public static Element stack(Object... children) {
        return stack(rem(1), children);
    }

    // ==================== Split ====================

    /**
     * Creates a split layout with two sections.
     * First child goes left, second goes right, with space between.
     */
    public static Element split(Object left, Object right) {
        return div(attrs().style().flexBetween(), left, right);
    }

    // ==================== Cluster ====================

    /**
     * Creates a cluster layout - horizontally aligned items with wrapping and gap.
     * Good for tags, buttons, navigation items.
     */
    public static Element cluster(CSSValue gap, Object... children) {
        return div(attrs().style()
                .flex()
                .flexWrap(() -> "wrap")
                .gap(gap)
                .alignItems(() -> "center"),
            children);
    }

    /**
     * Creates a cluster with default gap (0.5rem).
     */
    public static Element cluster(Object... children) {
        return cluster(rem(0.5), children);
    }

    // ==================== Aspect Ratio ====================

    /**
     * Creates a container with fixed aspect ratio.
     */
    public static Element aspectRatio(String ratio, Object... children) {
        return div(attrs().style()
                .prop("aspect-ratio", ratio)
                .fullWidth(),
            children);
    }

    /**
     * Creates a square container.
     */
    public static Element square(CSSValue size, Object... children) {
        return div(attrs().style()
                .width(size)
                .height(size),
            children);
    }

    // ==================== Cover ====================

    /**
     * Creates a cover layout - vertically centers content with optional min-height.
     */
    public static Element cover(CSSValue minHeight, Object... children) {
        return div(attrs().style()
                .minHeight(minHeight)
                .flexCenter(),
            children);
    }

    /**
     * Creates a full viewport height cover.
     */
    public static Element fullCover(Object... children) {
        return cover(vh(100), children);
    }

    // ==================== Sticky ====================

    /**
     * Creates a sticky positioned element.
     */
    public static Element sticky(CSSValue top, Object... children) {
        return div(attrs().style()
                .sticky()
                .top(top),
            children);
    }

    /**
     * Creates a sticky header (top: 0).
     */
    public static Element stickyHeader(Object... children) {
        return sticky(zero, children);
    }

    // ==================== Scrollable ====================

    /**
     * Creates a scrollable container with fixed height.
     */
    public static Element scrollable(CSSValue height, Object... children) {
        return div(attrs().style()
                .height(height)
                .overflow(() -> "auto"),
            children);
    }

    /**
     * Creates a horizontally scrollable container.
     */
    public static Element scrollX(Object... children) {
        return div(attrs().style()
                .overflow(() -> "auto")
                .prop("overflow-x", "auto")
                .prop("overflow-y", "hidden")
                .whiteSpace(() -> "nowrap"),
            children);
    }

    // ==================== Card ====================

    /**
     * Creates a card with padding and optional shadow.
     */
    public static Element card(Object... children) {
        return div(attrs().style()
                .padding(rem(1.5))
                .rounded(px(8))
                .backgroundColor(() -> "white")
                .boxShadow("0 1px 3px rgba(0,0,0,0.1)"),
            children);
    }

    /**
     * Creates a card with custom padding.
     */
    public static Element card(CSSValue padding, Object... children) {
        return div(attrs().style()
                .padding(padding)
                .rounded(px(8))
                .backgroundColor(() -> "white")
                .boxShadow("0 1px 3px rgba(0,0,0,0.1)"),
            children);
    }

    // ==================== Divider ====================

    /**
     * Creates a horizontal divider.
     */
    public static Element divider() {
        return hr(attrs().style()
                .prop("border", "none")
                .prop("border-top", "1px solid #e5e7eb")
                .prop("margin", rem(1).css() + " " + zero.css())
            .done());
    }

    /**
     * Creates a vertical divider.
     */
    public static Element verticalDivider(CSSValue height) {
        return div(attrs().style()
                .width(px(1))
                .height(height)
                .backgroundColor(() -> "#e5e7eb"));
    }

    // ==================== Spacer ====================

    /**
     * Creates flexible space that pushes siblings apart.
     */
    public static Element spacer() {
        return div(attrs().style().flexGrow(1));
    }

    /**
     * Creates fixed-size spacing.
     */
    public static Element space(CSSValue size) {
        return div(attrs().style().height(size).width(size));
    }

    // ==================== Hidden / Visible ====================

    /**
     * Creates a visually hidden element (for screen readers).
     */
    public static Element visuallyHidden(Object... children) {
        return span(attrs().style()
                .position(() -> "absolute")
                .width(px(1))
                .height(px(1))
                .padding(zero)
                .margin(px(-1))
                .overflow(() -> "hidden")
                .prop("clip", "rect(0, 0, 0, 0)")
                .whiteSpace(() -> "nowrap")
                .prop("border", "0"),
            children);
    }
}
