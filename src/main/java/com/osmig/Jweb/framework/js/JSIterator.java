package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Func;
import com.osmig.Jweb.framework.js.JS.Val;

import java.util.ArrayList;
import java.util.List;

/**
 * Iterator and Generator utilities for JavaScript.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSIterator.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Generator function
 * generator("fibonacci")
 *     .let_("a", 0)
 *     .let_("b", 1)
 *     .while_(variable("true"))
 *         .yield_(variable("a"))
 *         .let_("temp", variable("a"))
 *         .set("a", variable("b"))
 *         .set("b", variable("temp").plus(variable("b")))
 *     .endWhile();
 *
 * // Using iterator helpers (ES2023+)
 * iteratorFrom(variable("iterable"))
 *     .map(callback("x").ret(variable("x").times(2)))
 *     .filter(callback("x").ret(variable("x").gt(10)))
 *     .take(5)
 *     .forEach(callback("x").call("console.log", variable("x")));
 *
 * // Async generator
 * asyncGenerator("fetchPages")
 *     .let_("page", 1)
 *     .while_(variable("page").lte(10))
 *         .awaitYield_(fetch("/api/data?page=" + variable("page")))
 *         .inc("page")
 *     .endWhile();
 * </pre>
 */
public final class JSIterator {
    private JSIterator() {}

    // ==================== Iterator Protocol ====================

    /**
     * Symbol.iterator - well-known symbol for default iteration.
     */
    public static Val symbolIterator() {
        return new Val("Symbol.iterator");
    }

    /**
     * Symbol.asyncIterator - well-known symbol for async iteration.
     */
    public static Val symbolAsyncIterator() {
        return new Val("Symbol.asyncIterator");
    }

    /**
     * Creates an iterator result object: {value: val, done: false}
     */
    public static Val iteratorResult(Val value, boolean done) {
        return new Val("{value:" + value.js() + ",done:" + done + "}");
    }

    /**
     * Creates an iterator result object: {value: val, done: doneExpr}
     */
    public static Val iteratorResult(Val value, Val done) {
        return new Val("{value:" + value.js() + ",done:" + done.js() + "}");
    }

    /**
     * Creates a done result: {done: true}
     */
    public static Val iteratorDone() {
        return new Val("{done:true}");
    }

    // ==================== Generator Functions ====================

    /**
     * Creates a generator function.
     *
     * @param name the function name
     * @param params the function parameters
     * @return a GeneratorFunc builder
     */
    public static GeneratorFunc generator(String name, String... params) {
        return new GeneratorFunc(name, params);
    }

    /**
     * Creates an anonymous generator function (generator callback).
     *
     * @param params the function parameters
     * @return a GeneratorFunc builder
     */
    public static GeneratorFunc generatorCallback(String... params) {
        return new GeneratorFunc(null, params);
    }

    /**
     * Builder for generator functions (function*).
     */
    public static class GeneratorFunc {
        private final String name;
        private final String[] params;
        private final List<String> body = new ArrayList<>();

        GeneratorFunc(String name, String... params) {
            this.name = name;
            this.params = params;
        }

        public GeneratorFunc let_(String name, Object value) {
            body.add("let " + name + "=" + JS.toJs(value));
            return this;
        }

        public GeneratorFunc const_(String name, Object value) {
            body.add("const " + name + "=" + JS.toJs(value));
            return this;
        }

        public GeneratorFunc set(String name, Object value) {
            body.add(name + "=" + JS.toJs(value));
            return this;
        }

        public GeneratorFunc inc(String name) {
            body.add(name + "++");
            return this;
        }

        public GeneratorFunc dec(String name) {
            body.add(name + "--");
            return this;
        }

        /**
         * Yields a value: yield value
         */
        public GeneratorFunc yield_(Object value) {
            body.add("yield " + JS.toJs(value));
            return this;
        }

        /**
         * Delegate to another generator: yield* iterable
         */
        public GeneratorFunc yieldStar(Val iterable) {
            body.add("yield* " + iterable.js());
            return this;
        }

        public GeneratorFunc call(String fn, Object... args) {
            body.add(JS.call(fn, args).js());
            return this;
        }

        public GeneratorFunc log(Object... args) {
            body.add(JS.call("console.log", args).js());
            return this;
        }

        public GeneratorFunc ret() {
            body.add("return");
            return this;
        }

        public GeneratorFunc ret(Object value) {
            body.add("return " + JS.toJs(value));
            return this;
        }

        /**
         * Adds if statement.
         */
        public GeneratorFunc if_(Val condition, Object... thenStmts) {
            StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
            for (Object s : thenStmts) appendStmt(sb, s);
            sb.append("}");
            body.add(sb.toString());
            return this;
        }

        /**
         * Creates a for...of loop.
         */
        public ForOfBuilder forOf(String varName, Val iterable) {
            return new ForOfBuilder(this, varName, iterable);
        }

        /**
         * Creates a while loop.
         */
        public WhileBuilder while_(Val condition) {
            return new WhileBuilder(this, condition);
        }

        /**
         * Adds raw JavaScript.
         */
        public GeneratorFunc unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        /**
         * Returns the generator as an expression.
         */
        public String toExpr() {
            StringBuilder sb = new StringBuilder("function*(");
            sb.append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        /**
         * Returns the generator as a declaration.
         */
        public String toDecl() {
            StringBuilder sb = new StringBuilder("function* ");
            if (name != null) sb.append(name);
            sb.append("(").append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof JS.Stmt st) sb.append(st.code).append(";");
            else if (s instanceof Val val) sb.append(val.js()).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }

        /**
         * Builder for for...of loops within generators.
         */
        public class ForOfBuilder {
            private final GeneratorFunc parent;
            private final String varName;
            private final Val iterable;
            private final List<String> bodyStmts = new ArrayList<>();

            ForOfBuilder(GeneratorFunc parent, String varName, Val iterable) {
                this.parent = parent;
                this.varName = varName;
                this.iterable = iterable;
            }

            public ForOfBuilder yield_(Object value) {
                bodyStmts.add("yield " + JS.toJs(value));
                return this;
            }

            public ForOfBuilder yieldStar(Val iter) {
                bodyStmts.add("yield* " + iter.js());
                return this;
            }

            public ForOfBuilder call(String fn, Object... args) {
                bodyStmts.add(JS.call(fn, args).js());
                return this;
            }

            public ForOfBuilder set(String name, Object value) {
                bodyStmts.add(name + "=" + JS.toJs(value));
                return this;
            }

            public GeneratorFunc endFor() {
                StringBuilder sb = new StringBuilder("for(const ").append(varName)
                    .append(" of ").append(iterable.js()).append("){");
                for (String stmt : bodyStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
                parent.body.add(sb.toString());
                return parent;
            }
        }

        /**
         * Builder for while loops within generators.
         */
        public class WhileBuilder {
            private final GeneratorFunc parent;
            private final Val condition;
            private final List<String> bodyStmts = new ArrayList<>();

            WhileBuilder(GeneratorFunc parent, Val condition) {
                this.parent = parent;
                this.condition = condition;
            }

            public WhileBuilder yield_(Object value) {
                bodyStmts.add("yield " + JS.toJs(value));
                return this;
            }

            public WhileBuilder let_(String name, Object value) {
                bodyStmts.add("let " + name + "=" + JS.toJs(value));
                return this;
            }

            public WhileBuilder set(String name, Object value) {
                bodyStmts.add(name + "=" + JS.toJs(value));
                return this;
            }

            public WhileBuilder inc(String name) {
                bodyStmts.add(name + "++");
                return this;
            }

            public GeneratorFunc endWhile() {
                StringBuilder sb = new StringBuilder("while(").append(condition.js()).append("){");
                for (String stmt : bodyStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
                parent.body.add(sb.toString());
                return parent;
            }
        }
    }

    // ==================== Async Generators ====================

    /**
     * Creates an async generator function.
     *
     * @param name the function name
     * @param params the function parameters
     * @return an AsyncGeneratorFunc builder
     */
    public static AsyncGeneratorFunc asyncGenerator(String name, String... params) {
        return new AsyncGeneratorFunc(name, params);
    }

    /**
     * Creates an anonymous async generator function.
     *
     * @param params the function parameters
     * @return an AsyncGeneratorFunc builder
     */
    public static AsyncGeneratorFunc asyncGeneratorCallback(String... params) {
        return new AsyncGeneratorFunc(null, params);
    }

    /**
     * Builder for async generator functions (async function*).
     */
    public static class AsyncGeneratorFunc {
        private final String name;
        private final String[] params;
        private final List<String> body = new ArrayList<>();

        AsyncGeneratorFunc(String name, String... params) {
            this.name = name;
            this.params = params;
        }

        public AsyncGeneratorFunc let_(String name, Object value) {
            body.add("let " + name + "=" + JS.toJs(value));
            return this;
        }

        public AsyncGeneratorFunc const_(String name, Object value) {
            body.add("const " + name + "=" + JS.toJs(value));
            return this;
        }

        public AsyncGeneratorFunc set(String name, Object value) {
            body.add(name + "=" + JS.toJs(value));
            return this;
        }

        public AsyncGeneratorFunc inc(String name) {
            body.add(name + "++");
            return this;
        }

        public AsyncGeneratorFunc dec(String name) {
            body.add(name + "--");
            return this;
        }

        /**
         * Awaits and yields a value: yield await expr
         */
        public AsyncGeneratorFunc awaitYield_(Val expr) {
            body.add("yield await " + expr.js());
            return this;
        }

        /**
         * Yields a value: yield value
         */
        public AsyncGeneratorFunc yield_(Object value) {
            body.add("yield " + JS.toJs(value));
            return this;
        }

        /**
         * Delegate to another async generator: yield* asyncIterable
         */
        public AsyncGeneratorFunc yieldStar(Val asyncIterable) {
            body.add("yield* " + asyncIterable.js());
            return this;
        }

        /**
         * Awaits an expression and assigns to a variable.
         */
        public AsyncGeneratorFunc await_(String varName, Val expr) {
            body.add("const " + varName + "=await " + expr.js());
            return this;
        }

        /**
         * Awaits an expression without assignment.
         */
        public AsyncGeneratorFunc await_(Val expr) {
            body.add("await " + expr.js());
            return this;
        }

        public AsyncGeneratorFunc call(String fn, Object... args) {
            body.add(JS.call(fn, args).js());
            return this;
        }

        public AsyncGeneratorFunc log(Object... args) {
            body.add(JS.call("console.log", args).js());
            return this;
        }

        public AsyncGeneratorFunc ret() {
            body.add("return");
            return this;
        }

        public AsyncGeneratorFunc ret(Object value) {
            body.add("return " + JS.toJs(value));
            return this;
        }

        /**
         * Adds if statement.
         */
        public AsyncGeneratorFunc if_(Val condition, Object... thenStmts) {
            StringBuilder sb = new StringBuilder("if(").append(condition.js()).append("){");
            for (Object s : thenStmts) appendStmt(sb, s);
            sb.append("}");
            body.add(sb.toString());
            return this;
        }

        /**
         * Creates a for await...of loop.
         */
        public ForAwaitOfBuilder forAwaitOf(String varName, Val asyncIterable) {
            return new ForAwaitOfBuilder(this, varName, asyncIterable);
        }

        /**
         * Creates a while loop.
         */
        public WhileBuilder while_(Val condition) {
            return new WhileBuilder(this, condition);
        }

        /**
         * Adds raw JavaScript.
         */
        public AsyncGeneratorFunc unsafeRaw(String js) {
            body.add(js);
            return this;
        }

        /**
         * Returns the async generator as an expression.
         */
        public String toExpr() {
            StringBuilder sb = new StringBuilder("async function*(");
            sb.append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        /**
         * Returns the async generator as a declaration.
         */
        public String toDecl() {
            StringBuilder sb = new StringBuilder("async function* ");
            if (name != null) sb.append(name);
            sb.append("(").append(String.join(",", params)).append("){");
            for (String s : body) {
                sb.append(s);
                if (!s.endsWith("}")) sb.append(";");
            }
            return sb.append("}").toString();
        }

        private void appendStmt(StringBuilder sb, Object s) {
            if (s instanceof JS.Stmt st) sb.append(st.code).append(";");
            else if (s instanceof Val val) sb.append(val.js()).append(";");
            else if (s instanceof String str) {
                sb.append(str);
                if (!str.endsWith(";") && !str.endsWith("}")) sb.append(";");
            }
        }

        /**
         * Builder for for await...of loops within async generators.
         */
        public class ForAwaitOfBuilder {
            private final AsyncGeneratorFunc parent;
            private final String varName;
            private final Val asyncIterable;
            private final List<String> bodyStmts = new ArrayList<>();

            ForAwaitOfBuilder(AsyncGeneratorFunc parent, String varName, Val asyncIterable) {
                this.parent = parent;
                this.varName = varName;
                this.asyncIterable = asyncIterable;
            }

            public ForAwaitOfBuilder yield_(Object value) {
                bodyStmts.add("yield " + JS.toJs(value));
                return this;
            }

            public ForAwaitOfBuilder await_(String name, Val expr) {
                bodyStmts.add("const " + name + "=await " + expr.js());
                return this;
            }

            public ForAwaitOfBuilder call(String fn, Object... args) {
                bodyStmts.add(JS.call(fn, args).js());
                return this;
            }

            public AsyncGeneratorFunc endFor() {
                StringBuilder sb = new StringBuilder("for await(const ").append(varName)
                    .append(" of ").append(asyncIterable.js()).append("){");
                for (String stmt : bodyStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
                parent.body.add(sb.toString());
                return parent;
            }
        }

        /**
         * Builder for while loops within async generators.
         */
        public class WhileBuilder {
            private final AsyncGeneratorFunc parent;
            private final Val condition;
            private final List<String> bodyStmts = new ArrayList<>();

            WhileBuilder(AsyncGeneratorFunc parent, Val condition) {
                this.parent = parent;
                this.condition = condition;
            }

            public WhileBuilder yield_(Object value) {
                bodyStmts.add("yield " + JS.toJs(value));
                return this;
            }

            public WhileBuilder awaitYield_(Val expr) {
                bodyStmts.add("yield await " + expr.js());
                return this;
            }

            public WhileBuilder await_(String name, Val expr) {
                bodyStmts.add("const " + name + "=await " + expr.js());
                return this;
            }

            public WhileBuilder inc(String name) {
                bodyStmts.add(name + "++");
                return this;
            }

            public WhileBuilder let_(String name, Object value) {
                bodyStmts.add("let " + name + "=" + JS.toJs(value));
                return this;
            }

            public AsyncGeneratorFunc endWhile() {
                StringBuilder sb = new StringBuilder("while(").append(condition.js()).append("){");
                for (String stmt : bodyStmts) {
                    sb.append(stmt);
                    if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
                }
                sb.append("}");
                parent.body.add(sb.toString());
                return parent;
            }
        }
    }

    // ==================== Iterator Helpers (ES2023+) ====================

    /**
     * Iterator.from() - creates an iterator from an iterable.
     */
    public static IteratorHelperBuilder iteratorFrom(Val iterable) {
        return new IteratorHelperBuilder("Iterator.from(" + iterable.js() + ")");
    }

    /**
     * Builder for chaining iterator helper methods.
     */
    public static class IteratorHelperBuilder {
        private final StringBuilder chain;

        IteratorHelperBuilder(String base) {
            this.chain = new StringBuilder(base);
        }

        /**
         * Maps each element: .map(fn)
         */
        public IteratorHelperBuilder map(Func mapper) {
            chain.append(".map(").append(mapper.toExpr()).append(")");
            return this;
        }

        /**
         * Filters elements: .filter(fn)
         */
        public IteratorHelperBuilder filter(Func predicate) {
            chain.append(".filter(").append(predicate.toExpr()).append(")");
            return this;
        }

        /**
         * Takes first n elements: .take(n)
         */
        public IteratorHelperBuilder take(int n) {
            chain.append(".take(").append(n).append(")");
            return this;
        }

        /**
         * Takes first n elements (dynamic): .take(n)
         */
        public IteratorHelperBuilder take(Val n) {
            chain.append(".take(").append(n.js()).append(")");
            return this;
        }

        /**
         * Drops first n elements: .drop(n)
         */
        public IteratorHelperBuilder drop(int n) {
            chain.append(".drop(").append(n).append(")");
            return this;
        }

        /**
         * Drops first n elements (dynamic): .drop(n)
         */
        public IteratorHelperBuilder drop(Val n) {
            chain.append(".drop(").append(n.js()).append(")");
            return this;
        }

        /**
         * Flattens nested iterators: .flatMap(fn)
         */
        public IteratorHelperBuilder flatMap(Func mapper) {
            chain.append(".flatMap(").append(mapper.toExpr()).append(")");
            return this;
        }

        /**
         * Reduces to single value: .reduce(fn, initial)
         */
        public Val reduce(Func reducer, Object initial) {
            chain.append(".reduce(").append(reducer.toExpr()).append(",").append(JS.toJs(initial)).append(")");
            return new Val(chain.toString());
        }

        /**
         * Reduces to single value: .reduce(fn)
         */
        public Val reduce(Func reducer) {
            chain.append(".reduce(").append(reducer.toExpr()).append(")");
            return new Val(chain.toString());
        }

        /**
         * Converts to array: .toArray()
         */
        public Val toArray() {
            chain.append(".toArray()");
            return new Val(chain.toString());
        }

        /**
         * Executes function for each element: .forEach(fn)
         */
        public Val forEach(Func action) {
            chain.append(".forEach(").append(action.toExpr()).append(")");
            return new Val(chain.toString());
        }

        /**
         * Checks if some element matches: .some(fn)
         */
        public Val some(Func predicate) {
            chain.append(".some(").append(predicate.toExpr()).append(")");
            return new Val(chain.toString());
        }

        /**
         * Checks if every element matches: .every(fn)
         */
        public Val every(Func predicate) {
            chain.append(".every(").append(predicate.toExpr()).append(")");
            return new Val(chain.toString());
        }

        /**
         * Finds first matching element: .find(fn)
         */
        public Val find(Func predicate) {
            chain.append(".find(").append(predicate.toExpr()).append(")");
            return new Val(chain.toString());
        }

        /**
         * Returns the iterator as a Val.
         */
        public Val build() {
            return new Val(chain.toString());
        }
    }

    // ==================== Creating Iterables ====================

    /**
     * Creates an iterable object with Symbol.iterator method.
     *
     * @return an IterableBuilder
     */
    public static IterableBuilder iterable() {
        return new IterableBuilder();
    }

    /**
     * Builder for creating custom iterable objects.
     */
    public static class IterableBuilder {
        private GeneratorFunc generatorFunc;

        /**
         * Sets the iterator using a generator function.
         */
        public IterableBuilder iterator(GeneratorFunc gen) {
            this.generatorFunc = gen;
            return this;
        }

        /**
         * Builds the iterable object.
         */
        public Val build() {
            if (generatorFunc == null) {
                throw new IllegalStateException("Iterator generator function must be set");
            }
            return new Val("{[Symbol.iterator]:" + generatorFunc.toExpr() + "}");
        }
    }

    // ==================== Spread Operator ====================

    /**
     * Spreads an iterable into an array: [...iterable]
     */
    public static Val spreadToArray(Val iterable) {
        return new Val("[..." + iterable.js() + "]");
    }

    /**
     * Spreads multiple iterables into an array: [...iter1, ...iter2, ...]
     */
    public static Val spreadToArray(Val... iterables) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < iterables.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("...").append(iterables[i].js());
        }
        return new Val(sb.append("]").toString());
    }

    /**
     * Spreads an iterable as function arguments: fn(...iterable)
     */
    public static Val spreadCall(String fn, Val iterable) {
        return new Val(fn + "(..." + iterable.js() + ")");
    }

    // ==================== Array.from() with Iterables ====================

    /**
     * Array.from(iterable) - converts iterable to array.
     */
    public static Val arrayFrom(Val iterable) {
        return new Val("Array.from(" + iterable.js() + ")");
    }

    /**
     * Array.from(iterable, mapFn) - converts and maps.
     */
    public static Val arrayFrom(Val iterable, Func mapper) {
        return new Val("Array.from(" + iterable.js() + "," + mapper.toExpr() + ")");
    }

    // ==================== Built-in Iterables ====================

    /**
     * Gets entries iterator: obj.entries() or Map/Set .entries()
     */
    public static Val entries(Val obj) {
        return new Val(obj.js() + ".entries()");
    }

    /**
     * Gets keys iterator: obj.keys() or Map/Set .keys()
     */
    public static Val keys(Val obj) {
        return new Val(obj.js() + ".keys()");
    }

    /**
     * Gets values iterator: obj.values() or Map/Set .values()
     */
    public static Val values(Val obj) {
        return new Val(obj.js() + ".values()");
    }

    /**
     * Object.entries() - returns array of [key, value] pairs.
     */
    public static Val objectEntries(Val obj) {
        return new Val("Object.entries(" + obj.js() + ")");
    }

    /**
     * Object.keys() - returns array of keys.
     */
    public static Val objectKeys(Val obj) {
        return new Val("Object.keys(" + obj.js() + ")");
    }

    /**
     * Object.values() - returns array of values.
     */
    public static Val objectValues(Val obj) {
        return new Val("Object.values(" + obj.js() + ")");
    }

    // ==================== For...of Loops ====================

    /**
     * Creates a for...of statement (use in Func body via unsafeRaw).
     *
     * @param varName the loop variable
     * @param iterable the iterable to loop over
     * @param body the loop body statements
     * @return JavaScript code as a string
     */
    public static String forOf(String varName, Val iterable, String... body) {
        StringBuilder sb = new StringBuilder("for(const ").append(varName)
            .append(" of ").append(iterable.js()).append("){");
        for (String stmt : body) {
            sb.append(stmt);
            if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
        }
        return sb.append("}").toString();
    }

    /**
     * Creates a for await...of statement (use in async Func body via unsafeRaw).
     *
     * @param varName the loop variable
     * @param asyncIterable the async iterable to loop over
     * @param body the loop body statements
     * @return JavaScript code as a string
     */
    public static String forAwaitOf(String varName, Val asyncIterable, String... body) {
        StringBuilder sb = new StringBuilder("for await(const ").append(varName)
            .append(" of ").append(asyncIterable.js()).append("){");
        for (String stmt : body) {
            sb.append(stmt);
            if (!stmt.endsWith(";") && !stmt.endsWith("}")) sb.append(";");
        }
        return sb.append("}").toString();
    }

    // ==================== Range Helpers ====================

    /**
     * Creates a range generator: function*(start, end, step)
     * Returns a generator function that yields numbers from start to end.
     */
    public static Val range(int start, int end) {
        return new Val("(function*(){for(let i=" + start + ";i<" + end + ";i++)yield i})()");
    }

    /**
     * Creates a range generator with step: function*(start, end, step)
     */
    public static Val range(int start, int end, int step) {
        String op = step > 0 ? "<" : ">";
        String inc = step > 0 ? "i+=" + step : "i-=" + (-step);
        return new Val("(function*(){for(let i=" + start + ";i" + op + end + ";" + inc + ")yield i})()");
    }

    /**
     * Creates a range generator (dynamic): function*(start, end)
     */
    public static Val range(Val start, Val end) {
        return new Val("(function*(){for(let i=" + start.js() + ";i<" + end.js() + ";i++)yield i})()");
    }

    // ==================== Infinite Iterators ====================

    /**
     * Creates an infinite counter generator starting from 0.
     */
    public static Val count() {
        return new Val("(function*(){let i=0;while(true)yield i++})()");
    }

    /**
     * Creates an infinite counter generator starting from start.
     */
    public static Val count(int start) {
        return new Val("(function*(){let i=" + start + ";while(true)yield i++})()");
    }

    /**
     * Creates an infinite counter with step.
     */
    public static Val count(int start, int step) {
        return new Val("(function*(){let i=" + start + ";while(true){yield i;i+=" + step + "}})()");
    }

    /**
     * Creates an infinite cycle of the given iterable.
     */
    public static Val cycle(Val iterable) {
        return new Val("(function*(){const arr=[..." + iterable.js() + "];while(true)for(const x of arr)yield x})()");
    }

    /**
     * Repeats a value infinitely.
     */
    public static Val repeat(Object value) {
        return new Val("(function*(){while(true)yield " + JS.toJs(value) + "})()");
    }

    /**
     * Repeats a value n times.
     */
    public static Val repeat(Object value, int n) {
        return new Val("(function*(){for(let i=0;i<" + n + ";i++)yield " + JS.toJs(value) + "})()");
    }

    // ==================== Utility Functions ====================

    /**
     * Chains multiple iterables sequentially.
     */
    public static Val chain(Val... iterables) {
        StringBuilder sb = new StringBuilder("(function*(){");
        for (Val iter : iterables) {
            sb.append("yield* ").append(iter.js()).append(";");
        }
        return new Val(sb.append("})()").toString());
    }

    /**
     * Zips multiple iterables together: zip([1,2], ['a','b']) -> [[1,'a'], [2,'b']]
     */
    public static Val zip(Val... iterables) {
        StringBuilder sb = new StringBuilder("(function*(){const iters=[");
        for (int i = 0; i < iterables.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(iterables[i].js()).append("[Symbol.iterator]()");
        }
        sb.append("];while(true){const vals=iters.map(it=>it.next());");
        sb.append("if(vals.some(v=>v.done))break;yield vals.map(v=>v.value)}})()");
        return new Val(sb.toString());
    }

    /**
     * Enumerates an iterable with index: enumerate(['a','b']) -> [[0,'a'], [1,'b']]
     */
    public static Val enumerate(Val iterable) {
        return new Val("(function*(){let i=0;for(const x of " + iterable.js() + ")yield[i++,x]})()");
    }

    /**
     * Enumerates an iterable starting from a specific index.
     */
    public static Val enumerate(Val iterable, int start) {
        return new Val("(function*(){let i=" + start + ";for(const x of " + iterable.js() + ")yield[i++,x]})()");
    }
}
