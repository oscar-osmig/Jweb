package jweb;

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
 *         app.layout(MainLayout.class)
 *            .pages("/", HomePage.class);
 *     }
 * }
 * </pre>
 */
public interface JWebRoutes {

    /**
     * Configure your application routes.
     */
    void configure(JWeb app);
}
