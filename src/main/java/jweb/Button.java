package jweb;

/**
 * Dedicated button builder — variants, sizes and typed click handlers:
 *
 * <pre>{@code
 * import jweb.Button;
 *
 * Button.of("Save").primary().onClick(save("form"))
 * Button.submit("Send").large()
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.elements.Button} — the
 * same statics under the short import. The builder is an {@link Element}.</p>
 */
public class Button extends com.osmig.Jweb.framework.elements.Button {

    protected Button() {}
}
