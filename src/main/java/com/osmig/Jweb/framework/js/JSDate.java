package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * Date creation and manipulation API.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSDate.*;
 *
 * // Current date/time
 * now()                          // new Date()
 * timestamp()                    // Date.now()
 *
 * // Create dates
 * date("2024-01-15")            // new Date('2024-01-15')
 * date(2024, 0, 15)             // new Date(2024, 0, 15) - month is 0-indexed
 *
 * // Manipulation
 * addDays(variable("date"), 7)  // Adds 7 days
 * startOfDay(variable("date"))  // Sets to midnight
 * </pre>
 */
public final class JSDate {
    private JSDate() {}

    // ==================== Creation ====================

    /** Current date/time: new Date() */
    public static Val now() {
        return new Val("new Date()");
    }

    /** Current timestamp in milliseconds: Date.now() */
    public static Val timestamp() {
        return new Val("Date.now()");
    }

    /** Parse date string: new Date(string) */
    public static Val date(String dateString) {
        return new Val("new Date('" + JS.esc(dateString) + "')");
    }

    /** Create from expression: new Date(expr) */
    public static Val date(Val expr) {
        return new Val("new Date(" + expr.js() + ")");
    }

    /** Create from timestamp: new Date(ms) */
    public static Val fromTimestamp(Val ms) {
        return new Val("new Date(" + ms.js() + ")");
    }

    /** Create from components: new Date(year, month, day) - month is 0-indexed */
    public static Val date(int year, int month, int day) {
        return new Val("new Date(" + year + "," + month + "," + day + ")");
    }

    /** Create from components with time */
    public static Val date(int year, int month, int day, int hours, int minutes) {
        return new Val("new Date(" + year + "," + month + "," + day + "," + hours + "," + minutes + ")");
    }

    // ==================== Getters ====================

    public static Val getFullYear(Val d) { return new Val(d.js() + ".getFullYear()"); }
    public static Val getMonth(Val d) { return new Val(d.js() + ".getMonth()"); }
    public static Val getDate(Val d) { return new Val(d.js() + ".getDate()"); }
    public static Val getDay(Val d) { return new Val(d.js() + ".getDay()"); }
    public static Val getHours(Val d) { return new Val(d.js() + ".getHours()"); }
    public static Val getMinutes(Val d) { return new Val(d.js() + ".getMinutes()"); }
    public static Val getSeconds(Val d) { return new Val(d.js() + ".getSeconds()"); }
    public static Val getMilliseconds(Val d) { return new Val(d.js() + ".getMilliseconds()"); }
    public static Val getTime(Val d) { return new Val(d.js() + ".getTime()"); }

    // ==================== Setters (return new Date) ====================

    public static Val setFullYear(Val d, Val year) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setFullYear(" + year.js() + ");return n}())");
    }

    public static Val setMonth(Val d, Val month) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setMonth(" + month.js() + ");return n}())");
    }

    public static Val setDate(Val d, Val day) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setDate(" + day.js() + ");return n}())");
    }

    public static Val setHours(Val d, Val hours) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setHours(" + hours.js() + ");return n}())");
    }

    // ==================== Manipulation ====================

    /** Adds days to date (returns new Date) */
    public static Val addDays(Val d, int days) {
        return new Val("new Date(" + d.js() + ".getTime()+" + (days * 86400000L) + ")");
    }

    public static Val addDays(Val d, Val days) {
        return new Val("new Date(" + d.js() + ".getTime()+" + days.js() + "*86400000)");
    }

    /** Adds hours to date */
    public static Val addHours(Val d, int hours) {
        return new Val("new Date(" + d.js() + ".getTime()+" + (hours * 3600000L) + ")");
    }

    /** Adds minutes to date */
    public static Val addMinutes(Val d, int minutes) {
        return new Val("new Date(" + d.js() + ".getTime()+" + (minutes * 60000L) + ")");
    }

    /** Adds months to date */
    public static Val addMonths(Val d, int months) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setMonth(n.getMonth()+" + months + ");return n}())");
    }

    /** Adds years to date */
    public static Val addYears(Val d, int years) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setFullYear(n.getFullYear()+" + years + ");return n}())");
    }

    // ==================== Start/End ====================

    /** Start of day (midnight) */
    public static Val startOfDay(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear()," + d.js() + ".getMonth()," + d.js() + ".getDate())");
    }

    /** End of day (23:59:59.999) */
    public static Val endOfDay(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear()," + d.js() + ".getMonth()," + d.js() + ".getDate(),23,59,59,999)");
    }

    /** Start of month */
    public static Val startOfMonth(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear()," + d.js() + ".getMonth(),1)");
    }

    /** End of month */
    public static Val endOfMonth(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear()," + d.js() + ".getMonth()+1,0,23,59,59,999)");
    }

    // ==================== Comparison ====================

    /** Checks if date is before another */
    public static Val isBefore(Val d1, Val d2) {
        return new Val("(" + d1.js() + ".getTime()<" + d2.js() + ".getTime())");
    }

    /** Checks if date is after another */
    public static Val isAfter(Val d1, Val d2) {
        return new Val("(" + d1.js() + ".getTime()>" + d2.js() + ".getTime())");
    }

    /** Checks if same day */
    public static Val isSameDay(Val d1, Val d2) {
        return new Val("(" + d1.js() + ".toDateString()===" + d2.js() + ".toDateString())");
    }

    /** Difference in days */
    public static Val diffDays(Val d1, Val d2) {
        return new Val("Math.floor((" + d1.js() + ".getTime()-" + d2.js() + ".getTime())/86400000)");
    }

    // ==================== Formatting (simple) ====================

    /** ISO string: date.toISOString() */
    public static Val toISOString(Val d) { return new Val(d.js() + ".toISOString()"); }

    /** Date string: date.toDateString() */
    public static Val toDateString(Val d) { return new Val(d.js() + ".toDateString()"); }

    /** Time string: date.toTimeString() */
    public static Val toTimeString(Val d) { return new Val(d.js() + ".toTimeString()"); }

    /** Locale date string */
    public static Val toLocaleDateString(Val d) { return new Val(d.js() + ".toLocaleDateString()"); }

    /** Locale time string */
    public static Val toLocaleTimeString(Val d) { return new Val(d.js() + ".toLocaleTimeString()"); }

    // ==================== Validation ====================

    /** Checks if date is valid */
    public static Val isValid(Val d) {
        return new Val("!isNaN(" + d.js() + ".getTime())");
    }

    // ==================== UTC Getters ====================

    public static Val getUTCFullYear(Val d) { return new Val(d.js() + ".getUTCFullYear()"); }
    public static Val getUTCMonth(Val d) { return new Val(d.js() + ".getUTCMonth()"); }
    public static Val getUTCDate(Val d) { return new Val(d.js() + ".getUTCDate()"); }
    public static Val getUTCDay(Val d) { return new Val(d.js() + ".getUTCDay()"); }
    public static Val getUTCHours(Val d) { return new Val(d.js() + ".getUTCHours()"); }
    public static Val getUTCMinutes(Val d) { return new Val(d.js() + ".getUTCMinutes()"); }
    public static Val getUTCSeconds(Val d) { return new Val(d.js() + ".getUTCSeconds()"); }
    public static Val getUTCMilliseconds(Val d) { return new Val(d.js() + ".getUTCMilliseconds()"); }
    public static Val getTimezoneOffset(Val d) { return new Val(d.js() + ".getTimezoneOffset()"); }

    // ==================== UTC Setters (return new Date) ====================

    public static Val setUTCFullYear(Val d, Val year) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setUTCFullYear(" + year.js() + ");return n}())");
    }

    public static Val setUTCMonth(Val d, Val month) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setUTCMonth(" + month.js() + ");return n}())");
    }

    public static Val setUTCDate(Val d, Val day) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setUTCDate(" + day.js() + ");return n}())");
    }

    public static Val setUTCHours(Val d, Val hours) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setUTCHours(" + hours.js() + ");return n}())");
    }

    // ==================== UTC Static Methods ====================

    /** Creates UTC timestamp: Date.UTC(year, month, ...) */
    public static Val dateUTC(int year, int month, int day) {
        return new Val("Date.UTC(" + year + "," + month + "," + day + ")");
    }

    /** Creates UTC timestamp with time */
    public static Val dateUTC(int year, int month, int day, int hours, int minutes) {
        return new Val("Date.UTC(" + year + "," + month + "," + day + "," + hours + "," + minutes + ")");
    }

    /** Parses date string to timestamp: Date.parse(string) */
    public static Val parse(String dateString) {
        return new Val("Date.parse('" + JS.esc(dateString) + "')");
    }

    /** Parses date string to timestamp: Date.parse(expr) */
    public static Val parse(Val dateExpr) {
        return new Val("Date.parse(" + dateExpr.js() + ")");
    }

    // ==================== Week Operations ====================

    /** Start of week (Sunday) */
    public static Val startOfWeek(Val d) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setDate(n.getDate()-n.getDay());n.setHours(0,0,0,0);return n}())");
    }

    /** Start of week (Monday) */
    public static Val startOfWeekMonday(Val d) {
        return new Val("(function(){var n=new Date(" + d.js() + ");var day=n.getDay();n.setDate(n.getDate()-(day===0?6:day-1));n.setHours(0,0,0,0);return n}())");
    }

    /** End of week (Saturday 23:59:59.999) */
    public static Val endOfWeek(Val d) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setDate(n.getDate()+(6-n.getDay()));n.setHours(23,59,59,999);return n}())");
    }

    /** Adds weeks to date */
    public static Val addWeeks(Val d, int weeks) {
        return new Val("new Date(" + d.js() + ".getTime()+" + (weeks * 7 * 86400000L) + ")");
    }

    /** Adds weeks to date (dynamic) */
    public static Val addWeeks(Val d, Val weeks) {
        return new Val("new Date(" + d.js() + ".getTime()+" + weeks.js() + "*604800000)");
    }

    /** Gets ISO week number */
    public static Val getWeekNumber(Val d) {
        return new Val("(function(){var d=new Date(" + d.js() + ");d.setHours(0,0,0,0);d.setDate(d.getDate()+4-(d.getDay()||7));var y=new Date(d.getFullYear(),0,1);return Math.ceil((((d-y)/86400000)+1)/7)}())");
    }

    /** Difference in weeks */
    public static Val diffWeeks(Val d1, Val d2) {
        return new Val("Math.floor((" + d1.js() + ".getTime()-" + d2.js() + ".getTime())/604800000)");
    }

    // ==================== Quarter Operations ====================

    /** Gets quarter (1-4) */
    public static Val getQuarter(Val d) {
        return new Val("(Math.floor(" + d.js() + ".getMonth()/3)+1)");
    }

    /** Start of quarter */
    public static Val startOfQuarter(Val d) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setMonth(Math.floor(n.getMonth()/3)*3,1);n.setHours(0,0,0,0);return n}())");
    }

    /** End of quarter */
    public static Val endOfQuarter(Val d) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setMonth(Math.floor(n.getMonth()/3)*3+3,0);n.setHours(23,59,59,999);return n}())");
    }

    /** Adds quarters to date */
    public static Val addQuarters(Val d, int quarters) {
        return new Val("(function(){var n=new Date(" + d.js() + ");n.setMonth(n.getMonth()+" + (quarters * 3) + ");return n}())");
    }

    // ==================== Year Operations ====================

    /** Start of year */
    public static Val startOfYear(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear(),0,1)");
    }

    /** End of year */
    public static Val endOfYear(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear(),11,31,23,59,59,999)");
    }

    /** Checks if leap year */
    public static Val isLeapYear(Val d) {
        return new Val("(function(){var y=" + d.js() + ".getFullYear();return y%4===0&&(y%100!==0||y%400===0)}())");
    }

    /** Gets days in month */
    public static Val getDaysInMonth(Val d) {
        return new Val("new Date(" + d.js() + ".getFullYear()," + d.js() + ".getMonth()+1,0).getDate()");
    }

    // ==================== Additional Formatting ====================

    /** toUTCString */
    public static Val toUTCString(Val d) { return new Val(d.js() + ".toUTCString()"); }

    /** toJSON */
    public static Val toJSON(Val d) { return new Val(d.js() + ".toJSON()"); }

    /** Locale string with options */
    public static Val toLocaleString(Val d, String locale) {
        return new Val(d.js() + ".toLocaleString('" + JS.esc(locale) + "')");
    }
}
