package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * Regular expression DSL for JavaScript.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSRegex.*;
 *
 * regex("\\d+").test(variable("input"))         // /\d+/.test(input)
 * regex("hello", "i").match(variable("str"))    // /hello/i.match(str)
 * variable("email").matchesEmail()              // email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
 * </pre>
 */
public final class JSRegex {
    private JSRegex() {}

    /** Creates a regex: regex("pattern") -> /pattern/ */
    public static Regex regex(String pattern) {
        return new Regex(pattern, "");
    }

    /** Creates a regex with flags: regex("pattern", "gi") -> /pattern/gi */
    public static Regex regex(String pattern, String flags) {
        return new Regex(pattern, flags);
    }

    /** Creates a regex from a Val expression (dynamic pattern) */
    public static Val regexFromString(Val pattern) {
        return new Val("new RegExp(" + pattern.js() + ")");
    }

    /** Creates a regex from string with flags */
    public static Val regexFromString(Val pattern, String flags) {
        return new Val("new RegExp(" + pattern.js() + ",'" + flags + "')");
    }

    // Common pre-built patterns
    public static Regex EMAIL = regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static Regex PHONE = regex("^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$");
    public static Regex URL = regex("^https?://[^\\s]+$");
    public static Regex DIGITS = regex("^\\d+$");
    public static Regex ALPHA = regex("^[a-zA-Z]+$");
    public static Regex ALPHANUMERIC = regex("^[a-zA-Z0-9]+$");
    public static Regex SLUG = regex("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    public static Regex UUID = regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", "i");

    public static class Regex {
        private final String pattern;
        private final String flags;

        Regex(String pattern, String flags) {
            this.pattern = pattern;
            this.flags = flags;
        }

        private String toJs() {
            return "/" + pattern + "/" + flags;
        }

        /** Tests if string matches: /pattern/.test(str) */
        public Val test(Val str) {
            return new Val(toJs() + ".test(" + str.js() + ")");
        }

        /** Executes regex and returns match: /pattern/.exec(str) */
        public Val exec(Val str) {
            return new Val(toJs() + ".exec(" + str.js() + ")");
        }

        /** Returns all matches: str.match(/pattern/) */
        public Val match(Val str) {
            return new Val(str.js() + ".match(" + toJs() + ")");
        }

        /** Replaces matches: str.replace(/pattern/, replacement) */
        public Val replace(Val str, String replacement) {
            return new Val(str.js() + ".replace(" + toJs() + ",'" + JS.esc(replacement) + "')");
        }

        /** Replaces matches with expression: str.replace(/pattern/, expr) */
        public Val replace(Val str, Val replacement) {
            return new Val(str.js() + ".replace(" + toJs() + "," + replacement.js() + ")");
        }

        /** Splits by pattern: str.split(/pattern/) */
        public Val split(Val str) {
            return new Val(str.js() + ".split(" + toJs() + ")");
        }

        /** Returns regex as Val for use in expressions */
        public Val toVal() {
            return new Val(toJs());
        }

        /** Returns all matches as iterator: str.matchAll(/pattern/g) */
        public Val matchAll(Val str) {
            String flagsWithG = flags.contains("g") ? flags : flags + "g";
            return new Val("[..." + str.js() + ".matchAll(/" + pattern + "/" + flagsWithG + ")]");
        }

        /** Replaces all matches: str.replaceAll(/pattern/g, replacement) */
        public Val replaceAll(Val str, String replacement) {
            String flagsWithG = flags.contains("g") ? flags : flags + "g";
            return new Val(str.js() + ".replaceAll(/" + pattern + "/" + flagsWithG + ",'" + JS.esc(replacement) + "')");
        }

        /** Replaces all with expression */
        public Val replaceAll(Val str, Val replacement) {
            String flagsWithG = flags.contains("g") ? flags : flags + "g";
            return new Val(str.js() + ".replaceAll(/" + pattern + "/" + flagsWithG + "," + replacement.js() + ")");
        }

        /** Searches for pattern position: str.search(/pattern/) */
        public Val search(Val str) {
            return new Val(str.js() + ".search(" + toJs() + ")");
        }

        /** Gets global flag */
        public Val global() { return new Val(toJs() + ".global"); }

        /** Gets ignoreCase flag */
        public Val ignoreCase() { return new Val(toJs() + ".ignoreCase"); }

        /** Gets multiline flag */
        public Val multiline() { return new Val(toJs() + ".multiline"); }

        /** Gets source pattern */
        public Val source() { return new Val(toJs() + ".source"); }

        /** Gets flags string */
        public Val flagsVal() { return new Val(toJs() + ".flags"); }

        /** Gets lastIndex (for stateful regexes) */
        public Val lastIndex() { return new Val(toJs() + ".lastIndex"); }
    }

    // ==================== Regex with Named Groups ====================

    /** Creates regex with named capture groups */
    public static NamedRegex namedRegex(String pattern, String flags) {
        return new NamedRegex(pattern, flags);
    }

    /** Creates regex with named capture groups */
    public static NamedRegex namedRegex(String pattern) {
        return new NamedRegex(pattern, "");
    }

    public static class NamedRegex {
        private final String pattern;
        private final String flags;

        NamedRegex(String pattern, String flags) {
            this.pattern = pattern;
            this.flags = flags;
        }

        private String toJs() { return "/" + pattern + "/" + flags; }

        /** Executes and returns match with groups */
        public Val exec(Val str) {
            return new Val(toJs() + ".exec(" + str.js() + ")");
        }

        /** Gets named group from match result */
        public Val group(Val match, String name) {
            return new Val(match.js() + ".groups." + name);
        }

        /** Gets all named groups from match result */
        public Val groups(Val match) {
            return new Val(match.js() + ".groups");
        }

        /** Executes and extracts specific group */
        public Val extractGroup(Val str, String groupName) {
            return new Val("((" + toJs() + ".exec(" + str.js() + "))||{groups:{}}).groups." + groupName);
        }

        /** Executes and extracts all groups as object */
        public Val extractGroups(Val str) {
            return new Val("((" + toJs() + ".exec(" + str.js() + "))||{groups:{}}).groups");
        }

        public Val toVal() { return new Val(toJs()); }
    }

    // ==================== Advanced Regex Patterns ====================

    // Lookahead and lookbehind patterns
    public static Regex lookahead(String pattern, String ahead) {
        return regex(pattern + "(?=" + ahead + ")");
    }

    public static Regex negativeLookahead(String pattern, String ahead) {
        return regex(pattern + "(?!" + ahead + ")");
    }

    public static Regex lookbehind(String behind, String pattern) {
        return regex("(?<=" + behind + ")" + pattern);
    }

    public static Regex negativeLookbehind(String behind, String pattern) {
        return regex("(?<!" + behind + ")" + pattern);
    }

    // ==================== Additional Pre-built Patterns ====================

    public static Regex HEX_COLOR = regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    public static Regex IPV4 = regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
    public static Regex IPV6 = regex("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    public static Regex DATE_ISO = regex("^\\d{4}-\\d{2}-\\d{2}$");
    public static Regex TIME_24H = regex("^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$");
    public static Regex CREDIT_CARD = regex("^\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}$");
    public static Regex USERNAME = regex("^[a-zA-Z][a-zA-Z0-9_-]{2,19}$");
    public static Regex PASSWORD_STRONG = regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    public static Regex WHITESPACE = regex("\\s+", "g");
    public static Regex NON_ALPHANUMERIC = regex("[^a-zA-Z0-9]", "g");

    // ==================== Regex Utilities ====================

    /** Escapes special regex characters in a string */
    public static Val escapeRegex(Val str) {
        return new Val(str.js() + ".replace(/[.*+?^${}()|[\\]\\\\]/g,'\\\\$&')");
    }

    /** Creates regex from escaped string (safe for user input) */
    public static Val safeRegex(Val userInput) {
        return new Val("new RegExp(" + userInput.js() + ".replace(/[.*+?^${}()|[\\]\\\\]/g,'\\\\$&'))");
    }

    /** Tests if string is valid regex */
    public static Val isValidRegex(Val pattern) {
        return new Val("(function(){try{new RegExp(" + pattern.js() + ");return true}catch(e){return false}}())");
    }

    /** Counts matches in string */
    public static Val countMatches(Val str, Regex regex) {
        String flagsWithG = regex.flags.contains("g") ? regex.flags : regex.flags + "g";
        return new Val("(" + str.js() + ".match(/" + regex.pattern + "/" + flagsWithG + ")||[]).length");
    }
}
