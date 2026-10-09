# DSL 3.0.2 — no raw CSS or JavaScript strings

The 2026-10-08 pass. 3.0 made the rules true for the framework's own pages; 3.0.2 removes
the last reasons to type CSS or JavaScript as a string. Everything here is additive — 3.0
code compiles unchanged (two declared types change, see the end) — and the framework's
own app now builds under a hygiene test that fails on every raw shape listed below.

The one idea behind most of it: **a class name or id is a handle, declared once, used by
all three languages.**

```java
static final Cls CARD = cls("snip-card"), COPY = cls("snip-copy");
static final Id EDITOR = id("sandbox-editor");

article(CARD, button(COPY, "Copy"))                           // HTML: class="snip-card"
stylesheet().rule(CARD, style().display(grid))                 // CSS:  .snip-card{...}
            .rule(COPY.hover(), style().color(PRIMARY))        //       .snip-copy:hover{...}
            .rule(CARD.descendant(Selector.type("pre")), ...)  //       .snip-card pre{...}
delegate(CARD, "click", COPY)                                  // JS:   the same handle
byId(EDITOR).dot("value")                                      //       document.getElementById('sandbox-editor')
```

## HTML

| Was | Now |
|---|---|
| `cls("card")` returned an `Attr`; the stylesheet repeated `".card"` and the script `".card"` | `cls("card")` returns a `jweb.Cls`: an element argument, an immutable `Selector`, and a JS target. `id("x")` returns a `jweb.Id` likewise |
| `attrs().cls("a b")` / `classes("a", when(c, "b"))` | still work; `classes(...)` also takes `Cls` parts; `div(A, B)` sets both |
| — | `cls()` with no name mints a class scoped to the declaring class (`snippetcard-1`); declare it `static final` |
| a static component could not own styles — `SnippetCard.rules(sheet)` had to be called by every page | `styled(RULES, element)`: the stylesheet rides the page's one `<style>`, deduped, like a `Template.styles()` |
| `inlineScript(script.build())` — the only way to attach a script, a String by then | `inlineScript(Action)`; better, `Template.scripts()` — now collected for every template in the render (page, layout, components), deduped by content, and delivered with fragments, streamed chunks and DOM patches, exactly like `styles()` |
| `Ref` had no short name | `jweb.Ref` is the class; `Ref.of(Id)`, `ref.asId()`, `addClass(Cls)` |

## CSS

| Was | Now |
|---|---|
| `rule(String, Style)` only — `Selector` (100 methods) had no way into a stylesheet | `rule(Selector, Style)` on `Stylesheet`, `MediaQuery`, `ContainerQuery`, `Supports`, `Rule.of`, `startingStyle`, `scope`/`to` |
| `Selector` was a mutable builder (a shared one was corrupted by its first `.hover()`) | immutable: every method returns a new selector |
| the starters `tag("pre")`, `all()` lived only in `Selectors`, whose `id`/`cls` collide with `El` | `Selector.type("pre")`, `Selector.any()`, `Selector.of(css)` — qualified, no import clash |
| `fontFamily("ui-monospace, Menlo, monospace")` | `fontFamily(uiMonospace, font("Menlo"), monospace)` — constants `serif sansSerif monospace cursive fantasy systemUi uiSerif uiSansSerif uiMonospace uiRounded emoji math`; `font("Segoe UI")` quotes when needed |
| `.prop("box-shadow", "0 1px 2px rgba(…), 0 12px 32px -16px rgba(…)")` — one shadow only | `boxShadow(shadow(0, px(1), px(2), rgba(…)), shadow(0, px(12), px(32), px(-16), rgba(…)))`, `insetShadow(…)`, `textShadow(shadow…)` |
| `calc("100vh - 50px")` | `vh(100).minus(px(50))`, `.plus()`, `.times(2)`, `.div(2)` — on every `CSSValue`; nested calcs flatten |
| `keyframes("shift")` here, `.animation("shift", …)` there — a string on both ends | `static final Keyframes SHIFT = keyframes("shift")…;` then `.animation(SHIFT, s(3), linear)` and `.add(SHIFT)`; `animationName(SHIFT)` |
| `radialGradient("circle", …)` had no typed colour stop | `stop(hex("#e2e8f0"), px(1))` |
| `.prop("text-overflow", "ellipsis")`, `.prop("overflow-wrap", "anywhere")` — the keywords were missing | `ellipsis`, `anywhere`, `revert`, `listItem`, `default_` |
| `transition(CSSValue.of("filter"), …)` | `propFilter`, `propVisibility`, `propMaxHeight`, `propBorderBottomColor` beside the existing `prop*` constants |
| 40 properties with no setter (`outlineColor`, `borderTopWidth`, `rotate`, `scale`, `translate`, `zoom`, `perspective`, …) | added, typed and String forms each. `jweb/CssSpecCoverageTest` keeps the rule true: every property in `css-properties.txt` has its camelCase setter, every keyword in `css-keywords.txt` its constant |

## JavaScript

| Was | Now |
|---|---|
| `byId("x")`, `query(".x")`, `queryAll`, `dom`, `domAll`, `delegate(".a", "click", ".b")` — String only | each takes a `Selector` (so a `Cls`/`Id` handle); `byId(Id)`, `byId(Ref)` |
| `el.querySelector(".x")`, `closest`, `matches`, `classList.add('x')` by hand | `el.querySelector(SEL)`, `el.closest(SEL)`, `el.matches(SEL)`, `el.addClass(CLS)`, `removeClass`, `toggleClass`, `hasClass` |
| `*Script.build()` returned `String` | returns `Action`; the page's `scripts()` returns it (several: `all(a, b, c)`) |

## Pages, CSRF, responses

| Was | Now |
|---|---|
| a page with a constructor argument could not be in the page table, so it lost the default layout, `pageTitle()`, `scripts()`, `beforeRender()` and was hand-wrapped `app.get(path, ctx -> new Layout("Title", new Page(ctx.query("x"))))` | `app.pages("/sandbox", req -> new SandboxPage(req.query("file")))` — `pages(path, Function<Request, Template>)` and `pages(path, Supplier<Template>)` |
| `Response.html(new Layout(…))` skipped the runtime, hydration data and action definitions (only the CSS came along) | `Response.html(template)` returns a `Response.Page`; the router renders it exactly like a bare `Template`, with the status and headers you set |
| `Csrf.tokenField(token)` with the token threaded through page constructors | `Csrf.tokenField()`, `Csrf.tokenMeta()`, `Csrf.token()` read the request in flight |
| `Middlewares.recommended()` = security headers, request ids, compression; CSRF was checked by hand per route | `recommended()` includes `csrf()`: every router POST/PUT/PATCH/DELETE must carry the token (form field `_csrf` or header `X-CSRF-TOKEN`); a hand-written `fetch` reads it from `Csrf.tokenMeta()` |
| `Template.scripts()` was emitted only for the outermost template | collected for every template, see HTML above |

## The gate

`jweb/AppDslHygieneTest` fails the build when app code (not the docs samples or the Sandbox
interpreter's starter programs) contains:

```
.rule("   .prop("   CSSValue.of(   inlineScript("   .set("on…"   .color("#…"   .background("rgba
.fontFamily("   .boxShadow("   calc("   delegate/query/dom/byId("…   .querySelector("…   unsafeRaw(   .raw(
```

A deliberate demo of a raw form says so on the line: `// raw: <why>`.

## Breaking — two declared types

`cls(...)` and `id(...)` return `Cls` and `Id` instead of `Attr`. Every call site that
passes them as element arguments is unchanged; a variable declared as `Attr x = cls("a")`
becomes `Cls x = cls("a")` (two tests in this repo, nothing in the app). `Attr.name()` on
one of those gave the attribute name (`"class"`); `Cls.name()` gives the class name.
`Selector` methods return a new instance, so code that relied on in-place mutation of a
selector variable (none in this repo) must use the returned value.

`com.osmig.Jweb.framework.ref.Ref` is a deprecated alias of `jweb.Ref`; its factories
return `jweb.Ref`, so declare the variable as `jweb.Ref`.
