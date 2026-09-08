package com.osmig.Jweb.framework.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A route path compiled once: {@code :name} segments capture a parameter,
 * {@code *} matches the rest, a trailing slash is tolerated. Shared by
 * router routes and page routes.
 */
public final class PathPattern {

    private final String path;
    private final Pattern regex;
    private final List<String> paramNames;

    private PathPattern(String path) {
        this.path = path;
        List<String> names = new ArrayList<>();
        StringBuilder sb = new StringBuilder("^");
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) continue;
            sb.append("/");
            if (segment.startsWith(":")) {
                names.add(segment.substring(1));
                sb.append("([^/]+)");
            } else if (segment.equals("*")) {
                sb.append(".*");
            } else {
                sb.append(Pattern.quote(segment));
            }
        }
        sb.append("/?$");
        this.regex = Pattern.compile(sb.toString());
        this.paramNames = Collections.unmodifiableList(names);
    }

    public static PathPattern compile(String path) {
        return new PathPattern(path);
    }

    /** Whether {@code path} has parameters or a wildcard (needs matching, not lookup). */
    public static boolean isPattern(String path) {
        return path.contains("/:") || path.contains("*");
    }

    public String path() {
        return path;
    }

    public List<String> paramNames() {
        return paramNames;
    }

    public boolean matches(String candidate) {
        return regex.matcher(candidate).matches();
    }

    /** The captured parameters for a matching path (empty when it does not match). */
    public Map<String, String> extract(String candidate) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = regex.matcher(candidate);
        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }
        return params;
    }
}
