package jweb;

/**
 * Short alias for {@link com.osmig.Jweb.framework.cache.Cache} — same API,
 * shorter import: {@code import jweb.Cache;}
 */
@SuppressWarnings("deprecation")
public class Cache<K, V> extends com.osmig.Jweb.framework.cache.Cache<K, V> {

    protected Cache(java.time.Duration defaultTtl) {
        super(defaultTtl);
    }
}
