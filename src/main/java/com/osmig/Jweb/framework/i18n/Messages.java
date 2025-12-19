package com.osmig.Jweb.framework.i18n;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internationalization (i18n) message handling.
 *
 * <p>Provides multi-language support with message bundles and interpolation.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Setup
 * Messages.load("en", Map.of(
 *     "greeting", "Hello, {0}!",
 *     "items.count", "You have {0} items"
 * ));
 * Messages.load("es", Map.of(
 *     "greeting", "¡Hola, {0}!",
 *     "items.count", "Tienes {0} artículos"
 * ));
 *
 * // Usage
 * Messages.get("greeting", "World");           // "Hello, World!" (default locale)
 * Messages.get("es", "greeting", "Mundo");     // "¡Hola, Mundo!"
 * }</pre>
 *
 * <h2>With Request Locale</h2>
 * <pre>{@code
 * app.get("/", req -> {
 *     Locale locale = I18n.getLocale(req);
 *     String greeting = Messages.get(locale, "greeting", "User");
 *     return div(text(greeting));
 * });
 * }</pre>
 */
public final class Messages {

    private static final Map<String, Map<String, String>> bundles = new ConcurrentHashMap<>();
    private static Locale defaultLocale = Locale.ENGLISH;
    private static String fallbackLocale = "en";

    private Messages() {}

    /**
     * Sets the default locale.
     *
     * @param locale the default locale
     */
    public static void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
        fallbackLocale = locale.getLanguage();
    }

    /**
     * Gets the default locale.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Loads messages for a locale.
     *
     * @param language the language code (e.g., "en", "es", "fr")
     * @param messages map of message keys to message templates
     */
    public static void load(String language, Map<String, String> messages) {
        bundles.computeIfAbsent(language, k -> new ConcurrentHashMap<>()).putAll(messages);
    }

    /**
     * Loads messages from a properties-style map.
     *
     * @param language the language code
     * @param properties properties with message keys and values
     */
    public static void loadProperties(String language, Properties properties) {
        Map<String, String> messages = new HashMap<>();
        properties.forEach((k, v) -> messages.put(k.toString(), v.toString()));
        load(language, messages);
    }

    /**
     * Clears all loaded messages.
     */
    public static void clear() {
        bundles.clear();
    }

    /**
     * Gets a message using the default locale.
     *
     * @param key the message key
     * @param args optional arguments for interpolation
     * @return the formatted message, or the key if not found
     */
    public static String get(String key, Object... args) {
        return get(defaultLocale, key, args);
    }

    /**
     * Gets a message for a specific language.
     *
     * @param language the language code
     * @param key the message key
     * @param args optional arguments for interpolation
     * @return the formatted message
     */
    public static String get(String language, String key, Object... args) {
        return get(Locale.forLanguageTag(language), key, args);
    }

    /**
     * Gets a message for a specific locale.
     *
     * @param locale the locale
     * @param key the message key
     * @param args optional arguments for interpolation
     * @return the formatted message
     */
    public static String get(Locale locale, String key, Object... args) {
        String template = findMessage(locale, key);
        if (template == null) {
            return key; // Return key as fallback
        }
        if (args.length == 0) {
            return template;
        }
        return MessageFormat.format(template, args);
    }

    /**
     * Gets a message or returns a default value.
     *
     * @param key the message key
     * @param defaultValue value to return if key not found
     * @param args optional arguments for interpolation
     * @return the formatted message or default value
     */
    public static String getOrDefault(String key, String defaultValue, Object... args) {
        return getOrDefault(defaultLocale, key, defaultValue, args);
    }

    /**
     * Gets a message or returns a default value.
     *
     * @param locale the locale
     * @param key the message key
     * @param defaultValue value to return if key not found
     * @param args optional arguments for interpolation
     * @return the formatted message or default value
     */
    public static String getOrDefault(Locale locale, String key, String defaultValue, Object... args) {
        String template = findMessage(locale, key);
        if (template == null) {
            template = defaultValue;
        }
        if (args.length == 0) {
            return template;
        }
        return MessageFormat.format(template, args);
    }

    /**
     * Checks if a message exists for the default locale.
     *
     * @param key the message key
     * @return true if the message exists
     */
    public static boolean has(String key) {
        return has(defaultLocale, key);
    }

    /**
     * Checks if a message exists for a locale.
     *
     * @param locale the locale
     * @param key the message key
     * @return true if the message exists
     */
    public static boolean has(Locale locale, String key) {
        return findMessage(locale, key) != null;
    }

    /**
     * Gets all available locales.
     *
     * @return set of language codes
     */
    public static Set<String> getAvailableLocales() {
        return Collections.unmodifiableSet(bundles.keySet());
    }

    private static String findMessage(Locale locale, String key) {
        // Try exact locale (e.g., "en-US")
        String language = locale.toLanguageTag();
        Map<String, String> bundle = bundles.get(language);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }

        // Try language only (e.g., "en")
        language = locale.getLanguage();
        bundle = bundles.get(language);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }

        // Try fallback locale
        bundle = bundles.get(fallbackLocale);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }

        return null;
    }
}
