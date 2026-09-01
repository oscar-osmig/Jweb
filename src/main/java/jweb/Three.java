package jweb;

/**
 * Declarative 3D scenes rendered with three.js — declare the scene graph the
 * way you declare HTML, and the client runtime handles renderer setup,
 * sizing, the render loop and disposal:
 *
 * <pre>{@code
 * import static jweb.Three.*;
 *
 * div(class_("hero"),
 *     scene(style().height(px(420)),
 *         camera().position(0, 1.5, 4).orbit(),
 *         directionalLight().position(3, 5, 2),
 *         box().color("#10b981").spin()
 *     )
 * )
 * }</pre>
 *
 * <p>three.js loads lazily — only pages containing a scene fetch the bundle.
 * {@code scene(...)} returns a regular element, so HTML attributes chain on
 * it, and {@code .id("hero")} exposes the live three.js objects to scripts
 * at {@code JWebThree.get('hero')}.</p>
 */
public class Three extends com.osmig.Jweb.framework.three.Three {

    protected Three() {}
}
