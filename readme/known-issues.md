[← Back to README](./../README.md)

# Known Issues & Sharp Edges

A verified, code-level inventory of gaps and pitfalls. Originally compiled 2026-08-08 by
auditing every framework package; **updated 2026-08-09 after a framework-wide fix pass** that
resolved the large majority of the items. Fixed items are kept (collapsed) for history; the
short list of remaining sharp edges is what still deserves attention.

Legend: ✅ fixed 2026-08-09 · 🟡 remaining pitfall (by design or deferred)

---

## Remaining sharp edges (🟡)

By design (know them, don't "fix" them):

- **Paths starting with `/api/v` are hard-excluded from the JWeb router** (reserved for Spring
  MVC `@REST` controllers). `app.get("/api/v1/x", ...)` will never be reached — keep REST
  controllers under `/api/v*` and router/page routes elsewhere.
- **Page routes are exact-match only** — no `:param` support (use Router routes for that).
  They are GET/HEAD-only (other methods get a 405).
- **`Js.*` and `Actions.*` wildcard imports collide** (`script`, `query`, `fetch`, ...) —
  that's why they remain two facades in the `jweb` surface. Import one wildcard and qualify
  the other. (The old `CSSColors.*`+`CSSUnits.*` collisions on `lightDark`/`colorMix` are
  resolved inside `jweb.Css`.)
- **`jweb.El.*` + `jweb.Css.*` share a few names** (`id`, `fill`, `style`, `em`, `s`, ...).
  Most uses resolve by arity/argument types; when the compiler reports an ambiguous
  reference, use `attrs().id(...)`-style instance calls or qualify one side
  (`Css.id(...)`).
- **Single-`String` calls to `q`/`abbr`/`blockquote`** resolve to the varargs (child)
  overload, not the `(citeUrl, ...)` overload — pass `Attributes` explicitly when you need
  the cite URL.
- **Typed input helpers stay in `Input.*`** (`Input.text("name")`, `Input.email(...)`) — they
  can't be re-exported from `El` because the names collide with `El.text()` / `El.time()` etc.
- **`jweb.yaml` sets `prefetch.hover-delay: 300`** while the code default is 100 — either is
  fine, just know yaml wins.
- The Spring AI starters in pom.xml are commented out **by design** — AI integration ships
  built-in (`framework/ai`: `AI.ask/chat/agent`, zero extra dependencies, any
  OpenAI-compatible endpoint). Don't uncomment them unless you actually want the Spring AI
  stack as an alternative.

## 2026-08-29 — short-import surface (`jweb.*`)

The whole user-facing DSL moved behind a new top-level `jweb` package (spark/j2html-style):
`jweb.El` (HTML: union of legacy `El`+`Elements`), `jweb.Css` (union of `CSS`+`CSSUnits`+
`CSSColors`+`CSSGrid`+`CSSAnimations`+`CSSVariables` + `media()`/`keyframes()`/`stylesheet()`),
`jweb.Js` (`JS`+`Events`+`Runtime`+`Async`), `jweb.Actions`, `jweb.State`, `jweb.UI`,
`jweb.Layout`, `jweb.Input`, `jweb.Form`, `jweb.Mongo`/`Schema`/`Doc`, and the types
`jweb.Element`, `jweb.Template`, `jweb.Style`, `jweb.CSSValue`, `jweb.JWeb`, `jweb.JWebRoutes`.

- The legacy `com.osmig.Jweb.framework.*` entry points are `@Deprecated` aliases — **existing
  source keeps compiling** (`LegacyImportsCompatTest` locks this in).
- The change is source-compatible but **not binary-compatible** (several parameter types
  widened to `jweb.Element`/`jweb.CSSValue`); dependents must recompile — automatic with
  JitPack source builds; locally, run a `clean` build after pulling.
- One semantic pick in the `El` merge: `data(name, value)` = `data-*` attribute (the
  `Elements` meaning); the `<data>` element is `data_(...)`.
- Internally, `CSSColors←CSSGrid←CSSAnimations←CSSVariables←CSSUnits←CSS` and
  `Async←Runtime←Events←JS` now form inheritance chains purely for static-import
  aggregation; duplicate helpers (`lightDark`, `colorMix`, `var`, `env`) resolve to the
  subclass-most declaration.
- Long-tail follow-up (same day): every specialty styles module has a `jweb.css.X`
  shell and every `JS*` browser-API module a `jweb.js.X` shell (class names unchanged,
  so migration is a pure package-prefix swap); the legacy classes are `@Deprecated`
  aliases.
- App-infrastructure sweep (pre-v1.2.0): `jweb.JWebApplication` + `jweb.api.*`
  (annotations re-declared with the same Spring meta-annotations — both spellings
  work), `jweb.Middlewares`, `jweb.Response`, `jweb.Csrf`/`Auth`, `jweb.DevServer`,
  `jweb.I18n`, `jweb.OpenApi`, `jweb.Jobs`, `jweb.Suspense`, `jweb.Streamed`,
  `jweb.FileUpload`, `jweb.TypedRoute`, `jweb.JWebTest`, `jweb.Validators`,
  `jweb.Seo`, `jweb.Cache`, `jweb.Page`, and `jweb.el.*` for the element helper
  modules. For value types the factories now hand out `jweb` subtypes so either
  name works in declarations: `jweb.state.State` (from `useState`),
  `jweb.CsrfToken` (from `Csrf.getOrCreateToken`), `jweb.FormValidator` (from
  `create()`), `jweb.SseEmitter` (from `create()`).
- Still intentionally long: `Request`, `Principal`, `UploadedFile`, `ValidationResult`,
  `Doc`-as-declared-type, `RouteHandler`, `Middleware` — types you receive or
  implement where a rename would break override/lambda matching. Handlers infer
  `req` without any import; use `var` for the rest.

Fixed in the 2026-08-09 follow-up pass:

- ✅ `app.layout(...)` can now be called before **or after** `.pages(...)` — setting the
  default layout retroactively applies to already-registered pages without one.
- ✅ Multiple `JWebRoutes` beans configure in a **deterministic order**: `@Order`/`Ordered`
  first, then alphabetical by bean name.
- ✅ `Actions` script `build()` **auto-injects the helpers** (`$_`, `esc`, `fmtDate`) when the
  generated code uses them — `withHelpers()` is optional (and idempotent).
- ✅ `Actions.promiseAll(...)` emits a true parallel `Promise.all` (strips leading `await`
  from each action); new `Actions.promiseAllThen(...)` returns a chainable
  `Async.PromiseBuilder` for `.then(...)`.
- ✅ `showMessage(...)` is themeable: it sets `jweb-msg-<type>` classes and reads
  `--jweb-msg-<type>-bg`/`-fg` CSS variables (with the old colors as fallbacks).
- ✅ `JWebTest.test(...)` exercises **page routes** too (through the middleware stack, with
  lifecycle hooks), and returns 405 for method mismatches like production dispatch.
- ✅ JWeb router handlers can **return SSE emitters** (JWeb's `SseEmitter` or Spring's) — the
  controller streams them through Spring MVC.

---

## ✅ The reactive-state loop (fixed)

The full loop — browser event → WebSocket → Java handler → state change → DOM patch — now
works end-to-end:

- `JWebRuntime.getScriptTag()` is injected into every rendered page (after the hydration
  data). Opt out with `jweb.runtime.enabled: false`.
- Render contexts survive the request: the controller only detaches the ThreadLocal; the
  registry entry lives until the TTL reaper collects it (5 min idle, refreshed on every
  WebSocket access via `touch()`).
- `useComponent(id, () -> element)` (in `StateHooks`) registers a reactive region: it renders
  a `<div id=...>` wrapper and re-renders on the server when state changes during an event,
  patched into the DOM via `domUpdate`.
- `JWeb.setState()` is now a real protocol message (`setState`) handled by the server;
  `initState` reads the page's actual context (not a dead ThreadLocal); the client populates
  `dataset` (so `Event.data(name)` works), handles `initState`/`eventHandled`/`pong`, and
  `StateBinding.bindInput` gives two-way input binding via `data-state-input`.
- Handler registration is context-scoped with unguessable IDs (`h_<n>_<random>`), and
  handlers are evicted when their context dies — no more unbounded `EventRegistry` growth.
- Context IDs are UUID-based (unguessable).
- Enter/leave `Transition` data attributes are emitted and consumed by the runtime
  (`JWeb.leave(el)` runs leave classes then removes the node).

## ✅ Security (fixed)

- **Page routes and 404s run through the middleware stack** — auth, CSRF, headers, logging,
  and metrics now see all traffic. Page routes are GET/HEAD only (405 otherwise).
- **Header middlewares work for every result type**: they queue headers on the `Request`
  (`req.responseHeader(...)`) and `JWebController` applies them to the final response —
  `Element`/`String` results included. `etag()` also hashes Element/String bodies.
- **Error pages no longer leak stack traces**: generic message by default; set
  `jweb.dev.debug: true` to see exception details. Errors log via `Log.error`.
- **`Auth.customAuth`/`Auth.bearerAuth` now reject** (401) when no principal; the old
  attach-only behavior moved to `Auth.attachPrincipal(...)`.
- **WebSocket origins default to same-origin**; allow others with
  `jweb.websocket.allowed-origins` (comma-separated, `*` for dev).
- **`Middlewares.rateLimit` evicts expired windows** (bounded memory).
- **`Context`/`Portal`/`I18n` thread-locals are cleared per request** by `JWebController`
  (`Context.clear()`/`Portal.clear()` now `remove()` the ThreadLocal).
- **AdminApi** uses constant-time comparison (`MessageDigest.isEqual`) and per-IP login
  rate-limiting (5 failures / 15 min).
- **Hydration JSON escapes `<`** so `</script>` in state values can't break out of the
  script tag (both `HydrationData.toScriptTag` and `VNodeSerializer`).

## ✅ API bugs (fixed)

| Where | Fix |
|-------|-----|
| `FetchResult.asList(Class<T>)` | Resolves the element type at runtime (`Json.parseList(json, type)`) — returns real typed objects. |
| `MongoQuery.where("id", ...)` | Maps `id` → `_id` with ObjectId conversion, matching the update/delete builders. |
| `Suspense.timeout(n, unit)` | Blocking mode now enforces the timeout (error element on expiry). |
| `Style.prop(String)` | New 1-arg overload splits `"property:value"` strings — the six property-string CSS modules' examples now compile. |
| `PageRegistry.extractTitle` | Trailing slashes tolerated (no more `StringIndexOutOfBoundsException`). |
| `Request.body()` | Reads raw chars — newlines preserved. |
| `Request.queryInt/queryLong` (no-default) | Return null on bad input instead of throwing (consistent with `param*`). |
| `Transition` leave animations | Leave classes/durations emitted as data attributes; runtime consumes them. |
| `I18n.middleware()` | No longer clears the locale before lazy elements render (framework clears at end of request). |
| `Prefetch.setCacheTtl/setHoverDelay` | Invalidate the cached script. |
| `Cache.getOrSet` | Atomic per key (`compute`) — no thundering herd. |
| `PopoverElements.autoPopover/manualPopover` | Single-pass creation — `Attr` children are kept. |
| `Navigation.minifiedJs()` | Line-based conservative stripping that never touches lines containing quotes. |

## ✅ Formerly-phantom subsystems (wired)

- **Mongo `Schema`** — `register()` registers with `Mongo` (public API), constraints
  (`required`, `min`/`max`, `minLength`/`maxLength`, `pattern`, `enum_`, defaults, embedded)
  are validated on `Mongo.save`/`insert` (throws `Schema.ValidationException`), and declared
  indexes + unique fields are created on connect (`Mongo.ensureIndexes`).
- **`Template` lifecycle hooks** — `beforeRender`/`afterRender` run for page routes and for
  Templates returned from Router handlers; `pageTitle` overrides the layout title and the
  `<title>` tag; `metaDescription`/`extraHead` inject into the head; `scripts`/`onMount`/
  `onUnmount` inject before `</body>`; `cacheable()`/`cacheDuration()` drive Cache-Control.
- **`@Page(path=...)`** — `app.scanPages("com.example.pages")` scans a package and registers
  annotated Templates at their declared paths.
- **`OpenApi.scan(package)`** — real classpath scan for `@REST` classes.
- **Javadocs corrected** — `JWeb`/`JWebAutoConfiguration` (`@REST` et al.), `Csrf`
  (`Middlewares.csrf()`), `Jwt` path scoping, `Toast.toastScript()`, `Ref` (`inputRef.focus()`),
  `FormModel.bindFromParameterMap`, `ErrorHandler` real method names.

## ✅ Routing / dispatch (fixed)

- Router method mismatch → **405 with an `Allow` header** (`Router.allowedMethods`).
- **HEAD is served by GET handlers**; `PATCH` routes supported (`app router .patch(...)`).
- **Middleware path scoping understands globs**: `"/api/**"`, `"/files/*.png"`, plus plain
  prefixes.
- `processResult`: POJOs are serialized with Jackson (real JSON); JSON-shaped strings are
  served as `application/json`.
- Metrics/logging see page-route and 404 traffic (middleware runs for them).

## ✅ DSL (fixed)

- `El` now re-exports: conditionals (`when`/`match`/`cond`/`otherwise`), `errorBoundary`,
  `hr`, `video`/`audio`/`canvas`/`iframe`/`track`, popover members, `srcset`/`responsiveImg`/
  `lazyImg`.
- `option(String)` 1-arg form (value = text).
- VDOM attribute values escape `'` as well as `"`.

## ✅ Dead code & config (cleaned)

- Deleted: `state/ComponentRegistry` (duplicate of `StateContext.registerComponent`),
  `server/JWebEventController` (transport with no client), the six orphaned `Style*` mixin
  interfaces, `Route`'s duplicate `/`-branch, unused imports (`Middlewares`, `Link`).
- `state/StateBinding` and `hydration/*` are no longer dead — the runtime consumes them.
- `JWebSocketHandler.sessionContextMap` is used as a contextId fallback for events.
- Dockerfile `EXPOSE 8085` (matches the app default).
- `jweb.api.base` removed from `application.yaml` (nothing read it).
- Stale `AutoConfiguration.imports` comment fixed; configuration metadata now covers
  `jweb.admin.*`, `jweb.data.mongo.*`, `jweb.dev.debug`, `jweb.runtime.enabled`,
  `jweb.websocket.allowed-origins`, `jweb.markitdown.*`.
- Inert `.gitignore` rules for tracked docs replaced with an explanatory note.
- The test suite runs without MongoDB (`jweb.data.enabled=false` for `contextLoads`), and
  covers Route matching, middleware ordering/scoping/headers, the state loop
  (context lifetime, scoped handlers, `useComponent`), Schema validation, and the DSL fixes
  (the full suite now stands at 107 tests, including the AI module and
  `LegacyImportsCompatTest`).

## Companion documents (cleaned 2026-08-09)

- `dsl-todos.md` — the 12 shipped modules (6 CSS, 6 HTML) checked off with their commit.
- `JWEB_EXAMPLES.md` — imports switched to the `El` facade (matching app code); broken
  inline-image heading removed.
- `prompt.md` — file-size limit aligned with STANDARD.md (100–200 lines).
- `framework/MODERN_ELEMENTS.md` — phantom `attrs().onclick(...)`/`oninput(...)` calls
  replaced with the real `attrs().set("onclick", ...)` API.
