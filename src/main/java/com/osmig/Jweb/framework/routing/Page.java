package com.osmig.Jweb.framework.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Template class as a routable page.
 *
 * <pre>
 * {@code
 * @Page(path = "/about", title = "About Us")
 * public class AboutPage implements Template {
 *     public Element render() { ... }
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Page {
    /**
     * The URL path for this page.
     */
    String path() default "";

    /**
     * The page title (shown in browser tab).
     */
    String title() default "";
}
