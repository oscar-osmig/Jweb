package com.osmig.Jweb.framework;

/**
 * Interface for defining application routes.
 *
 * Create a Routes class and define your endpoints:
 *
 * <pre>
 * {@literal @}Component
 * public class Routes implements JWebRoutes {
 *     {@literal @}Override
 *     public void configure(JWeb app) {
 *         app.get("/", HomePage::new)
 *            .get("/about", AboutPage::new);
 *     }
 * }
 * </pre>
 *
 * @deprecated Replaced by {@code jweb.JWebRoutes} — shorter import, same API. Existing code keeps working.
 */
@Deprecated
public interface JWebRoutes {

    /**
     * Configure your application routes.
     */
    void configure(JWeb app);
}
