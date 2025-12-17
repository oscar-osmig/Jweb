package com.osmig.Jweb.framework.template;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.vdom.VNode;

/**
 * Base interface for reusable UI components.
 *
 * Templates are the principal way to organize your web apps.
 * They allow you to create reusable components with a simple, memorable syntax.
 *
 * Example - Creating a reusable Card component:
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
 * Example - Using the Card component:
 * <pre>
 * div(class_("container"),
 *     new Card("Welcome", "Hello World!"),
 *     new Card("Features", "Build apps in pure Java")
 * )
 * </pre>
 *
 * Templates can be composed:
 * - Layouts wrap pages with common structure (header, footer, nav)
 * - Pages represent entire routes
 * - Partials are reusable pieces (cards, buttons, forms)
 *
 * Recommended app structure:
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
     */
    Element render();

    @Override
    default VNode toVNode() {
        return render().toVNode();
    }
}
