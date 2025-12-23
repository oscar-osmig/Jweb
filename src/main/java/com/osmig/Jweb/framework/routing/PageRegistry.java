package com.osmig.Jweb.framework.routing;

import com.osmig.Jweb.framework.template.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Registry for page routes with O(1) lookup.
 */
public class PageRegistry {

    private final List<PageRoute> routes = new ArrayList<>();
    // O(1) lookup index for exact path matches
    private final Map<String, PageRoute> routeIndex = new HashMap<>();
    private Class<? extends Template> defaultLayout;

    /**
     * Sets the default layout for all pages.
     */
    public void setDefaultLayout(Class<? extends Template> layoutClass) {
        this.defaultLayout = layoutClass;
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
            }
        }
    }

    private void registerClass(String path, Class<? extends Template> pageClass) {
        // Check for @Page annotation
        Page annotation = pageClass.getAnnotation(Page.class);
        String title = annotation != null && !annotation.title().isEmpty()
            ? annotation.title()
            : extractTitle(path);

        Supplier<? extends Template> supplier = () -> {
            try {
                return pageClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate page: " + pageClass.getName(), e);
            }
        };

        PageRoute route = new PageRoute(path, title, supplier, defaultLayout);
        addRoute(route);
    }

    private void addRoute(PageRoute route) {
        routes.add(route);
        routeIndex.put(route.path(), route);
    }

    /**
     * Finds a route by exact path match in O(1) time.
     *
     * @param path the path to match
     * @return the matching route, or empty if not found
     */
    public Optional<PageRoute> findByPath(String path) {
        return Optional.ofNullable(routeIndex.get(path));
    }

    private String extractTitle(String path) {
        if (path.equals("/")) return "Home";
        String name = path.substring(1); // remove leading /
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        // Capitalize first letter
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public List<PageRoute> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public void clear() {
        routes.clear();
        routeIndex.clear();
    }
}
