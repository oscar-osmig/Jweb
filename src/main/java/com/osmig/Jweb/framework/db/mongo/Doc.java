package com.osmig.Jweb.framework.db.mongo;

import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.Doc} — same class, shorter import.
 *             {@code Doc.of(...)} and every Mongo read return {@code jweb.Doc}.
 */
@Deprecated
public class Doc extends jweb.Doc {

    protected Doc(String collectionName) {
        super(collectionName);
    }

    protected Doc(String collectionName, Map<String, Object> data) {
        super(collectionName, data);
    }
}
