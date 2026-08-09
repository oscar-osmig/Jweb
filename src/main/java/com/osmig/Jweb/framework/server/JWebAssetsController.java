package com.osmig.Jweb.framework.server;

import com.osmig.Jweb.framework.js.JWebRuntime;
import com.osmig.Jweb.framework.performance.Prefetch;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * Serves the framework's client scripts as external, browser-cacheable
 * assets instead of inlining them into every page.
 *
 * <p>Pages reference these with a content-hash version parameter
 * ({@code /jweb/runtime.js?v=<hash>}), so they can be cached immutably —
 * the URL changes whenever the script content changes.</p>
 */
@Controller
public class JWebAssetsController {

    private static final CacheControl IMMUTABLE = CacheControl
            .maxAge(365, TimeUnit.DAYS)
            .cachePublic()
            .immutable();

    /** Content-hash of a script, used as its cache-busting version. */
    public static String versionOf(String script) {
        return Integer.toHexString(script.hashCode());
    }

    @GetMapping("/jweb/runtime.js")
    @ResponseBody
    public ResponseEntity<String> runtime() {
        return js(JWebRuntime.getScript());
    }

    @GetMapping("/jweb/prefetch.js")
    @ResponseBody
    public ResponseEntity<String> prefetch() {
        return js(Prefetch.clientScript());
    }

    private ResponseEntity<String> js(String script) {
        return ResponseEntity.ok()
                .cacheControl(IMMUTABLE)
                .contentType(MediaType.parseMediaType("application/javascript;charset=UTF-8"))
                .body(script);
    }
}
