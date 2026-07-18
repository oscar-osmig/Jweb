# JWeb — Improvements, Fixes & Technical Debt

**Audit date:** 2026-07-18
**Scope:** Full repository — framework core, security, demo/docs app, build & configuration, tests, documentation.
**Build status:** `./mvnw compile` succeeds against Spring Boot 4.0.0 / Java 21. However `mvnw` is committed without its executable bit, and the test suite is a single empty `contextLoads()`.

This document catalogs concrete problems and improvement opportunities found during a read-through of the codebase. Items are grouped by area and ranked by severity. Line numbers are approximate and refer to the state of the repo at audit time.

> **Update (2026-07-18) — critical reactive-pipeline fixes landed on this branch.**
> The core round-trip now works end-to-end and is safe: the client runtime is
> injected and unified on a single HTTP transport (`fetch` → `/jweb/event`),
> state contexts survive past render with an access-based TTL, event handlers
> are scoped per-session behind unguessable context ids (no cross-user
> execution, no unbounded global leak), the event thread-local is always
> released in a `finally`, and the hydration `<script>` payload is escaped.
> Verified by a new `ReactivePipelineTest` and an HTTP smoke test.
> Resolved items: **C1, C2, C3, C4, C6**, plus **H5** (state listener
> thread-safety), **L2** (reflected handler id), and **C8**'s broken
> `contextLoads()` test.
>
> **Update (2026-07-18, round 2) — security, correctness & build hardening.**
> Also now fixed: **H1** (stack traces gated behind `jweb.errors.detail`, off by
> default; errors logged server-side instead of `printStackTrace`), **H2**
> (WebSocket no longer allows all origins — same-origin by default, allowlist
> via `jweb.websocket.allowed-origins`), **H3** (admin login/logout now
> CSRF-protected, logout is POST, login rate-limited per IP), **H7** (HTML5
> doctype emitted for every page), **H10** (`/docs/content` tolerates a missing
> section), **H13** (`mvnw` is executable in git), **M6/L1** (admin authz checks
> the `admin` role; token compared in constant time), **M18** (MongoDB version
> managed by the Boot BOM), **M19** (Dockerfile runs non-root, correct port,
> plus a `.dockerignore`). Added a **CI workflow** (`.github/workflows/ci.yml`)
> and `HardeningTest`. Verified with the full test suite and an HTTP smoke test
> (doctype present, CSRF field rendered, un-tokened login rejected, rate limit
> returns 429, 500s show no stack trace). Remaining items below are untouched.
>
> **Update (2026-07-18, round 3) — leaks & CLI codegen.** Also now fixed:
> **H11** (per-instance `Cache` cleanup task that leaked every cache replaced
> with one global sweeper over a weak registry, so unreferenced caches are
> collected), **M8** (`Cache.getOrSet` is now atomic per key — the supplier
> runs once under a concurrent-miss stampede), **M7** (tracked `Jobs` are
> auto-evicted a short while after they finish instead of accumulating
> forever), and **H8/H9** (`jweb new` now generates a project that compiles:
> `Routes` matches `configure(JWeb app)`, the pom declares the real JWeb
> dependency and Boot 4/Java 21, the app scans both the framework and app
> packages, `detectProject` reads the project's own groupId not the parent's,
> and a broken `MainLayout` template was fixed). Verified: full suite green
> (15 tests, incl. new `CacheTest` and `JWebCliTest`), a generated project
> compiles end-to-end against the framework, and the app still boots and
> serves. Remaining items below are untouched.

---

## Executive summary

JWeb is an ambitious "build web apps in pure Java" framework — a React-like DSL (type-safe HTML/CSS/JS), reactive `State<T>`, a virtual DOM, routing/middleware, MongoDB integration, security helpers, and ~200 documentation section classes. The static-rendering path (server renders HTML from Java) is broad and works. But three categories of problem dominate:

1. **The headline reactive feature is currently non-functional end-to-end.** The client runtime script is never injected into pages, the server destroys state contexts at the end of the request that renders them (invalidating the `contextId` handed to the client), and two competing event transports (HTTP POST vs WebSocket) are both half-wired. A real browser cannot complete a round-trip event today.

2. **Shared mutable/static state leaks memory and bleeds across users.** A global, never-evicted handler registry; thread-local contexts and portals never cleared on pooled servlet threads; unbounded caches, job maps, and connection maps. Several of these are also security problems (cross-user handler execution).

3. **Almost no safety net.** One empty test across 346 source files, no CI, and several injection points (hydration script tag, inline JS escaping) that the otherwise-correct body-text escaping doesn't cover.

The recommended order of work: **(A)** decide on one event transport and make the reactive round-trip actually work; **(B)** make handler/context/portal lifetimes explicit (per-session, access-based TTL, unguessable IDs, cleared in `finally`); **(C)** close the injection gaps and add CSRF/auth to state-changing and event endpoints; **(D)** stand up CI plus a starter test suite; **(E)** clean up build/docs hygiene.

---

## Critical

### C1. The client interactivity pipeline is never wired into rendered pages
- **Where:** `framework/js/JWebRuntime.java:32` (`getScriptTag()` has zero callers); `framework/events/EventHandler.java:73` (emits `onclick="JWeb.call('h_1', event)"`); `framework/server/JWebController.java` (`injectHydrationData` never injects the runtime).
- **Problem:** Every `onClick(...)` renders markup that references a `JWeb` global object that is never defined on the page. The runtime that *would* define it speaks WebSocket to `/jweb`, while `framework/server/JWebEventController.java` expects HTTP POST to `/jweb/event` — two transports, neither connected to the render path.
- **Why it matters:** The framework's core selling point (server-driven reactivity) cannot work in a real browser.
- **Fix:** Inject `JWebRuntime.getScriptTag()` during hydration injection, and commit to **one** transport (recommend HTTP POST for simplicity, or WS for push) end-to-end.

### C2. State contexts are destroyed at the end of the request that created them
- **Where:** `framework/server/JWebController.java:109` and `:250` call `context.clearContext()` in a `finally`; `buildHydrationScript` embeds `context.getSessionId()` for later event calls.
- **Problem:** The `contextId` handed to the client is invalidated the instant the page finishes rendering. Every later `StateManager.getContextById(contextId)` (`JWebEventController.java:57`, WS handler `:99`) returns `null`.
- **Why it matters:** Server-side reactive state can never survive past the initial render — compounds C1.
- **Fix:** Keep contexts alive past the render, tied to a session with an access-based TTL (see C4/C6); only evict on expiry, not in the render `finally`.

### C3. Global handler registry leaks memory and allows cross-user execution
- **Where:** `framework/events/EventRegistry.java:38-41` (`globalHandlers.put(id, handler)` into a static map; sequential IDs `h_1, h_2, …`); registered from all `onClick/onChange/...` overloads in `attributes/Attributes.java:1099+` and `elements/Tag.java:128+`. `clearSession`/`clearGlobal` have no production callers.
- **Problem:** Every page render permanently leaks one lambda per handler — plus everything each lambda captures (State objects, services, request data). IDs are guessable, and `/jweb/event` executes any ID with no auth/CSRF.
- **Why it matters:** OOM under sustained traffic; any client can brute-force/replay other users' handler IDs (IDOR) and mutate their captured state.
- **Fix:** Scope handlers to the render/session context, evict with it, and use unguessable random IDs. Require a session/CSRF token on the event endpoint.

### C4. Thread-local context leaks onto pooled servlet threads
- **Where:** `framework/server/JWebEventController.java:59` (`StateManager.setContext(context)` with no `try/finally`); `framework/context/Context.java` (`clear()` never called by framework code).
- **Problem:** If a handler throws, `StateManager.clearContext()` is skipped and the stale context stays bound to the Tomcat worker thread; the next unrelated request's `useState` calls register into another user's context.
- **Why it matters:** Silent cross-user state corruption.
- **Fix:** Wrap context binding in `try/finally` and always clear. Same pattern for `Context.clear()`.

### C5. `Portal` ThreadLocal is never cleared — cross-request content leakage
- **Where:** `framework/portal/Portal.java:74-76, 143-145` — `clear()` javadoc says "called automatically at end of request," but there are no callers.
- **Problem:** On pooled threads, portal content accumulates: modals duplicate on every request served by the same thread, and one user's portal content (possibly private) can render into another user's page.
- **Fix:** Call `Portal.clear()` in a `finally` in the request pipeline.

### C6. Stored XSS through the hydration `<script>` data block
- **Where:** `framework/hydration/HydrationData.java:73-97` (`toScriptTag` concatenates state JSON into `<script id="__JWEB_DATA__">…</script>`); `framework/hydration/VNodeSerializer.java:123-162` (`escapeJson` handles only `"`, `\`, control chars — not `<`, `>`, `/`); `framework/state/State.java:149-151` (Jackson serialization, which also does not escape `<`/`/`).
- **Problem:** A state value containing `</script><script>alert(1)</script>` terminates the tag and executes. `contextId` and each `handlers` entry are concatenated with no JSON escaping at all.
- **Why it matters:** Any app that stores user input in reactive state (a normal pattern) is exposed despite the correct body-text escaping.
- **Fix:** When embedding JSON in an inline script, escape `<`, `>`, `&`, and `/` (e.g. `<` → `<`); JSON-escape all fields.

### C7. `FormModel.bind` is a mass-assignment vulnerability
- **Where:** `framework/forms/FormModel.java:843-873`.
- **Problem:** `bind()` reflectively sets **any** declared field (walking superclasses, `setAccessible(true)`) from user form data; it does not filter by `@FormField` and does not honor `@FormIgnore`/`@FormHidden`. A POST with `role=ADMIN` or `id=1` silently overwrites those fields. Conversion failures are swallowed (`catch … ignored`), so invalid input silently no-ops.
- **Why it matters:** Privilege escalation / object tampering via extra form fields.
- **Fix:** Bind only fields annotated `@FormField` (or an explicit allowlist); skip `@FormIgnore`; collect conversion errors into a `ValidationResult`.

### C8. Effectively zero test coverage + no CI
- **Where:** `src/test/java/com/osmig/Jweb/JwebApplicationTests.java` (single empty `contextLoads()`); no `.github/workflows`.
- **Problem:** 346 source files, one trivial test, nothing runs on push/PR. The framework even ships an unused, untested `testing/` package.
- **Why it matters:** Every regression (including the C1–C7 class of bug) lands invisibly.
- **Fix:** Add a minimal CI workflow (`setup-java` 21 + `./mvnw verify`) and a starter suite over pure logic: `validation/*`, `forms/FormModel.bind/convert`, `cache/Cache`, router match, `cli` generation (golden-file). Add `resetForTesting()` hooks to the static singletons so tests aren't order-dependent.

---

## High

### H1. Full stack traces served to end users on every 500
- **Where:** `framework/server/JWebController.java:207-212` (`handleError` unconditionally renders `ErrorPage.render(500, …, e)`); `framework/server/ErrorPage.java:21-58, 47-50, 87-91` (embeds full stack trace; uses `e.printStackTrace()`).
- **Problem:** No dev/prod gate. Attackers trigger errors (e.g. `section=null` on `/docs/content`, malformed input) to harvest class names, versions, and file paths.
- **Fix:** Render stack traces only behind an explicit dev flag; generic error page in prod; log details via SLF4J.

### H2. WebSocket endpoint allows all origins with no authentication
- **Where:** `framework/websocket/JWebSocketConfig.java:31` — `setAllowedOrigins("*")` on an unauthenticated WS that executes server-side handlers.
- **Problem:** Cross-Site WebSocket Hijacking — any site a logged-in user visits can open a WS and drive server-side handlers/state on their behalf.
- **Fix:** Restrict `setAllowedOrigins` to a config-driven allowlist; validate session/JWT in `afterConnectionEstablished`.

### H3. No CSRF on state-changing endpoints (admin login/logout, events)
- **Where:** `app/Routes.java:63` (`POST /only-admin/log/in`), `:83` (`GET /only-admin/logout`); `app/pages/admin/AdminLoginPage.java:55` (form has no CSRF field); event endpoints in `JWebEventController`/`JWebSocketHandler`.
- **Problem:** The framework ships a full CSRF facility (`security/Csrf.validate`, `Csrf.tokenField`) but no route or middleware uses it. Logout is a `GET` (triggerable via `<img>`). No rate limit on login → admin-token brute force.
- **Fix:** Add `Csrf.tokenField` to forms and `Csrf.validate` on POST handlers (or a global CSRF middleware); make logout a POST; add `RateLimit` to login.

### H4. Context TTL is creation-based, not access-based
- **Where:** `framework/state/StateManager.java:47,69` — cleanup removes contexts when `now - createdAt > 5min`.
- **Problem:** A user actively clicking for 6 minutes loses their state; meanwhile every ordinary render's full state tree is retained ~5 minutes after the request.
- **Fix:** Track last-access time, touch on use, evict on idle.

### H5. Shared mutable collections iterated without synchronization
- **Where:** `framework/state/State.java:34` (`subscribers` is a plain `ArrayList` iterated in `notifySubscribers()` while `subscribe()` may run on another thread; `value`/`dirty` non-volatile); `framework/state/StateManager.java:36` (`globalListeners` plain `ArrayList`).
- **Problem:** `ConcurrentModificationException` between HTTP and WebSocket threads; visibility bugs.
- **Fix:** Use `CopyOnWriteArrayList`; make `value`/`dirty` volatile or guard them.

### H6. Catch-all `/**` controller returns `null` to "skip"
- **Where:** `framework/server/JWebController.java:73-85` — `@RequestMapping("/**")` with a hardcoded skip-list returning `null`.
- **Problem:** Spring treats a `null` return as "handled" (empty 200) rather than delegating; the skip-list (`/api/v`, `/h2-console`, `/jweb`) is brittle and misses `/jweb/event`, `/jweb/ping`, actuator, static resources.
- **Fix:** Register routes explicitly or use a low-precedence `HandlerMapping`/fallback controller pattern.

### H7. Pages render in quirks mode — no `<!DOCTYPE html>`
- **Where:** `framework/elements/DocumentElements.html()` emits `<html>…` with no doctype; `JWebController` returns `element.toHtml()` verbatim.
- **Problem:** Every JWeb page renders in browser quirks mode, causing subtle CSS/layout differences.
- **Fix:** Prepend `<!DOCTYPE html>` when the root element is `html`.

### H8. CLI-generated projects don't compile
- **Where:** `framework/cli/JWebCli.java:384-403` (`generateRoutes` emits `configure(Router router)` but `JWebRoutes` declares `configure(JWeb app)`); `:352` (generated `pom.xml` has a `<!-- Add JWeb dependency here -->` placeholder while files import `com.osmig.Jweb.framework.*`); `cli/Templates.java:292-316` (`generate crud` emits JPA code with no `spring-boot-starter-data-jpa`).
- **Problem:** Newly scaffolded projects fail to build.
- **Fix:** Align the Routes template with `JWebRoutes`, emit a real JWeb dependency coordinate, add the JPA starter for crud. A golden-file test that compiles a generated project catches all three.

### H9. `JWebCli.detectProject` picks up the parent groupId
- **Where:** `framework/cli/JWebCli.java:255-276` — takes the first `<groupId>` in the pom, which is `org.springframework.boot` from the `<parent>` block.
- **Problem:** `jweb generate page X` writes files under `src/main/java/org/springframework/boot/…`.
- **Fix:** Parse the project-level `/project/groupId` (skip the parent block) with a parent fallback.

### H10. `/docs/content` throws NPE (500) when `section` is absent
- **Where:** `app/docs/DocContent.java:9` (`switch (section)` on `null`); wired at `app/Routes.java:50` passing `ctx.query("section")` straight through.
- **Fix:** `if (section == null) return IntroSection.render();` or `Objects.requireNonNullElse(section, "intro")`.

### H11. Unbounded `Cache` instances that can never be GC'd
- **Where:** `framework/cache/Cache.java:66-74, 360-369` — every constructor calls `scheduleCleanup()` registering `this::cleanup` on a shared scheduler forever; default `maxSize` is `Integer.MAX_VALUE`; `NAMED_CACHES` registry grows without bound.
- **Problem:** Any `Cache.create()` (per-request/per-user) leaks the cache and a repeating task.
- **Fix:** Cancel the scheduled task via a returned `ScheduledFuture` + `close()`, or one global sweeper over a `WeakReference` registry; give `create()` a sane default max size.

### H12. Inline JS/toast escaping doesn't neutralize `</script>` (XSS)
- **Where:** `framework/ui/Toast.java:394-401` and `framework/elements/Elements.java:167` (`inlineScript` uses `TextElement.raw`); `framework/js/JS.java:335` (`esc()` handles only `\`, `'`, `\n`); `framework/ref/Ref.java` `escapeJs` similar; `addClass`/`toggleClass`/`setAttribute` interpolate names with no escaping.
- **Problem:** `Toast.initial(Type.ERROR, userValue)` or `JS.toJs(userString)` inside an inline `<script>` can break out with `</script>`; also missing `"`, `\r`, U+2028/U+2029.
- **Fix:** Escape `<` (e.g. `<`) and the missing characters, or JSON-encode with `<`-escaping.

### H13. `mvnw` committed without executable bit
- **Where:** repo root; git mode `100644`.
- **Problem:** Fresh clones can't run `./mvnw` (`Permission denied`); every README command fails as written. The Dockerfile papers over it with `chmod +x`.
- **Fix:** `git update-index --chmod=+x mvnw`; drop the Dockerfile `chmod`.

---

## Medium

### M1. `href`/URL attributes accept `javascript:` scheme
- **Where:** `framework/vdom/VElement.java:115-129` — `escapeAttribute` quote-escapes only, no scheme validation.
- **Problem:** `a(attrs().href(userUrl))` with `javascript:alert(1)` executes on click.
- **Fix:** Reject/neutralize `javascript:`, `data:`, `vbscript:` on URL-bearing attributes.

### M2. Rate-limit key derived from spoofable headers
- **Where:** `framework/security/RateLimit.java:282-292` — `defaultKeyExtractor` trusts `X-Forwarded-For` then `X-Real-IP` before `getRemoteAddr()`; same pattern in `framework/server/Request.java:459` (`Request.ip()`).
- **Problem:** A client rotates `X-Forwarded-For` to get a fresh bucket per request, bypassing rate limiting; forged identity in audit logs.
- **Fix:** Only honor forwarded headers from configured trusted proxies; otherwise use `getRemoteAddr()`.

### M3. JWT tokens can be minted with no expiration
- **Where:** `framework/security/Jwt.java:345-361` — `sign()` sets expiration only if provided; `create()` has no default; `parse()` doesn't require `exp`.
- **Problem:** `Jwt.create().subject(x).sign()` produces a non-expiring token; no revocation.
- **Fix:** Enforce a default/max expiration at signing; reject tokens lacking `exp` at parse time.

### M4. Route regex compiled/matched twice per request
- **Where:** `framework/routing/Router.java:48-51` calls `route.matches(...)` then `route.extractParams(path)` (`routing/Route.java:65`), which creates and matches a second `Matcher`. `Router.match` is also O(n) over all routes.
- **Fix:** Match once, extract groups from that matcher; consider a prefix/trie index for large route tables.

### M5. Template lifecycle API is entirely dead code
- **Where:** `framework/template/Template.java` defines `beforeRender`, `afterRender`, `onMount`, `onUnmount`, `pageTitle`, `metaDescription`, `extraHead`, `scripts`, `cacheable`, `cacheDuration` — zero call sites; `handlePageRoute` calls only `render()`.
- **Problem:** Users following the Javadoc get silent no-ops (e.g. `beforeRender` data loading never runs).
- **Fix:** Invoke the hooks in `JWebController.handlePageRoute`, or delete them.

### M6. `Suspense` is half-implemented
- **Where:** `framework/async/Suspense.java:73` (`timeoutMs` never read in `toVNode()` → blocking mode waits indefinitely); `:242` (non-blocking discards the future's result, so "Loading…" is permanent).
- **Fix:** Implement deferred delivery (SSE/WS patch), or document that non-blocking renders loading-only and wire the timeout.

### M7. Unbounded job/connection maps
- **Where:** `framework/async/Jobs.java:134,150` (`tasks.put(...)`, removal only via un-scheduled `cleanupCompletedTasks()`); `framework/websocket/JWebSocketHandler.java:41,44` and `framework/sse/SseBroadcaster.java:55-56` (no per-client/per-IP cap).
- **Fix:** Remove/TTL-expire completed tasks on a completion callback; cap and idle-timeout connections.

### M8. `Cache.getOrSet` is not atomic (cache stampede)
- **Where:** `framework/cache/Cache.java:151-161` — check-then-act, so concurrent callers all invoke the supplier.
- **Fix:** Use per-key `computeIfAbsent` or a lock-striped loader. Also `Cache.named(name, ttl)` (`:119-121`) silently ignores `ttl` when the cache already exists — log or throw on mismatch.

### M9. Non-standard cron semantics and a parser crash
- **Where:** `framework/async/Scheduler.java:516,533` — day-of-month and day-of-week combined with AND (standard cron uses OR when both restricted); `parseField` can't handle range-with-step (`1-5/2` → `Integer.parseInt("1-5")` → NFE). Job state fields (`paused`, `lastRun`, `runCount`, `future`) are non-volatile though read/written across threads.
- **Fix:** OR the day fields when both restricted; support `a-b/step`; make cross-thread job state `volatile`.

### M10. Query-param parsing throws 500s on bad input
- **Where:** `framework/server/Request.java:281-293` — `queryInt`/`queryLong` call `Integer/Long.parse` unguarded (`?page=abc` → 500), while `paramInt` and `queryInt(name, default)` handle it.
- **Fix:** Return `null` on unparseable input, consistent with siblings. Also `Request.body()` (`:329-341`) joins `readLine()` without newlines, corrupting multi-line bodies.

### M11. Admin endpoint checks authentication, not authorization; non-constant-time token compare
- **Where:** `app/api/AdminApi.java:26` (`adminToken.equals(token)`), `:38-40` (`isAuthenticated` true for any principal).
- **Problem:** Any authenticated principal (framework also ships `Auth`/`OAuth2`) can reach `/only-admin/messages`; timing side channel on the shared token.
- **Fix:** Check `principal.hasRole("admin")`; use `MessageDigest.isEqual`.

### M12. `ContactApi.submit` has no size limits/rate limiting and returns 200 on error
- **Where:** `app/api/ContactApi.java:16-24`.
- **Problem:** Anyone can spam multi-MB messages into Mongo; validation failure returns `{"error":…}` with HTTP 200.
- **Fix:** Cap field lengths, return 4xx, add rate limiting.

### M13. Health checks have no timeout
- **Where:** `framework/health/Health.java` — `checkInternal` runs checks inline.
- **Problem:** One hung dependency (e.g. DB) hangs the readiness probe, turning a slow dependency into a restart loop under Kubernetes.
- **Fix:** Run checks with a bounded timeout; report `DOWN(timeout)`.

### M14. `FormValidator` is half-mutable, half-immutable — a silent-drop trap
- **Where:** `framework/validation/FormValidator.java:52-57` — lambda `field(name, value, configure)` returns a new instance (sharing `legacyFields`), but legacy methods mutate `this`.
- **Problem:** Mixing the styles (natural, since siblings are mutable) silently validates nothing.
- **Fix:** Pick one paradigm; make legacy methods consistent or deprecate.

### M15. Inconsistent validator null handling
- **Where:** `framework/validation/Validators.java:77-95, 334-353` — `minLength` fails on null but `maxLength` passes; `minSize` fails, `maxSize` passes.
- **Problem:** "Optional but at least 3 chars" is impossible to express with `and()`.
- **Fix:** Non-required validators should pass null/blank (as `FieldValidator.validate` already does). Also `NumberValidators` (M-dup) is a 1:1 duplicate of the number section of `Validators` — delete one and delegate. And `Validators.url()` (`:34-36`) accepts `foo` — require the scheme or use `java.net.URI`.

### M16. `DevServer.setWatchPaths` leaks the old watcher thread
- **Where:** `framework/dev/DevServer.java:113-119, 293` — sets `watching=false` then immediately `startWatching()` (`watching=true`); the old watcher wakes, re-checks `while (watching)` → true, and both run forever.
- **Fix:** Interrupt/close the old WatchService before starting a new one; keep the executor as a field. Related: `HotReload.clientScript()` (`dev/HotReload.java:139-152`) is a placebo that can never detect a change — alias `hotReloadScript()` to the working `fast()`; and `DevServer.liveReloadScript` (`:170`) uses `arguments.callee` (illegal in strict mode).

### M17. Forms have zero CSRF integration
- **Where:** `framework/forms/Form.java` / `FormModel.java` — no reference to `security/Csrf`.
- **Fix:** Auto-inject the CSRF token as a hidden field when CSRF middleware is active.

### M18. MongoDB driver pins override Spring Boot 4's managed versions
- **Where:** `pom.xml:69-83` — `mongodb-driver-sync`, `bson`, `mongodb-driver-core` all pinned to 5.2.0.
- **Problem:** Silently downgrades below the Boot-tested version; `bson`/`mongodb-driver-core` are transitive of `mongodb-driver-sync`, inviting skew if one is bumped alone.
- **Fix:** Remove the `<version>` tags (keep only `mongodb-driver-sync`), or set the `<mongodb.version>` property if an override is truly needed.

### M19. Docker port mismatch and runs as root
- **Where:** `Dockerfile:14` (`EXPOSE 8080`) vs `src/main/resources/application.yaml:8` (`${PORT:8085}`); no `USER` directive.
- **Problem:** `docker run -p 8080:8080` hits a dead port; a compromised app has root in the container.
- **Fix:** `EXPOSE 8085` or `ENV PORT=8080`; add a non-root `USER`; add a `.dockerignore` (`target/`, `.git/`, `*.md`, `.env*`); reconcile the `--add-opens` JVM args (the container's `java -jar` drops them).

### M20. `optional=true` misused on runtime-required security deps
- **Where:** `pom.xml:86-112` — `spring-security-crypto` and the three JJWT artifacts marked `<optional>true</optional>`.
- **Problem:** `optional` only affects downstream consumers; a consumer pulling the jar gets `NoClassDefFoundError` from `security/Password.java`/`Jwt.java`.
- **Fix:** Make them compile deps, or split into a real framework module with documented opt-ins.

---

## Low

### L1. Predictable, collision-prone session IDs used as capability tokens
- **Where:** `framework/state/StateManager.java:233` — `sessionId = "ctx_" + createdAt + "_" + threadId`.
- **Problem:** Two requests in the same millisecond on a recycled thread collide; the ID is guessable yet authorizes `/jweb/event`/WS attachment.
- **Fix:** `UUID.randomUUID()` / `SecureRandom`.

### L2. JSON injection / reflected ID in event error response
- **Where:** `framework/server/JWebEventController.java:77` — `"…Handler not found: " + handlerId + "…"` interpolated raw into a JSON string.
- **Fix:** Build the response with `Json.stringify` rather than concatenation. (Content-type is JSON, so not browser-executable, but malformed output.)

### L3. User-supplied Mongo regex compiled directly (ReDoS)
- **Where:** `framework/db/mongo/MongoQuery.java:149-166` — `Pattern.compile(pattern)` on caller input.
- **Fix:** Document/limit or sanitize regex from untrusted input.

### L4. `RawContent` content types mostly ignored
- **Where:** `framework/core/RawContent.java` supports arbitrary `contentType`, but `JWebController.processResult` only checks `isJson()` and defaults everything to `text/html`.
- **Problem:** `RawContent.text(...)` served as HTML (minor injection footgun).
- **Fix:** Honor the declared content type.

### L5. Page routes silently don't support path parameters
- **Where:** `framework/routing/PageRegistry.findByPath` is exact-string only, while the legacy `Router` supports `:param`; `pages("/users/:id", …)` registers but never matches. `registerClass` captures `defaultLayout` at registration time, so `.pages(...)` before `.layout(...)` yields layout-less pages; `extractTitle("")` throws `StringIndexOutOfBoundsException`.
- **Fix:** Unify page matching with the parameterized router; resolve layout lazily; guard `extractTitle`.

### L6. Dead / duplicated code in hot paths
- **Where:** `framework/routing/Route.java:47-51` (both branches of an `if/else` identical); `framework/hydration/VNodeSerializer.java:170-230` (`toPrettyJson` duplicates the serializer with weaker escaping and appears unused); `CronJob.getInfo` computes `next` and discards it.
- **Fix:** Collapse the branch; remove or fix the pretty-printer.

### L7. API surface duplication / God classes
- **Where:** `framework/elements/El.java` and `elements/Elements.java` are parallel static-factory surfaces; `Tag.java:128-237` re-implements the event API already in `Attributes.java:1099-1430`; `framework/js/Actions.java` is ~3,997 lines / 127 statics.
- **Fix:** Consolidate on one attribute pathway; split `Actions` by concern.

### L8. `useComputed`/`useEffect` subscriptions are permanent
- **Where:** `framework/state/StateHooks.java:82-129` — each call does `dep.subscribe(...)` with no unsubscribe path.
- **Problem:** With long-lived dependencies, subscriber lists grow without bound and re-run stale computations.
- **Fix:** Return a disposable / tie subscription lifetime to the state context.

### L9. `UI` components are unthemeable and API-inconsistent
- **Where:** `framework/ui/UI.java` hardcodes `#6366f1` etc. (~40 occurrences); `Modal`/`Tabs`/`Dropdown` nest under `UI` while `Toast` is top-level; `UI` builders' `build()` returns `Element` but `Toast.Builder.build()` returns a JS `String`; `UI.tabsScript` (`:974-990`) only restyles the LINE variant (PILLS/BOXED active state never updates).
- **Fix:** Extract a palette/theme hook; align builder return types; toggle a CSS class instead of inline styles for tabs.

### L10. Repository hygiene — committed scratch artifacts, ignored-but-tracked files
- **Where:** `.gitignore:43-46` lists `prompt.md`, `PLAN.md`, `JWEB_EXAMPLES.md`, `dsl-todos.md` as ignored, but all are tracked (gitignore is inert once committed). `prompt.md` is a leftover AI multi-agent system prompt (and contradicts `STANDARD.md`: "<100 lines" vs "100–200 lines"). `img.png` (147 KB) is orphaned, referenced only by a stray `![img.png]` pasted onto a heading in `JWEB_EXAMPLES.md:17`.
- **Fix:** `git rm --cached` the scratch files (move `prompt.md` to `.claude/`); delete `img.png` and the stray fragment.

### L11. Documentation drift and sprawl
- **Where:** `README.md:3` (hand-maintained "Last Updated" timestamp — drift-bait); `:39-40` (advertises "AI Integration — Spring AI" which is commented out in `pom.xml`, and "13 Claude agents" which live in the gitignored `.claude/`); `:156` (claims MIT license with no `LICENSE` file and no `<licenses>` in the POM). Three parallel doc trees (`readme/*.md`, `framework/MODERN_ELEMENTS.md`, `framework/docs/*.md`) partially duplicate; `framework/docs/*.md` sit under `src/main/java` and aren't packaged. The docs app has two conventions: 83 section files using shared `DocComponents` plus a separate 1,585-line `DocExamples.java`, with a hand-maintained `switch` in `DocContent.java`.
- **Fix:** Add a real `LICENSE` + `<licenses>`; drop the timestamp and stale feature claims (or mark "planned"); consolidate docs under one tree; replace the `DocContent` switch with a `Map<String, Supplier<Element>>` registry and fold `DocExamples` into the section files.

### L12. Missing build guardrails and config profiling
- **Where:** `pom.xml` has no `maven-enforcer-plugin` (no `requireJavaVersion`/`dependencyConvergence`), a redundant explicit `jackson-databind` (already transitive), and `spring-dotenv:4.0.0` predates Boot 4 (may silently fail to load env vars). `src/main/resources/jweb.yaml:18-39` ships `spring.main.lazy-initialization: true` and devtools config unconditionally (not profile-gated). `dev/DevController.java` endpoints are unauthenticated (gated only by a property the CLI enables by default).
- **Fix:** Add enforcer rules; drop the redundant Jackson pin; profile-gate dev/lazy-init settings under a `dev` profile; bind dev endpoints to localhost.

---

## Suggested roadmap

1. **Make reactivity real (or scope it down honestly).** Fix C1–C4: inject the runtime, pick one transport, keep contexts alive with access-based TTL, clear thread-locals in `finally`. If full server-driven reactivity is out of near-term reach, document the framework as static-render-first and mark the reactive path experimental so users aren't misled.
2. **Close the leaks and cross-user bleed.** C3, C5, H4, H5, M7 — scope handlers/contexts/portals per session, use unguessable IDs, evict deterministically.
3. **Harden the injection and auth surface.** C6, C7, H1–H3, H12, M1–M3, M11, M17 — escape inline-script data, allowlist form binding, gate stack traces, add CSRF/auth/rate-limits.
4. **Build a safety net.** C8 + H8/H9 — CI, a starter test suite over pure logic, golden-file tests for the CLI, and `resetForTesting()` hooks on the static singletons.
5. **Clean up build & docs hygiene.** H13, M18–M20, L10–L12 — executable `mvnw`, dependency management, Docker fixes, remove scratch files, reconcile the README, add a LICENSE.
