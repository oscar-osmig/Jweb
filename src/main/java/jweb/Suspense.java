package jweb;

/**
 * Short alias for {@link com.osmig.Jweb.framework.async.Suspense} — same API,
 * shorter import: {@code import jweb.Suspense;}
 */
@SuppressWarnings("deprecation")
public class Suspense<T> extends com.osmig.Jweb.framework.async.Suspense<T> {

    protected Suspense(java.util.concurrent.Callable<T> dataLoader) {
        super(dataLoader);
    }
}
