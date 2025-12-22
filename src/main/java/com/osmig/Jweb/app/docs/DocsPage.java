package com.osmig.Jweb.app.docs;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.Elements.*;
import static com.osmig.Jweb.framework.styles.CSS.*;
import static com.osmig.Jweb.framework.styles.CSSUnits.*;
import static com.osmig.Jweb.framework.styles.CSSColors.*;
import static com.osmig.Jweb.framework.styles.Stylesheet.*;
import static com.osmig.Jweb.framework.styles.Selectors.*;
import static com.osmig.Jweb.framework.js.JS.*;
import static com.osmig.Jweb.framework.js.Async.*;
import static com.osmig.Jweb.app.layout.Theme.*;

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
        return div(attrs().style()
                .display(flex)
                .flex(num(1))
                .minHeight(num(0))
            .done(),
            new DocSidebar(section).render(),
            div(attrs().class_("docs-content").style()
                .flex(num(1))
                .padding(SP_8, SP_12)
                .overflowY(auto)
            .done(),
                DocContent.get(section)
            ),
            style(stylesheet()
                .rule(scrollbar(".docs-content"), style().width(px(6)))
                .rule(scrollbarTrack(".docs-content"), style().background(transparent))
                .rule(scrollbarThumb(".docs-content"), style()
                    .background(rgba(0, 0, 0, 0.1)).borderRadius(px(3)))
                .rule(scrollbarThumbHover(".docs-content"), style()
                    .background(rgba(0, 0, 0, 0.2)))
                .rule(scrollbar(".docs-sidebar"), style().width(px(4)))
                .rule(scrollbarTrack(".docs-sidebar"), style().background(transparent))
                .rule(scrollbarThumb(".docs-sidebar"), style()
                    .background(rgba(0, 0, 0, 0.05)).borderRadius(px(2)))
                .rule(scrollbarThumbHover(".docs-sidebar"), style()
                    .background(rgba(0, 0, 0, 0.1)))
                .build()),
            inlineScript(clientNavScript())
        );
    }

    private String clientNavScript() {
        return script()
            .add(asyncFunc("loadSection", "section")
                .await_("response", fetch(variable("'/docs/content?section='+section")).get().toVal())
                .await_("html", variable("response").dot("text").invoke())
                .unsafeRaw("document.querySelector('.docs-content').innerHTML=html")
                .call("updateActiveLink", variable("section"))
                .call("history.pushState", null_(), str(""), variable("'/docs?section='+section")))
            .add(func("updateActiveLink", "section")
                .unsafeRaw("document.querySelectorAll('.docs-nav-link').forEach(function(link){" +
                    "var isActive=link.dataset.section===section;" +
                    "link.style.color=isActive?'#4f46e5':'#64748b';" +
                    "link.style.fontWeight=isActive?'600':'400';" +
                    "link.style.backgroundColor=isActive?'#eef2ff':'transparent'" +
                    "})"))
            .add(func("initDocsNav")
                .unsafeRaw("document.querySelectorAll('.docs-nav-link').forEach(function(link){" +
                    "link.addEventListener('click',function(e){" +
                    "e.preventDefault();" +
                    "loadSection(this.dataset.section)" +
                    "})" +
                    "})"))
            .unsafeRaw("initDocsNav()")
            .build();
    }
}
