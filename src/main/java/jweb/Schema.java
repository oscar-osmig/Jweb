package jweb;

/**
 * MongoDB schema definition and validation:
 *
 * <pre>{@code
 * import static jweb.Schema.*;
 * }</pre>
 *
 * <p>Short alias for the legacy
 * {@code com.osmig.Jweb.framework.db.mongo.Schema} entry point.</p>
 */
@SuppressWarnings("deprecation")
public class Schema extends com.osmig.Jweb.framework.db.mongo.Schema {

    protected Schema(String collectionName) {
        super(collectionName);
    }
}
