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
        // Use singleton pattern to prevent duplicate initialization
        return "(function(){" +
            // Prevent duplicate initialization
            "if(window.__docsNavInit)return;" +
            "window.__docsNavInit=true;" +

            // Use global cache (persists across navigation)
            "window.__docsCache=window.__docsCache||{};" +
            "var contentCache=window.__docsCache;" +
            "var contentPending={};" +
            "var TTL=300000;" + // 5 min cache

            // Prefetch content section
            "function prefetchContent(section){" +
                "var url='/docs/content?section='+section;" +
                "if(contentCache[section]&&Date.now()-contentCache[section].time<TTL)return;" +
                "if(contentPending[section])return;" +
                "contentPending[section]=true;" +
                "fetch(url,{credentials:'same-origin'})" +
                ".then(function(r){return r.text()})" +
                ".then(function(html){" +
                    "contentCache[section]={html:html,time:Date.now()};" +
                    "delete contentPending[section]" +
                "})" +
                ".catch(function(){delete contentPending[section]})" +
            "}" +

            // Load section - use cache if available
            "function loadSection(section){" +
                "var cached=contentCache[section];" +
                "if(cached&&Date.now()-cached.time<TTL){" +
                    "document.querySelector('.docs-content').innerHTML=cached.html;" +
                    "updateActiveLink(section);" +
                    "history.pushState(null,'','/docs?section='+section);" +
                    "return" +
                "}" +
                // Fetch if not cached
                "fetch('/docs/content?section='+section,{credentials:'same-origin'})" +
                ".then(function(r){return r.text()})" +
                ".then(function(html){" +
                    "contentCache[section]={html:html,time:Date.now()};" +
                    "document.querySelector('.docs-content').innerHTML=html;" +
                    "updateActiveLink(section);" +
                    "history.pushState(null,'','/docs?section='+section)" +
                "})" +
            "}" +

            "function updateActiveLink(section){" +
                "document.querySelectorAll('.docs-nav-link').forEach(function(link){" +
                    "var isActive=link.dataset.section===section;" +
                    "link.style.color=isActive?'#4f46e5':'#64748b';" +
                    "link.style.fontWeight=isActive?'600':'400';" +
                    "link.style.backgroundColor=isActive?'#eef2ff':'transparent'" +
                "})" +
            "}" +

            // Hover prefetch using event delegation (single listener)
            "var hoverTimeout=null;" +
            "var sidebar=document.querySelector('.docs-sidebar');" +
            "if(sidebar){" +
                "sidebar.addEventListener('mouseover',function(e){" +
                    "var link=e.target.closest('.docs-nav-link');" +
                    "if(!link)return;" +
                    "var section=link.dataset.section;" +
                    "if(!section)return;" +
                    "if(hoverTimeout)clearTimeout(hoverTimeout);" +
                    "hoverTimeout=setTimeout(function(){prefetchContent(section)},50)" +
                "});" +
                "sidebar.addEventListener('mouseout',function(){" +
                    "if(hoverTimeout){clearTimeout(hoverTimeout);hoverTimeout=null}" +
                "});" +
                // Click handler using event delegation (single listener)
                "sidebar.addEventListener('click',function(e){" +
                    "var link=e.target.closest('.docs-nav-link');" +
                    "if(!link)return;" +
                    "e.preventDefault();" +
                    "loadSection(link.dataset.section)" +
                "})" +
            "}" +

            // Handle browser back/forward (single global listener)
            "if(!window.__docsPopstate){" +
                "window.__docsPopstate=true;" +
                "window.addEventListener('popstate',function(){" +
                    "var params=new URLSearchParams(location.search);" +
                    "var section=params.get('section')||'intro';" +
                    "loadSection(section)" +
                "})" +
            "}" +
        "})();";
    }
}
