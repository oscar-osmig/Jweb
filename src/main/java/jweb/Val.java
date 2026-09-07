package jweb;

import jweb.js.Stmt;

import static com.osmig.Jweb.framework.js.JS.esc;
import static com.osmig.Jweb.framework.js.JS.toJs;

/**
 * A JavaScript expression — the value type of the JS DSL. Every expression
 * starter ({@code v("x")}, {@code dom("#id")}, {@code call(...)}) returns
 * one, and every operator on it returns another, so expressions chain:
 *
 * <pre>{@code
 * import jweb.Val;
 * import static jweb.Js.*;
 *
 * Val total = v("price").times(v("qty")).plus(5);
 * }</pre>
 *
 * <p>{@link #js()} is the generated JavaScript. Was {@code JS.Val} before 3.0.</p>
 */
public class Val {
    protected final String code;

    public Val(String code) { this.code = code; }

    public String js() { return code; }

    public Val dot(String prop) { return new Val(code + "." + prop); }
    public Val at(int index) { return new Val(code + "[" + index + "]"); }
    public Val at(Val key) { return new Val(code + "[" + key.code + "]"); }

    /**
     * Access a nested property path in a single call.
     * Simplifies: variable("e").dot("target").dot("result")
     * To: variable("e").path("target.result")
     *
     * @param dotPath the dot-separated property path (e.g., "target.result")
     * @return a Val representing the nested property access
     */
    public Val path(String dotPath) {
        return new Val(code + "." + dotPath);
    }

    /**
     * Shorthand for dot().invoke() - calls a method on this value.
     * Simplifies: variable("response").dot("json").invoke()
     * To: variable("response").call("json")
     *
     * @param method the method name to call
     * @param args optional arguments to pass to the method
     * @return a Val representing the method call
     */
    public Val call(String method, Object... args) {
        StringBuilder sb = new StringBuilder(code).append(".").append(method).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJs(args[i]));
        }
        return new Val(sb.append(")").toString());
    }

    // Common method shortcuts
    /** Shorthand for .call("json") - common for fetch responses */
    public Val json() { return call("json"); }
    /** Shorthand for .call("text") - common for fetch responses */
    public Val text() { return call("text"); }
    /** Shorthand for .call("blob") - common for fetch responses */
    public Val blob() { return call("blob"); }
    /** Shorthand for .call("arrayBuffer") - common for fetch responses */
    public Val arrayBuffer() { return call("arrayBuffer"); }
    /** Shorthand for .call("formData") - common for fetch responses */
    public Val formData() { return call("formData"); }
    /** Shorthand for .call("clone") - clones a Response or Request */
    public Val clone() { return call("clone"); }

    public Val invoke(Object... args) {
        StringBuilder sb = new StringBuilder(code).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJs(args[i]));
        }
        return new Val(sb.append(")").toString());
    }

    public Val plus(Object o) { return new Val("(" + code + "+" + toJs(o) + ")"); }
    public Val minus(Object o) { return new Val("(" + code + "-" + toJs(o) + ")"); }
    public Val times(Object o) { return new Val("(" + code + "*" + toJs(o) + ")"); }
    public Val div(Object o) { return new Val("(" + code + "/" + toJs(o) + ")"); }
    public Val mod(Object o) { return new Val("(" + code + "%" + toJs(o) + ")"); }

    public Val eq(Object o) { return new Val("(" + code + "===" + toJs(o) + ")"); }
    public Val neq(Object o) { return new Val("(" + code + "!==" + toJs(o) + ")"); }
    public Val gt(Object o) { return new Val("(" + code + ">" + toJs(o) + ")"); }
    public Val gte(Object o) { return new Val("(" + code + ">=" + toJs(o) + ")"); }
    public Val lt(Object o) { return new Val("(" + code + "<" + toJs(o) + ")"); }
    public Val lte(Object o) { return new Val("(" + code + "<=" + toJs(o) + ")"); }

    public Val and(Val o) { return new Val("(" + code + "&&" + o.code + ")"); }
    public Val or(Val o) { return new Val("(" + code + "||" + o.code + ")"); }
    public Val not() { return new Val("(!" + code + ")"); }
    public Val ternary(Object ifTrue, Object ifFalse) {
        return new Val("(" + code + "?" + toJs(ifTrue) + ":" + toJs(ifFalse) + ")");
    }

    // ==================== String Methods ====================

    public Val padStart(int len, String pad) {
        return new Val("String(" + code + ").padStart(" + len + ",'" + esc(pad) + "')");
    }
    public Val padEnd(int len, String pad) {
        return new Val("String(" + code + ").padEnd(" + len + ",'" + esc(pad) + "')");
    }
    public Val length() { return new Val(code + ".length"); }
    public Val trim() { return new Val(code + ".trim()"); }
    public Val toLowerCase() { return new Val(code + ".toLowerCase()"); }
    public Val toUpperCase() { return new Val(code + ".toUpperCase()"); }

    /** Gets substring: str.substring(start, end) */
    public Val substring(int start, int end) {
        return new Val(code + ".substring(" + start + "," + end + ")");
    }

    /** Gets substring from start: str.substring(start) */
    public Val substring(int start) {
        return new Val(code + ".substring(" + start + ")");
    }

    /** Gets character at index: str.charAt(index) */
    public Val charAt(int index) {
        return new Val(code + ".charAt(" + index + ")");
    }

    /** Finds index of substring: str.indexOf(search) */
    public Val indexOf(String search) {
        return new Val(code + ".indexOf('" + esc(search) + "')");
    }

    /** Finds last index of substring: str.lastIndexOf(search) */
    public Val lastIndexOf(String search) {
        return new Val(code + ".lastIndexOf('" + esc(search) + "')");
    }

    /** Splits string: str.split(separator) */
    public Val split(String separator) {
        return new Val(code + ".split('" + esc(separator) + "')");
    }

    /** Replaces first occurrence: str.replace(search, replacement) */
    public Val replace(String search, String replacement) {
        return new Val(code + ".replace('" + esc(search) + "','" + esc(replacement) + "')");
    }

    /** Replaces all occurrences: str.replaceAll(search, replacement) */
    public Val replaceAll(String search, String replacement) {
        return new Val(code + ".replaceAll('" + esc(search) + "','" + esc(replacement) + "')");
    }

    /** Checks if string starts with prefix: str.startsWith(prefix) */
    public Val startsWith(String prefix) {
        return new Val(code + ".startsWith('" + esc(prefix) + "')");
    }

    /** Checks if string ends with suffix: str.endsWith(suffix) */
    public Val endsWith(String suffix) {
        return new Val(code + ".endsWith('" + esc(suffix) + "')");
    }

    /** Checks if string includes substring: str.includes(search) */
    public Val includes(String search) {
        return new Val(code + ".includes('" + esc(search) + "')");
    }

    /** Repeats string n times: str.repeat(count) */
    public Val repeat(int count) {
        return new Val(code + ".repeat(" + count + ")");
    }

    /** Repeats string n times: str.repeat(count) */
    public Val repeat(Val count) {
        return new Val(code + ".repeat(" + count.code + ")");
    }

    /** @deprecated Use {@link #slice(int, int)} — it emits exactly the same {@code .slice()} call. */
    @Deprecated
    public Val sliceStr(int start, int end) {
        return slice(start, end);
    }

    /** @deprecated Use {@link #slice(int)} — it emits exactly the same {@code .slice()} call. */
    @Deprecated
    public Val sliceStr(int start) {
        return slice(start);
    }

    /** Searches for regex: str.search(regex) */
    public Val search(Val regex) {
        return new Val(code + ".search(" + regex.code + ")");
    }

    /** Matches against regex: str.match(regex) */
    public Val match(Val regex) {
        return new Val(code + ".match(" + regex.code + ")");
    }

    /** Matches all against regex: str.matchAll(regex) */
    public Val matchAll(Val regex) {
        return new Val(code + ".matchAll(" + regex.code + ")");
    }

    /** Normalizes Unicode: str.normalize() */
    public Val normalize() {
        return new Val(code + ".normalize()");
    }

    /** Normalizes Unicode with form: str.normalize(form) */
    public Val normalize(String form) {
        return new Val(code + ".normalize('" + esc(form) + "')");
    }

    /** Trims start of string: str.trimStart() */
    public Val trimStart() {
        return new Val(code + ".trimStart()");
    }

    /** Trims end of string: str.trimEnd() */
    public Val trimEnd() {
        return new Val(code + ".trimEnd()");
    }

    /** Locale compare: str.localeCompare(other) */
    public Val localeCompare(Val other) {
        return new Val(code + ".localeCompare(" + other.code + ")");
    }

    /** Locale compare: str.localeCompare(other) */
    public Val localeCompare(String other) {
        return new Val(code + ".localeCompare('" + esc(other) + "')");
    }

    /** Dynamic padStart: str.padStart(len, pad) */
    public Val padStart(Val len, String pad) {
        return new Val("String(" + code + ").padStart(" + len.code + ",'" + esc(pad) + "')");
    }

    /** Dynamic padEnd: str.padEnd(len, pad) */
    public Val padEnd(Val len, String pad) {
        return new Val("String(" + code + ").padEnd(" + len.code + ",'" + esc(pad) + "')");
    }

    // ==================== Array Methods ====================

    /**
     * Filters array elements: arr.filter(callback)
     *
     * <p>Example:</p>
     * <pre>
     * variable("items").filter(callback("x").ret(variable("x").gt(5)))
     * // Output: items.filter(function(x){return (x>5);})
     * </pre>
     */
    public Val filter(Func predicate) {
        return new Val(code + ".filter(" + predicate.toExpr() + ")");
    }

    /**
     * Maps array elements: arr.map(callback)
     *
     * <p>Example:</p>
     * <pre>
     * variable("items").map(callback("x").ret(variable("x").times(2)))
     * // Output: items.map(function(x){return (x*2);})
     * </pre>
     */
    public Val map(Func transformer) {
        return new Val(code + ".map(" + transformer.toExpr() + ")");
    }

    /**
     * Iterates over array: arr.forEach(callback)
     *
     * <p>Example:</p>
     * <pre>
     * variable("items").forEach(callback("item").call("process", variable("item")))
     * </pre>
     */
    public Val forEach(Func action) {
        return new Val(code + ".forEach(" + action.toExpr() + ")");
    }

    /**
     * Finds first matching element: arr.find(callback)
     */
    public Val find(Func predicate) {
        return new Val(code + ".find(" + predicate.toExpr() + ")");
    }

    /**
     * Finds index of first matching element: arr.findIndex(callback)
     */
    public Val findIndex(Func predicate) {
        return new Val(code + ".findIndex(" + predicate.toExpr() + ")");
    }

    /**
     * Checks if any element matches: arr.some(callback)
     */
    public Val some(Func predicate) {
        return new Val(code + ".some(" + predicate.toExpr() + ")");
    }

    /**
     * Checks if all elements match: arr.every(callback)
     */
    public Val every(Func predicate) {
        return new Val(code + ".every(" + predicate.toExpr() + ")");
    }

    /**
     * Reduces array to single value: arr.reduce(callback, initialValue)
     */
    public Val reduce(Func reducer, Object initialValue) {
        return new Val(code + ".reduce(" + reducer.toExpr() + "," + toJs(initialValue) + ")");
    }

    /**
     * Reduces array to single value: arr.reduce(callback)
     */
    public Val reduce(Func reducer) {
        return new Val(code + ".reduce(" + reducer.toExpr() + ")");
    }

    /**
     * Gets slice of array: arr.slice(start, end)
     */
    public Val slice(int start, int end) {
        return new Val(code + ".slice(" + start + "," + end + ")");
    }

    /**
     * Gets slice of array from start: arr.slice(start)
     */
    public Val slice(int start) {
        return new Val(code + ".slice(" + start + ")");
    }

    /**
     * Concatenates arrays: arr.concat(other)
     */
    public Val concat(Val other) {
        return new Val(code + ".concat(" + other.code + ")");
    }

    /**
     * Joins array to string: arr.join(separator)
     */
    public Val join(String separator) {
        return new Val(code + ".join('" + esc(separator) + "')");
    }

    /**
     * Reverses array: arr.reverse()
     */
    public Val reverse() {
        return new Val(code + ".reverse()");
    }

    /**
     * Sorts array: arr.sort()
     */
    public Val sort() {
        return new Val(code + ".sort()");
    }

    /**
     * Sorts array with comparator: arr.sort(comparator)
     */
    public Val sort(Func comparator) {
        return new Val(code + ".sort(" + comparator.toExpr() + ")");
    }

    /**
     * Checks if array includes value: arr.includes(value)
     */
    public Val includes(Val value) {
        return new Val(code + ".includes(" + value.code + ")");
    }

    /**
     * Gets first element: arr[0]
     */
    public Val first() {
        return new Val(code + "[0]");
    }

    /**
     * Gets last element: arr[arr.length-1]
     */
    public Val last() {
        return new Val(code + "[" + code + ".length-1]");
    }

    /**
     * Pushes element to array: arr.push(value)
     */
    public Val push(Object value) {
        return new Val(code + ".push(" + toJs(value) + ")");
    }

    /**
     * Pops element from array: arr.pop()
     */
    public Val pop() {
        return new Val(code + ".pop()");
    }

    /**
     * Shifts element from array: arr.shift()
     */
    public Val shift() {
        return new Val(code + ".shift()");
    }

    /**
     * Unshifts element to array: arr.unshift(value)
     */
    public Val unshift(Object value) {
        return new Val(code + ".unshift(" + toJs(value) + ")");
    }

    /**
     * Flattens nested arrays: arr.flat(depth)
     */
    public Val flat() {
        return new Val(code + ".flat()");
    }

    /**
     * Flattens nested arrays: arr.flat(depth)
     */
    public Val flat(int depth) {
        return new Val(code + ".flat(" + depth + ")");
    }

    /**
     * Maps and flattens: arr.flatMap(callback)
     */
    public Val flatMap(Func mapper) {
        return new Val(code + ".flatMap(" + mapper.toExpr() + ")");
    }

    /**
     * Gets element at index with negative support: arr.at(index)
     */
    public Val atIndex(int index) {
        return new Val(code + ".at(" + index + ")");
    }

    /**
     * Gets element at index with negative support: arr.at(index)
     */
    public Val atIndex(Val index) {
        return new Val(code + ".at(" + index.code + ")");
    }

    /**
     * Fills array with value: arr.fill(value)
     */
    public Val fill(Object value) {
        return new Val(code + ".fill(" + toJs(value) + ")");
    }

    /**
     * Fills array with value from start to end: arr.fill(value, start, end)
     */
    public Val fill(Object value, int start, int end) {
        return new Val(code + ".fill(" + toJs(value) + "," + start + "," + end + ")");
    }

    /**
     * Copies array section within itself: arr.copyWithin(target, start, end)
     */
    public Val copyWithin(int target, int start) {
        return new Val(code + ".copyWithin(" + target + "," + start + ")");
    }

    /**
     * Copies array section within itself: arr.copyWithin(target, start, end)
     */
    public Val copyWithin(int target, int start, int end) {
        return new Val(code + ".copyWithin(" + target + "," + start + "," + end + ")");
    }

    /**
     * Adds/removes elements: arr.splice(start, deleteCount, ...items)
     */
    public Val splice(int start, int deleteCount, Object... items) {
        StringBuilder sb = new StringBuilder(code + ".splice(" + start + "," + deleteCount);
        for (Object item : items) {
            sb.append(",").append(toJs(item));
        }
        return new Val(sb.append(")").toString());
    }

    /**
     * Finds last matching element: arr.findLast(callback)
     */
    public Val findLast(Func predicate) {
        return new Val(code + ".findLast(" + predicate.toExpr() + ")");
    }

    /**
     * Finds index of last matching element: arr.findLastIndex(callback)
     */
    public Val findLastIndex(Func predicate) {
        return new Val(code + ".findLastIndex(" + predicate.toExpr() + ")");
    }

    /**
     * Creates array without mutating: arr.toSorted(comparator)
     */
    public Val toSorted() {
        return new Val(code + ".toSorted()");
    }

    /**
     * Creates array without mutating: arr.toSorted(comparator)
     */
    public Val toSorted(Func comparator) {
        return new Val(code + ".toSorted(" + comparator.toExpr() + ")");
    }

    /**
     * Creates reversed array without mutating: arr.toReversed()
     */
    public Val toReversed() {
        return new Val(code + ".toReversed()");
    }

    /**
     * Finds indexOf: {@code arr.indexOf(value)} — same platform method as
     * {@link #indexOf(String)}, overloaded on the argument type.
     */
    public Val indexOf(Val value) {
        return new Val(code + ".indexOf(" + value.code + ")");
    }

    /** @deprecated Use {@link #indexOf(Val)}. */
    @Deprecated
    public Val indexOfVal(Val value) {
        return indexOf(value);
    }

    // ==================== Object Methods ====================

    /**
     * Gets object keys: Object.keys(obj)
     */
    public Val keys() {
        return new Val("Object.keys(" + code + ")");
    }

    /**
     * Gets object values: Object.values(obj)
     */
    public Val values() {
        return new Val("Object.values(" + code + ")");
    }

    /**
     * Gets object entries: Object.entries(obj)
     */
    public Val entries() {
        return new Val("Object.entries(" + code + ")");
    }

    /**
     * Checks if object has own property: obj.hasOwnProperty(key)
     */
    public Val hasOwnProperty(String key) {
        return new Val(code + ".hasOwnProperty('" + esc(key) + "')");
    }

    /**
     * Checks if object has own property with dynamic key: obj.hasOwnProperty(key)
     */
    public Val hasOwnProperty(Val key) {
        return new Val(code + ".hasOwnProperty(" + key.code + ")");
    }

    // ==================== Number Formatting ====================

    /** Formats to fixed decimal places: num.toFixed(digits) */
    public Val toFixed(int digits) {
        return new Val(code + ".toFixed(" + digits + ")");
    }

    /** Formats to exponential notation: num.toExponential(digits) */
    public Val toExponential(int digits) {
        return new Val(code + ".toExponential(" + digits + ")");
    }

    /** Formats to precision: num.toPrecision(precision) */
    public Val toPrecision(int precision) {
        return new Val(code + ".toPrecision(" + precision + ")");
    }

    /** Converts to string with radix: num.toString(radix) */
    public Val toStringRadix(int radix) {
        return new Val(code + ".toString(" + radix + ")");
    }

    // ==================== Type Checking ====================

    /**
     * Gets typeof: typeof value
     */
    public Val typeof() {
        return new Val("typeof " + code);
    }

    /**
     * Checks instanceof: value instanceof Type
     */
    public Val instanceof_(String type) {
        return new Val("(" + code + " instanceof " + type + ")");
    }

    // ==================== Assignment ====================

    /** Assignment statement: variable("x").assign(5) -> x=5 */
    public Stmt assign(Object value) { return new Stmt(code + "=" + toJs(value)); }

    /** Compound assignment: variable("x").addAssign(5) -> x+=5 */
    public Stmt addAssign(Object value) { return new Stmt(code + "+=" + toJs(value)); }

    /** Compound assignment: variable("x").subAssign(5) -> x-=5 */
    public Stmt subAssign(Object value) { return new Stmt(code + "-=" + toJs(value)); }

    /** Compound assignment: variable("x").mulAssign(5) -> x*=5 */
    public Stmt mulAssign(Object value) { return new Stmt(code + "*=" + toJs(value)); }

    /** Compound assignment: variable("x").divAssign(5) -> x/=5 */
    public Stmt divAssign(Object value) { return new Stmt(code + "/=" + toJs(value)); }

    @Override
    public String toString() { return code; }
}
