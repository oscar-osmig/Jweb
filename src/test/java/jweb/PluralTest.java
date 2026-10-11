package jweb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static jweb.Plural.Category.*;
import static org.junit.jupiter.api.Assertions.*;

class PluralTest {

    @AfterEach
    void reset() {
        Messages.clear();
        Messages.setDefaultLocale(Locale.ENGLISH);
    }

    @Test
    void englishLikeLanguagesHaveOneAndOther() {
        assertEquals(OTHER, Plural.of(Locale.ENGLISH, 0));
        assertEquals(ONE, Plural.of(Locale.ENGLISH, 1));
        assertEquals(OTHER, Plural.of(Locale.ENGLISH, 2));
        assertEquals(ONE, Plural.of(Locale.GERMAN, 1));
        assertEquals(OTHER, Plural.of("es", 11));
        assertEquals(ONE, Plural.of("xx-unknown", 1), "unknown languages use the English rule");
        assertEquals(ONE, Plural.of(Locale.ENGLISH, -1), "sign does not matter");
    }

    @Test
    void frenchCountsZeroAsSingular() {
        assertEquals(ONE, Plural.of(Locale.FRENCH, 0));
        assertEquals(ONE, Plural.of(Locale.FRENCH, 1));
        assertEquals(OTHER, Plural.of(Locale.FRENCH, 2));
        assertEquals(ONE, Plural.of("pt-BR", 0));
        assertEquals(OTHER, Plural.of("pt-PT", 0), "European Portuguese does not");
    }

    @Test
    void eastSlavicAndPolishPickByTheLastDigits() {
        Locale ru = Locale.forLanguageTag("ru");
        assertEquals(ONE, Plural.of(ru, 1));
        assertEquals(FEW, Plural.of(ru, 2));
        assertEquals(FEW, Plural.of(ru, 4));
        assertEquals(MANY, Plural.of(ru, 5));
        assertEquals(MANY, Plural.of(ru, 11));
        assertEquals(MANY, Plural.of(ru, 12));
        assertEquals(ONE, Plural.of(ru, 21));
        assertEquals(FEW, Plural.of(ru, 22));
        assertEquals(MANY, Plural.of(ru, 25));
        assertEquals(MANY, Plural.of(ru, 111));
        assertEquals(ONE, Plural.of(ru, 101));

        Locale pl = Locale.forLanguageTag("pl");
        assertEquals(ONE, Plural.of(pl, 1));
        assertEquals(FEW, Plural.of(pl, 2));
        assertEquals(MANY, Plural.of(pl, 5));
        assertEquals(MANY, Plural.of(pl, 12));
        assertEquals(FEW, Plural.of(pl, 22));
        assertEquals(MANY, Plural.of(pl, 21), "Polish 21 is many, unlike Russian");
    }

    @Test
    void czechArabicRomanianHebrewAndNone() {
        assertEquals(ONE, Plural.of("cs", 1));
        assertEquals(FEW, Plural.of("cs", 3));
        assertEquals(OTHER, Plural.of("cs", 5));

        assertEquals(ZERO, Plural.of("ar", 0));
        assertEquals(ONE, Plural.of("ar", 1));
        assertEquals(TWO, Plural.of("ar", 2));
        assertEquals(FEW, Plural.of("ar", 3));
        assertEquals(FEW, Plural.of("ar", 110));
        assertEquals(MANY, Plural.of("ar", 11));
        assertEquals(MANY, Plural.of("ar", 99));
        assertEquals(OTHER, Plural.of("ar", 100));

        assertEquals(ONE, Plural.of("ro", 1));
        assertEquals(FEW, Plural.of("ro", 0));
        assertEquals(FEW, Plural.of("ro", 19));
        assertEquals(OTHER, Plural.of("ro", 20));
        assertEquals(FEW, Plural.of("ro", 119));

        assertEquals(TWO, Plural.of("he", 2));
        assertEquals(OTHER, Plural.of("he", 3));

        assertEquals(OTHER, Plural.of(Locale.JAPANESE, 1));
        assertEquals(OTHER, Plural.of("zh", 1));
    }

    @Test
    void aRuleCanBeRegistered() {
        Plural.register("zz", n -> n == 3 ? FEW : OTHER);
        assertEquals(FEW, Plural.of("zz", 3));
        assertEquals(OTHER, Plural.of("zz", 1));
    }

    @Test
    void messagesPickTheFormAndFallBack() {
        Messages.load("en", Map.of("items.one", "{0} item", "items.other", "{0} items",
                                   "files.one", "{0} file in {1}", "files.other", "{0} files in {1}"));
        Messages.load("ru", Map.of("items.one", "{0} товар", "items.few", "{0} товара", "items.many", "{0} товаров"));
        Messages.load("fr", Map.of("items.other", "{0} articles"));

        assertEquals("1 item", Messages.plural("items", 1));
        assertEquals("5 items", Messages.plural("items", 5));
        assertEquals("0 items", Messages.plural("items", 0));
        assertEquals("2 files in docs", Messages.plural("files", 2, "docs"), "the count is {0}, your args follow");

        assertEquals("1 товар", Messages.plural("ru", "items", 1));
        assertEquals("22 товара", Messages.plural("ru", "items", 22));
        assertEquals("25 товаров", Messages.plural("ru", "items", 25));

        assertEquals("1 articles", Messages.plural("fr", "items", 1), "a missing form falls back to .other");
        assertEquals("missing", Messages.plural("missing", 3), "no entry at all: the key, like get()");

        Messages.load("en", Map.of("plain", "{0} things"));
        assertEquals("4 things", Messages.plural("plain", 4), "a bare key still works");
    }

    @Test
    void theCountIsFormattedForTheLocale() {
        Messages.load("en", Map.of("n.other", "{0} items"));
        Messages.load("de", Map.of("n.other", "{0} Artikel"));
        assertEquals("1,000 items", Messages.plural("en", "n", 1000));
        assertEquals("1.000 Artikel", Messages.plural("de", "n", 1000));
    }

    @Test
    void i18nUsesTheCurrentLocale() {
        Messages.load("en", Map.of("items.one", "{0} item", "items.other", "{0} items"));
        Messages.load("fr", Map.of("items.one", "{0} article", "items.other", "{0} articles"));
        I18n.setCurrent(Locale.FRENCH);
        try {
            assertEquals("0 article", I18n.plural("items", 0));
            assertEquals("3 articles", I18n.plural("items", 3));
        } finally {
            I18n.clearCurrent();
        }
    }
}
