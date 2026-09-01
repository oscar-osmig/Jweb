package com.osmig.Jweb.app.docs;

import static jweb.Js.*;

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
                // The docs version in view; threads through fetches, cache
                // keys and history so client-side nav stays on the version
                .unsafeRaw("var DOCS_V=new URLSearchParams(location.search).get('v')||''")
                .unsafeRaw("function withV(u){return DOCS_V?u+'&v='+DOCS_V:u}")
                .unsafeRaw(prefetchFunc())
                .unsafeRaw(loadSectionFunc())
                .unsafeRaw(updateActiveLinkFunc())
                .unsafeRaw(hoverPrefetch())
                .unsafeRaw(clickHandler())
                .unsafeRaw(onPopState("__docsPopstate").handler(
                    "var q=new URLSearchParams(location.search);DOCS_V=q.get('v')||'';" +
                    "loadSection(q.get('section')||'intro')").js())
                .build())
            .build();
    }

    private static String prefetchFunc() {
        return "function prefetchContent(section){" +
            "var k=withV(section);" +
            "var url=withV('/docs/content?section='+section);" +
            "if(" + CACHE.isValid("k").js() + ")return;" +
            "if(contentPending[k])return;contentPending[k]=true;" +
            "fetch(url,{credentials:'same-origin'}).then(function(r){return r.text()})" +
            ".then(function(html){" + CACHE.set("k", "html").js() + ";delete contentPending[k]})" +
            ".catch(function(){delete contentPending[k]})}";
    }

    private static String loadSectionFunc() {
        return "function loadSection(section){" +
            "var k=withV(section);" +
            "if(" + CACHE.isValid("k").js() + "){" +
            setInnerHTML(".docs-content", CACHE.getData("k")).js() + ";" +
            "updateActiveLink(section);" + pushStateExpr(expr("withV('/docs?section='+section)")).js() + ";return}" +
            "fetch(withV('/docs/content?section='+section),{credentials:'same-origin'})" +
            ".then(function(r){return r.text()}).then(function(html){" + CACHE.set("k", "html").js() + ";" +
            setInnerHTML(".docs-content", expr("html")).js() + ";updateActiveLink(section);" +
            pushStateExpr(expr("withV('/docs?section='+section)")).js() + "})}";
    }

    private static String updateActiveLinkFunc() {
        // The 'active' class drives the animated gradient border
        // (.docs-nav-link.active::before) — toggle it along with the inline styles
        return "function updateActiveLink(section){" +
            "document.querySelectorAll('.docs-nav-link').forEach(function(link){" +
            "var a=link.dataset.section===section;link.classList.toggle('active',a);" +
            "link.style.color=a?'#4f46e5':'#64748b';" +
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
