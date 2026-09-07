package jweb;

/**
 * Portals — render content into a named outlet elsewhere in the document
 * (modals, tooltips, toasts):
 *
 * <pre>{@code
 * import jweb.Portal;
 *
 * body(main(...), Portal.outlet("modals"))
 * Portal.to("modals", div(class_("modal"), ...))
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.portal.Portal} — the same
 * statics under the short import.</p>
 */
public class Portal extends com.osmig.Jweb.framework.portal.Portal {

    protected Portal() {}
}
