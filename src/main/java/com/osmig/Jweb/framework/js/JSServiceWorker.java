package com.osmig.Jweb.framework.js;

import com.osmig.Jweb.framework.js.JS.Val;
import com.osmig.Jweb.framework.js.JS.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Worker API for offline support, caching, and background operations.
 *
 * <p>Usage:</p>
 * <pre>
 * import static com.osmig.Jweb.framework.js.JSServiceWorker.*;
 *
 * // Register service worker
 * register("/sw.js")
 *     .onSuccess(callback("reg").log(str("Service Worker registered")))
 *     .onError(callback("err").log(str("Registration failed"), variable("err")))
 *     .build();
 *
 * // Service worker lifecycle (in sw.js)
 * onInstall()
 *     .waitUntil(
 *         openCache("v1").then(
 *             callback("cache").ret(
 *                 variable("cache").invoke("addAll", array("/", "/app.css", "/app.js"))
 *             )
 *         ).toVal()
 *     )
 *     .build();
 *
 * // Fetch intercept
 * onFetch()
 *     .respondWith(
 *         cacheMatch(request()).then(
 *             callback("response").ret(
 *                 variable("response").or(fetch(request()))
 *             )
 *         ).toVal()
 *     )
 *     .build();
 *
 * // Cache operations
 * openCache("v1")
 *     .then(callback("cache")
 *         .call(variable("cache").invoke("add", str("/page.html")))
 *     )
 *     .build();
 * </pre>
 */
public final class JSServiceWorker {
    private JSServiceWorker() {}

    // ==================== Registration ====================

    /**
     * Registers a service worker.
     *
     * @param scriptUrl the URL of the service worker script
     * @return a RegistrationBuilder for configuring the registration
     */
    public static RegistrationBuilder register(String scriptUrl) {
        return new RegistrationBuilder(scriptUrl);
    }

    /**
     * Registers a service worker with dynamic URL.
     *
     * @param scriptUrl the service worker script URL expression
     * @return a RegistrationBuilder for configuring the registration
     */
    public static RegistrationBuilder register(Val scriptUrl) {
        return new RegistrationBuilder(scriptUrl);
    }

    /**
     * Gets the service worker ready promise: navigator.serviceWorker.ready
     */
    public static Val ready() {
        return new Val("navigator.serviceWorker.ready");
    }

    /**
     * Gets the active service worker controller: navigator.serviceWorker.controller
     */
    public static Val controller() {
        return new Val("navigator.serviceWorker.controller");
    }

    /**
     * Gets a specific registration by scope: navigator.serviceWorker.getRegistration(scope)
     */
    public static Val getRegistration(String scope) {
        return new Val("navigator.serviceWorker.getRegistration('" + JS.esc(scope) + "')");
    }

    /**
     * Gets all registrations: navigator.serviceWorker.getRegistrations()
     */
    public static Val getRegistrations() {
        return new Val("navigator.serviceWorker.getRegistrations()");
    }

    /**
     * Checks if service workers are supported: ('serviceWorker' in navigator)
     */
    public static Val isSupported() {
        return new Val("('serviceWorker' in navigator)");
    }

    // ==================== ServiceWorkerRegistration Methods ====================

    /**
     * Updates a service worker registration: registration.update()
     */
    public static Val update(Val registration) {
        return new Val(registration.js() + ".update()");
    }

    /**
     * Unregisters a service worker: registration.unregister()
     */
    public static Val unregister(Val registration) {
        return new Val(registration.js() + ".unregister()");
    }

    /**
     * Gets installing worker: registration.installing
     */
    public static Val installing(Val registration) {
        return new Val(registration.js() + ".installing");
    }

    /**
     * Gets waiting worker: registration.waiting
     */
    public static Val waiting(Val registration) {
        return new Val(registration.js() + ".waiting");
    }

    /**
     * Gets active worker: registration.active
     */
    public static Val active(Val registration) {
        return new Val(registration.js() + ".active");
    }

    /**
     * Gets registration scope: registration.scope
     */
    public static Val scope(Val registration) {
        return new Val(registration.js() + ".scope");
    }

    /**
     * Gets updateViaCache setting: registration.updateViaCache
     */
    public static Val updateViaCache(Val registration) {
        return new Val(registration.js() + ".updateViaCache");
    }

    // ==================== Service Worker Context (self) ====================

    /**
     * Gets the service worker self reference: self
     */
    public static Val self() {
        return new Val("self");
    }

    /**
     * Skips waiting: self.skipWaiting()
     */
    public static Val skipWaiting() {
        return new Val("self.skipWaiting()");
    }

    /**
     * Claims clients: self.clients.claim()
     */
    public static Val claimClients() {
        return new Val("self.clients.claim()");
    }

    /**
     * Gets all clients: self.clients.matchAll()
     */
    public static Val matchAllClients() {
        return new Val("self.clients.matchAll()");
    }

    /**
     * Gets all clients with options: self.clients.matchAll(options)
     */
    public static Val matchAllClients(Val options) {
        return new Val("self.clients.matchAll(" + options.js() + ")");
    }

    /**
     * Gets a specific client: self.clients.get(id)
     */
    public static Val getClient(Val id) {
        return new Val("self.clients.get(" + id.js() + ")");
    }

    /**
     * Opens a window: self.clients.openWindow(url)
     */
    public static Val openWindow(String url) {
        return new Val("self.clients.openWindow('" + JS.esc(url) + "')");
    }

    /**
     * Opens a window with dynamic URL: self.clients.openWindow(url)
     */
    public static Val openWindow(Val url) {
        return new Val("self.clients.openWindow(" + url.js() + ")");
    }

    // ==================== Lifecycle Events ====================

    /**
     * Creates an install event listener.
     */
    public static InstallEventBuilder onInstall() {
        return new InstallEventBuilder();
    }

    /**
     * Creates an activate event listener.
     */
    public static ActivateEventBuilder onActivate() {
        return new ActivateEventBuilder();
    }

    /**
     * Creates a fetch event listener.
     */
    public static FetchEventBuilder onFetch() {
        return new FetchEventBuilder();
    }

    /**
     * Creates a message event listener.
     */
    public static MessageEventBuilder onMessage() {
        return new MessageEventBuilder();
    }

    /**
     * Creates a push event listener.
     */
    public static PushEventBuilder onPush() {
        return new PushEventBuilder();
    }

    /**
     * Creates a sync event listener.
     */
    public static SyncEventBuilder onSync() {
        return new SyncEventBuilder();
    }

    /**
     * Creates a notificationclick event listener.
     */
    public static NotificationClickEventBuilder onNotificationClick() {
        return new NotificationClickEventBuilder();
    }

    // ==================== Cache API ====================

    /**
     * Opens a cache: caches.open(cacheName)
     */
    public static CachePromise openCache(String cacheName) {
        return new CachePromise("caches.open('" + JS.esc(cacheName) + "')");
    }

    /**
     * Opens a cache with dynamic name: caches.open(cacheName)
     */
    public static CachePromise openCache(Val cacheName) {
        return new CachePromise("caches.open(" + cacheName.js() + ")");
    }

    /**
     * Matches a request in any cache: caches.match(request)
     */
    public static CachePromise cacheMatch(Val request) {
        return new CachePromise("caches.match(" + request.js() + ")");
    }

    /**
     * Matches a request in any cache: caches.match(url)
     */
    public static CachePromise cacheMatch(String url) {
        return new CachePromise("caches.match('" + JS.esc(url) + "')");
    }

    /**
     * Checks if a cache exists: caches.has(cacheName)
     */
    public static Val hasCache(String cacheName) {
        return new Val("caches.has('" + JS.esc(cacheName) + "')");
    }

    /**
     * Deletes a cache: caches.delete(cacheName)
     */
    public static Val deleteCache(String cacheName) {
        return new Val("caches.delete('" + JS.esc(cacheName) + "')");
    }

    /**
     * Gets all cache names: caches.keys()
     */
    public static Val cacheKeys() {
        return new Val("caches.keys()");
    }

    // ==================== Cache Instance Methods ====================

    /**
     * Adds a request to cache: cache.add(request)
     */
    public static Val cacheAdd(Val cache, String url) {
        return new Val(cache.js() + ".add('" + JS.esc(url) + "')");
    }

    /**
     * Adds a request to cache: cache.add(request)
     */
    public static Val cacheAdd(Val cache, Val request) {
        return new Val(cache.js() + ".add(" + request.js() + ")");
    }

    /**
     * Adds multiple requests to cache: cache.addAll(requests)
     */
    public static Val cacheAddAll(Val cache, Val requests) {
        return new Val(cache.js() + ".addAll(" + requests.js() + ")");
    }

    /**
     * Puts a request/response pair in cache: cache.put(request, response)
     */
    public static Val cachePut(Val cache, Val request, Val response) {
        return new Val(cache.js() + ".put(" + request.js() + "," + response.js() + ")");
    }

    /**
     * Matches a request in cache: cache.match(request)
     */
    public static Val cacheMatchRequest(Val cache, Val request) {
        return new Val(cache.js() + ".match(" + request.js() + ")");
    }

    /**
     * Matches all requests in cache: cache.matchAll(request)
     */
    public static Val cacheMatchAll(Val cache, Val request) {
        return new Val(cache.js() + ".matchAll(" + request.js() + ")");
    }

    /**
     * Matches all requests in cache: cache.matchAll()
     */
    public static Val cacheMatchAll(Val cache) {
        return new Val(cache.js() + ".matchAll()");
    }

    /**
     * Deletes a request from cache: cache.delete(request)
     */
    public static Val cacheDeleteRequest(Val cache, Val request) {
        return new Val(cache.js() + ".delete(" + request.js() + ")");
    }

    /**
     * Gets all cache keys: cache.keys()
     */
    public static Val cacheRequestKeys(Val cache) {
        return new Val(cache.js() + ".keys()");
    }

    // ==================== Fetch Event Properties ====================

    /**
     * Gets the fetch event request: event.request
     */
    public static Val request() {
        return new Val("event.request");
    }

    /**
     * Gets request URL: event.request.url
     */
    public static Val requestUrl() {
        return new Val("event.request.url");
    }

    /**
     * Gets request method: event.request.method
     */
    public static Val requestMethod() {
        return new Val("event.request.method");
    }

    /**
     * Gets request headers: event.request.headers
     */
    public static Val requestHeaders() {
        return new Val("event.request.headers");
    }

    /**
     * Gets client ID: event.clientId
     */
    public static Val clientId() {
        return new Val("event.clientId");
    }

    // ==================== Push Event ====================

    /**
     * Gets push event data: event.data
     */
    public static Val pushData() {
        return new Val("event.data");
    }

    /**
     * Gets push data as text: event.data.text()
     */
    public static Val pushText() {
        return new Val("event.data.text()");
    }

    /**
     * Gets push data as JSON: event.data.json()
     */
    public static Val pushJson() {
        return new Val("event.data.json()");
    }

    /**
     * Gets push data as ArrayBuffer: event.data.arrayBuffer()
     */
    public static Val pushArrayBuffer() {
        return new Val("event.data.arrayBuffer()");
    }

    // ==================== Push Subscription ====================

    /**
     * Subscribes to push notifications: registration.pushManager.subscribe(options)
     */
    public static Val subscribePush(Val registration, Val options) {
        return new Val(registration.js() + ".pushManager.subscribe(" + options.js() + ")");
    }

    /**
     * Gets push subscription: registration.pushManager.getSubscription()
     */
    public static Val getPushSubscription(Val registration) {
        return new Val(registration.js() + ".pushManager.getSubscription()");
    }

    /**
     * Gets permission state: registration.pushManager.permissionState(options)
     */
    public static Val pushPermissionState(Val registration, Val options) {
        return new Val(registration.js() + ".pushManager.permissionState(" + options.js() + ")");
    }

    /**
     * Unsubscribes from push: subscription.unsubscribe()
     */
    public static Val unsubscribePush(Val subscription) {
        return new Val(subscription.js() + ".unsubscribe()");
    }

    /**
     * Gets subscription endpoint: subscription.endpoint
     */
    public static Val subscriptionEndpoint(Val subscription) {
        return new Val(subscription.js() + ".endpoint");
    }

    /**
     * Gets subscription keys: subscription.getKey(name)
     */
    public static Val subscriptionKey(Val subscription, String name) {
        return new Val(subscription.js() + ".getKey('" + JS.esc(name) + "')");
    }

    // ==================== Background Sync ====================

    /**
     * Registers a background sync: registration.sync.register(tag)
     */
    public static Val registerSync(Val registration, String tag) {
        return new Val(registration.js() + ".sync.register('" + JS.esc(tag) + "')");
    }

    /**
     * Gets sync tags: registration.sync.getTags()
     */
    public static Val getSyncTags(Val registration) {
        return new Val(registration.js() + ".sync.getTags()");
    }

    /**
     * Gets sync event tag: event.tag
     */
    public static Val syncTag() {
        return new Val("event.tag");
    }

    /**
     * Checks if this is the last retry: event.lastChance
     */
    public static Val lastChance() {
        return new Val("event.lastChance");
    }

    // ==================== Notification Event ====================

    /**
     * Gets notification from event: event.notification
     */
    public static Val eventNotification() {
        return new Val("event.notification");
    }

    /**
     * Gets notification action: event.action
     */
    public static Val notificationAction() {
        return new Val("event.action");
    }

    /**
     * Closes notification: event.notification.close()
     */
    public static Val closeNotification() {
        return new Val("event.notification.close()");
    }

    /**
     * Shows notification: registration.showNotification(title, options)
     */
    public static Val showNotification(Val registration, String title, Val options) {
        return new Val(registration.js() + ".showNotification('" + JS.esc(title) + "'," + options.js() + ")");
    }

    /**
     * Gets notifications: registration.getNotifications(options)
     */
    public static Val getNotifications(Val registration, Val options) {
        return new Val(registration.js() + ".getNotifications(" + options.js() + ")");
    }

    /**
     * Gets notifications: registration.getNotifications()
     */
    public static Val getNotifications(Val registration) {
        return new Val(registration.js() + ".getNotifications()");
    }

    // ==================== Builders ====================

    /**
     * Builder for service worker registration.
     */
    public static class RegistrationBuilder {
        private final String scriptUrl;
        private String scope;
        private String updateViaCache;
        private Func onSuccessFunc, onErrorFunc;

        RegistrationBuilder(String scriptUrl) {
            this.scriptUrl = "'" + JS.esc(scriptUrl) + "'";
        }

        RegistrationBuilder(Val scriptUrl) {
            this.scriptUrl = scriptUrl.js();
        }

        public RegistrationBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public RegistrationBuilder updateViaCache(String mode) {
            this.updateViaCache = mode;
            return this;
        }

        public RegistrationBuilder onSuccess(Func callback) {
            this.onSuccessFunc = callback;
            return this;
        }

        public RegistrationBuilder onError(Func callback) {
            this.onErrorFunc = callback;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("navigator.serviceWorker.register(");
            sb.append(scriptUrl);

            if (scope != null || updateViaCache != null) {
                sb.append(",{");
                boolean first = true;
                if (scope != null) {
                    sb.append("scope:'").append(JS.esc(scope)).append("'");
                    first = false;
                }
                if (updateViaCache != null) {
                    if (!first) sb.append(",");
                    sb.append("updateViaCache:'").append(updateViaCache).append("'");
                }
                sb.append("}");
            }

            sb.append(")");

            if (onSuccessFunc != null) {
                sb.append(".then(").append(onSuccessFunc.toExpr()).append(")");
            }
            if (onErrorFunc != null) {
                sb.append(".catch(").append(onErrorFunc.toExpr()).append(")");
            }

            return new Val(sb.toString());
        }
    }

    /**
     * Builder for install event handler.
     */
    public static class InstallEventBuilder {
        private final List<String> waitUntilPromises = new ArrayList<>();
        private String body;

        public InstallEventBuilder waitUntil(Val promise) {
            waitUntilPromises.add("event.waitUntil(" + promise.js() + ")");
            return this;
        }

        public InstallEventBuilder skipWaiting() {
            waitUntilPromises.add("event.waitUntil(self.skipWaiting())");
            return this;
        }

        public InstallEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('install',function(event){");
            for (String promise : waitUntilPromises) {
                sb.append(promise).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for activate event handler.
     */
    public static class ActivateEventBuilder {
        private final List<String> waitUntilPromises = new ArrayList<>();
        private String body;

        public ActivateEventBuilder waitUntil(Val promise) {
            waitUntilPromises.add("event.waitUntil(" + promise.js() + ")");
            return this;
        }

        public ActivateEventBuilder claimClients() {
            waitUntilPromises.add("event.waitUntil(self.clients.claim())");
            return this;
        }

        public ActivateEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('activate',function(event){");
            for (String promise : waitUntilPromises) {
                sb.append(promise).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for fetch event handler.
     */
    public static class FetchEventBuilder {
        private String respondWithExpr;
        private String body;

        public FetchEventBuilder respondWith(Val response) {
            this.respondWithExpr = "event.respondWith(" + response.js() + ")";
            return this;
        }

        public FetchEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('fetch',function(event){");
            if (respondWithExpr != null) {
                sb.append(respondWithExpr).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for message event handler.
     */
    public static class MessageEventBuilder {
        private Func handler;
        private String body;

        public MessageEventBuilder handler(Func callback) {
            this.handler = callback;
            return this;
        }

        public MessageEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('message',");
            if (handler != null) {
                sb.append(handler.toExpr());
            } else {
                sb.append("function(event){");
                if (body != null) {
                    sb.append(body);
                }
                sb.append("}");
            }
            sb.append(")");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for push event handler.
     */
    public static class PushEventBuilder {
        private String waitUntilExpr;
        private String body;

        public PushEventBuilder waitUntil(Val promise) {
            this.waitUntilExpr = "event.waitUntil(" + promise.js() + ")";
            return this;
        }

        public PushEventBuilder showNotification(String title, Val options) {
            this.waitUntilExpr = "event.waitUntil(self.registration.showNotification('" +
                JS.esc(title) + "'," + options.js() + "))";
            return this;
        }

        public PushEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('push',function(event){");
            if (waitUntilExpr != null) {
                sb.append(waitUntilExpr).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for sync event handler.
     */
    public static class SyncEventBuilder {
        private String waitUntilExpr;
        private String body;

        public SyncEventBuilder waitUntil(Val promise) {
            this.waitUntilExpr = "event.waitUntil(" + promise.js() + ")";
            return this;
        }

        public SyncEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('sync',function(event){");
            if (waitUntilExpr != null) {
                sb.append(waitUntilExpr).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Builder for notificationclick event handler.
     */
    public static class NotificationClickEventBuilder {
        private String waitUntilExpr;
        private String body;

        public NotificationClickEventBuilder waitUntil(Val promise) {
            this.waitUntilExpr = "event.waitUntil(" + promise.js() + ")";
            return this;
        }

        public NotificationClickEventBuilder openWindow(String url) {
            this.waitUntilExpr = "event.waitUntil(self.clients.openWindow('" + JS.esc(url) + "'))";
            return this;
        }

        public NotificationClickEventBuilder body(String code) {
            this.body = code;
            return this;
        }

        public Val build() {
            StringBuilder sb = new StringBuilder("self.addEventListener('notificationclick',function(event){");
            sb.append("event.notification.close();");
            if (waitUntilExpr != null) {
                sb.append(waitUntilExpr).append(";");
            }
            if (body != null) {
                sb.append(body);
            }
            sb.append("})");
            return new Val(sb.toString());
        }
    }

    /**
     * Promise builder for cache operations.
     */
    public static class CachePromise {
        private final String base;
        private final List<String> chain = new ArrayList<>();

        CachePromise(String base) {
            this.base = base;
        }

        public CachePromise then(Func handler) {
            chain.add(".then(" + handler.toExpr() + ")");
            return this;
        }

        public CachePromise catch_(Func handler) {
            chain.add(".catch(" + handler.toExpr() + ")");
            return this;
        }

        public CachePromise finally_(Func handler) {
            chain.add(".finally(" + handler.toExpr() + ")");
            return this;
        }

        public Val toVal() {
            StringBuilder sb = new StringBuilder(base);
            for (String c : chain) {
                sb.append(c);
            }
            return new Val(sb.toString());
        }

        public String build() {
            return toVal().js();
        }
    }
}
