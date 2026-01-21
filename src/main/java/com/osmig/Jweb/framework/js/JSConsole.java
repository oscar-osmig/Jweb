package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;

/**
 * Extended console methods for debugging.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSConsole.*;
 *
 * warn("Warning message")           // console.warn('Warning message')
 * error(variable("err"))            // console.error(err)
 * table(variable("users"))          // console.table(users)
 * time("fetch"); ... timeEnd("fetch") // Performance timing
 * </pre>
 */
public final class JSConsole {
    private JSConsole() {}

    // ==================== Logging Levels ====================

    /** console.log(...args) */
    public static Val log(Object... args) {
        return call("console.log", args);
    }

    /** console.warn(...args) */
    public static Val warn(Object... args) {
        return call("console.warn", args);
    }

    /** console.error(...args) */
    public static Val error(Object... args) {
        return call("console.error", args);
    }

    /** console.info(...args) */
    public static Val info(Object... args) {
        return call("console.info", args);
    }

    /** console.debug(...args) */
    public static Val debug(Object... args) {
        return call("console.debug", args);
    }

    // ==================== Structured Output ====================

    /** console.table(data) - displays tabular data */
    public static Val table(Val data) {
        return new Val("console.table(" + data.js() + ")");
    }

    /** console.table(data, columns) - displays specific columns */
    public static Val table(Val data, String... columns) {
        StringBuilder sb = new StringBuilder("console.table(").append(data.js()).append(",[");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("'").append(JS.esc(columns[i])).append("'");
        }
        return new Val(sb.append("])").toString());
    }

    /** console.dir(obj) - displays object properties */
    public static Val dir(Val obj) {
        return new Val("console.dir(" + obj.js() + ")");
    }

    /** console.dirxml(elem) - displays XML/HTML representation */
    public static Val dirxml(Val elem) {
        return new Val("console.dirxml(" + elem.js() + ")");
    }

    // ==================== Timing ====================

    /** console.time(label) - starts a timer */
    public static Val time(String label) {
        return new Val("console.time('" + JS.esc(label) + "')");
    }

    /** console.timeEnd(label) - ends timer and logs duration */
    public static Val timeEnd(String label) {
        return new Val("console.timeEnd('" + JS.esc(label) + "')");
    }

    /** console.timeLog(label) - logs current timer value without stopping */
    public static Val timeLog(String label) {
        return new Val("console.timeLog('" + JS.esc(label) + "')");
    }

    // ==================== Grouping ====================

    /** console.group(label) - starts a collapsed group */
    public static Val group(String label) {
        return new Val("console.group('" + JS.esc(label) + "')");
    }

    /** console.groupCollapsed(label) - starts a collapsed group */
    public static Val groupCollapsed(String label) {
        return new Val("console.groupCollapsed('" + JS.esc(label) + "')");
    }

    /** console.groupEnd() - ends current group */
    public static Val groupEnd() {
        return new Val("console.groupEnd()");
    }

    // ==================== Assertions & Counting ====================

    /** console.assert(condition, message) - logs if assertion fails */
    public static Val assert_(Val condition, String message) {
        return new Val("console.assert(" + condition.js() + ",'" + JS.esc(message) + "')");
    }

    /** console.count(label) - counts occurrences */
    public static Val count(String label) {
        return new Val("console.count('" + JS.esc(label) + "')");
    }

    /** console.countReset(label) - resets counter */
    public static Val countReset(String label) {
        return new Val("console.countReset('" + JS.esc(label) + "')");
    }

    // ==================== Other ====================

    /** console.clear() - clears the console */
    public static Val clear() {
        return new Val("console.clear()");
    }

    /** console.trace() - outputs a stack trace */
    public static Val trace() {
        return new Val("console.trace()");
    }

    /** console.trace(label) - outputs a labeled stack trace */
    public static Val trace(String label) {
        return new Val("console.trace('" + JS.esc(label) + "')");
    }

    private static Val call(String method, Object... args) {
        StringBuilder sb = new StringBuilder(method).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(JS.toJs(args[i]));
        }
        return new Val(sb.append(")").toString());
    }
}
