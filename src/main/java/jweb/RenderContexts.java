package jweb;

/**
 * Registry of {@link Propagator}s that carry request-scoped state (thread
 * locals) onto the threads that render streamed and suspended fragments:
 *
 * <pre>{@code
 * import jweb.RenderContexts;
 *
 * RenderContexts.register(new RenderContexts.Propagator() {
 *     public Object capture() { return CURRENT.get(); }
 *     public void restore(Object snapshot) { CURRENT.set((Tenant) snapshot); }
 * });
 * }</pre>
 *
 * <p>Short alias for {@link com.osmig.Jweb.framework.async.RenderContexts} —
 * the same statics (and the nested {@code Propagator} interface) under the
 * short import.</p>
 */
public class RenderContexts extends com.osmig.Jweb.framework.async.RenderContexts {

    protected RenderContexts() {}
}
