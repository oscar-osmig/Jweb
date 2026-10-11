package jweb;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which plural form a count takes in a language — the CLDR cardinal
 * categories, for whole numbers.
 *
 * <p>English has two forms ({@code 1 item}, {@code 2 items}); French puts 0
 * with 1; Russian and Polish have three forms chosen by the last digits;
 * Arabic has six; Japanese has one. {@link Messages#plural(String, long, Object...)}
 * looks the category up here and reads {@code key.one}, {@code key.few},
 * {@code key.other}… from the bundle, so a message file says
 * {@code items.one=1 item} / {@code items.other={0} items} and the right one
 * is picked per locale.</p>
 *
 * <pre>
 * Plural.of(Locale.ENGLISH, 1)   // ONE
 * Plural.of(Locale.ENGLISH, 2)   // OTHER
 * Plural.of(Locale.FRENCH, 0)    // ONE   (0 and 1 are singular in French)
 * Plural.of(Locale.forLanguageTag("ru"), 22)   // FEW
 * </pre>
 *
 * <p>Languages not in the table use the English rule (one / other).
 * {@link #register(String, Rule)} adds or overrides one.</p>
 */
public final class Plural {

    private Plural() {}

    /** The CLDR cardinal categories. {@link #key()} is the message-key suffix. */
    public enum Category {
        ZERO, ONE, TWO, FEW, MANY, OTHER;

        /** {@code "one"}, {@code "few"}… — the suffix after the message key. */
        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** A language's rule: the category of a whole-number count. */
    @FunctionalInterface
    public interface Rule {
        Category of(long n);
    }

    /** one / other — English, German, Dutch, Spanish, Italian, the Nordic languages, Greek, Turkish… */
    public static final Rule ONE_OTHER = n -> n == 1 ? Category.ONE : Category.OTHER;

    /** 0 and 1 are "one" — French, Brazilian Portuguese, Hindi, Bengali, Persian, Armenian. */
    public static final Rule ZERO_ONE = n -> n == 0 || n == 1 ? Category.ONE : Category.OTHER;

    /** No plural forms — Japanese, Chinese, Korean, Vietnamese, Thai, Indonesian, Malay. */
    public static final Rule NONE = n -> Category.OTHER;

    /** Russian, Ukrainian, Belarusian: one / few / many by the last two digits. */
    public static final Rule EAST_SLAVIC = n -> {
        long mod10 = n % 10, mod100 = n % 100;
        if (mod10 == 1 && mod100 != 11) return Category.ONE;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return Category.FEW;
        return Category.MANY;
    };

    /** Polish: one is exactly 1; few / many by the last two digits. */
    public static final Rule POLISH = n -> {
        if (n == 1) return Category.ONE;
        long mod10 = n % 10, mod100 = n % 100;
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return Category.FEW;
        return Category.MANY;
    };

    /** Czech, Slovak: one (1), few (2–4), other. */
    public static final Rule CZECH = n -> n == 1 ? Category.ONE : n >= 2 && n <= 4 ? Category.FEW : Category.OTHER;

    /** Arabic: zero, one, two, few (3–10), many (11–99), other. */
    public static final Rule ARABIC = n -> {
        if (n == 0) return Category.ZERO;
        if (n == 1) return Category.ONE;
        if (n == 2) return Category.TWO;
        long mod100 = n % 100;
        if (mod100 >= 3 && mod100 <= 10) return Category.FEW;
        if (mod100 >= 11 && mod100 <= 99) return Category.MANY;
        return Category.OTHER;
    };

    /** Romanian: one (1), few (0, or 2–19 by the last two digits), other. */
    public static final Rule ROMANIAN = n -> {
        if (n == 1) return Category.ONE;
        long mod100 = n % 100;
        if (n == 0 || (mod100 >= 2 && mod100 <= 19)) return Category.FEW;
        return Category.OTHER;
    };

    /** Hebrew: one, two, other. */
    public static final Rule HEBREW = n -> n == 1 ? Category.ONE : n == 2 ? Category.TWO : Category.OTHER;

    private static final Map<String, Rule> RULES = new ConcurrentHashMap<>();

    static {
        for (String l : new String[] {"fr", "pt", "hi", "bn", "fa", "hy"}) RULES.put(l, ZERO_ONE);
        RULES.put("pt-PT", ONE_OTHER);
        for (String l : new String[] {"ja", "zh", "ko", "vi", "th", "id", "ms", "km", "lo", "my"}) RULES.put(l, NONE);
        for (String l : new String[] {"ru", "uk", "be"}) RULES.put(l, EAST_SLAVIC);
        RULES.put("pl", POLISH);
        RULES.put("cs", CZECH);
        RULES.put("sk", CZECH);
        RULES.put("ar", ARABIC);
        RULES.put("ro", ROMANIAN);
        RULES.put("he", HEBREW);
        RULES.put("iw", HEBREW);
    }

    /** The category of {@code n} in this locale (full tag first, then the language, then one / other). */
    public static Category of(Locale locale, long n) {
        return rule(locale).of(Math.abs(n));
    }

    /** The category of {@code n} for a language tag such as {@code "ru"} or {@code "pt-BR"}. */
    public static Category of(String languageTag, long n) {
        return of(Locale.forLanguageTag(languageTag), n);
    }

    /** The rule a locale resolves to. */
    public static Rule rule(Locale locale) {
        if (locale == null) return ONE_OTHER;
        Rule rule = RULES.get(locale.toLanguageTag());
        if (rule == null) rule = RULES.get(locale.getLanguage());
        return rule == null ? ONE_OTHER : rule;
    }

    /** Adds or replaces the rule for a language ({@code "sl"}) or a full tag ({@code "pt-PT"}). */
    public static void register(String languageTag, Rule rule) {
        RULES.put(languageTag, rule);
    }
}
