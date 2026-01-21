package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * Internationalization APIs: number, date, time, currency formatting.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSIntl.*;
 *
 * formatNumber(variable("price"), "en-US")           // 1,234.56
 * formatCurrency(variable("price"), "USD", "en-US")  // $1,234.56
 * formatDate(variable("date"), "en-US")              // 1/20/2026
 * formatRelativeTime(-5, "minutes", "en-US")         // 5 minutes ago
 * </pre>
 */
public final class JSIntl {
    private JSIntl() {}

    // ==================== Number Formatting ====================

    /** Formats a number: new Intl.NumberFormat(locale).format(num) */
    public static Val formatNumber(Val num, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "').format(" + num.js() + ")");
    }

    /** Formats with options */
    public static Val formatNumber(Val num, String locale, String style) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{style:'" + style + "'}).format(" + num.js() + ")");
    }

    /** Formats as currency */
    public static Val formatCurrency(Val num, String currency, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) +
            "',{style:'currency',currency:'" + JS.esc(currency) + "'}).format(" + num.js() + ")");
    }

    /** Formats as percentage */
    public static Val formatPercent(Val num, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{style:'percent'}).format(" + num.js() + ")");
    }

    /** Formats with min/max fraction digits */
    public static Val formatNumber(Val num, String locale, int minFrac, int maxFrac) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) +
            "',{minimumFractionDigits:" + minFrac + ",maximumFractionDigits:" + maxFrac + "}).format(" + num.js() + ")");
    }

    /** Compact notation: 1K, 1M, etc. */
    public static Val formatCompact(Val num, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{notation:'compact'}).format(" + num.js() + ")");
    }

    // ==================== Date/Time Formatting ====================

    /** Formats a date */
    public static Val formatDate(Val date, String locale) {
        return new Val("new Intl.DateTimeFormat('" + JS.esc(locale) + "').format(" + date.js() + ")");
    }

    /** Formats with style: 'full', 'long', 'medium', 'short' */
    public static Val formatDate(Val date, String locale, String dateStyle) {
        return new Val("new Intl.DateTimeFormat('" + JS.esc(locale) +
            "',{dateStyle:'" + dateStyle + "'}).format(" + date.js() + ")");
    }

    /** Formats date and time */
    public static Val formatDateTime(Val date, String locale, String dateStyle, String timeStyle) {
        return new Val("new Intl.DateTimeFormat('" + JS.esc(locale) +
            "',{dateStyle:'" + dateStyle + "',timeStyle:'" + timeStyle + "'}).format(" + date.js() + ")");
    }

    /** Formats time only */
    public static Val formatTime(Val date, String locale, String timeStyle) {
        return new Val("new Intl.DateTimeFormat('" + JS.esc(locale) +
            "',{timeStyle:'" + timeStyle + "'}).format(" + date.js() + ")");
    }

    // ==================== Relative Time ====================

    /** Formats relative time: "5 minutes ago", "in 2 days" */
    public static Val formatRelativeTime(int value, String unit, String locale) {
        return new Val("new Intl.RelativeTimeFormat('" + JS.esc(locale) + "').format(" + value + ",'" + unit + "')");
    }

    /** Formats relative time with expression */
    public static Val formatRelativeTime(Val value, String unit, String locale) {
        return new Val("new Intl.RelativeTimeFormat('" + JS.esc(locale) + "').format(" + value.js() + ",'" + unit + "')");
    }

    /** Formats relative time with style: 'long', 'short', 'narrow' */
    public static Val formatRelativeTime(Val value, String unit, String locale, String style) {
        return new Val("new Intl.RelativeTimeFormat('" + JS.esc(locale) +
            "',{style:'" + style + "'}).format(" + value.js() + ",'" + unit + "')");
    }

    // ==================== List Formatting ====================

    /** Formats a list: "A, B, and C" */
    public static Val formatList(Val arr, String locale) {
        return new Val("new Intl.ListFormat('" + JS.esc(locale) + "').format(" + arr.js() + ")");
    }

    /** Formats list with type: 'conjunction', 'disjunction', 'unit' */
    public static Val formatList(Val arr, String locale, String type) {
        return new Val("new Intl.ListFormat('" + JS.esc(locale) +
            "',{type:'" + type + "'}).format(" + arr.js() + ")");
    }

    // ==================== Plural Rules ====================

    /** Gets plural category: 'zero', 'one', 'two', 'few', 'many', 'other' */
    public static Val pluralSelect(Val num, String locale) {
        return new Val("new Intl.PluralRules('" + JS.esc(locale) + "').select(" + num.js() + ")");
    }

    // ==================== Browser Locale ====================

    /** Gets browser's preferred locale */
    public static Val browserLocale() {
        return new Val("navigator.language||'en-US'");
    }

    /** Gets all browser locales */
    public static Val browserLocales() {
        return new Val("navigator.languages||['en-US']");
    }

    // ==================== Display Names ====================

    /** Gets display name for a language code */
    public static Val languageName(String code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'language'}).of('" + JS.esc(code) + "')");
    }

    /** Gets display name for a language code (dynamic) */
    public static Val languageName(Val code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'language'}).of(" + code.js() + ")");
    }

    /** Gets display name for a region code */
    public static Val regionName(String code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'region'}).of('" + JS.esc(code) + "')");
    }

    /** Gets display name for a region code (dynamic) */
    public static Val regionName(Val code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'region'}).of(" + code.js() + ")");
    }

    /** Gets display name for a script code */
    public static Val scriptName(String code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'script'}).of('" + JS.esc(code) + "')");
    }

    /** Gets display name for a currency code */
    public static Val currencyName(String code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'currency'}).of('" + JS.esc(code) + "')");
    }

    /** Gets display name for a currency code (dynamic) */
    public static Val currencyName(Val code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'currency'}).of(" + code.js() + ")");
    }

    /** Gets display name for a calendar */
    public static Val calendarName(String code, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'calendar'}).of('" + JS.esc(code) + "')");
    }

    /** Gets display name for a date time field */
    public static Val dateTimeFieldName(String field, String locale) {
        return new Val("new Intl.DisplayNames('" + JS.esc(locale) + "',{type:'dateTimeField'}).of('" + JS.esc(field) + "')");
    }

    // ==================== Collator (Sorting/Comparison) ====================

    /** Creates a collator for locale-aware string comparison */
    public static Val collator(String locale) {
        return new Val("new Intl.Collator('" + JS.esc(locale) + "')");
    }

    /** Compares two strings using locale rules */
    public static Val localeCompare(Val a, Val b, String locale) {
        return new Val("new Intl.Collator('" + JS.esc(locale) + "').compare(" + a.js() + "," + b.js() + ")");
    }

    /** Compares with sensitivity: 'base', 'accent', 'case', 'variant' */
    public static Val localeCompare(Val a, Val b, String locale, String sensitivity) {
        return new Val("new Intl.Collator('" + JS.esc(locale) + "',{sensitivity:'" + sensitivity + "'}).compare(" + a.js() + "," + b.js() + ")");
    }

    /** Creates comparator function for sorting */
    public static Val comparator(String locale) {
        return new Val("new Intl.Collator('" + JS.esc(locale) + "').compare");
    }

    /** Creates numeric-aware comparator */
    public static Val numericComparator(String locale) {
        return new Val("new Intl.Collator('" + JS.esc(locale) + "',{numeric:true}).compare");
    }

    /** Sorts array with locale-aware comparison */
    public static Val localeSortedArray(Val arr, String locale) {
        return new Val(arr.js() + ".sort(new Intl.Collator('" + JS.esc(locale) + "').compare)");
    }

    // ==================== Unit Formatting ====================

    /** Formats a unit value */
    public static Val formatUnit(Val value, String unit, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{style:'unit',unit:'" + JS.esc(unit) + "'}).format(" + value.js() + ")");
    }

    /** Formats a unit with display style: 'long', 'short', 'narrow' */
    public static Val formatUnit(Val value, String unit, String locale, String unitDisplay) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{style:'unit',unit:'" + JS.esc(unit) + "',unitDisplay:'" + unitDisplay + "'}).format(" + value.js() + ")");
    }

    // Common unit shortcuts
    public static Val formatKilometers(Val value, String locale) { return formatUnit(value, "kilometer", locale); }
    public static Val formatMiles(Val value, String locale) { return formatUnit(value, "mile", locale); }
    public static Val formatMeters(Val value, String locale) { return formatUnit(value, "meter", locale); }
    public static Val formatFeet(Val value, String locale) { return formatUnit(value, "foot", locale); }
    public static Val formatKilograms(Val value, String locale) { return formatUnit(value, "kilogram", locale); }
    public static Val formatPounds(Val value, String locale) { return formatUnit(value, "pound", locale); }
    public static Val formatLiters(Val value, String locale) { return formatUnit(value, "liter", locale); }
    public static Val formatGallons(Val value, String locale) { return formatUnit(value, "gallon", locale); }
    public static Val formatCelsius(Val value, String locale) { return formatUnit(value, "celsius", locale); }
    public static Val formatFahrenheit(Val value, String locale) { return formatUnit(value, "fahrenheit", locale); }
    public static Val formatBytes(Val value, String locale) { return formatUnit(value, "byte", locale); }
    public static Val formatKilobytes(Val value, String locale) { return formatUnit(value, "kilobyte", locale); }
    public static Val formatMegabytes(Val value, String locale) { return formatUnit(value, "megabyte", locale); }
    public static Val formatGigabytes(Val value, String locale) { return formatUnit(value, "gigabyte", locale); }

    // ==================== Currency Display Options ====================

    /** Formats currency with display option: 'symbol', 'narrowSymbol', 'code', 'name' */
    public static Val formatCurrency(Val num, String currency, String locale, String currencyDisplay) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "',{style:'currency',currency:'" + JS.esc(currency) + "',currencyDisplay:'" + currencyDisplay + "'}).format(" + num.js() + ")");
    }

    // ==================== Format To Parts ====================

    /** Formats number to parts array */
    public static Val formatNumberToParts(Val num, String locale) {
        return new Val("new Intl.NumberFormat('" + JS.esc(locale) + "').formatToParts(" + num.js() + ")");
    }

    /** Formats date to parts array */
    public static Val formatDateToParts(Val date, String locale) {
        return new Val("new Intl.DateTimeFormat('" + JS.esc(locale) + "').formatToParts(" + date.js() + ")");
    }

    // ==================== Segmenter (Text Segmentation) ====================

    /** Segments text into graphemes */
    public static Val segmentGraphemes(Val text, String locale) {
        return new Val("[...new Intl.Segmenter('" + JS.esc(locale) + "',{granularity:'grapheme'}).segment(" + text.js() + ")].map(s=>s.segment)");
    }

    /** Segments text into words */
    public static Val segmentWords(Val text, String locale) {
        return new Val("[...new Intl.Segmenter('" + JS.esc(locale) + "',{granularity:'word'}).segment(" + text.js() + ")].filter(s=>s.isWordLike).map(s=>s.segment)");
    }

    /** Segments text into sentences */
    public static Val segmentSentences(Val text, String locale) {
        return new Val("[...new Intl.Segmenter('" + JS.esc(locale) + "',{granularity:'sentence'}).segment(" + text.js() + ")].map(s=>s.segment)");
    }

    /** Gets grapheme count (proper character count for emoji, etc.) */
    public static Val graphemeCount(Val text, String locale) {
        return new Val("[...new Intl.Segmenter('" + JS.esc(locale) + "',{granularity:'grapheme'}).segment(" + text.js() + ")].length");
    }

    // ==================== Locale Negotiation ====================

    /** Gets best matching locale from supported list */
    public static Val matchLocale(Val requested, Val available) {
        return new Val("Intl.getCanonicalLocales(" + requested.js() + ").find(l=>" + available.js() + ".includes(l))||" + available.js() + "[0]");
    }

    /** Gets canonical locale */
    public static Val canonicalLocale(String locale) {
        return new Val("Intl.getCanonicalLocales('" + JS.esc(locale) + "')[0]");
    }

    /** Gets canonical locales from array */
    public static Val canonicalLocales(Val locales) {
        return new Val("Intl.getCanonicalLocales(" + locales.js() + ")");
    }
}
