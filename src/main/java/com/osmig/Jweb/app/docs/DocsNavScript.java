package com.osmig.Jweb.app.docs;

import static com.osmig.Jweb.framework.js.Runtime.*;
import static com.osmig.Jweb.framework.js.Events.*;
import static com.osmig.Jweb.framework.js.JS.*;

/**
 * Client-side navigation script for documentation pages.
 * Uses Runtime and Events DSL for IIFE, caching, event delegation.
 */
final class DocsNavScript {
    private DocsNavScript() {}

    private static final int TTL = 300000;
    private static final Cache CACHE = cache("contentCache", TTL);

    static String build() {
        return iife()
            .unsafeRaw(guard("__docsNavInit")
                .unsafeRaw(globalCache("__docsCache").js())
                .unsafeRaw("var contentCache=window.__docsCache;var contentPending={};var TTL=" + TTL)
                .unsafeRaw(prefetchFunc())
                .unsafeRaw(loadSectionFunc())
                .unsafeRaw(updateActiveLinkFunc())
                .unsafeRaw(hoverPrefetch())
                .unsafeRaw(clickHandler())
                .unsafeRaw(onPopState("__docsPopstate").handler(
                    "var s=new URLSearchParams(location.search).get('section')||'intro';loadSection(s)").js())
                .build())
            .build();
    }

    private static String prefetchFunc() {
        return "function prefetchContent(section){" +
            "var url='/docs/content?section='+section;" +
            "if(" + CACHE.isValid("section").js() + ")return;" +
            "if(contentPending[section])return;contentPending[section]=true;" +
            "fetch(url,{credentials:'same-origin'}).then(function(r){return r.text()})" +
            ".then(function(html){" + CACHE.set("section", "html").js() + ";delete contentPending[section]})" +
            ".catch(function(){delete contentPending[section]})}";
    }

    private static String loadSectionFunc() {
        return "function loadSection(section){" +
            "if(" + CACHE.isValid("section").js() + "){" +
            setInnerHTML(".docs-content", CACHE.getData("section")).js() + ";" +
            "updateActiveLink(section);" + pushStateExpr(expr("'/docs?section='+section")).js() + ";return}" +
            "fetch('/docs/content?section='+section,{credentials:'same-origin'})" +
            ".then(function(r){return r.text()}).then(function(html){" + CACHE.set("section", "html").js() + ";" +
            setInnerHTML(".docs-content", expr("html")).js() + ";updateActiveLink(section);" +
            pushStateExpr(expr("'/docs?section='+section")).js() + "})}";
    }

    private static String updateActiveLinkFunc() {
        return "function updateActiveLink(section){" +
            "document.querySelectorAll('.docs-nav-link').forEach(function(link){" +
            "var a=link.dataset.section===section;link.style.color=a?'#4f46e5':'#64748b';" +
            "link.style.fontWeight=a?'600':'400';link.style.backgroundColor=a?'#eef2ff':'transparent'})}";
    }

    private static String hoverPrefetch() {
        var d = debounce("hoverTimeout", 50);
        return "var hoverTimeout=null;" +
            delegate(".docs-sidebar", "mouseover", ".docs-nav-link")
                .handler("var s=t.dataset.section;if(!s)return;" + d.wrap("prefetchContent(s)").js()).js() + ";" +
            delegate(".docs-sidebar", "mouseout", ".docs-nav-link").handler(d.clear().js()).js();
    }

    private static String clickHandler() {
        return delegate(".docs-sidebar", "click", ".docs-nav-link")
            .handler("e.preventDefault();loadSection(t.dataset.section)").js();
    }
}
