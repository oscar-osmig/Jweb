# DSL Simplification 3.0 — what changed

The 2026-09-04 pass over the HTML, CSS, JavaScript and Three DSLs. The 2.0 pass
([dsl-simplification.md](dsl-simplification.md)) set six rules; this pass fixes the places
where the framework's own pages could not follow them. The goals are unchanged: builder
style, names that are simple to remember, no raw JS/HTML/CSS, pure Java.

Every claim below about what compiles was checked with a scratch file that imports all four
DSL wildcards (`jweb.El.*`, `jweb.Css.*`, `jweb.Js.*`, `jweb.Three.*`) at once. That file
is the check to re-run when adding a static to any facade.

---

## What was in the way

- `id("main")` and `raw("...")` **did not compile** under the standard `El.*` + `Css.*`
  dual import, because the CSS Selector-builder starters (`select() all() tag() cls() id()`)
  lived in the `Css` facade. `select()` and `tag("x")` silently returned a `Selector`.
  This is why the sample pages wrote `attrs().id(...)` everywhere.
- No handler, swap, ref or state binding had a top-level factory, so every interactive
  element became `attrs().id().onClick().style()....done()`.
- Seven non-void elements read their first String as an attribute (`a`, `label`, `option`,
  `blockquote`, `datalist`, `optgroup`, `abbr`), so `a("Hello ", strong("world"))` compiled
  as a link to "Hello ". Nobody trusted "a String is text", and `text("…")` wrapped
  everything.
- `then_ elif_ let_ await_ in_ delete_ data_ var_ title_ style_ template_` carried an
  underscore without being Java keywords.
- `Ref`, `Toast`, `UI.Modal`, the popover helpers and the Template lifecycle hooks
  returned raw JavaScript Strings.
- `Actions` and `Js` shared `fetch(String)`, `call(String)`, `sleep(int)` with identical
  signatures, so pages qualified `jweb.Actions.toggle(...)` by hand.
- `Suspense.of` had `Callable` and `Supplier` twins, so every lambda needed a cast.
- `Template` already extended `Element`, but every sample called `.render()` on it.

## The rules now

1. **A String argument is text**, wherever it appears. Void elements keep positional
   Strings (`img(src, alt)`, `meta(name, content)`, `input(type, name)`), and the
   code-bearing `inlineScript(js)`/`style(css)` emit theirs verbatim.
2. **Every element is `name(Object...)`** — attributes, handlers, a bare `style()` and
   children in any order. `attrs()` is the long tail.
3. **Every CSS property takes a String as well as a typed value.**
4. **A trailing underscore marks a Java keyword, and nothing else.** When an element and
   an attribute (or another DSL) share a name, the common one keeps the bare name and the
   other has no shortcut.
5. **A block takes its body inline** — including `if`.
6. **Platform names win.**
7. **The four DSL imports coexist**, and **anything that emits JavaScript is an `Action`**,
   never a String.

---

## HTML

| Was | Now |
|---|---|
| `button(attrs().id("x").onClick(h).style()….done(), text("Save"))` | `button(id("x"), onClick(h), style()…, "Save")` |
| `form(attrs().id("f").action(u).method("post").swapForm(u, "#s"), …)` | `form(id("f"), action(u), method("post"), swapForm(u, "#s"), …)` |
| `input(attrs().ref(r))` | `input(ref(r))` |
| `span(attrs().data("state", s.getId()), text(String.valueOf(s.get())))` | `span(bind(s), s.get())` |
| `a("/home", "Home")` — first String was the href | `a(href("/home"), "Home")` — a String is text |
| `label("email", "Email:")` | `label(for_("email"), "Email:")` |
| `option("us", "United States")`, `option("Chrome", "Chrome")` | `option(value("us"), "United States")`, `option("Chrome")` |
| `abbr("HTML", "HyperText…")` | `abbr(attr("title", "HyperText…"), "HTML")` |
| `blockquote(citeUrl, …)`, `datalist(id, …)`, `optgroup(label, …)` | `blockquote(attr("cite", url), …)`, `datalist(id("x"), …)`, `optgroup(attr("label", "x"), …)` |
| `h1(text("Title"))` | `h1("Title")` (`text()` still exists) |
| `data_(…)`, `var_(…)` elements; `title_(…)`, `style_(…)` shortcuts; `template_` | `tag("data", …)`, `tag("var", …)`; `attrs().title(…)`, a bare `style()` argument; `template` |
| `when(c).then(a).elif(c2, b).otherwise(d)`, `match(cond(…), otherwise(…))` | deprecated — `when(c, x)`, a ternary, or a `switch` expression |
| `body(new Nav().render(), …)` | `body(new Nav(), …)` — a Template is an Element |
| `submitButton("x")` | `button(type("submit"), "x")` (the name collided with app helpers) |

New top-level factories in `jweb.El`: every `on*` handler (both `Consumer<Event>` and
`Action` forms), `on(type, handler)`, `swap`, `swapOuter`, `swapMorph`, `swapForm`,
`swapPush`, `ref`, `bind`, `bindInput`.

## CSS

| Was | Now |
|---|---|
| `id("x")` ambiguous; `select()`/`tag("x")` returned a Selector | Selector starters moved to `jweb.css.Selectors` (`import static jweb.css.Selectors.*`); a String selector `rule(".card:hover")` needs nothing |
| `raw("x")` as a CSSValue (ambiguous with HTML `raw`) | `CSSValue.of("x")` — rarely needed, every property takes a String |
| `span("sidebar")` grid line-name span (captured `span("text")`) | `gridColumn("span sidebar")` |
| `.animation(anim("name"), s(3), …)` | `.animation("name", s(3), …)` |
| `stylesheet().mediaQuery(mq, new Rule(sel, style))` | `stylesheet().add(mq.rule(sel, style))` |
| `.keyframes(k)`, `.fontFace(f)`, `.supports(s)` | `.add(k)`, `.add(f)`, `.add(s)` (old names still work) |

## JavaScript

| Was | Now |
|---|---|
| `import static jweb.Actions.*` + `import static jweb.Js.*`, collisions on `fetch/call/sleep` | one import, `import static jweb.Js.*`; `jweb.Actions` is the same surface |
| `Async.fetch("/url")`, `Async.sleep(ms)` (expression twins) | `fetch(str("/url"))` / `fetch(v("url"))`, `delay(ms)` — the bare names are the page-level Actions |
| `.if_(c).then_(a).elif_(c2).then_(b).else_(d).end()` | `.if_(c, a).elif(c2, b).else_(d)` |
| `let_`, `await_`, `awaitYield_`, `then_`, `elif_`, `in_`, `delete_` | `let`, `await`, `awaitYield`, `then`, `elif`, `in`, `delete` |
| `Actions.script()`, `query()`, `queryAll()` (deprecated aliases) | `actions()`, `dom()`, `domAll()` |
| `template("r")`, `field("x")`, `dateField`, `renderList` (client template engine) | deleted — render on the server and swap a fragment |
| `attrs().set("onclick", ref.focus())` — `Ref` returned Strings | `onClick(ref.focus())` — `Ref` methods are Actions; `ref.get("value")` is a `Val` |
| `Toast.successJs("x")` | `Toast.success("x")` (Action); `*Js` deprecated |
| `UI.Modal.openJs(id)` / `closeJs(id)` | `UI.Modal.open(id)` / `close(id)` (Actions) |
| `showPopover(id)` returned a String | returns an Action |
| `Toast.builder().action("Reload", "location.reload()")` | `.action("Reload", reload())` |
| `Template.onMount()`/`onUnmount()` returned JS Strings; `scripts()` returned `Optional<String>` | return `Action` / `Optional<Action>` (an `actions()` builder is an Action) |

An `Action` is a statement anywhere a statement goes: `.if_(cond, toggle("panel"))` works.

## Async

`Suspense.of(() -> …)` — only the `Callable` overload exists, so the cast is gone.

## Three

`plane(w, h).flat()`, `disc(r).flat()`, `ring(a, b).flat()` lay the shape on the ground
(`rotation(-90, 0, 0)`, named). Under `jweb.Js.*`, `patch(url)` is HTTP PATCH, so the
live-scene patch stays qualified: `Three.patch(sceneId)`.

## Short names are the real names

The 2026-09-07 rule: **every type an app author can name has a `jweb.*` spelling that IS the
type the framework returns or accepts.** The check is what the IDE auto-imports when you write
`X x = someJwebCall();` — if it starts with `com.osmig`, the job was not done. Until now many
`jweb.*` names were sub-type shells (a protected constructor over the framework class): fine for
statics, but `jweb.css.Stylesheet s = stylesheet()` did not compile because the framework
returned the supertype. The real classes now live under `jweb`; the old fully-qualified names
remain as `@Deprecated` aliases so existing source keeps compiling.

| Before | After | Old name is now |
|---|---|---|
| `com.osmig.Jweb.framework.elements.Tag` | `jweb.Tag` | subclass alias (all constructors) |
| `com.osmig.Jweb.framework.attributes.Attributes` | `jweb.Attributes` | subclass alias |
| `com.osmig.Jweb.framework.attributes.Attr` | `jweb.Attr` (record) | static factories only — not usable as a type |
| `com.osmig.Jweb.framework.events.Event` | `jweb.Event` | sub-interface alias |
| `com.osmig.Jweb.framework.js.Actions.Action` | `jweb.Action` | **deleted** (nested) |
| `com.osmig.Jweb.framework.js.JS.Val` | `jweb.Val` | **deleted** (nested) |
| `com.osmig.Jweb.framework.js.JS.Func` | `jweb.Func` | **deleted** (nested) |
| `com.osmig.Jweb.framework.js.JS.Stmt` | `jweb.js.Stmt` | **deleted** (nested) |
| `com.osmig.Jweb.framework.server.Request` | `jweb.Request` | subclass alias |
| `com.osmig.Jweb.framework.server.Response` | `jweb.Response` | subclass alias (statics) |
| `com.osmig.Jweb.framework.security.Principal` | `jweb.Principal` | subclass alias (statics) |
| `com.osmig.Jweb.framework.upload.UploadedFile` | `jweb.UploadedFile` | subclass alias |
| `com.osmig.Jweb.framework.middleware.Middleware` | `jweb.Middleware` | sub-interface alias |
| `com.osmig.Jweb.framework.routing.RouteHandler` | `jweb.RouteHandler` | sub-interface alias |
| `com.osmig.Jweb.framework.seo.Seo` | `jweb.Seo` (`Seo.of(title, description)` works) | subclass alias |
| `com.osmig.Jweb.framework.db.mongo.Doc` | `jweb.Doc` | subclass alias |
| `com.osmig.Jweb.framework.async.Streamed` | `jweb.Streamed` (record) | `of(...)` only |
| `com.osmig.Jweb.framework.async.BackgroundTask` | `jweb.BackgroundTask` | subclass alias |
| `com.osmig.Jweb.framework.sse.SseEvent` | `jweb.SseEvent` (record) | static factories only |
| `com.osmig.Jweb.framework.sse.SseBroadcaster` | `jweb.SseBroadcaster` | subclass alias |
| `com.osmig.Jweb.framework.i18n.Messages` | `jweb.Messages` | subclass alias (statics) |
| `com.osmig.Jweb.framework.http.FetchResult` | `jweb.FetchResult` | subclass alias |
| `com.osmig.Jweb.framework.validation.ValidationResult` | `jweb.ValidationResult` | subclass alias |
| `com.osmig.Jweb.framework.validation.Validator` | `jweb.Validator` | sub-interface alias |
| `com.osmig.Jweb.framework.core.ErrorBoundary` | `jweb.ErrorBoundary` | subclass alias (statics) |
| `com.osmig.Jweb.framework.testing.MockRequest` | `jweb.MockRequest` | subclass alias (statics) |
| `com.osmig.Jweb.framework.testing.MockSession` | `jweb.MockSession` | subclass alias |
| `com.osmig.Jweb.framework.testing.TestClient` | `jweb.TestClient` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.Stylesheet` | `jweb.css.Stylesheet` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.Stylesheet.Rule` | `jweb.css.Rule` (record, `build()`) | **deleted** (nested) |
| `com.osmig.Jweb.framework.styles.MediaQuery` | `jweb.css.MediaQuery` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.MediaQuery.Rule` | `jweb.css.Rule` | **deleted** (nested; `Supports.makeRule` and the `rules(MediaQuery.Rule...)` overloads went with it) |
| `com.osmig.Jweb.framework.styles.Rule` | `jweb.css.Rule` | `of(...)` only |
| `com.osmig.Jweb.framework.styles.CSS.Selector` | `jweb.css.Selector` | **deleted** (nested) |
| `com.osmig.Jweb.framework.styles.ContainerQuery` | `jweb.css.ContainerQuery` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.Keyframes` | `jweb.css.Keyframes` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.FontFace` | `jweb.css.FontFace` | subclass alias (statics) |
| `com.osmig.Jweb.framework.styles.Supports` | `jweb.css.Supports` | subclass alias (statics) |
| `com.osmig.Jweb.framework.three.Three` | `jweb.Three` (the real facade) | subclass alias (statics) |
| `com.osmig.Jweb.framework.three.*` (every node, `ThreePatch`, `ThreeRuntime`, `ThreeAssets`, `SceneSetting`) | `jweb.three.*` | **no alias** — never a documented import |

Also in this pass:

- `Template.extraHead()` returns `Optional<jweb.Element>` (was the legacy `core.Element`, which
  forced a long import into every override).
- New shells for the modules the gallery imports: `jweb.Password`, `jweb.Jwt`, `jweb.Toast`,
  `jweb.Transition`, `jweb.Portal`, `jweb.Markitdown`, `jweb.Fetch`, `jweb.StateManager`,
  `jweb.RenderContexts`, `jweb.Button`. Their statics return `jweb` value
  types (`Toast.success(...)` is a `jweb.Action`, `Fetch.get(url).send()` a `jweb.FetchResult`).
- `JS.esc(String)` and `JS.toJs(Object)` became public (the hoisted `Val`/`Func` need them),
  so they now appear under `import static jweb.Js.*`.
- `SceneSetting.put(key, value)` and the node constructors are public — the facade lives in
  another package now. Whole-valued doubles still serialize narrowed (`40`, not `40.0`).
- Guards: `jweb.JwebSurfaceTest` and `jweb.NoLongImportsTest` (see `readme/known-issues.md`).

**Migration:** a subclass alias keeps `OldName.staticCall()` and `new OldName(...)` compiling,
but `OldName x = frameworkCall()` no longer does — the framework returns the `jweb` type.
Swap the import; the simple name is unchanged for everything except the hoisted nested types.

---

## Breaking — and NOT compile errors

These still compile and now mean something else. Search for them:

1. **`a("/x", "Text")`** renders the text `/xText`. → `a(href("/x"), "Text")`.
2. **`label("id", "Text")`** → `label(for_("id"), "Text")`.
3. **`option("v", "Text")`** → `option(value("v"), "Text")`.
4. **`call("fn")`, `fetch("/url")`, `sleep(ms)` under `Js.*`** are now Actions, not
   Vals. Inside expression builders they still work as statements; if you assigned them to
   a `Val`, use `JS.call(...)`, `fetch(str(...))`, `delay(...)`.
5. **`span("x")` with `Css.*` imported** is now the `<span>` element (it used to be the
   grid line-name span).

Everything else is a compile error with an obvious fix, or a deprecation warning:
deleted `abbr(text, title)` / `blockquote(cite, …)` / `datalist(id, …)` / `optgroup(label, …)`,
`data_` / `var_` / `title_` / `style_` / `template_`, `Css.raw`, the Selector starters in
`Css`, `Async.fetch(String)` / `Async.sleep`, the `_`-suffixed JS names, `Suspense.of(Supplier)`,
`submitButton` / `resetButton`, the Actions template engine and its `script/query/queryAll`
aliases. `Template.onMount/onUnmount/scripts` change return type, so overrides fail loudly.

The release is **source- and binary-incompatible** — recompile downstream code.

## Deliberately not done

- **Numbers meaning pixels** for length properties (React's convention). Real ergonomics,
  but a convention rather than a platform name.
- **Responsive rules inside an inline `style()`** — a feature, not syntax.
- **Renaming `class_`, `for_`, `float_`, `if_`, `return_`** and the rest of the keyword set,
  `hex()`, `px()`, or removing `attrs()` — all honest.
