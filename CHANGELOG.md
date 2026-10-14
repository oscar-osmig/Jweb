# Changelog

Every tagged release, newest first. Each section is self-contained: the GitHub Release for a
tag is this file's section for it (see [Cutting a release](./readme/publish.md#cutting-a-release)).
The detailed before → after tables live in the migration guides linked from each entry.

Versions follow the policy in the README: a tag is cut only for framework changes; sample-app
and docs work is committed to `main` without one.

## [Unreleased]

Work on `main` since 3.0.2. Not tagged.

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

[Unreleased]: https://github.com/oscar-osmig/Jweb/compare/v3.0.2...main
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
