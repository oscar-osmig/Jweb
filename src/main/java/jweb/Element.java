package jweb;

import com.osmig.Jweb.framework.core.Renderable;

/**
 * Represents an HTML element that can be rendered.
 * This is the primary interface for building UIs.
 *
 * <p>Elements can be:</p>
 * <ul>
 *   <li>HTML tags (div, p, span, etc.)</li>
 *   <li>Text nodes</li>
 *   <li>Templates (reusable components)</li>
 * </ul>
 *
 * <pre>{@code
 * import jweb.Element;
 * import static jweb.El.*;
 *
 * Element card = div(class_("card"), h2(text("Hello")));
 * }</pre>
 */
public interface Element extends Renderable {

    /**
     * Renders this element to HTML.
     */
    default String toHtml() {
        return toVNode().toHtml();
    }
}
