# Changelog

Every tagged release, newest first. Each section is self-contained: the GitHub Release for a
tag is this file's section for it (see [Cutting a release](./readme/publish.md#cutting-a-release)).
The detailed before → after tables live in the migration guides linked from each entry.

Versions follow the policy in the README: a tag is cut only for framework changes; sample-app
and docs work is committed to `main` without one.

## [Unreleased]

Nothing yet.

## [3.0.3] — 2026-10-15

The backend set. Additive over 3.0.2: nothing in a 3.0.2 app changes, except that MongoDB
users add the driver (below). Every protocol is spoken by the framework itself — Redis, SMTP,
S3, the DevTools protocol — so the dependency list is unchanged.

- **A real browser for a test.** `jweb.Browser` launches a headless Chrome and drives it over
  the DevTools protocol itself — no WebDriver, no Playwright, nothing to add: `Browser.launch()`,
  `browser.open(url)`, `click`/`type`/`fill`/`press`/`submit` as real input events,
  `waitFor`/`waitForText`/`waitForHtml`/`waitUntil` polling up to five seconds, `html`/`text`/
  `value`/`exists`/`count`/`eval`, the console and its errors, a screenshot. `Browser.available()`
  is whether a Chrome can be found (`JWEB_CHROME`, the usual locations, the `PATH`), so a suite
  gates on it with `@EnabledIf("jweb.Browser#available")` and runs it where there is one —
  GitHub's runners included. The sample app's `BrowserTest` clicks through the streaming demo:
  the runtime's script, the socket, a server handler, a bound state, a live region and a
  client-side action, in Chrome.
- **An admin, and scaffolding on SQL.** `Admin.mount(app, "/admin", todos, users)
  .guard(Auth.requireRole("admin"))`: a page per `Sql.table` that lists, searches and pages
  its rows, a form to add or edit one (the record is the form, its `Form.*` annotations the
  rules, the id hidden), a delete — templates in the page table, so the layout wraps them and
  the CSRF token rides along; read-only for a record without an `id`. `Sql.Table` gains
  `page(n, size)`, `search(text, n, size)`, `fields()`, `type()`, `hasId()` and `idOf(record)`.
  `jweb new` now gives a project its database — `spring-boot-starter-jdbc` and H2 as a file
  under `./data`, `spring.datasource.url`, `db/migration/` — and `jweb generate resource
  Todo title:string! done:boolean` writes the record (the row and the form), the migration
  (`V<next>__create_todos.sql`, next after the highest version present), a list page, a form
  page and a `JWebRoutes` bean under `/todos`; `crud` is its alias and no longer generates
  JPA. The tutorial keeps its todos in the table. Two framework fixes on the way: a POST to a
  path that also has a page route now reaches the handler registered for it (it was a 405
  whatever was registered), and `JWebTest.test` answers a guard's or handler's
  `JWebException` with its status instead of a 500. A `Form` field set to `type("hidden")`
  renders just the input.
- **A shared cache, locks, and rate limits across the fleet.** `Cache.shared(name, Type.class)`
  is a typed cache every instance sees — values as JSON, `get`/`getOrSet`/`set` with a time to
  live, `delete`, `increment`, `clear` — on `jweb.RedisCache` when `jweb.cache.redis.url` (or
  `jweb.redis.url`, which now serves PubSub too) is set, and on this instance's memory
  otherwise with the same semantics. `Cache.lock(name, ttl)` and `Cache.once(name, ttl, work)`
  are the lock the instances share: the scheduled job five instances would run five times runs
  on the one that takes it. `RateLimit` counts in the shared store on its own once one is
  configured, so a limit is per fleet, not per instance (`.shared(false)`, `.name(...)`). A
  store that cannot be reached is a miss, a no-op, "not acquired", or a request let through —
  logged, never a 500. `RedisCache` speaks the protocol itself over a few pooled connections
  (`GET`, `SET PX NX`, `DEL`, `INCRBY`+`PEXPIRE`, `SCAN`), retrying once on a dropped
  connection; the RESP client it shares with `RedisPubSub` is `jweb.Redis`. Tested against an
  in-process stand-in with expiry and, in CI, a real Redis. The multi-instance table says what
  `spring-session-jdbc` adds for sessions. Fixed on the way: the first touch of the in-memory
  `Cache` class threw `ExceptionInInitializerError` (its cleanup scheduler was declared after
  the global instance that schedules on it).
- **Request ids, traces.** Every request has an id before anything sees it (a servlet filter:
  the caller's `X-Request-ID` when sane, a fresh one otherwise), `req.id()` reads it, the
  response carries it back, the error page and the JSON error quote it, and every log line
  written during the request shows it — Spring Boot's `logging.pattern.correlation` is set
  by the framework (an `EnvironmentPostProcessor`, before logging starts) to
  `[requestId traceId] `, which prints nothing outside a request; a browser event or PubSub
  push keeps the page's `contextId` in the MDC instead. `Middlewares.logging()` is now one
  line per request with the status. `jweb.Trace`: spans with tags and errors, free without
  a sink; the framework opens `jweb.route` (the pattern), `jweb.sql` (`db.operation`,
  `db.statement`), `jweb.job`, `jweb.pubsub.deliver`, `jweb.mail.send`, `jweb.fetch` and
  `jweb.event`; `Trace.span(...)`/`Trace.in(...)` for the app's own. `ObservationSink`
  turns them into Micrometer observations when the app has an `ObservationRegistry` (the
  actuator) — timers per name, and spans in a real trace with `micrometer-tracing` and an
  exporter. Any `Trace.Sink` bean replaces it; `jweb.observability.*` turns each piece off.
  New reference topic `ref-observability`.
- **File storage.** `jweb.Storage`: `Storage.upload("avatars", file)` stores an upload under a
  generated key (`avatars/<32 hex>.png`), `put`/`get`/`bytes`/`open`/`list`/`delete` work on
  any key, `Storage.url(key)` is where a browser fetches it and `Storage.url(key, ttl)` a link
  that expires. `Storage.Local` (the default, `jweb.storage.dir`) keeps files in a directory
  and the framework serves them at `jweb.storage.path` (`/files`) with the content type, an
  ETag and 304s, `Cache-Control` and byte ranges — a route like any other, so guards apply;
  `jweb.storage.access: signed` signs every URL under `jweb.auth.secret` and the route refuses
  the rest. `Storage.S3` is any bucket that speaks the S3 API (Amazon, Cloudflare R2, MinIO,
  Backblaze B2, DigitalOcean Spaces, GCS with HMAC keys): the framework signs its own requests
  (Signature Version 4, matched against Amazon's published vectors in the suite) over the
  JDK's HTTP client, hands out presigned URLs (`uploadUrl` for a direct browser upload) or
  plain ones under `public-url`; selected by `jweb.storage.s3.bucket`. `Storage.Memory` for
  tests; any `Storage.Store` bean replaces the store. A `ResponseEntity` with a `Resource` or
  `byte[]` body now passes through the controller untouched (it used to be cast to a String
  body when a render had collected CSS).
- **The auth flows.** The user model stays the app's; `jweb.Auth` gains the mechanics every
  flow needs, stateless or on a store the instances share. `Auth.token(purpose, subject,
  ttl, fingerprint)` / `Auth.verify(...)`: HMAC-SHA256 tokens for password reset (bound to
  the password hash, so the link dies when the password changes) and email verification
  (bound to the address). `Auth.rememberCookie(principal, ttl)`, `Auth.forgetCookie()` and
  the `Auth.remember(loader)` middleware: remember me, with `Response.redirect(...).cookie(…)`
  and `Response.ok().cookie(…)` to attach a cookie. `Auth.logoutEverywhere(userId)`: a
  per-user session version checked on every `getPrincipal`, in memory or in
  `jweb_auth_versions` when `Sql` is connected. `Auth.Totp`: RFC 6238 two-factor (secret,
  `otpauth://` URI, verify with skew; the RFC's test vectors in the suite) and
  `Auth.recoveryCodes(n)`. `jweb.auth.secret` signs it all; blank generates one per start
  with a warning. `MockRequest.cookie(name, value)` for tests. A full recipe (sign-up,
  verify, login with remember-me and a second factor, forgot/reset, two-factor setup) in the
  Backend guide.
- **Durable jobs.** `Jobs.handle("welcome", Welcome.class, w -> …)` says what a named job
  does; `Jobs.enqueue("welcome", payload)` stores one (`Jobs.options().after(…)`, `.at(…)`,
  `.retries(n)`, `.key(k)` for one pending at a time). The worker, started with the app,
  polls the store, runs due jobs on virtual threads, retries a failure at 2, 4, 8… seconds
  (capped at an hour) until the attempts are spent, then keeps it `FAILED` with the error for
  `Jobs.failed()` / `Jobs.retry(id)`; a job whose worker died is released after
  `jweb.jobs.timeout`, done jobs are purged after `jweb.jobs.keep-done`. `Jobs.Memory` by
  default; `Jobs.SqlStore` (the `jweb_jobs` table, created if missing, claims by atomic
  update so a job runs once across instances) installed automatically when `Sql` is
  connected; any `Jobs.Store` bean. `Jobs.runPending()` runs what is due in a test.
- **Mail.** `Mail.to(…).subject(…).html(div(…)).send()` — the HTML DSL as the body, a text
  part derived from it, cc/bcc/replyTo, attachments, headers. With `jweb.mail.smtp.host` set
  the framework's own SMTP client sends it (EHLO, STARTTLS or implicit TLS, AUTH PLAIN or
  LOGIN, MIME with base64 parts, RFC 2047 subjects) — no mail library; without it the
  outbox keeps the last two hundred messages for `Mail.outbox()` and logs each. `.later()`
  enqueues the message as the durable job `jweb.mail`. Any `Mail.Sender` bean replaces the
  sender. Tested against an in-process SMTP stand-in.
- **PubSub: one publish, every page.** `jweb.PubSub.publish(topic, message)` re-renders
  every page that `PubSub.subscribe(topic, Type.class, handler)`-d while rendering: the
  handler runs in the page's context, the live regions reading the states it changed are
  re-rendered, and the patches go down the page's socket — the server-started half of the
  event loop (`EventDispatch.push`, `ContextSockets`). A subscription made outside a render
  is a plain listener. Messages are JSON on the wire, locally too, so a page behaves the same
  on one instance and on five. `jweb.RedisPubSub` carries topics across instances with no
  client library (it speaks RESP over a socket; `jweb.pubsub.redis.url`, `rediss://` for TLS,
  reconnect with backoff); any `PubSub.Backend` bean replaces it. `RenderedPage.awaitPush()`
  is the socket's side of a publish in a test. CI runs the Redis round-trip against a real
  Redis service; the suite covers the protocol with a stand-in.
- **OAuth2 state lives in the session, with PKCE.** `provider.authorizationUrl(req)` stores
  a fresh state and an S256 code verifier in the visitor's session (ten minutes, single use);
  `provider.callback(req)` checks the state, exchanges the code with the verifier and fetches
  the profile, and throws `OAuth2Exception` naming what went wrong (the provider's error, a
  mismatch, an expired attempt, a missing code). The callback can land on any instance.
  `.pkce(false)` per provider; the in-memory `generateState`/`verifyState` are deprecated.
- **Running more than one instance** is written down: the table in the State & Realtime
  guide says what is per-instance (render contexts → sticky sessions, `Cache`, `RateLimit`,
  `Scheduler`) and what is shared (`PubSub` via Redis, the session via Spring Session, the
  OAuth2 state with it, `Migrations` via the lock row).
- **SQL, with migrations.** `jweb.Sql` runs SQL against the app's `DataSource` — queries
  mapped to records by column name (`created_at` fills `createdAt`), positional and `:named`
  parameters, inserts that hand back the generated key, batches, `transaction(...)` with
  rollback on any exception and nesting that joins — and `Sql.table("todos", Todo.class)` for
  the CRUD a record needs (`insert` fills in the generated id, `find`/`get`/`where`/`count`/
  `update`/`save`/`delete`). `jweb.Migrations` applies `V<version>__<name>.sql` files under
  `db/migration` once each, in order, recorded with a checksum in `jweb_migrations`; an
  edited file fails the next start naming the version (`repair` for deliberate edits);
  instances starting together take turns on a lock row. Both are written against
  `javax.sql.DataSource` only: an app adds `spring-boot-starter-jdbc` and a driver and sets
  `spring.datasource.url`, and `SqlConfiguration` binds the bean and migrates before any
  runner of the app's own. `jweb.sql.migrate` / `jweb.sql.migrations` configure it; Flyway
  or Liquibase on the classpath takes over. 20 tests against H2; a reference topic
  (`ref-sql`) and a guide section.
- **MongoDB is opt-in.** The driver is now an optional dependency: an app that uses SQL, or
  no database, no longer ships `mongodb-driver-sync`, `bson` and `mongodb-driver-core`, and
  never loads a Mongo type. The startup connection moved from `JWebConfiguration` to
  `db.mongo.MongoConfiguration`, which exists only when the driver does; `jweb.data.enabled`
  on without the driver logs the dependency to add. To keep using `jweb.Mongo`, add
  `org.mongodb:mongodb-driver-sync:5.10.0` to the app's pom.
- **CI.** `.github/workflows/ci.yml` runs the full suite and builds the library jar on every
  push to `main` and every pull request (JDK 21, Node present so the generated-JavaScript
  syntax tests run), then builds the demo Docker image. Until now JitPack and the Dockerfile
  both built with `-DskipTests`, so nothing gated `main`.
- **This changelog**, and GitHub Releases for every existing tag.
- **The page tells the user when it expired or lost the server.** The runtime used to log
  `context_expired` to the console and give up reconnecting after five linear retries, so
  a page whose context was reaped (or whose server restarted) silently stopped responding.
  Now: a fixed notice with a Reload button (`jweb.runtime.expired`: `banner` default,
  `reload`, or `silent` + a `jweb:expired` event), exponential backoff 1s → 30s for
  `jweb.runtime.reconnect-attempts` (default 8) with an immediate retry when the device comes
  back online or the tab becomes visible, events fired while reconnecting held and delivered
  on reopen, and `initState` carries `known:false` so a reconnect after a restart announces
  the expiry before the first dropped click. Themeable via `--jweb-notice-*`.
- **The event loop is testable without a browser.** `JWebTest.render(template)` returns a
  `jweb.RenderedPage`: the template rendered in a live render context, `click / input /
  change / check / submit / keydown / fire` to run the server handler an element carries,
  and `html()` with the DOM patches applied. It goes through `EventDispatch`, the class the
  WebSocket handler now delegates to, so a test exercises the real protocol: handler lookup
  in the page's context only, changed states, re-rendered live regions, `context_expired`.
- **Invalid HTML nesting is reported.** `p(div(…))` compiles and used to render silently,
  leaving the browser to repair it into a DOM that no longer matched the tree. The
  serializer now checks every parent/child pair against the content model (`HtmlNesting`:
  phrasing-only parents, list / table / select structure, nested interactive content,
  `<title>` outside `<head>`) and logs each offending pair once with the reason;
  `jweb.dev.strict-html: true` fails the render instead.
- **Plural forms in i18n.** `Messages.plural("items", n)` and `I18n.plural("items", n)` read
  `items.one` / `items.few` / `items.many` / `items.other`… for the form the count takes in the
  locale, with `{0}` the count formatted for that locale. `jweb.Plural` carries the CLDR
  cardinal rules (English-like, French and Brazilian Portuguese, the East Slavic languages,
  Polish, Czech/Slovak, Arabic, Romanian, Hebrew, the no-plural languages) and takes
  registrations for the rest.
- **The docs site has search.** A field above the sidebar nav; hits as you type, each linking
  to the section and heading. The index is built from the rendered Java sections themselves
  (split at their headings, prose and code alike), so it can never drift from the content.
  `GET /docs/search?q=` is the fragment behind it.
- **A tutorial, and a `jweb new` that matches it.** `readme/tutorial.md` builds a todo app
  in seven steps — page and layout, live state, a validated record form, a `@REST`
  endpoint, tests with `JWebTest.render`, a Docker build — and every block is compiled by
  the suite. The CLI's generated project had drifted three releases behind (Spring Boot
  3.2.0, JWeb v1.1.0, `@SpringBootApplication`, a static layout with raw CSS, a `.rounded()`
  that does not exist, `@Autowired` fields on pages the registry constructs); it now takes
  its versions from the framework it runs in, uses `@JWebApplication`, `app.layout(...)
  .pages(...)`, handle-based pages and layouts with `styles()`, constructor-injected
  repositories, a Maven-image Dockerfile, and `JWebCliTest` compiles what it generates.
- **Metrics reach Micrometer.** `jweb.Metrics` gained a `Sink`; the Micrometer bridge
  (`MicrometerSink`, bound automatically when the app has a `MeterRegistry` bean, i.e.
  actuator plus a registry) mirrors every counter, timer and gauge — including the
  framework's new `jweb.contexts.active` — into Prometheus, Datadog, OTLP or whatever the
  registry exports to. `micrometer-core` is an optional dependency; without it nothing
  changes.
- **JavaScript locals as handles.** `jweb.Var` from `local("q")`: a `Val` that declares
  itself (`Q.let(expr)`, `Q.const_(expr)`), assigns itself (`Q.set`, `Q.increment()`) and is
  a callback parameter by handle (`callback(E, T)`, `func("f", E)`, `Func.let(Q, expr)`). The
  JavaScript counterpart of `Cls` and `Id`, and the first step of the 4.0 plan to retire
  `v("x")` in app code; the docs search script is written with it.
- **The 4.0 plan is written down.** `readme/roadmap-4.0.md`: what the measurements say is
  left (string identifiers in the JS DSL, the deprecated surface, `Val` as a statement, the
  component form), with a recommendation on each and the order to do them in. Nothing
  breaking ships before the tag.
- **Maven Central, ready when the namespace is.** A `central` profile adds the javadoc jar,
  GPG signing and the Central publishing plugin; `developers` and `developerConnection` join
  the pom metadata. The default JitPack build is unchanged. The remaining steps (namespace
  verification, portal token, GPG key) are listed in readme/publish.md.
- **Fixed, in the review before the tag.** The hot-reload emitters are completed on shutdown
  (a test JVM used to wait 30 seconds for them). `Redis` has a reply timeout, verifies the
  host on `rediss://`, honours a `/db` path and drops the connection on any failure.
  `Sql.table` binds lazily, `search` escapes `%` and `_`, and a `Number` fills a
  `LocalDateTime`; `Migrations` rethrows lock errors that are not duplicates; `Admin` answers
  404 on a bad id. `Storage` takes the content type from the extension, sends `nosniff`
  always and a CSP sandbox on HTML and SVG, honours `If-None-Match`/`If-Modified-Since`, and
  strips quotes and control characters from filenames. `Mail` rejects addresses with CR, LF
  or spaces (SMTP injection) and verifies the host on TLS. `Jobs` lets only the worker that
  holds a job finish it, counts a stale release as an attempt, and stores timestamps in
  UTC. A page's event dispatch, `setState` and PubSub delivery are serialized per context,
  socket sends per session. `Browser.launch` cleans up Chrome and the profile on failure;
  `RenderedPage` reads elements inside patched regions; the runtime opens one socket at a
  time and moves a live region's `<style>` to the head. `jweb generate` refuses names that
  would not compile, spells columns the way `Sql` reads them, writes `not null` once, reads
  the package from the application class, and gives the app's `Routes` `@Order(0)` so its
  layout wraps the generated pages. `HtmlNesting` allows `<a>` in `<label>` and `<title>` in
  SVG; Romanian plurals pick the right form.
- **The docs teach the typed selectors.** The styling sections and the CSS guide write
  `rule(CARD.hover(), …)` with `Cls`/`Id` handles and `Selector`, not `rule(".card:hover", …)`;
  a test keeps the string form out of the samples.

## [3.0.2] — 2026-10-08

No raw CSS or JavaScript strings. Additive over 3.0.1 (two declared types change, below).
Guide: [dsl-3.0.2.md](./dsl-3.0.2.md).

- **Handles.** `cls("card")` and `id("x")` return `jweb.Cls` / `jweb.Id`: an element
  argument, an immutable `Selector` and a JavaScript target, declared once and used by all
  three languages. `cls()` with no name mints a class scoped to the declaring class.
  `styled(rules, element)` lets a static component own its stylesheet.
- **Typed selectors in every rule.** `rule(Selector, Style)` on `Stylesheet`, `MediaQuery`,
  `ContainerQuery`, `Supports`, `Rule.of`, `startingStyle`, `scope`; `Selector` is immutable
  (every method returns a new instance); `Selector.type("pre")`, `Selector.any()`,
  `Selector.of(css)` replace the colliding starters.
- **Value builders.** `shadow()` / `insetShadow()`, `font("Segoe UI")` plus the generic
  font-family constants, `stop(color, position)`, calc arithmetic on every `CSSValue`
  (`plus / minus / times / div`), `animation(Keyframes, …)`, the missing keywords
  (`ellipsis anywhere revert listItem default_`), `propFilter` and friends, and 40 properties
  that had no setter. `jweb/CssSpecCoverageTest` keeps `css-properties.txt` and
  `css-keywords.txt` true.
- **JavaScript.** `byId / query / queryAll / dom / domAll / delegate` and every behavior take
  a `Selector`; `el.querySelector / closest / matches / addClass / removeClass / toggleClass /
  hasClass` take handles; `inlineScript(Action)`; `*Script.build()` returns `Action`.
- **Collected `scripts()`.** `Template.scripts()` is gathered from every template in a render
  (page, layout, components), deduped, and delivered with the page, swap fragments, streamed
  chunks and DOM patches — exactly like `styles()`.
- **Pages and CSRF.** `app.pages(path, req -> new Page(req.query("q")))` and
  `pages(path, Supplier)` keep pages with constructor arguments in the page table.
  `Response.html(template)` renders as a full page (runtime, hydration, scripts, styles) with
  your status and headers. `Csrf.tokenField() / tokenMeta() / token()` take no argument inside
  a render. `Middlewares.recommended()` includes `csrf()`.
- **Sample app.** Snippet gallery: submissions from the Sandbox, admin review, `/snippets`.
  `jweb/AppDslHygieneTest` fails the build on raw CSS/JS strings in app code.
- **Breaking.** `Attr x = cls("a")` becomes `Cls x = cls("a")`; `Attr.name()` on a handle is
  the class or id name; code that relied on in-place mutation of a `Selector` must use the
  returned value; `com.osmig.Jweb.framework.ref.Ref` is a deprecated alias of `jweb.Ref`.

## [3.0.1] — 2026-09-11

The 3.0 syntax, published (3.0.0 was a working version and was never tagged).
**Source- and binary-incompatible with 2.x** — recompile downstream code.
Guide: [dsl-simplification-3.md](./dsl-simplification-3.md).

- **Short names are the real names.** Every type an author can name lives in `jweb.*`
  (`Tag Attributes Attr Event Action Val Func Request Response Principal Middleware
  RouteHandler …`, `jweb.css.*`, `jweb.three.*`); the `com.osmig…` names are deprecated
  aliases. The four DSL wildcards `jweb.El.* Css.* Js.* Three.*` coexist without ambiguity.
- **HTML.** Handlers, `swap*`, `ref`, `bind` and `bindInput` are element arguments; a String
  is text wherever it appears (the String-first overloads of `a label option blockquote
  datalist optgroup abbr q` are gone); `cls / classes / classIf`, `when(c, a, b)`, one
  record-bound form system `form(X.class)`, the whole SVG element and attribute set.
- **CSS.** Page-owned stylesheets (`Template.styles()`, one nonce-stamped `<style>`);
  `:hover / :focus / dark / media` on inline styles via generated classes; the
  `row / stack / center / card` mixins; one custom-property `Theme`; the modern-CSS modules
  folded into `Css` and typed; `@starting-style`, view-transition and container at-rules.
- **JavaScript.** One import: `jweb.Js ⊇ Actions`. Behaviors as Actions (`copy`, `scrollSpy`,
  `navigate().prefetch()`, `splitPane`, `lineGutter`, `markLine`…), Web Components,
  `jweb.js.Pwa`, `syncState`; `return_()` and `preventDefault()` as statements; the
  underscore names that were not Java keywords renamed (`then elif let await in delete`);
  everything that emits JavaScript is an `Action`, never a String. The sample app contains no
  raw JavaScript.
- **State, routing, API.** `live(state, s -> el)` regions re-render on the server and morph
  into the DOM; `bind(state)` renders its value; `jweb.Session`; record-bound action routes;
  `:param` and `*` page routes; `app.guard(pattern, …)`; Spring-free `jweb.api` parameter
  annotations; no global handler fallback over the socket.
- **Three.** The walker's body (ground, solid, fly, click-to-move, spawn, pointer lock, touch,
  gamepad, footsteps), `link / near / zone` reactions, sound, wires, sweeps, terrain, spot
  lights, glass, instancing, mirrors, motion presets (`pulse glow appear delay follow`),
  structural patches (`add / remove / replace`); the runtime is three joined text blocks.
- **Docs.** Every Java sample in the docs site, the readmes and the framework topic files
  compiles (`DocSamplesCompileTest`, `ReadmeSamplesCompileTest`); the Get Started version
  picker lists the three newest releases.
- **Breaking without a compile error** — search for these: `a("/x", "Text")` renders the text
  `/xText`; `label("id", "Text")`; `option("v", "Text")`; `call / fetch / sleep` under `Js.*`
  are Actions; `span("x")` with `Css.*` imported is the element; `style().prop(module())` →
  `.apply(module())`; `span(bind(state))` renders the value; `state.update(...)` notifies;
  `button(title("x"))` still nests a `<title>` (now logged). The guide lists all sixteen.

## [2.2.3] — 2026-09-01

Three DSL, additive over 2.2.0: first-person walk mode, live scene patches over the socket
(`Three.patch`), curves, particles, bloom, mirrors. Docs registry gains v2.2.3.

## [2.2.0] — 2026-08-31

Additive minor over 2.1.0.

- **Three DSL.** New geometry (`torusKnot`, `capsule`, `disc`, `ring`, platonic solids),
  billboard and sprite camera-facing nodes, declarative hover effects, Actions-DSL mesh
  clicks, equirect environment and sky lighting, glTF animation playback, offscreen
  render-loop pause.
- **CSP.** Actions-DSL handlers survive the nonce Content-Security-Policy: definitions are
  registered in the render context and delivered as nonce-stamped scripts, elements carry
  `data-jweb-act<type>` and the runtime delegates. Raw `on<type>=` attributes are rewritten to
  the same delegation at the serializer. Streamed `useState` hydrates via its chunk; async
  blocks carry every render-scoped ThreadLocal.
- **Docs.** Version picker; prose inside shared sections can branch by version.

## [2.1.0] — 2026-08-31

Additive minor over 2.0.0.

- **The Three DSL** (`jweb.Three`): declarative 3D scenes rendered with a vendored three.js
  bundle and a scene-graph runtime — the full shape set, shadows, glTF models, clickable
  objects.
- **`/docs/tell`** serves the whole documentation set as one markdown document for AI
  assistants (`?topic=` for one file).
- **Fix.** 500 on `/docs/tell` in production: the guides were not in the Docker build
  context.

## [2.0.0] — 2026-08-30

DSL simplification: platform parity in builder style. **Source-compatible but not
binary-compatible** — recompile against the new jar. Guide:
[dsl-simplification.md](./dsl-simplification.md).

- **Six rules.** A lone String child is text; every element is `name(Object...)`; every CSS
  property takes a String as well as a typed value; a trailing underscore marks a Java keyword
  and nothing else; a block takes its body inline; platform names win.
- 139 element overloads deleted; the attribute surface moved to `HtmlAttributes` and is shared
  by `Tag` and `Attributes`; every builder chain can be terminated and keeps its type; the JS
  modules read and write properties under one name and match platform names;
  `Style.toMap()` preserves declaration order.
- **Fixes nine shipped-broken behaviours**: deaf auto-reconnect WebSockets, non-deterministic
  style ordering, invalid `:placeholder` and `&__header` output, no-op `setTimeout`,
  always-"forward" history direction, unreachable Async / Keyframe / Func terminals.
- **Breaking by hand.** `textarea("hello")` renders text (it used to set `name`);
  `pushState(state, url)` reads platform order; 29 CSS animation presets that animated
  nothing are deleted.
- Tests 107 → 216.

## [1.2.1] — 2026-08-29

`Form.create()` hands out the `jweb.Form` subtype, completing the narrowed-factory pattern
(`jweb.state.State`, `jweb.CsrfToken`, `jweb.FormValidator`, `jweb.SseEmitter`, `jweb.Form`).

## [1.2.0] — 2026-08-29

The `jweb.*` short-import surface, complete.

- `jweb.css.*` and `jweb.js.*` shells for the long-tail styles and browser-API modules (class
  names unchanged — a pure package-prefix swap).
- App infrastructure under `jweb`: `JWebApplication`, `jweb.api.*` annotations,
  `Middlewares`, `Response`, `Csrf`, `Auth`, `DevServer`, `I18n`, `OpenApi`, `Jobs`,
  `Suspense`, `Seo`, `Cache`, `TypedRoute`, `JWebTest`, `Validators`, `FileUpload`, `Page`,
  `Streamed`, `jweb.el.*`.
- Source-verified accuracy pass over all twelve readme guides and the in-app docs.

## [1.1.0] — 2026-08-29

- **`jweb.*` short imports**: `jweb.El` (HTML), `jweb.Css`, `jweb.Js`, `jweb.Actions`,
  `jweb.State`, `jweb.UI`, `jweb.Layout`, `jweb.Form`, `jweb.Mongo`; the legacy
  `com.osmig.Jweb.framework.*` entry points are deprecated aliases and keep compiling.
  Source-compatible, not binary-compatible.
- **Sandbox**: live code playground with a DSL interpreter.
- **Security**: CSRF protection on every state-changing app route, nonce-based
  Content-Security-Policy, session-fixation fix and cookie hardening, contact-form limits,
  dependency patch sweep (Spring Boot 4.0.8, MongoDB driver 5.10.0, jjwt 0.12.7).
- Responsive phone view; hover-reveal copy buttons on docs code blocks.

## [1.0.6] — 2026-08-09

First published release (JitPack). Spring Boot 4.0.7 and Jackson 3.1.4 (CVE-2026-29062).
The HTML/CSS/JS DSLs, routing, MongoDB integration, security and validation layers; the
reactive state loop wired end to end (runtime injected, contexts survive the request, live
regions patch the DOM); page routes and 404s through the middleware stack; the framework-wide
fix pass recorded in [known-issues.md](./readme/known-issues.md).

[Unreleased]: https://github.com/oscar-osmig/Jweb/compare/v3.0.3...main
[3.0.3]: https://github.com/oscar-osmig/Jweb/compare/v3.0.2...v3.0.3
[3.0.2]: https://github.com/oscar-osmig/Jweb/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/oscar-osmig/Jweb/compare/v2.2.3...v3.0.1
[2.2.3]: https://github.com/oscar-osmig/Jweb/compare/v2.2.0...v2.2.3
[2.2.0]: https://github.com/oscar-osmig/Jweb/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/oscar-osmig/Jweb/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/oscar-osmig/Jweb/compare/v1.2.1...v2.0.0
[1.2.1]: https://github.com/oscar-osmig/Jweb/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/oscar-osmig/Jweb/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/oscar-osmig/Jweb/compare/v1.0.6...v1.1.0
[1.0.6]: https://github.com/oscar-osmig/Jweb/releases/tag/v1.0.6
