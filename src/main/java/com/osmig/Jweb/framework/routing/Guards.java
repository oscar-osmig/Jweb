package com.osmig.Jweb.framework.routing;

import jweb.Guard;
import jweb.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The app's guards, in registration order, each scoped to a path pattern:
 * a plain prefix ({@code /admin}), {@code /admin/**} (the prefix and
 * everything under it) or a glob with {@code *} inside one segment — the
 * same rules {@code app.use(path, middleware)} follows.
 */
public final class Guards {

    private record Entry(String pattern, Predicate<String> matches, Guard guard) {}

    private final List<Entry> entries = new ArrayList<>();

    /** Adds a guard for every path matching {@code pattern}. */
    public Guards add(String pattern, Guard guard) {
        if (pattern == null || pattern.isBlank()) throw new IllegalArgumentException("guard pattern is blank");
        if (guard == null) throw new IllegalArgumentException("guard is null");
        entries.add(new Entry(pattern, matcher(pattern), guard));
        return this;
    }

    /**
     * Runs the guards whose pattern matches the request path, in order; the
     * first non-null answer is returned, null means every guard passed.
     */
    public Object check(Request request) throws Exception {
        if (entries.isEmpty()) return null;
        String path = request.path();
        for (Entry entry : entries) {
            if (!entry.matches().test(path)) continue;
            Object answer = entry.guard().check(request);
            if (answer != null) return answer;
        }
        return null;
    }

    /** Whether any guard's pattern covers {@code path}. */
    public boolean covers(String path) {
        for (Entry entry : entries) {
            if (entry.matches().test(path)) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /** The registered patterns, in order. */
    public List<String> patterns() {
        return entries.stream().map(Entry::pattern).toList();
    }

    static Predicate<String> matcher(String pattern) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path -> path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (pattern.contains("*")) {
            String regex = Pattern.quote(pattern)
                .replace("**", "\u0000")
                .replace("*", "\\E[^/]*\\Q")
                .replace("\u0000", "\\E.*\\Q");
            Pattern compiled = Pattern.compile(regex);
            return path -> compiled.matcher(path).matches();
        }
        return path -> path.startsWith(pattern);
    }
}
