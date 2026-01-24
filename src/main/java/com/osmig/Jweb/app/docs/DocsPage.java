package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.Stylesheet.*;
import static com.osmig.Jweb.framework.styles.Selectors.*;
import static com.osmig.Jweb.app.layout.Theme.*;
import com.osmig.Jweb.app.subheader.SubheaderSidebar;
import com.osmig.Jweb.app.subheader.SubheaderScript;

/**
 * Documentation page with sidebar and content.
 */
public class DocsPage implements Template {
    private final String section;

    public DocsPage(String section) {
        this.section = section != null ? section : "intro";
    }

    @Override
    public Element render() {
        return div(attrs().style().display(flex).flex(num(1)).minHeight(num(0)).done(),
            new DocSidebar(section).render(),
            div(attrs().class_("docs-content").style()
                    .flex(num(1)).padding(SP_8, SP_12).overflowY(auto).done(),
                DocContent.get(section)),
            new SubheaderSidebar().render(),
            style(scrollbarStyles()),
            inlineScript(DocsNavScript.build()),
            inlineScript(SubheaderScript.build())
        );
    }

    private String scrollbarStyles() {
        return stylesheet()
            .rule(scrollbar(".docs-content"), style().width(px(6)))
            .rule(scrollbarTrack(".docs-content"), style().background(transparent))
            .rule(scrollbarThumb(".docs-content"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(scrollbarThumbHover(".docs-content"), style().background(rgba(0, 0, 0, 0.2)))
            .rule(scrollbar(".docs-sidebar"), style().width(px(4)))
            .rule(scrollbarTrack(".docs-sidebar"), style().background(transparent))
            .rule(scrollbarThumb(".docs-sidebar"), style().background(rgba(0, 0, 0, 0.05)).borderRadius(px(2)))
            .rule(scrollbarThumbHover(".docs-sidebar"), style().background(rgba(0, 0, 0, 0.1)))
            .rule(scrollbar("#subheader-nav"), style().width(px(6)))
            .rule(scrollbarTrack("#subheader-nav"), style().background(transparent))
            .rule(scrollbarThumb("#subheader-nav"), style().background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
            .rule(scrollbarThumbHover("#subheader-nav"), style().background(rgba(0, 0, 0, 0.2)))
            .rule(".docs-content h2, .docs-content h3", style().prop("scroll-margin-top", "1.5rem"))
            .rule(".docs-nav-link.active, .subheader-link.active", style()
                .position(relative)
                .overflow(visible)
                .zIndex(1))
            .rule(".docs-nav-link.active::before, .subheader-link.active::before", style()
                .prop("content", "''")
                .position(absolute)
                .top(zero).left(zero).right(zero).bottom(zero)
                .borderRadius(px(6))
                .padding(px(2))
                .background(linearGradient("90deg",
                    hex("#6366f1"), hex("#8b5cf6"), hex("#a855f7"),
                    hex("#ec4899"), hex("#8b5cf6"), hex("#6366f1")))
                .prop("background-size", "300% 100%")
                .prop("animation", "gradientShift 3s linear infinite")
                .prop("-webkit-mask", "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)")
                .prop("mask", "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)")
                .prop("-webkit-mask-composite", "xor")
                .prop("mask-composite", "exclude")
                .zIndex(-1)
                .pointerEvents(none))
            .build();
    }
}
