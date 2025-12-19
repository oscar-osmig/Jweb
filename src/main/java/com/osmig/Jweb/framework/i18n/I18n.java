package com.osmig.Jweb.framework.i18n;

import com.osmig.Jweb.framework.server.Request;

import java.util.List;
import java.util.Locale;

/**
 * Internationalization utilities for JWeb.
 *
 * <h2>Locale Detection</h2>
 * <p>Determines user locale from (in order of priority):</p>
 * <ol>
 *   <li>Query parameter: {@code ?lang=es}</li>
 *   <li>Session attribute: {@code lang}</li>
 *   <li>Cookie: {@code lang}</li>
 *   <li>Accept-Language header</li>
 *   <li>Default locale</li>
 * </ol>
 *
 * <h2>Usage in Routes</h2>
 * <pre>{@code
 * app.get("/", req -> {
 *     Locale locale = I18n.getLocale(req);
 *     String title = Messages.get(locale, "page.title");
 *     return page(title, ...);
 * });
 * }</pre>
 *
 * <h2>Middleware for Automatic Locale</h2>
 * <pre>{@code
 * app.use(I18n.middleware());
 * }</pre>
 */
public final class I18n {

    private static final String LANG_PARAM = "lang";
    private static final String LANG_SESSION = "lang";
    private static final String LANG_COOKIE = "lang";
    private static final ThreadLocal<Locale> currentLocale = new ThreadLocal<>();

    private I18n() {}

    /**
     * Gets the locale for the current request.
     *
     * @param request the HTTP request
     * @return the detected locale
     */
    public static Locale getLocale(Request request) {
        // 1. Check query parameter
        String lang = request.query(LANG_PARAM);
        if (lang != null && !lang.isEmpty()) {
            return Locale.forLanguageTag(lang);
        }

        // 2. Check session
        Object sessionLang = request.sessionAttr(LANG_SESSION);
        if (sessionLang != null) {
            return Locale.forLanguageTag(sessionLang.toString());
        }

        // 3. Check cookie
        String cookieLang = request.cookie(LANG_COOKIE);
        if (cookieLang != null && !cookieLang.isEmpty()) {
            return Locale.forLanguageTag(cookieLang);
        }

        // 4. Parse Accept-Language header
        String acceptLanguage = request.header("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            return parseAcceptLanguage(acceptLanguage);
        }

        // 5. Default locale
        return Messages.getDefaultLocale();
    }

    /**
     * Gets the current thread's locale (set by middleware).
     *
     * @return the current locale, or default if not set
     */
    public static Locale current() {
        Locale locale = currentLocale.get();
        return locale != null ? locale : Messages.getDefaultLocale();
    }

    /**
     * Sets the current thread's locale.
     *
     * @param locale the locale to set
     */
    public static void setCurrent(Locale locale) {
        currentLocale.set(locale);
    }

    /**
     * Clears the current thread's locale.
     */
    public static void clearCurrent() {
        currentLocale.remove();
    }

    /**
     * Creates i18n middleware that sets the locale for each request.
     *
     * @return the middleware
     */
    public static com.osmig.Jweb.framework.middleware.Middleware middleware() {
        return (req, chain) -> {
            try {
                Locale locale = getLocale(req);
                setCurrent(locale);
                req.raw().setAttribute("locale", locale);
                return chain.next();
            } finally {
                clearCurrent();
            }
        };
    }

    /**
     * Shorthand for getting a message with the current locale.
     *
     * @param key the message key
     * @param args optional arguments
     * @return the formatted message
     */
    public static String t(String key, Object... args) {
        return Messages.get(current(), key, args);
    }

    /**
     * Shorthand for getting a message with a specific request's locale.
     *
     * @param request the request
     * @param key the message key
     * @param args optional arguments
     * @return the formatted message
     */
    public static String t(Request request, String key, Object... args) {
        return Messages.get(getLocale(request), key, args);
    }

    /**
     * Parses the Accept-Language header and returns the best matching locale.
     *
     * @param header the Accept-Language header value
     * @return the best matching locale
     */
    public static Locale parseAcceptLanguage(String header) {
        if (header == null || header.isEmpty()) {
            return Messages.getDefaultLocale();
        }

        // Parse header like "en-US,en;q=0.9,es;q=0.8"
        List<Locale.LanguageRange> ranges;
        try {
            ranges = Locale.LanguageRange.parse(header);
        } catch (IllegalArgumentException e) {
            return Messages.getDefaultLocale();
        }

        // Find best match from available locales
        var available = Messages.getAvailableLocales();
        if (available.isEmpty()) {
            return ranges.isEmpty() ? Messages.getDefaultLocale()
                : Locale.forLanguageTag(ranges.getFirst().getRange());
        }

        for (Locale.LanguageRange range : ranges) {
            String tag = range.getRange();
            if (available.contains(tag)) {
                return Locale.forLanguageTag(tag);
            }
            // Try just the language part
            String lang = tag.contains("-") ? tag.split("-")[0] : tag;
            if (available.contains(lang)) {
                return Locale.forLanguageTag(lang);
            }
        }

        return Messages.getDefaultLocale();
    }

    /**
     * Gets a list of supported locales for displaying a language picker.
     *
     * @return list of locale info
     */
    public static List<LocaleInfo> getSupportedLocales() {
        return Messages.getAvailableLocales().stream()
            .map(lang -> {
                Locale locale = Locale.forLanguageTag(lang);
                return new LocaleInfo(
                    lang,
                    locale.getDisplayLanguage(locale),
                    locale.getDisplayLanguage(Locale.ENGLISH)
                );
            })
            .toList();
    }

    /**
     * Information about a supported locale.
     *
     * @param code the language code (e.g., "en", "es")
     * @param nativeName the language name in its own language
     * @param englishName the language name in English
     */
    public record LocaleInfo(String code, String nativeName, String englishName) {}
}
