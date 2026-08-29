package jweb;

/**
 * A reusable page or component with lifecycle hooks.
 *
 * <pre>{@code
 * import jweb.Element;
 * import jweb.Template;
 * import static jweb.El.*;
 *
 * public class HomePage implements Template {
 *     @Override
 *     public Element render() {
 *         return div(h1(text("Welcome")));
 *     }
 * }
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.template.Template} —
 * both names work everywhere in the framework.</p>
 */
public interface Template extends com.osmig.Jweb.framework.template.Template {
}
