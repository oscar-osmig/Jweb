package com.osmig.Jweb.framework.async;

import com.osmig.Jweb.framework.security.CspNonce;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Carries render-scoped {@code ThreadLocal} state onto the threads that run
 * async blocks — Suspense data loaders and streamed block renders. Values are
 * captured on the requesting thread when the block is created, restored on
 * the executing thread around the block, and always cleared afterwards (the
 * executor's threads must never leak one render's state into another).
 *
 * <p>The framework registers its own contexts (the CSP nonce); anything else
 * holding per-render {@code ThreadLocal}s — like the docs app's version
 * context — registers a {@link Propagator} once, typically from a static
 * initializer, and every async render path picks it up from then on.</p>
 */
public final class RenderContexts {

    /** One ThreadLocal-backed context that must survive the async thread hop. */
    public interface Propagator {

        /** Reads the value to carry; runs on the requesting thread. May return null. */
        Object capture();

        /** Installs the captured value; runs on the executing thread before the block. */
        void restore(Object snapshot);

        /** Removes the context; runs on the executing thread after the block. */
        void clear();
    }

    private static final List<Propagator> PROPAGATORS = new CopyOnWriteArrayList<>();

    static {
        // The request's CSP nonce: inline scripts rendered inside an async
        // block must be stamped like everything else on the page
        register(new Propagator() {
            @Override public Object capture() { return CspNonce.current(); }
            @Override public void restore(Object snapshot) {
                if (snapshot != null) CspNonce.set((String) snapshot);
            }
            @Override public void clear() { CspNonce.clear(); }
        });
    }

    private RenderContexts() {}

    /** Adds a context to carry across every async render from now on. */
    public static void register(Propagator propagator) {
        PROPAGATORS.add(propagator);
    }

    /**
     * Captures every registered context <b>now</b>, on the calling thread;
     * the returned supplier restores them around the block wherever it runs.
     */
    public static <T> Supplier<T> carry(Supplier<T> block) {
        Propagator[] propagators = PROPAGATORS.toArray(new Propagator[0]);
        Object[] snapshots = captureAll(propagators);
        return () -> {
            for (int i = 0; i < propagators.length; i++) propagators[i].restore(snapshots[i]);
            try {
                return block.get();
            } finally {
                for (int i = propagators.length - 1; i >= 0; i--) propagators[i].clear();
            }
        };
    }

    /**
     * {@link #carry(Supplier)} for {@code Callable} blocks (data loaders).
     * Named distinctly — a bare lambda would be ambiguous between the two.
     */
    public static <T> Callable<T> carryCallable(Callable<T> block) {
        Propagator[] propagators = PROPAGATORS.toArray(new Propagator[0]);
        Object[] snapshots = captureAll(propagators);
        return () -> {
            for (int i = 0; i < propagators.length; i++) propagators[i].restore(snapshots[i]);
            try {
                return block.call();
            } finally {
                for (int i = propagators.length - 1; i >= 0; i--) propagators[i].clear();
            }
        };
    }

    private static Object[] captureAll(Propagator[] propagators) {
        Object[] snapshots = new Object[propagators.length];
        for (int i = 0; i < propagators.length; i++) snapshots[i] = propagators[i].capture();
        return snapshots;
    }
}
