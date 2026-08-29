package jweb;

import java.util.Map;

/**
 * MongoDB document builder:
 *
 * <pre>{@code
 * import static jweb.Doc.*;
 * }</pre>
 *
 * <p>Short alias for the legacy
 * {@code com.osmig.Jweb.framework.db.mongo.Doc} entry point.</p>
 */
@SuppressWarnings("deprecation")
public class Doc extends com.osmig.Jweb.framework.db.mongo.Doc {

    protected Doc(String collectionName) {
        super(collectionName);
    }

    protected Doc(String collectionName, Map<String, Object> data) {
        super(collectionName, data);
    }
}
