package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.template.Template;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry for page routes. Plain paths resolve by O(1) lookup; paths with
 * {@code :param} segments or a {@code *} wildcard are matched in
 * registration order after the exact ones.
 */
public class PageRegistry {

    private final List<PageRoute> routes = new ArrayList<>();
    // O(1) lookup index for exact path matches
    private final Map<String, PageRoute> routeIndex = new HashMap<>();
    // Parameterised routes, matched in order when no exact route hits
    private final List<PatternRoute> patternRoutes = new ArrayList<>();
    private Class<? extends Template> defaultLayout;

    private record PatternRoute(PathPattern pattern, PageRoute route) {}

    // Cache constructor references to avoid reflection overhead per request
    private static final Map<Class<?>, Constructor<?>> constructorCache = new ConcurrentHashMap<>();

    /**
     * Sets the default layout for all pages. Also applies to pages that were
     * registered before this call (unless they already have a layout), so
     * `layout(...)` and `pages(...)` can be called in either order.
     */
    public void setDefaultLayout(Class<? extends Template> layoutClass) {
        this.defaultLayout = layoutClass;
        for (int i = 0; i < routes.size(); i++) {
            PageRoute route = routes.get(i);
            if (route.layoutClass() == null) {
                PageRoute updated = new PageRoute(route.path(), route.title(), route.pageSupplier(),
                    layoutClass, route.pageFactory());
                routes.set(i, updated);
                index(updated);
            }
        }
    }

    public Class<? extends Template> getDefaultLayout() {
        return defaultLayout;
    }

    /**
     * Registers pages using map-style syntax.
     *
     * @param pathsAndPages alternating path strings and page classes
     */
    public void register(Object... pathsAndPages) {
        if (pathsAndPages.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide path-page pairs");
        }

        for (int i = 0; i < pathsAndPages.length; i += 2) {
            String path = (String) pathsAndPages[i];
            Object pageArg = pathsAndPages[i + 1];

            if (pageArg instanceof Class<?> clazz) {
                @SuppressWarnings("unchecked")
                Class<? extends Template> pageClass = (Class<? extends Template>) clazz;
                registerClass(path, pageClass);
            } else if (pageArg instanceof Supplier<?> supplier) {
                @SuppressWarnings("unchecked")
                Supplier<? extends Template> pageSupplier = (Supplier<? extends Template>) supplier;
                PageRoute route = new PageRoute(path, extractTitle(path), pageSupplier, defaultLayout);
                addRoute(route);
            } else if (pageArg instanceof java.util.function.Function<?, ?> function) {
                @SuppressWarnings("unchecked")
                java.util.function.Function<jweb.Request, ? extends Template> factory =
                    (java.util.function.Function<jweb.Request, ? extends Template>) function;
                register(path, factory);
            } else {
                throw new IllegalArgumentException("A page is a Template class, a Supplier or a "
                    + "Function<Request, Template> — not " + (pageArg == null ? "null" : pageArg.getClass().getName()));
            }
        }
    }

    /**
     * Registers a page whose constructor needs the request — a query
     * parameter, a store lookup, a flash message:
     * {@code register("/sandbox", req -> new SandboxPage(req.query("file")))}.
     * The page keeps the default layout and every {@code Template} hook.
     */
    public void register(String path, java.util.function.Function<jweb.Request, ? extends Template> factory) {
        PageRoute route = new PageRoute(path, extractTitle(path), null, defaultLayout, factory);
        addRoute(route);
    }

    /** Registers a page made by a supplier; the same as the {@code pages(path, supplier)} pair form. */
    public void register(String path, Supplier<? extends Template> supplier) {
        PageRoute route = new PageRoute(path, extractTitle(path), supplier, defaultLayout);
        addRoute(route);
    }

    private void registerClass(String path, Class<? extends Template> pageClass) {
        // Check for @Page annotation
        Page annotation = pageClass.getAnnotation(Page.class);
        String title = annotation != null && !annotation.title().isEmpty()
            ? annotation.title()
            : extractTitle(path);

        // Cache the constructor reference for fast instantiation
        @SuppressWarnings("unchecked")
        Constructor<? extends Template> constructor = (Constructor<? extends Template>) constructorCache.computeIfAbsent(
            pageClass,
            clazz -> {
                try {
                    Constructor<?> ctor = clazz.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    return ctor;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Page class must have no-arg constructor: " + clazz.getName(), e);
                }
            }
        );

        Supplier<? extends Template> supplier = () -> {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate page: " + pageClass.getName(), e);
            }
        };

        PageRoute route = new PageRoute(path, title, supplier, defaultLayout);
        addRoute(route);
    }

    private void addRoute(PageRoute route) {
        routes.add(route);
        index(route);
    }

    private void index(PageRoute route) {
        if (route.isPattern()) {
            patternRoutes.removeIf(p -> p.route().path().equals(route.path()));
            patternRoutes.add(new PatternRoute(PathPattern.compile(route.path()), route));
        } else {
            routeIndex.put(route.path(), route);
        }
    }

    /**
     * Finds a route by exact path match in O(1) time — parameterised routes
     * are not consulted; use {@link #match(String)} for dispatch.
     *
     * @param path the path to match
     * @return the matching route, or empty if not found
     */
    public Optional<PageRoute> findByPath(String path) {
        return Optional.ofNullable(routeIndex.get(path));
    }

    /**
     * Resolves a request path to a page route: the exact index first, then
     * the parameterised routes in registration order.
     *
     * @param path the request path
     * @return the route and its captured parameters, or empty
     */
    public Optional<PageRoute.Match> match(String path) {
        PageRoute exact = routeIndex.get(path);
        if (exact != null) {
            return Optional.of(new PageRoute.Match(exact, Map.of()));
        }
        for (PatternRoute candidate : patternRoutes) {
            if (candidate.pattern().matches(path)) {
                return Optional.of(new PageRoute.Match(candidate.route(), candidate.pattern().extract(path)));
            }
        }
        return Optional.empty();
    }

    private String extractTitle(String path) {
        if (path.equals("/")) return "Home";
        String name = path.substring(1); // remove leading /
        while (name.endsWith("/")) { // tolerate trailing slashes ("/about/")
            name = name.substring(0, name.length() - 1);
        }
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        if (name.isEmpty() || name.startsWith(":") || name.equals("*")) return "Page";
        // Capitalize first letter
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public List<PageRoute> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public void clear() {
        routes.clear();
        routeIndex.clear();
        patternRoutes.clear();
    }
}
