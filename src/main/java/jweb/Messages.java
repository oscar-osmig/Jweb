package jweb;

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
public class Messages {

    private static final Map<String, Map<String, String>> bundles = new ConcurrentHashMap<>();
    private static Locale defaultLocale = Locale.ENGLISH;
    private static String fallbackLocale = "en";

    protected Messages() {}

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
     * A message in the plural form the count takes, using the default locale.
     *
     * <p>The bundle holds one entry per form — {@code items.one=1 item},
     * {@code items.other={0} items}, and {@code .few} / {@code .many} /
     * {@code .zero} / {@code .two} where the language has them
     * ({@link Plural}). {@code {0}} is the count; your own arguments follow as
     * {@code {1}}, {@code {2}}… A form the bundle lacks falls back to
     * {@code .other}, then to the bare key.</p>
     *
     * @param key   the message key without a suffix
     * @param count the number the message is about
     * @param args  further arguments, numbered from {@code {1}}
     */
    public static String plural(String key, long count, Object... args) {
        return plural(defaultLocale, key, count, args);
    }

    /** {@link #plural(String, long, Object...)} for a language code. */
    public static String plural(String language, String key, long count, Object... args) {
        return plural(Locale.forLanguageTag(language), key, count, args);
    }

    /** {@link #plural(String, long, Object...)} for a locale. */
    public static String plural(Locale locale, String key, long count, Object... args) {
        Plural.Category category = Plural.of(locale, count);
        // The locale's own bundles first, across every form it might carry,
        // before the fallback locale: a French ".other" beats an English ".one"
        String template = findForm(locale, key, category, false);
        if (template == null) {
            template = findForm(locale, key, category, true);
        }
        if (template == null) {
            return key;
        }
        Object[] all = new Object[args.length + 1];
        all[0] = count;
        System.arraycopy(args, 0, all, 1, args.length);
        // Locale-aware: a Russian "1 000" and an English "1,000" from the same {0}
        return new MessageFormat(template, locale == null ? Locale.ROOT : locale).format(all);
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
        String found = findIn(locale, key);
        return found != null ? found : findInFallback(key);
    }

    /** The form for a category, then {@code .other}, then the bare key — in the locale's own bundles or the fallback's. */
    private static String findForm(Locale locale, String key, Plural.Category category, boolean fallback) {
        String[] candidates = category == Plural.Category.OTHER
            ? new String[] {key + ".other", key}
            : new String[] {key + "." + category.key(), key + ".other", key};
        for (String candidate : candidates) {
            String found = fallback ? findInFallback(candidate) : findIn(locale, candidate);
            if (found != null) return found;
        }
        return null;
    }

    /** Exact tag ("en-US") then language ("en") — the locale's own bundles only. */
    private static String findIn(Locale locale, String key) {
        if (locale == null) return null;
        Map<String, String> bundle = bundles.get(locale.toLanguageTag());
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }
        bundle = bundles.get(locale.getLanguage());
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }
        return null;
    }

    private static String findInFallback(String key) {
        Map<String, String> bundle = bundles.get(fallbackLocale);
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.get(key);
        }
        return null;
    }
}
