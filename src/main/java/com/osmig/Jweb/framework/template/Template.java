package com.osmig.Jweb.framework.template;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.js.Actions.Action;
import com.osmig.Jweb.framework.server.Request;
import com.osmig.Jweb.framework.vdom.VNode;

import java.util.Optional;

/**
 * Base interface for reusable UI components.
 *
 * <p>Templates are the principal way to organize your web apps.
 * They allow you to create reusable components with a simple, memorable syntax.</p>
 *
 * <h2>Example - Creating a reusable Card component:</h2>
 * <pre>
 * public class Card implements Template {
 *     private final String title;
 *     private final String content;
 *
 *     public Card(String title, String content) {
 *         this.title = title;
 *         this.content = content;
 *     }
 *
 *     public Element render() {
 *         return div(class_("card"),
 *             h3(title),
 *             p(content)
 *         );
 *     }
 * }
 * </pre>
 *
 * <h2>Example - Using the Card component:</h2>
 * <pre>
 * div(class_("container"),
 *     new Card("Welcome", "Hello World!"),
 *     new Card("Features", "Build apps in pure Java")
 * )
 * </pre>
 *
 * <h2>Lifecycle Hooks</h2>
 * <p>Templates can implement lifecycle hooks for setup, data loading, and cleanup:</p>
 * <pre>
 * public class DataPage implements Template {
 *     private List&lt;User&gt; users;
 *
 *     &#64;Override
 *     public void beforeRender(Request request) {
 *         // Load data before rendering
 *         users = userService.findAll();
 *     }
 *
 *     &#64;Override
 *     public Element render() {
 *         return ul(each(users, u -&gt; li(u.getName())));
 *     }
 *
 *     &#64;Override
 *     public String onMount() {
 *         // JavaScript to run after DOM is ready
 *         return "console.log('Page mounted');";
 *     }
 * }
 * </pre>
 *
 * <h2>Recommended app structure:</h2>
 * <pre>
 * app/
 *   layouts/     - Page layouts (MainLayout, AdminLayout)
 *   pages/       - Full pages (HomePage, AboutPage)
 *   partials/    - Reusable components (Card, Nav, Footer)
 *   Routes.java  - Route definitions
 *   App.java     - Entry point
 * </pre>
 */
public interface Template extends Element {

    /**
     * Renders this template to an Element.
     * Override this method to define your component's structure.
     *
     * @return the rendered Element
     */
    jweb.Element render();

    @Override
    default VNode toVNode() {
        return render().toVNode();
    }

    // ==================== Lifecycle Hooks ====================

    /**
     * Called before render() is invoked.
     * Use this for data loading, validation, or setup.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public void beforeRender(Request request) {
     *     this.userId = request.requireParamInt("id");
     *     this.user = userService.findById(userId);
     * }
     * </pre>
     *
     * @param request the HTTP request (may be null for non-page templates)
     */
    default void beforeRender(Request request) {
        // Override to add setup logic
    }

    /**
     * Called after render() returns.
     * Use this for cleanup or post-processing.
     *
     * @param request the HTTP request (may be null for non-page templates)
     */
    default void afterRender(Request request) {
        // Override to add cleanup logic
    }

    /**
     * An Action to run when the component mounts (DOM ready). It is wrapped
     * in a DOMContentLoaded listener for you.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public Action onMount() {
     *     return all(call("initCharts"), call("setupEventListeners"));
     * }
     * </pre>
     *
     * @return the Action to run, or null for none
     */
    default Action onMount() {
        return null;
    }

    /**
     * An Action to run when the component unmounts (beforeunload or SPA
     * navigation) — cleanup such as cancelling timers.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public Action onUnmount() {
     *     return call("clearInterval", v("refreshTimer"));
     * }
     * </pre>
     *
     * @return the Action to run, or null for none
     */
    default Action onUnmount() {
        return null;
    }

    /**
     * Returns the page title for this template.
     * Used by layouts to set the document title.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public Optional&lt;String&gt; pageTitle() {
     *     return Optional.of("User Profile - " + user.getName());
     * }
     * </pre>
     *
     * @return the page title, or empty to use default
     */
    default Optional<String> pageTitle() {
        return Optional.empty();
    }

    /**
     * Returns meta description for SEO.
     *
     * @return the meta description, or empty to skip
     */
    default Optional<String> metaDescription() {
        return Optional.empty();
    }

    /**
     * Returns additional head elements (meta tags, links, scripts).
     * These are merged into the document head.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public Optional&lt;Element&gt; extraHead() {
     *     return Optional.of(fragment(
     *         meta("og:title", getTitle()),
     *         css("/css/page-specific.css")
     *     ));
     * }
     * </pre>
     *
     * @return head elements, or empty for none
     */
    default Optional<Element> extraHead() {
        return Optional.empty();
    }

    /**
     * A script for this template, added at the end of the body. Any
     * {@code Action} works; a whole {@code actions()} builder is one too.
     *
     * <p>Example:</p>
     * <pre>
     * &#64;Override
     * public Optional&lt;Action&gt; scripts() {
     *     return Optional.of(
     *         actions().add(onClick("btn").does(toggle("panel")))
     *     );
     * }
     * </pre>
     *
     * @return the script Action, or empty for none
     */
    default Optional<Action> scripts() {
        return Optional.empty();
    }

    /**
     * Indicates if this template should be cached.
     * Override to disable caching for dynamic content.
     *
     * @return true if cacheable (default), false to disable caching
     */
    default boolean cacheable() {
        return true;
    }

    /**
     * Returns cache duration in seconds.
     * Only used if cacheable() returns true.
     *
     * @return cache duration in seconds (default: 0 = no explicit cache header)
     */
    default int cacheDuration() {
        return 0;
    }
}
