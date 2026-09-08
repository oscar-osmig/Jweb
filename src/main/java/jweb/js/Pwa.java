package jweb.js;

import jweb.Action;
import jweb.Element;
import jweb.JWeb;
import jweb.Response;
import org.springframework.http.MediaType;

import static jweb.js.JsOpts.quote;

/**
 * The three pieces that make a JWeb app installable — the manifest, the tag
 * that points at it, and the service-worker registration:
 *
 * <pre>{@code
 * import jweb.js.Manifest;
 * import static jweb.js.Pwa.*;
 *
 * static final Manifest APP = manifest("JWeb Demo")
 *     .shortName("JWeb").display("standalone").startUrl("/")
 *     .themeColor("#4f46e5").icon("/icon-192.png", "192x192");
 *
 * // routes
 * serve(app, APP);
 *
 * // page head
 * head(title("JWeb"), link())
 *
 * // page script
 * inlineScript(actions().does(registerServiceWorker("/sw.js")).build())
 * }</pre>
 */
public final class Pwa {

    private Pwa() {}

    /** Where {@link #serve} publishes and {@link #link} points by default. */
    public static final String PATH = "/manifest.webmanifest";

    /** Starts a manifest for an app with this name. */
    public static Manifest manifest(String name) {
        return new Manifest(name);
    }

    /** The {@code <link rel="manifest">} for the default path. */
    public static Element link() {
        return link(PATH);
    }

    /** The {@code <link rel="manifest">} for a path you serve yourself. */
    public static Element link(String url) {
        return jweb.El.link(jweb.El.attr("rel", "manifest"), jweb.El.href(url));
    }

    /** Publishes the manifest at {@link #PATH}. */
    public static JWeb serve(JWeb app, Manifest manifest) {
        return serve(app, manifest, PATH);
    }

    /** Publishes the manifest at {@code path}, with the media type browsers expect. */
    public static JWeb serve(JWeb app, Manifest manifest, String path) {
        String json = manifest.json();
        return app.get(path, request -> Response.ok()
            .contentType(MediaType.parseMediaType("application/manifest+json"))
            .body(json));
    }

    /**
     * Registers a service worker once the page has loaded — a no-op in
     * browsers (and insecure origins) without service-worker support.
     */
    public static Action registerServiceWorker(String url) {
        return () -> "if('serviceWorker' in navigator)"
            + "window.addEventListener('load',function(){"
            + "navigator.serviceWorker.register(" + quote(url) + ")"
            + ".catch(function(e){console.warn('[JWeb] service worker failed:',e)})})";
    }

    /** Unregisters every service worker for this origin. */
    public static Action unregisterServiceWorkers() {
        return () -> "if('serviceWorker' in navigator)"
            + "navigator.serviceWorker.getRegistrations().then(function(rs){"
            + "rs.forEach(function(r){r.unregister()})})";
    }
}
