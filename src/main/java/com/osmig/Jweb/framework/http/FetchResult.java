package com.osmig.Jweb.framework.http;

import java.util.List;
import java.util.Map;

/**
 * @deprecated Moved to {@link jweb.FetchResult} — same class, shorter import.
 *             {@code Fetch.get(url).execute()} returns {@code jweb.FetchResult}.
 */
@Deprecated
public class FetchResult extends jweb.FetchResult {

    public FetchResult(int status, String body, Map<String, List<String>> headers) {
        super(status, body, headers);
    }
}
