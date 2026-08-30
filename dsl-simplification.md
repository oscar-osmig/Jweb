# DSL Simplification — what changed

The 2026-08-30 pass over the HTML, CSS and JavaScript DSLs. The goal was parity with the
platform (anything you can write in HTML/CSS/JS you can write here) using names the
platform already taught you, and removing the places where the DSL guessed at your intent.

Nothing was deleted without either a replacement or a compile error pointing at one. Old
names still work unless listed under **Breaking** below.

**191 tests pass** (107 before this pass; 84 added).

---

## The rules the DSL now follows

These are the whole pass in six lines. Everything below is a consequence of one of them.

1. **A lone String child is text.** `a("Home")` renders `<a>Home</a>`.
2. **Every element is `name(Object... attributesAndChildren)`.** Attributes and children mix
   in any order, in one call.
3. **Every CSS property takes a plain String as well as a typed value.**
   `cursor("copy")` and `cursor(pointer)` both work.
4. **A Java keyword gets a trailing underscore, and nothing else does.** `if_`, `return_`.
5. **A block takes its body inline.** No `endFor()`, `endWhile()`, `endTry()`, `endSwitch()`.
6. **Platform names win.** `writeText`, `getRandomValues`, `getItem`, `fillStyle`,
   `pushState(state, url)`.

---

## HTML

| Was | Now |
|---|---|
| `a("Home")` → `href="Home"` | `a("Home")` → `<a>Home</a>`; use `a(href("/"), "Home")` |
| `label("Email:")` → `for="Email:"` | text (same for `blockquote`, `q`, `datalist`, `optgroup`) |
| `img(...)` had no varargs form | `img(src("x"), alt("y"), loading("lazy"))` |
| `div(attrs()…, children)` needed an `(Attributes, …)` overload | one `(Object...)` form per element |
| `attrs().style().color(...)` chained ~87 shim methods | `attrs().style(s -> s.color(...))` or `.done()` |
| `class_(cond, "x")` / `addClass(cond, "x")` / `classIf("x", cond)` | `classIf("x", cond)`, `classToggle(cond, a, b)` |
| `attrs().flexCenter()` overwrote the style attribute | merges; and is deprecated toward `style(s -> s.flexCenter())` |
| `Elements.link(href, text)` built an `<a>` | deleted — `a(href, text)` |
| `fetchPriority`, `popoverTarget`, `minLength` | `fetchpriority`, `popovertarget`, `minlength` (exact HTML spelling) |
| an unrecognized child rendered via `toString()` | throws, naming the type |
| `Style` inside an `Iterable` child was dropped | applied |
| `Input`/`Button` were dead ends | `.attr/.data/.aria/.toTag()`, and `Button.onClick` |
| `Elements.radio` → id `a-b`, `Input.radio` → id `a_b` | both `a-b` |

Deprecated composites (each saved about one attribute): `responsiveImg`, `lazyImg`,
`progressIndeterminate`, `timeWithDatetime`, `autoPopover`, `manualPopover`,
`popover*Button`, `lazyLoad`, `eagerLoad`, `submitButton`, `resetButton`, `strokeRound`,
`lineIcon`, `optgroupDisabled`, `fieldsetDisabled`.

Kept, because they save real boilerplate: `metaCharset`, `metaViewport`, `css(href)`,
`icon`, `appleIcon`, `targetBlank`, `option(v,t)`, `meter(v,min,max)`, `progress(v,max)`,
`abbr(text,title)`, and the Dialog/Details helpers.

## CSS

- **Every property takes a String** — 285 overloads added. The invented constants
  (`bgCover`, `selectNone`, `copyCursor`, `filterOpacity`, …) are now optional.
- `prop(name, value)` is the escape hatch again; `unsafeProp` is the deprecated one.
- The six **string-property modules** (`CSSAnchorPositioning`, `CSSLogicalProperties`,
  `CSSMasking`, `CSSScrollSnap`, `CSSSubgrid`, `CSSTextWrap`) returned `"prop:value"`
  strings that had to be wrapped in `.prop(...)` and couldn't take typed units. Deprecated;
  every property is a real `Style` method.
- `snapPadding`/`snapMargin` named the wrong CSS property → `scrollPadding`/`scrollMargin`.
- **29 animation presets deleted**: `fadeInUp`, `flipX`, `jello`, `tada`, `rubberBand` and
  friends had no `@keyframes` behind them, so they animated nothing. The 11 backed ones
  remain, and `Keyframes` ships their keyframes.
- One `Rule` record for `@media` / `@container` / `@supports` / `@scope`; `MediaQuery.and()`
  (a documented no-op) deleted.
- Duplicates deleted: `columnGapMulti` (same property as `columnGap`), `lightDark`, `env`,
  `colorMix`, `sequenceAnimations`, `timingSteps`, `animName`.
- Units added: `pt`, `cm`, `mm`, `q`, `inch`, `dpi`, `dppx`, `ms(double)`.
- Deprecated as opinion rather than parity: `Utility` (whose `hover:`/`dark:`/responsive
  builders emitted class names with no matching CSS), `Theme.preset()`, the shadow and
  radius scales, `mobile`/`tablet`/`desktop` (a second breakpoint set overlapping
  `xs`–`xxl`), the 17 `supportsX()` one-liners, `lighten`/`darken`, `CSSNested.block()`.

Bugs fixed: `::placeholder` was emitted as the invalid `:placeholder`; the BEM builder
emitted `&__header`, which native CSS nesting reads as a type selector; `retina()` used the
legacy `-webkit-min-device-pixel-ratio`; `AnimationBuilder.timeline()` was silently dropped;
`Style.toMap()` returned an unordered map, making declaration order inside
`@media`/`@container`/`@keyframes` non-deterministic — and in CSS order decides the winner.

## JavaScript

| Was | Now |
|---|---|
| `variable("x")`, `getElem("x")` | `v("x")`, `byId("x")` |
| `.ret(x)` | `.return_(x)` |
| `for_(...).body(...).endFor()` | `for_("i", 0, n, stmts...)` — same for while/forOf/forIn/doWhile |
| `try_().body(...).catch_("e").body(...).endTry()` | `try_().body(...).catch_("e", stmts...)` |
| `switch_(v).case_("a").then_(...).endSwitch()` | `.case_("a", stmts...)`, `.default_(stmts...)` |
| `setTextExpr`, `jsonExpr`, `bearerExpr`, `assignVar`… | one `Val` overload each |
| `Actions.query/queryAll/script` | `dom/domAll/actions` (the old names collided with `Js.*`) |
| `whenVar("x").equals("y")` | `.eq("y")` / `.neq("y")` — `equals` shadowed `Object.equals` |
| `debounce("timerVar", 300)` | `debounce(300, fn)` — a closure, no variable to name |
| `script().withHelpers()` | `actions()` — helpers are injected automatically |
| `setFillStyle(ctx, c)` / `getFillStyle(ctx)` | `fillStyle(ctx, c)` / `fillStyle(ctx)`; plus chainable `ctx2d(canvas)` |
| a bare String meant getElementById *or* querySelector | pass `byId("x")` or `query(".sel")` |
| `Async.promiseAll`, `Events.pushState`, `JSUrl.currentUrl` | `JSPromise`, `JSHistory`; `JSUrl.currentUrlObject()` |

Bugs fixed: `JSWebSocket.autoReconnect` rebuilt the socket without re-attaching any handler,
so every reconnected socket was deaf; `JSAnimation.transition` used one spelling for both
the JS style key and the CSS shorthand, so one half was always invalid;
`JSHistory.detectDirection` compared `history.length` across popstate, which never changes,
so it always answered "forward" (deleted); `PromiseExecutorBuilder.setTimeout` was an empty
method (deleted); an animation with no keyframes ran `element.animate([], …)` (now throws);
`JSGeolocation`'s builder threw at runtime for the wrong entry point (now a compile error);
`JSRegex`'s 24 shared constants were reassignable.

Widget-level builders in `Actions` are deprecated as outside HTML/JS parity: `alertModal`,
`showModalHtml`, `confirmDialog`, `statusFeedback`, `colorSwitch`, `tabs`, `renderList`, the
`template`/`field`/`dateField` engine, `externalService`, `hideOnBackdropClick`.

---

## Breaking

Three changes are not compile errors, so they are worth checking for by hand:

1. **`textarea("hello")`** now renders text; it used to set `name="hello"`. Use
   `textarea(name("bio"))`.
2. **`JSHistory.pushState(Val, Val)`** now reads `(state, url)`, matching the platform. The
   `(String url, Val state)` and 3-argument forms are deleted, so those calls *do* fail to
   compile.
3. **29 CSS animation presets are deleted** — but they never animated anything, so the
   visible behaviour is unchanged.

Everything else either still compiles with a deprecation warning, or fails loudly.

## Deliberately not done

- **Raw-String twins for ~40 JS handlers.** Each would bake in a callback parameter name the
  caller cannot see, so a wrong guess produces silently broken JS. The rule is: handlers take
  a `Func`; for raw JS use `callback("e").unsafeRaw(code)`, which puts the parameter name
  where the code that uses it is written.
- **Expanding the top-level `Attr` statics to all ~150 attributes** (cross-import collision
  risk), **thinning `Tag`'s fluent surface**, and **removing the `swap*`/`ref` framework
  attributes** — those are jweb's server-driven-UI feature, not an HTML-parity gap.
