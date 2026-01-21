package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

/**
 * Browser storage APIs: localStorage, sessionStorage, cookies.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSStorage.*;
 *
 * local().get("token")                    // localStorage.getItem('token')
 * local().setJson("user", variable("u"))  // localStorage.setItem('user', JSON.stringify(u))
 * session().remove("temp")                // sessionStorage.removeItem('temp')
 * </pre>
 */
public final class JSStorage {
    private JSStorage() {}

    public static Storage local() { return new Storage("localStorage"); }
    public static Storage session() { return new Storage("sessionStorage"); }

    public static class Storage {
        private final String storage;

        Storage(String storage) { this.storage = storage; }

        /** Gets item: storage.getItem('key') */
        public Val get(String key) {
            return new Val(storage + ".getItem('" + JS.esc(key) + "')");
        }

        /** Gets item with dynamic key */
        public Val get(Val key) {
            return new Val(storage + ".getItem(" + key.js() + ")");
        }

        /** Gets and parses JSON: JSON.parse(storage.getItem('key')) */
        public Val getJson(String key) {
            return new Val("JSON.parse(" + storage + ".getItem('" + JS.esc(key) + "'))");
        }

        /** Gets JSON with fallback if null/invalid */
        public Val getJsonOr(String key, Object fallback) {
            return new Val("(function(){try{var v=" + storage + ".getItem('" + JS.esc(key) +
                "');return v?JSON.parse(v):" + JS.toJs(fallback) + "}catch(e){return " +
                JS.toJs(fallback) + "}}())");
        }

        /** Sets item: storage.setItem('key', 'value') */
        public Val set(String key, String value) {
            return new Val(storage + ".setItem('" + JS.esc(key) + "','" + JS.esc(value) + "')");
        }

        /** Sets item from expression */
        public Val set(String key, Val value) {
            return new Val(storage + ".setItem('" + JS.esc(key) + "'," + value.js() + ")");
        }

        /** Sets item as JSON: storage.setItem('key', JSON.stringify(value)) */
        public Val setJson(String key, Val value) {
            return new Val(storage + ".setItem('" + JS.esc(key) + "',JSON.stringify(" + value.js() + "))");
        }

        /** Removes item: storage.removeItem('key') */
        public Val remove(String key) {
            return new Val(storage + ".removeItem('" + JS.esc(key) + "')");
        }

        /** Clears all: storage.clear() */
        public Val clear() {
            return new Val(storage + ".clear()");
        }

        /** Gets key at index: storage.key(index) */
        public Val key(int index) {
            return new Val(storage + ".key(" + index + ")");
        }

        /** Gets length: storage.length */
        public Val length() {
            return new Val(storage + ".length");
        }

        /** Checks if key exists */
        public Val has(String key) {
            return new Val(storage + ".getItem('" + JS.esc(key) + "')!==null");
        }
    }

    // ==================== Cookie Helpers ====================

    /** Gets a cookie value */
    public static Val getCookie(String name) {
        return new Val("(document.cookie.match('(^|;)\\\\s*" + JS.esc(name) +
            "\\\\s*=\\\\s*([^;]+)')||[])[2]||''");
    }

    /** Sets a cookie */
    public static Val setCookie(String name, String value, int days) {
        return new Val("document.cookie='" + JS.esc(name) + "='+encodeURIComponent('" +
            JS.esc(value) + "')+';max-age=" + (days * 86400) + ";path=/'");
    }

    /** Sets a cookie with expression value */
    public static Val setCookie(String name, Val value, int days) {
        return new Val("document.cookie='" + JS.esc(name) + "='+encodeURIComponent(" +
            value.js() + ")+';max-age=" + (days * 86400) + ";path=/'");
    }

    /** Deletes a cookie */
    public static Val deleteCookie(String name) {
        return new Val("document.cookie='" + JS.esc(name) + "=;max-age=0;path=/'");
    }

    /** Sets cookie with options */
    public static CookieBuilder cookie(String name) {
        return new CookieBuilder(name);
    }

    // ==================== Storage Events ====================

    /** Listens for storage changes (cross-tab communication) */
    public static Val onStorageChange(Func handler) {
        return new Val("window.addEventListener('storage'," + handler.toExpr() + ")");
    }

    /** Listens for storage changes with raw code */
    public static Val onStorageChange(String code) {
        return new Val("window.addEventListener('storage',function(e){" + code + "})");
    }

    /** Listens for specific key changes */
    public static Val onStorageKeyChange(String key, Func handler) {
        return new Val("window.addEventListener('storage',function(e){if(e.key==='" + JS.esc(key) + "')(" + handler.toExpr() + ")(e)})");
    }

    /** Gets event key: event.key */
    public static Val eventKey(Val event) { return new Val(event.js() + ".key"); }

    /** Gets old value: event.oldValue */
    public static Val eventOldValue(Val event) { return new Val(event.js() + ".oldValue"); }

    /** Gets new value: event.newValue */
    public static Val eventNewValue(Val event) { return new Val(event.js() + ".newValue"); }

    /** Gets storage area: event.storageArea */
    public static Val eventStorageArea(Val event) { return new Val(event.js() + ".storageArea"); }

    /** Gets URL where change occurred: event.url */
    public static Val eventUrl(Val event) { return new Val(event.js() + ".url"); }

    // ==================== Storage Iteration ====================

    /** Iterates all keys in storage */
    public static Val forEachKey(String storageType, Func callback) {
        return new Val("(function(){var s=" + storageType + ";for(var i=0;i<s.length;i++)(" + callback.toExpr() + ")(s.key(i),s.getItem(s.key(i)))}())");
    }

    /** Gets all keys from storage */
    public static Val getAllKeys(String storageType) {
        return new Val("Object.keys(" + storageType + ")");
    }

    /** Gets all entries from storage as object */
    public static Val toObject(String storageType) {
        return new Val("(function(){var o={},s=" + storageType + ";for(var i=0;i<s.length;i++){var k=s.key(i);o[k]=s.getItem(k)}return o}())");
    }

    // ==================== Cookie Builder ====================

    public static class CookieBuilder {
        private final String name;
        private String path = "/";
        private String domain;
        private Integer maxAge;
        private String expires;
        private boolean secure;
        private String sameSite;
        private boolean httpOnly;

        CookieBuilder(String name) { this.name = name; }

        public CookieBuilder path(String path) { this.path = path; return this; }
        public CookieBuilder domain(String domain) { this.domain = domain; return this; }
        public CookieBuilder maxAge(int seconds) { this.maxAge = seconds; return this; }
        public CookieBuilder days(int days) { this.maxAge = days * 86400; return this; }
        public CookieBuilder expires(String dateString) { this.expires = dateString; return this; }
        public CookieBuilder secure() { this.secure = true; return this; }
        public CookieBuilder sameSite(String value) { this.sameSite = value; return this; }
        public CookieBuilder sameStrict() { this.sameSite = "Strict"; return this; }
        public CookieBuilder sameLax() { this.sameSite = "Lax"; return this; }
        public CookieBuilder sameNone() { this.sameSite = "None"; this.secure = true; return this; }

        /** Sets cookie with static value */
        public Val set(String value) {
            return new Val("document.cookie='" + JS.esc(name) + "='+encodeURIComponent('" + JS.esc(value) + "')" + buildOptions());
        }

        /** Sets cookie with dynamic value */
        public Val set(Val value) {
            return new Val("document.cookie='" + JS.esc(name) + "='+encodeURIComponent(" + value.js() + ")" + buildOptions());
        }

        /** Deletes this cookie */
        public Val delete_() {
            return new Val("document.cookie='" + JS.esc(name) + "=;max-age=0;path=" + path + "'");
        }

        private String buildOptions() {
            StringBuilder sb = new StringBuilder();
            if (path != null) sb.append("+';path=").append(JS.esc(path)).append("'");
            if (domain != null) sb.append("+';domain=").append(JS.esc(domain)).append("'");
            if (maxAge != null) sb.append("+';max-age=").append(maxAge).append("'");
            if (expires != null) sb.append("+';expires=").append(JS.esc(expires)).append("'");
            if (secure) sb.append("+';secure'");
            if (sameSite != null) sb.append("+';samesite=").append(sameSite).append("'");
            return sb.toString();
        }
    }

    // ==================== Storage Quota ====================

    /** Estimates storage quota (returns Promise) */
    public static Val estimateQuota() {
        return new Val("navigator.storage.estimate()");
    }

    /** Checks if storage is persisted (returns Promise) */
    public static Val isPersisted() {
        return new Val("navigator.storage.persisted()");
    }

    /** Requests persistent storage (returns Promise) */
    public static Val requestPersist() {
        return new Val("navigator.storage.persist()");
    }
}
