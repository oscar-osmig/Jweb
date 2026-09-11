# DSL Simplification 3.0 — what changed

The 2026-09-04 pass over the HTML, CSS, JavaScript and Three DSLs, and the 2026-09-07
follow-up that finished the HTML side (class names, conditionals, one form system). The 2.0 pass
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
| `blockquote(citeUrl, …)`, `datalist(id, …)`, `optgroup(label, …)` | `blockquote(attrs().cite(url), …)`, `datalist(id("x"), …)`, `optgroup(attrs().label("x"), …)` |
| `h1(text("Title"))` | `h1("Title")` — `text(String)` is deprecated |
| `data_(…)`, `var_(…)` elements; `title_(…)`, `style_(…)` shortcuts; `template_` | `tag("data", …)`, `tag("var", …)`; `attrs().title(…)`, a bare `style()` argument; `template` |
| `when(c).then(a).elif(c2, b).otherwise(d)`, `match(cond(…), otherwise(…))`, `Tag.ifElse` | **deleted** — `when(c, x)`, `when(c, x, y)`, `when(c).then(x).otherwise(y)`, or a `switch` expression |
| `body(new Nav().render(), …)` | `body(new Nav(), …)` — a Template is an Element |
| `submitButton("x")` | `button(type("submit"), "x")` (the name collided with app helpers) |

New top-level factories in `jweb.El`: every `on*` handler (both `Consumer<Event>` and
`Action` forms), `on(type, handler)`, `swap`, `swapOuter`, `swapMorph`, `swapForm`,
`swapPush`, `ref`, `bind`, `bindInput`.

### The second HTML pass (classes, conditionals, one form system)

| Was | Now |
|---|---|
| `class_("card")` everywhere | `cls("card")` — `class_` stays as the keyword-rule twin |
| `class_("chip" + (active ? " chip-on" : ""))` | `classes("chip", when(active, "chip-on"))` |
| `attrs().classIf("active", isActive)` | `attrs().classIf(isActive, "active")` — condition first, like `when` |
| `when(cond, x)` twice, the second negated | `when(cond, ifTrue, ifFalse)` |
| `when(c).then(a).elif(c2, b).otherwise(d)` | `when(c).then(a).otherwise(b)` — no `elif`; several branches are a `switch` |
| `match(cond(a, x), cond(b, y), otherwise(z))`, `Elements.CondCase`, `Elements.Condition` | **deleted** — `when`, a ternary, or a `switch` expression |
| `Tag.ifElse(c, a, b)`, `Elements.ifElse(c, a, b)` | **deleted** — `when(c, a, b)` |
| `text("Hi")` as a child | `"Hi"` — the wrapper is `@Deprecated` |
| `attrs().style().padding(px(8)).done().id("x")` | `attrs().style(s -> s.padding(px(8))).id("x")` — `Attributes.style()` and `InlineStyle.done()`/`toAttrs()` are **deleted** |
| `div(attrs().style().padding(px(8)), …)` | `div(style().padding(px(8)), …)` |
| `optgroup(attr("label", "Cars"), …)` | `optgroup(attrs().label("Cars"), …)` |
| `blockquote(attr("cite", url), …)` | `blockquote(attrs().cite(url), …)` |
| `option(attrs().set("selected", ""), …)` | `option(selected(), …)` / `attrs().selected(isChosen)` |
| `form(attrs().enctype("multipart/form-data"), …)` | `form(enctype("multipart/form-data"), …)` — a free static, and a record form with an `UploadedFile` component sets it itself |
| `button(title("x"), …)` silently nested a `<title>` | `button(attrs().title("x"), …)`; a `<title>` outside a head/SVG parent now logs a warning |
| SVG `defs/symbol/use/ellipse/tspan/textPath/stop/pattern/filter/mask/clipPath/image/animate/animateTransform/animateMotion/fe*` reachable only from `jweb.el.SVGElements` | exported from `jweb.El`. Three are renamed because the plain name is taken under the four wildcards: `svgText`, `svgLinearGradient`, `svgRadialGradient` |
| `<lineargradient>`, `<clippath>`, `<animatetransform>` in the output | the camelCase SVG names are preserved by the serializer |
| `jweb.Input` / `Input.text("q")`, `jweb.Button` / `Button.submit("Go")` | **deleted** — `input(type("text"), name("q"), id("q"))`, `button(type("submit"), "Go")` |
| `jweb.el.DialogHelper.showModal/show/close/toggle/…` | `jweb.El.openDialog(id)` / `closeDialog(id)` / `closeDialog(id, value)` / `toggleDialog(id)` — the rest was dead |
| `jweb.el.DetailsHelper.open/close/toggle/openExclusive/closeAll/…` | `jweb.El.openDetails(id)` / `closeDetails(id)` / `toggleDetails(id)` |
| `jweb.el.FormEnhancements.*` | **deleted** — `datalist`, `fieldset`, `legend`, `optgroup` are on `jweb.El`; the rest were typed-input helpers |
| `textInput/emailInput/passwordInput/numberInput/checkbox/radio/radioId/hiddenInput/fileInput/dateInput/timeInput/datetimeInput/monthInput/weekInput/searchInput/telInput/urlInput/rangeInput/colorInput/field` (23 statics) | **deleted** — a form is a record; anything else is the element with its attributes |
| `com.osmig.Jweb.framework.elements.Form` (the small builder) | **deleted** — `form(…)` the element, or `form(SomeRecord.class)` |
| `Form.create().text("name", f -> …).email(…).submit("Go").build()` | `form(Contact.class).action(url).field("name", f -> …).submit("Go")` |
| `FormModel.of(Registration.class)…build()`, `@FormField` / `@FormHidden` / `@FormIgnore`, `FormModel.bindFromParameterMap(...)` | **deleted** — `form(Registration.class)`, `@Form.Required` / `@Form.Email` / `@Form.Password` / `@Form.Multiline` / `@Form.Label` / `@Form.Length`, `Form.bind(Registration.class, req)` |
| `Csrf.getOrCreateToken(req)` passed into the page, `Csrf.tokenField(token)` per form | the record form adds the hidden field from the current request; `.csrf(token)` overrides |
| hand-written `if (isBlank(name) …) return error(...)` in the route | `Form.bind(...)`, then `bound.ok()` / `bound.errors()` |
| no field-level error rendering anywhere | `form(X.class).errors(bound)` — per-field message, `aria-invalid`, a summary, and the values as typed |

`jweb.Form` is now a real class (it used to be an alias for `forms/Form`), and it is
generic: `jweb.Form<T extends Record>`. `jweb.When` is the new chained-conditional type;
`jweb.Form.Bound<T>` is what `Form.bind` returns. `Form.styles()` is a drop-in stylesheet
for the class names the form emits.


## CSS

| Was | Now |
|---|---|
| `id("x")` ambiguous; `select()`/`tag("x")` returned a Selector | Selector starters moved to `jweb.css.Selectors` (`import static jweb.css.Selectors.*`); a String selector `rule(".card:hover")` needs nothing |
| `raw("x")` as a CSSValue (ambiguous with HTML `raw`) | `CSSValue.of("x")` — rarely needed, every property takes a String |
| `span("sidebar")` grid line-name span (captured `span("text")`) | `gridColumn("span sidebar")` |
| `.animation(anim("name"), s(3), …)` | `.animation("name", s(3), …)` |
| `stylesheet().mediaQuery(mq, new Rule(sel, style))` | `stylesheet().add(mq.rule(sel, style))` |
| `.keyframes(k)`, `.fontFace(f)`, `.supports(s)` | `.add(k)`, `.add(f)`, `.add(s)` (old names still work) |

### Stylesheets belong to pages

| Was | Now |
|---|---|
| `head(…, style(globalStyles()))` with `private String globalStyles()` returning `stylesheet()….build()` | `@Override public Stylesheet styles()` on the layout/page/component — the render collects every template's, dedupes by content, and emits one nonce-stamped `<style>` in `<head>` |
| `div(class_("docs-layout"), style(docsStyles()), …)` | the same `styles()` hook; no `<style>` in the body |
| `Stylesheet.toStyleTag()` (used 0 times — there was no way to attach a sheet to a page) | still there, but `styles()` is the way |
| `div().styled(s).hover(s2)` — `Tag.styled()/.hover()/.focus()/.active()` and `StyledElement`, which minted a counter-based `jweb-N` class and emitted a `<style>` block next to the element (no dedupe, no hoisting, a different class every render) | **deleted**. `div(style().apply(s).hover(s2))` |
| no way to write `:hover` on an element without a class rule | `style().hover(style()…)` — a content-hash class (`j-3f9a1c`) is generated, its rules ride the page stylesheet, the plain declarations stay inline |
| — | `.focus .focusVisible .focusWithin .active .visited .disabled .checked .placeholder .popoverOpen .before(content, s) .after(content, s) .on(":nth-child(2n)", s)` |
| — | `.at(md(), s)`, `.at(container("card")…, s)`, `.dark(s)`, `.reducedMotion(s)`, `.startingStyle(s)` — they nest, and `apply()` carries them |
| — | `MediaQuery.query()` / `ContainerQuery.query()` — the condition line without its rules |

Fragments, streamed blocks and WebSocket patches each carry the CSS their own render
introduced, exactly as they already carry Actions definitions.

### Layout is a style, not an element

| Was | Now |
|---|---|
| `jweb.Layout` / `com.osmig.Jweb.framework.layout.Layout` — 45 element-returning wrappers, used by nothing | **deleted**. `row() row(gap) stack() stack(gap) center() cluster(gap) container(maxWidth) card() truncate() truncate(lines) srOnly() fullBleed() aspect(w,h) cover() contain() grid(cols,gap) autoGrid(min) autoGrid(min,gap)` on `jweb.Css`, each returning a `Style` |
| `style().display(flex).flexDirection(column).gap(SP_4)` (×46 in our own pages) | `stack(SP_4)` |
| `style().maxWidth(px(500)).margin(zero, auto)` | `container(px(500))` |

### One token system

| Was | Now |
|---|---|
| `com.osmig.Jweb.framework.styles.Theme` (601 lines, `Theme.create()`, `toCss()/toFullCss()/toStyleElement()`) — dead code | **deleted**; `jweb.css.Theme` is the real type |
| `CSSVariables.designSystem()` / `DesignSystemBuilder` | **deleted** — a third token-naming scheme |
| `CSSVariables.theme()` / `ThemeBuilder` (`light(…).dark(…).buildBoth()`) | **deleted** — a fourth |
| `CSSVariables.scoped(scope, name)`, `component(c, p)`, `theme(name)` | **deleted** — string concatenation dressed as API |
| `CSSVariables.themeColor/spacing/radius/fontSizeVar/shadow(level, fallback)` | **deleted** — they encoded a naming convention; use `Theme.color("x")` etc. |
| an app's tokens as `static final CSSValue PRIMARY = hex("#4f46e5")` | `Theme.light().color("primary", hex("#4f46e5"))` emitted through `styles()`, read back as `Theme.color("primary")` → `var(--color-primary)`; the app keeps its constant names |
| dark mode by hand | `.dark(Theme.dark().color("bg", …))` → `:root`, `@media (prefers-color-scheme: dark){:root:not([data-theme=light])}` and `:root[data-theme=dark]` |
| `Utility.generateCss(Theme)` | `Utility.generateCss()` — the parameter was never read (`Utility` stays deprecated) |

### The modern-CSS modules are part of the DSL now

Ten modules were separate static imports whose methods returned raw
`"property:value"` strings, so they only composed through `.prop(...)`. They are now in
the `Css` inheritance chain — `CSSColors extends CSSNested extends CSSProperty extends
CSSScope extends CSSLayer extends CSSScrollSnap extends CSSMasking extends
CSSLogicalProperties extends CSSSubgrid extends CSSTextWrap extends
CSSAnchorPositioning` — and they are typed.

| Was | Now |
|---|---|
| ten imports: `import static jweb.css.CSSTextWrap.*;` … | `import static jweb.Css.*` reaches all of them |
| `style().prop(textWrapBalance())` — the method returned `"text-wrap:balance"` | `style().apply(textWrapBalance())` — it returns a `Style` |
| `style().prop("top", anchor("--menu", "bottom"))` — a bare `String` | `style().top(anchor("--menu", "bottom"))` — it returns a `CSSValue` |
| `CSSNested.rule(sel)` — collided with `Css.rule(sel)` | `nest(sel)` |
| `CSSProperty.color/length/number/percentage/integer/angle/time/image(name)` — collided with the unit and colour statics | `colorProperty/lengthProperty/numberProperty/percentageProperty/integerProperty/angleProperty/timeProperty/imageProperty(name)` |
| `CSSSubgrid.gridTemplateColumns/gridTemplateRows/gridColumn/gridRow(String)` | **deleted** — the `Style` methods of the same name already did it |
| `CSSAnchorPositioning.top/right/bottom/left(String)` | **deleted** — `style().top(anchor(…))` |
| every value-taking module method took a `String` | each also takes a `CSSValue` (`inlineSize(rem(1))`, `snapPadding(px(20))`, `maskSize(percent(100))`) |

The six value modules (anchor positioning, text wrap, subgrid, logical properties,
masking, scroll snap) are **no longer deprecated** — the deprecation said "returns a
pre-joined string, use the `Style` method", which stopped being true.

### Properties and at-rules that were missing

| Was | Now |
|---|---|
| `.prop("border-right", "none")` (only the 3-arg form existed) | `.borderRight(none)`, and `borderLeft/Top/Bottom(CSSValue)` |
| — | `fieldSizing(content\|fixed)`, `scrollbarColor(thumb, track)`, `scrollbarWidth`, `scrollbarGutter(stable\|stableBothEdges)`, `textBoxTrim`, `textBoxEdge`, `textBox(trim, edge)`, `anchorScope`, `viewTransitionClass`, `interpolateSize(allowKeywords)` |
| — | `@starting-style`: `style().startingStyle(s)` and `stylesheet().startingStyle(sel, s)` |
| — | `Selector.popoverOpen()` / `.open()`, and `style().popoverOpen(s)` |
| — | `ContainerQuery.style("--x", "y")` → `@container style(--x: y)` |
| — | keyword constants `content`, `thin`, `stableBothEdges`, `trimBoth/trimStart/trimEnd`, `capAlphabetic`, `exAlphabetic`, `allowKeywords` |

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

### Behaviors — the JavaScript our own app could not write in the DSL

The framework's own pages carried ~520 lines of raw JavaScript in Java strings: an SPA
content swap with its own fetch/cache/popstate code, a scrollspy, a copy button with an
`execCommand` fallback, a line gutter and a split-pane drag. All of it is verbs now, and
the four scripts are pure DSL (a test fails if `unsafeRaw` or `.raw(` reappears in them).

| Was | Now |
|---|---|
| `iife().unsafeRaw(guard("x").unsafeRaw(js).build())` — raw JS strings in every script | `actions().does(guard("x").does(behavior, behavior))` — `does(Object...)` takes an `Action`, `Val`, `Stmt` or `Func` |
| a hand-written copy button + `legacyCopy` textarea fallback | `copy(text)` / `copyFrom(selector)` with `.feedback(text, ms)`, `.feedbackClass`, `.failText`, `.trigger(el)` |
| 80 lines of scrollspy (activation line, click lock, MutationObserver) | `scrollSpy(nav, headings)` with `.within`, `.linkClass`, `.activeClass`, `.hasHeadingsClass`, `.host`, `.offset`, `.scrollMargin` |
| a private fetch + TTL cache + `pushState` + popstate reimplementation of `swap()` | `swap(...)`/`swapPush(...)` markup, plus `navigate(url)` (`.target/.push/.morph/.replace/.cache/.prefetch`), `prefetch(selector)` (`.within/.onHover/.onVisible/.delay/.cache/.url`), `activeLink(selector)`, `onPopState(Func)` |
| a drag handler and a wrapped-line gutter in strings | `splitPane(handle, left)` (`.container/.min/.max/.minPercent/.maxPercent/.persist`), `lineGutter(textarea, gutter)` (`.mirror/.errorClass`) + `relineGutter`, `markLine`, `resizeToContent`, `insertText` |
| no Java-side reader of client state (`JWeb.getState` existed, nothing called it) | `syncState(state)` → a `Val`; `onStateChange(state, callback)` → an `Action` |
| `.raw("if(x !== y) return;")` for an early return | `if_(condition, return_())` — a `Stmt`, anywhere a statement goes |
| `callback("e").raw("e.preventDefault()")` | `callback("e").does(preventDefault())`; also `stopPropagation()`, `stopImmediatePropagation()` |
| `.onUpgrade(callback("db").raw(createStore(v("db"), …).build().js()))` | `.onUpgrade(db -> createStore(db, "users").keyPath("id").build())` |
| no Web Components, no shadow DOM, no PWA manifest | `customElement(name)` (`observedAttributes/shadow/template/connected/disconnected/attributeChanged/adopted/extendsTag`), `attachShadow`, `shadowRoot`, `assignSlot`, `assignedSlot`, `assignedElements`, `assignedNodes`, `onSlotChange`; `jweb.js.Pwa` — `manifest(...)`, `link()`, `serve(app, manifest)`, `registerServiceWorker(url)` |
| `document.createElement` / `document.activeElement` / template parsing by hand | `createElement(tag)`, `activeElement()`, `parseHtml(html)` |
| `sse(url)` had no named-event listener | `.on(eventName, callback)` |
| `byId(x).addEventListener(type, callback("e").raw(debounced.js()))` | `byId(x).addEventListener(type, debounce(300, callback("e")…))` — the `Val` overload |

New builder types, all `jweb.js.*` and all `Action`s: `Behavior`, `Copy`, `Navigate`,
`Prefetch`, `ActiveLink`, `ScrollSpy`, `SplitPane`, `LineGutter`, `CustomElement`,
`Manifest`, `Pwa`.

The client runtime grew the implementations (`JWeb.copyText/scrollSpy/prefetchOn/
activeLink/splitPane/lineGutter/resizeToContent/insertText/onState/nearest/parseHtml`) plus
a ready queue behaviors install through, and `swap()` gained `opts.push`, `opts.mode`, a
shared TTL cache (`data-swap-cache`), and a `pushState`-before-dispatch order so a
`jweb:swap` listener sees the URL it navigated to.

### The ten most-used browser modules under `jweb.Js.*`

`JSClipboard`, `JSObservers`, `JSStorage`, `JSHistory`, `JSUrl`, `JSFormData`,
`JSAnimation`, `JSWebAnimations`, `JSMedia` and `JSIndexedDB` are re-exported from
`jweb.Js`, so `local()`, `cookie("theme")`, `replaceState("/x")`, `openDB("app", 1)`,
`intersection()` and `writeText(...)` need no second import. Their own imports still work
and are unchanged.

**Nothing was renamed.** Where forwarding a name would have captured a call that means
something else, the name stays module-only instead: `get`, `delete`, `pushState`,
`onPopState`, `eventKey` (the Actions layer owns those); `animate`, `currentTime`,
`currentUrl`, `disconnect`, `getQueryParam`, `pause`, `play`, `playbackRate`,
`setCurrentTime`, `setPlaybackRate`, `toObject` (two of the ten declare each); `audio`,
`video`, `src`, `href`, `url`, `search`, `duration`, `onAnimationEnd`, `onCancel`,
`onTransitionEnd` (the HTML or CSS DSL owns those under the four-wildcard import).
Renaming any of them would have broken a module's own readable API to buy an alias.
`JsModuleFacadeTest` fails if a module grows a static the facade does not forward, or if
an excluded name has no collision to justify it.

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
| `com.osmig.Jweb.framework.error.JWebException` | `jweb.JWebException` (+ an `int` status constructor, no Spring import) | subclass alias |
| `com.osmig.Jweb.framework.error.ValidationException` | `jweb.ValidationException` | subclass alias |
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
| `com.osmig.Jweb.framework.forms.Form` (the fluent builder), `com.osmig.Jweb.framework.forms.FormModel`, `com.osmig.Jweb.framework.elements.Form` | `jweb.Form<T extends Record>` (a new class, not an alias) | **deleted** |
| — | `jweb.Form.Bound<T>`, `jweb.Form.Field`, and the `@Form.Required/Email/Password/Multiline/Label/Length` hints | new |
| `com.osmig.Jweb.framework.elements.Elements.Condition` (the `when(c)` chain) | `jweb.When` | **deleted** |

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

## State, sessions, routing, API

The 2026-09-07 batch over what a page author needs from the server side, and what Spring
should not leak into author code.

| Was | Now |
|---|---|
| `span(bind(clicks), clicks.get())` — the value written twice | `span(bind(clicks))` — `bind` renders the value when it is the element's only content (the two-argument form still works, once) |
| `input(bindInput(name))` rendered no value | renders `value=` (a Boolean state checks a checkbox) |
| no attribute or class reactivity | `bindAttr(state, "disabled")`, `bindClass(state, "on")` (in `jweb.State`) |
| `useComponent("id", () -> element)` — a `<div id>` wrapper re-rendered on every change, documented nowhere | `live(state, s -> element)`, `live(a, b, (x, y) -> ...)`, `live(() -> ..., deps...)` — the root gets `data-live`, re-renders only for its own states, and is **morphed** in (focus/scroll/input kept); `useComponent` stays as a deprecated alias |
| `items.update(l -> { l.add(x); return l; })` — same instance, `equals` true, no notification | notifies; plus `items.mutate(l -> l.add(x))` |
| `Visit.of(req)` hand-rolled over `req.session().getAttribute` | `Session.of(Visit.class, req)`, `Session.of(req).get/put/find/flash`, `session()` inside a Template |
| `Optional<String> pageTitle()` / `metaDescription()` | `String pageTitle()` / `String description()` (null = default); `metaDescription()` stays deprecated and feeds `description()` |
| page routes exact-match only | `app.pages("/users/:id", UserPage.class)`; `pathParam("id")` / `params()` / `beforeRender(req)` |
| `if (!isAuthenticated(ctx)) return Response.redirect(...)` per route | `app.guard("/admin/**", Auth.requireLogin("/admin/login"))` — pages, router routes and `@REST` alike |
| `read query param → validate against a Set<String> → mutate → redirect` per route | `app.action("/act/x", Rec.class, (req, rec) -> ...)` with enums by name, `Optional<T>`, `@Range`/`@Length`/`@Pattern`; 400 with a message on failure |
| `Response.redirect(cond ? "/a#x" : "/a#y")` | `Response.redirect("/a").anchor(cond ? "x" : "y")`, `Response.redirectBack(req)` |
| `Response.html(HttpStatus.INTERNAL_SERVER_ERROR, page)` | `Response.html(500, page)`, `json(201, body)`, `status(304).header(..).build()`, `.contentType("text/markdown")`, `tooManyRequests()`, `error(418, "...")` — the `HttpStatus` overloads are deprecated |
| `@PathVariable`, `@RequestParam`, `@RequestBody`, `HttpServletRequest`, `MultipartFile`, `@Component`, `@Value` in a `@REST` class | `@Param`, `@Query`, `@Body`, `jweb.Request`, `@Upload` → `UploadedFile`, `jweb.api.Component`, `jweb.api.Value` |
| a WebSocket event could fall back to the global handler registry | a message that names a context runs only that context's handlers; unknown context → logged and dropped |
| a guard/middleware throwing `JWebException` (401/403) rendered a 500 page | answers with its own status |

**Breaking:** `Template.pageTitle()` now returns `String` — an `Optional<String>` override
fails to compile (drop the `Optional.of`). `EventRegistry.get(sessionId, id)` /
`execute(sessionId, ...)` no longer fall back to the global registry. Everything else is
additive or deprecated-with-delegation. `Response.redirect(...)` returns `Response.Redirect`,
a `ResponseEntity<Void>` — existing declarations keep compiling.

**Why not `Template.title()`:** a method named `title` on `Template` would shadow the
`title(...)` element factory inside every Template class (a member shadows a static import
by simple name, whatever the arity) — `head(title("x"))` would stop compiling. So the plain
hooks are `pageTitle()` and `description()`.

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
6. **`center`, `cover`, `contain`, `grid`, `row` with `Css.*` imported** are still the
   keyword constants; the new same-named *methods* (`center()`, `grid(3, gap)`) are a
   separate namespace, so `display(grid)` is unchanged.
7. **A `style()` with only conditional rules** (`style().hover(…)` and nothing else) no
   longer renders an empty `style=""` attribute.
8. **`style().prop(textWrapBalance())`** — the single-argument `prop(String)` that split a
   `"property:value"` string still exists, but the modules no longer return one, so this
   is now a compile error. → `style().apply(textWrapBalance())`.
9. **`asyncFunc(...).does(...)`** takes `Object...` rather than `Action...`. Every call
   still compiles; only passing an `Action[]` array as the single argument changes meaning
   (it becomes one `Object` argument). Pass the actions, not an array.
10. **A `Val` handed to `El.addEventListener(type, ...)`** now binds the new `Val` overload
   (the listener expression) instead of failing to compile. That is the point —
   `debounce(300, …)` attaches directly — but a `Val` you meant as something else no
   longer errors.
11. **A page that swaps and pushes history** reloads instead of doing nothing when the user
   goes back past the first swap: that entry is the document as the server first sent it,
   and nothing in the page remembers what that was.
12. **`span(bind(state))` renders the value** (it used to render an empty element that the
   runtime filled in later). Code that wrote the value next to it, `span(bind(s), s.get())`,
   renders exactly as before.
13. **`state.update(l -> { l.add(x); return l; })` now notifies** — regions and bindings
   re-render where they silently did nothing.
14. **`attrs().classIf("active", isActive)`** still compiles — the old
   `(String, boolean)` order is deprecated and delegates to `classIf(boolean, String)`,
   so the meaning is unchanged, but new code should read condition-first.
15. **`when(cond)`** returns `jweb.When` instead of the deleted `Elements.Condition`.
   A chain that ended in `.end()` or `.elif(...)` is a compile error; a
   `.then(...).otherwise(...)` chain keeps working and now also renders on its own
   without `otherwise(...)`.
16. **`button(title("x"), …)`** compiled before and compiles now, and still nests a
   `<title>` element rather than setting the tooltip. It is now loud: the renderer logs
   a warning for a `<title>` outside a head or SVG parent. Use `attrs().title("x")`.

Everything else is a compile error with an obvious fix, or a deprecation warning:
deleted `abbr(text, title)` / `blockquote(cite, …)` / `datalist(id, …)` / `optgroup(label, …)`,
`data_` / `var_` / `title_` / `style_` / `template_`, `Css.raw`, the Selector starters in
`Css`, `Async.fetch(String)` / `Async.sleep`, the `_`-suffixed JS names, `Suspense.of(Supplier)`,
`submitButton` / `resetButton`, the Actions template engine and its `script/query/queryAll`
aliases, `Attributes.style()` / `InlineStyle.done()` / `InlineStyle.toAttrs()`,
`match` / `cond` / `otherwise` / `CondCase` / `Condition` / `ifElse`, `jweb.Input`,
`jweb.Button`, `jweb.el.DialogHelper` / `DetailsHelper` / `FormEnhancements`,
`elements/Form`, `forms/FormModel`, and the 23 typed-input statics. `Template.onMount/onUnmount/scripts` change return type, so overrides fail loudly.

The release is **source- and binary-incompatible** — recompile downstream code.

## Deliberately not done

- **Numbers meaning pixels** for length properties (React's convention). Real ergonomics,
  but a convention rather than a platform name.
- **Renaming `class_`, `for_`, `float_`, `if_`, `return_`** and the rest of the keyword set,
  `hex()`, `px()`, or removing `attrs()` — all honest.
