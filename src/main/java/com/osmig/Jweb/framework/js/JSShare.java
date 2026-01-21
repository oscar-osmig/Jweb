package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Web Share API for native sharing functionality.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSShare.*;
 *
 * // Share text and URL
 * share()
 *     .title("Check this out!")
 *     .text("I found this amazing article")
 *     .url("https://example.com/article")
 *     .build()
 *
 * // Share files
 * shareFiles(variable("files"))
 *     .title("Photos")
 *     .build()
 *
 * // Check if sharing is supported
 * if_(canShare(), showShareButton())
 * </pre>
 */
public final class JSShare {
    private JSShare() {}

    // ==================== Share Builder ====================

    /** Creates a share builder */
    public static ShareBuilder share() {
        return new ShareBuilder();
    }

    /** Creates a share builder with files */
    public static ShareBuilder shareFiles(Val files) {
        return new ShareBuilder().files(files);
    }

    // ==================== Support Checks ====================

    /** Checks if Web Share API is supported */
    public static Val isSupported() {
        return new Val("('share' in navigator)");
    }

    /** Checks if can share specific data */
    public static Val canShare(Val data) {
        return new Val("(navigator.canShare&&navigator.canShare(" + data.js() + "))");
    }

    /** Checks if can share (basic) */
    public static Val canShare() {
        return new Val("('share' in navigator)");
    }

    /** Checks if can share files */
    public static Val canShareFiles() {
        return new Val("(navigator.canShare&&navigator.canShare({files:[new File([''],'t.txt')]}))");
    }

    // ==================== Direct Share ====================

    /** Shares URL directly */
    public static Val shareUrl(String url) {
        return new Val("navigator.share({url:'" + JS.esc(url) + "'})");
    }

    /** Shares URL directly (dynamic) */
    public static Val shareUrl(Val url) {
        return new Val("navigator.share({url:" + url.js() + "})");
    }

    /** Shares text directly */
    public static Val shareText(String text) {
        return new Val("navigator.share({text:'" + JS.esc(text) + "'})");
    }

    /** Shares text directly (dynamic) */
    public static Val shareText(Val text) {
        return new Val("navigator.share({text:" + text.js() + "})");
    }

    /** Shares current page */
    public static Val shareCurrentPage() {
        return new Val("navigator.share({title:document.title,url:location.href})");
    }

    /** Shares current page with custom text */
    public static Val shareCurrentPage(String text) {
        return new Val("navigator.share({title:document.title,text:'" + JS.esc(text) + "',url:location.href})");
    }

    // ==================== Share Builder ====================

    public static class ShareBuilder {
        private String title, text, url;
        private Val titleExpr, textExpr, urlExpr, filesExpr;
        private Func thenFunc, catchFunc;

        public ShareBuilder title(String title) { this.title = title; return this; }
        public ShareBuilder title(Val title) { this.titleExpr = title; return this; }
        public ShareBuilder text(String text) { this.text = text; return this; }
        public ShareBuilder text(Val text) { this.textExpr = text; return this; }
        public ShareBuilder url(String url) { this.url = url; return this; }
        public ShareBuilder url(Val url) { this.urlExpr = url; return this; }
        public ShareBuilder files(Val files) { this.filesExpr = files; return this; }

        public ShareBuilder then(Func handler) { this.thenFunc = handler; return this; }
        public ShareBuilder catch_(Func handler) { this.catchFunc = handler; return this; }

        /** Builds the share call */
        public Val build() {
            StringBuilder sb = new StringBuilder("navigator.share({");
            boolean first = true;

            if (title != null) { sb.append("title:'").append(JS.esc(title)).append("'"); first = false; }
            else if (titleExpr != null) { sb.append("title:").append(titleExpr.js()); first = false; }

            if (text != null) { if (!first) sb.append(","); sb.append("text:'").append(JS.esc(text)).append("'"); first = false; }
            else if (textExpr != null) { if (!first) sb.append(","); sb.append("text:").append(textExpr.js()); first = false; }

            if (url != null) { if (!first) sb.append(","); sb.append("url:'").append(JS.esc(url)).append("'"); first = false; }
            else if (urlExpr != null) { if (!first) sb.append(","); sb.append("url:").append(urlExpr.js()); first = false; }

            if (filesExpr != null) { if (!first) sb.append(","); sb.append("files:").append(filesExpr.js()); }

            sb.append("})");

            if (thenFunc != null) sb.append(".then(").append(thenFunc.toExpr()).append(")");
            if (catchFunc != null) sb.append(".catch(").append(catchFunc.toExpr()).append(")");

            return new Val(sb.toString());
        }
    }

    // ==================== Share Target (for PWAs) ====================

    /** Gets shared data from URL params (for PWA share target) */
    public static Val getSharedTitle() {
        return new Val("new URLSearchParams(location.search).get('title')");
    }

    /** Gets shared text from URL params */
    public static Val getSharedText() {
        return new Val("new URLSearchParams(location.search).get('text')");
    }

    /** Gets shared URL from URL params */
    public static Val getSharedUrl() {
        return new Val("new URLSearchParams(location.search).get('url')");
    }
}
