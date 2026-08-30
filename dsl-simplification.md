# DSL Simplification Catalog

Full audit of the HTML / CSS / JS DSL surfaces (2026-08-30), looking for syntax that can be
simplified for easier code style and easier memorization. Guiding constraint: **no
special-purpose props — parity with what HTML/CSS/JS can do, using names the platform
already taught people.**

Every item has an ID so decisions can be recorded per item (`X1`, `H3`, `C7`, `J12`…).
Nothing here is implemented; this is the decision list.

---

## X. Cross-cutting rules (decide once, apply everywhere)

These fix whole families of problems in one policy decision.

- **X1 — A bare String child always means text.** Today `a("Home")` sets `href="Home"`,
  `label("Email:")` sets `for="Email:"`, `textarea("hello")` sets `name="hello"`,
  `script("alert(1)")` sets `src=...`, `blockquote("Quote")`/`q("text")` set `cite=...`,
  `datalist/optgroup/slot(String)` set id/label/name. One uniform rule ("a lone String is
  always escaped text") is the single highest-value memorability fix. Optionally keep only
  `a(String href, Object...)` as the one blessed exception.

- **X2 — Every CSS property accepts a plain String value.** `style().cursor("copy")`,
  `.display("flex")`, `.transition("color .2s ease")`. This gives 1:1 CSS parity, makes the
  ~50 invented disambiguation constants optional instead of mandatory, and removes the
  need to memorize jweb-only names (`copyCursor`, `bgCover`, `selectNone`,
  `filterOpacity`, `columnBreak`, `grayscale_`, `overscrollContain`, `layoutContain`,
  `hueBlend`, `maskAdd`, `snapStart`, `timingEase`, `fillModeForwards`, `iterationInfinite`…).
  Constants stay as optional autocomplete sugar. Note: this conflicts with the earlier
  decision to deprecate `prop(String,String)` / raw-String overloads (see C16/C17) — pick a
  direction.

- **X3 — One keyword-collision policy.** Today: `class_`, `for_`, `if_`, `var_`, `let_`,
  `const_`, `catch_`, `float_`, `double_`… but `ret` (abbreviation) for `return`, and
  `cls()` in the CSS selector DSL vs `class_()` in HTML. Pick one rule (e.g. trailing `_`
  for true Java keywords, nothing else) and add ergonomic aliases where wanted
  (`cls`/`className` for `class_`). Bonus: `var_` in Elements is unnecessary — `var` is
  a reserved *type name*, not a keyword; a static method named `var(...)` is legal Java
  (CSS.var() already proves it).

- **X4 — One terminal-method contract in the JS DSL.** `toVal()` = expression, always;
  `build(String varName)` = statement. Today `JSWorker.toVal()`, `JSWebSocket.toVal()`,
  and WebRTC's `PeerConnectionBuilder.toVal()` actually emit `var ws=...` *statements*
  with hardcoded names — a semantic lie vs every other module.

- **X5 — Replace the `*Expr` / `*FromVar` / `*Str` twin-method families with one
  `Val`-typed overload.** Actions alone has ~20 pairs: `setText/setTextExpr`,
  `thenValue/thenExpr`, `header/headerExpr`, `json/jsonExpr`, `bearer/bearerExpr`,
  `urlFromVar/appendVar/headerFromVar`, `assignVar/assignStr`, `pushState/pushStateExpr`,
  `setTextAndColor/setTextAndColorExpr`… One overload taking `JS.Val` (with `str("...")`
  for literals) collapses all of them.

- **X6 — Uniform handler overloads: every `onXxx` accepts both `Func` and raw-String.**
  Present in Events/JSFile/JSGeolocation/JSMedia/JSWebSocket/etc., absent in
  JSIndexedDB/JSPointer/JSDragDrop/JSWebRTC/JSSpeech. Wrap String→Func at the setter and
  delete the ~60 duplicated `xxxFunc`/`xxxCode` field pairs.

- **X7 — One meaning for a bare String element reference in JS modules.** Today
  `draggable("card-1")`/`onPointerDown("canvas")`/`requestFullscreen("id")` mean
  getElementById while `delegate("ul")`/`setInnerHTML("sel")`/`root("sel")` mean
  querySelector. Platform mental model: String = CSS selector; id lookup only via an
  explicit `byId(...)`.

- **X8 — Attribute names use exact HTML spelling.** Drift today: `fetchPriority` vs
  `fetchpriority`, `popoverTarget` vs `popovertarget`, `minLength` vs `minlength`.
  Rule: exact HTML casing, camelCase only where HTML itself is camelCase (`viewBox`).

- **X9 — One home per platform API (kill wildcard-import collisions).** Hard collisions
  under `import static jweb.Js.*` + a `jweb.js.*` module: `Async.promiseAll/…` vs
  `JSPromise.*` (10 methods), `Events.pushState` vs `JSHistory.pushState`,
  `JSHistory.getQueryParam` vs `JSUrl.getQueryParam` (identical — delete one),
  `JSDate.now()` vs `JSPerformance.now()`, `JSCrypto.arrayBuffer` vs `JSWorker.arrayBuffer`,
  `JSIterator.spread*` vs `JSOperators.spread*`, `JSOperators.pow` vs `JSMath.pow`,
  `JSFile.getFiles` vs `JSDragDrop.getFiles`, `Events.onPopState` vs `JSHistory.onPopState`.
  Most dangerous: `JSHistory.currentUrl()` (→ `location.href`, a string) vs
  `JSUrl.currentUrl()` (→ `new URL(location.href)`, an object) — same name, different type.

- **X10 — Prune or quarantine opinionated presets.** Per the parity constraint, everything
  that encodes a design opinion rather than a platform capability either goes away or
  moves to one clearly-labeled opt-in module (e.g. `jweb.css.Presets`): Tailwind-ish
  shadow/rounded scales, `Utility.java`, `Theme.preset()`, animate.css zoo, breakpoint
  sets, UI widgets inside Actions. Individual items listed under H11, C10, J9.

---

## H. HTML DSL (elements / attributes / Tag)

- **H1 — String-first overload footguns** (see X1 for the rule). Concrete deletions:
  `label(String forId, …)`, `textarea(String name, …)`, `blockquote(String cite, …)`
  (InteractiveElements), `q(String cite, …)`, `datalist(String id, …)`,
  `optgroup(String label, …)`, `slot(String name)`, `script(String src)` vs
  `inlineScript` split. Decide `a(String href, …)` keep/drop.

- **H2 — Delete the redundant `(Attributes, Object...)` and `(InlineStyle, Object...)`
  overload families.** `Tag.create`'s extractor already pulls Attr/Attributes/InlineStyle/
  Style out of the plain varargs, so `div(attrs().class_("x"), p("hi"))` works through
  `div(Object...)`. The ~90 `(Attributes, …)` + ~50 `(InlineStyle, …)` overloads in
  Elements are functionally redundant — deleting them halves the file with zero
  expressiveness loss.

- **H3 — Give every element one `(Object...)` form.** `img`, `video`, `audio`, `iframe`,
  `canvas`, `track`, `embed`, `object`, `param`, `map`, `area`, `col`, `source` currently
  require `(Attributes)` (or only fixed-arg forms). `img(src("x"), alt("y"),
  loading("lazy"))` doesn't compile today — which is why `responsiveImg`/`lazyImg` exist.
  One rule: **everything is `name(Object... itemsAndAttrs)`**.

- **H4 — Collapse `InlineStyle` to lambda + `done()`.** It's a Style subclass with three
  termination modes (auto-finalize, ~100 chain-through shim methods that each call
  `complete()`, explicit `.done()`). The lambda form
  `attrs().style(s -> s.display(flex).padding(px(10)))` already makes the shims
  unnecessary. Delete the ~300-line shim block and the `(InlineStyle, …)` element
  overloads (H2).

- **H5 — Decide the Attr-statics coverage line.** Flat style covers only ~26 attributes
  (`div(class_("x"))` works; `div(rel("…"))`, `loading`, `tabindex`, `srcset` don't — you
  must switch to `attrs()` or `attr(n,v)`). Either export the full attribute set as
  top-level statics (parity, one style) or shrink to `attr(n,v)` + keyword-clash names
  and teach the builder as the primary style.

- **H6 — One conditional-class API.** Today four with clashing arg orders:
  `class_(boolean, String)`, `addClass(boolean, String)`, `classIf(String, boolean)`
  (args reversed vs addClass!), `classToggle(boolean, a, b)`. Keep `classIf` +
  `classToggle`, drop the boolean-first pair.

- **H7 — Two conditional-rendering systems, not five.** `when(cond, el|supplier)` +
  `match(cond(...), otherwise(...))` stay; drop the `when(cond).then().elif().end()`
  Condition chain (it eager-evaluates unless you remember Supplier — the exact trap it
  looks like it avoids), `Tag.ifElse`, and the deprecated `ifElse`.

- **H8 — Fix or remove the Attributes layout presets.** `flexCenter()`, `flexColumn()`,
  `flexRow()`, `flexBetween()`, `gridCols()` each do `set("style", …)` — silently
  **clobbering** any style already set (and being clobbered by a later `.style()`).
  They're also CSS opinions living on the HTML attributes builder. Prune (preferred per
  constraint) or make them merge into the existing style.

- **H9 — One input story.** Four parallel ways today: `input(attrs()…)`,
  `El.emailInput(name)` (defined in Elements AND FormElements AND partially
  FormEnhancements), `Input.email(name)…`, and the `Form`/`Button` class builders.
  Problems: all typed helpers silently set `id = name` (not HTML parity);
  `Elements.radio(n,v)` id is `name-value` but `Input.radio(n,v)` id is `name_value`;
  `FormEnhancements.dateInput` sets no id while `Elements.dateInput` does; `Input` and
  `Button` are dead-ends (no `.attr()`, no data-*/aria/style, **Button has no onClick**),
  forcing rewrites once you need one more attribute. Proposal: typed helpers return
  `Tag` (so everything chains), one id policy (explicit or none), delete the duplicate
  definitions.

- **H10 — Attribute casing cleanup** (X8 applied): `fetchPriority`→`fetchpriority`,
  `popoverTarget`→`popovertarget`, `minLength/maxLength`→`minlength/maxlength` (or
  aliases both ways, one deprecated).

- **H11 — Composites: keep the boilerplate-savers, prune the trivia.**
  Keep (real savings, platform-semantic): `metaCharset()`, `metaViewport()`, `css(href)`,
  `icon(...)`, `appleIcon(...)`, `targetBlank()` (security value), `option(v,t)`,
  `meter(v,min,max)`, `progress(v,max)`, `abbr(text,title)`, Dialog/Details JS helpers.
  Prune (one attr of savings or invented name): `progressIndeterminate()` (= `progress()`),
  `lazyLoad()/eagerLoad()` (= `loading("lazy")`), `optgroupDisabled`, `fieldsetDisabled`,
  `popoverShowButton/HideButton/ToggleButton`, `autoPopover/manualPopover`,
  `timeWithDatetime(dt,txt)` (make it a `time(datetime, …)` overload),
  `responsiveImg/lazyImg` (H3 makes them unnecessary), `strokeRound()`, `lineIcon(int)`,
  `submitButton/resetButton` (triplicated with `Button.submit/reset` and
  `button(type("submit"))`).

- **H12 — Dead/confusing entry points.** `Elements.link(String href, String text)`
  creates an `<a>` while `link(Object...)` creates `<link>` — delete the first
  (`a(href, text)` exists). `template_` duplicates `template` on the jweb.El facade —
  delete `template_`. `Attrs.java` (third deprecated attributes entry point) — delete at
  next major, along with legacy `El`/`CSS` shells.

- **H13 — Fix Tag's silent varargs rules.** (a) `Style`/`InlineStyle` inside an
  `Iterable` child are silently dropped (extractAttrs' iterable branch only handles
  Attr/Attributes); (b) any unrecognized Object becomes `toString()` text silently — a
  stray Map renders as text. Make both loud (throw or handle consistently).

- **H14 — Tag fluent API drift.** Tag has 13 `on*` methods, Attributes ~40 (plus Action
  overloads); Tag's attribute set is a third subset different from Attr's and
  Attributes'. Either generate all three from one source or thin Tag's fluent surface to
  `attr/id/class_/style/on` and let `attrs()` be the full surface.

- **H15 — Boolean-attribute conditional overloads are arbitrary.** `disabled(boolean)`,
  `checked(boolean)`, `hidden(boolean)`, `open(boolean)` exist; `required`, `readonly`,
  `autofocus`, `multiple`, `controls`, `loop`, `muted` have no boolean form. Add the
  boolean overload to all boolean attributes (uniform rule). Also `Attr.disabled()` uses
  value `null` while `Tag.disabled()` uses `""` — unify.

- **H16 — `swap/swapOuter/swapMorph/swapForm/swapPush`** (data-swap-* protocol), `ref()`,
  and the Action-typed `on*` overloads are framework features, not HTML parity. They're
  core to what jweb *is* (server-driven UI), so probably keep — but flagged per the
  constraint so it's an explicit decision.

- **H17 — Numeric overload surprises.** `attrs().value(3)` widens to `value(double)` →
  renders `value="3.0"`; `step(2)` → `"2.0"`. Add int overloads. `Input.step(int|String)`
  vs `Attributes.step(double|String)` — align.

---

## C. CSS DSL

- **C1 — Promote the string-property modules to first-class Style methods (biggest CSS
  win).** CSSAnchorPositioning, CSSLogicalProperties, CSSMasking, CSSScrollSnap,
  CSSSubgrid, CSSTextWrap return pre-joined `"prop:value"` Strings that must be wrapped:
  `rule(".card").prop(marginInline("auto"))` — and they take raw Strings, so typed units
  don't compile (`marginInline(rem(1))` fails). These are just CSS properties; make them
  chain methods taking CSSValue: `style().marginInline(auto)`, `.scrollSnapType(x,
  mandatory)`, `.clipPath(circle(percent(50)))`. (Style already has many of the logical
  properties — finish the job and delete the string-returning duplicates.)

- **C2 — Kill mandatory invented constant names** (X2 applied). Also drop the
  property-prefix from constants where no collision exists: `timingEase→ease` (exists),
  `directionAlternate→alternate`, `fillModeForwards→forwards`,
  `iterationInfinite→infinite`, `playStatePaused→paused` — CSSAnimations re-prefixes
  values that CSS.java already exposes bare.

- **C3 — Delete literal duplicates.** `lightDark` defined identically in CSSColors AND
  CSSUnits; `env` in CSSVariables AND CSSUnits; `steps` vs `timingSteps` (same output);
  `colorMix` twice with different arities/arg orders (merge into one family in
  CSSColors); `Style.columnGap` vs `Style.columnGapMulti` (same `column-gap` property);
  `composeAnimations` vs `sequenceAnimations` (byte-identical output);
  `animName(String)` (= `raw(name)`); `duration(v)`/`delay(v)` identity wrappers.
  Consolidate color functions (hsl/hsla vs hwb/lab/lch/oklch) into CSSColors.

- **C4 — One rule-builder shape for all at-rules.** `media()`, `container()`,
  `supports()`, `scope()`, `layer()` all express "block of rules" differently; `Rule`
  lives inside MediaQuery so `Supports.rules(MediaQuery.Rule...)` reads wrong; CSSScope
  takes `CSS.StyleBuilder` while others take `(String, Style)`; `Supports.makeRule` vs
  `Stylesheet.cssRule` name the same record. Hoist `Rule` to top level, give every
  builder the same `.rule(selector, style)` + varargs shape. Also: `MediaQuery.and()` is
  a documented no-op — delete it.

- **C5 — Transition composition parity.** Today:
  `transition(transitions(trans(propColor, s(0.2), ease), trans(propTransform, s(0.3), easeOut)))`.
  CSS: `transition: color .2s ease, transform .3s ease-out`. Fix: varargs
  `.transition(Transition...)` directly, plus the X2 String overload
  `.transition("color .2s ease, transform .3s ease-out")`. The `propXxx` constants become
  unnecessary (any CSSValue/String names the property).

- **C6 — Scroll-snap naming corrections.** `snapPadding`→`scrollPadding`,
  `snapMargin`→`scrollMargin` (the real properties are NOT `scroll-snap-*` — current
  names actively misteach); `snapType/snapAlign/snapStop`→`scrollSnapType/Align/Stop`
  (folds into C1).

- **C7 — Animation presets vs Keyframes mismatch.** CSSAnimations ships ~35 named
  presets (`jello`, `tada`, `rubberBand`…) but Keyframes defines only 12 — the other ~23
  silently animate nothing unless the user hand-writes keyframes. Either ship keyframes
  for every preset, prune the unbacked ones, or (per X10) drop the zoo entirely. Also
  `rotate360()` emits an animation named "spin" — name/output mismatch.

- **C8 — AnimationBuilder silently drops `.timeline(...)`** — settable but never emitted
  in `css()`. Remove from the builder (shorthand can't express it anyway); keep only the
  `animationTimeline` property.

- **C9 — Units cleanup.** Every unit gets a `double` overload (today `ms` is int-only,
  `ch/vmin/vmax/deg/dvh/cqw/ex/lh` are double-only, `px/rem/em/%/vh/vw/fr` have both);
  add missing `pt`, `cm`, `mm`, `in`, `q`, `dpi`, `dppx` (MediaQuery's own javadoc
  references `pt(12)` which doesn't exist, and `minResolution()` can't be fed a typed
  value).

- **C10 — Preset prune list (X10 applied).**
  `Utility.java` (~1000-line Tailwind clone; note: many builder methods — hover:, dark:,
  responsive variants, text-gray-* — have NO generated CSS and silently do nothing);
  `Theme.preset()` (embedded Tailwind tokens); Style's `shadowXs…shadowNone` +
  `roundedNone…roundedFull` scales; `flexCenter/flexCol/flexRow/flexBetween/grid(n)`,
  `full/fullWidth/fullHeight/absolute/relative/…/textCenter/bold/clickable/truncate/
  noSelect/centerX` presets (some are genuinely popular — decide keep-list);
  `srOnly()`/`borderMask()` (useful recipes — maybe keep, they encode non-obvious
  platform tricks); MediaQuery's two overlapping breakpoint systems
  (`xs/sm/md/lg/xl/xxl` Bootstrap values AND `mobile/tablet/desktop` — keep at most one);
  `retina()` (uses legacy `-webkit-min-device-pixel-ratio`); 17 `supportsX()` one-liners;
  CSSMasking clip-shape presets (`clipStar`, `clipHexagon`…); `lighten/darken`
  SASS-isms; `positionAbove/Below/Left/Right`; `staggerDelay` (= `ms(i*d)`);
  CSSVariables naming-convention helpers (`scoped()`, `component()`, `themeColor()`,
  `spacing()`, `radius()`, `fontSizeVar()`, `shadow()`).

- **C11 — Two selector systems.** `Selectors.java` (String-returning, composed with Java
  `+`) vs `CSS.Selector` (typed builder) — half of Selectors' methods have typed
  overloads too. Pick one as primary. Also `cls("btn")` here vs `class_("btn")` in HTML
  (X3). Note most users can just write `rule(".btn:hover")` — raw selector strings are
  already full parity; consider documenting that as the default path.

- **C12 — StyledElement pseudo support.** `placeholder` emits single-colon
  `:placeholder` (invalid — needs `::placeholder`); `before/after` emit legacy
  single-colon; only ~16 pseudo-classes supported with no escape hatch. Add generic
  `.pseudo(String, style)` for parity, fix the colon handling, prune the enumerated list.

- **C13 — Anchor-positioning correctness.** `positionFallback()`/`tryTactic()` generate
  the dropped-from-spec `@position-fallback`/`@try` syntax. Verify against current spec;
  likely delete in favor of `position-try-fallbacks`/`@position-try`.

- **C14 — CSSNested.** `.nest()/.parent()` walking is SASS ceremony; a varargs children
  form reads closer to the emitted CSS. The BEM `block()` builder emits `&__header`
  which native CSS nesting does not resolve to `.card__header` — likely generates broken
  CSS (verify). BEM itself is a framework opinion (X10).

- **C15 — One theme/token builder.** Three entry points today: `CSSVariables.theme(String)`
  (string prefix helper), `CSSVariables.theme()` (ThemeBuilder), `Theme.create()/preset()`
  — plus `designSystem()` — each with different variable naming schemes. Keep one
  (Theme.java is the most complete), deprecate the rest.

- **C16 — Decide the blessed raw-value escape.** `prop(String,String)` is deprecated in
  favor of `unsafeProp(String,String)`, but "unsafe" is a strange label for
  `unsafeProp("container-type", "inline-size")` — there's no injection surface beyond
  what CSSValue lambdas already allow (`() -> "flex"` is the same String). If X2 is
  adopted, un-deprecate `prop(name, value)` and delete `unsafeProp`.

- **C17 — Same tension in the deprecated String overloads** of
  `gridTemplateColumns/gridTemplateRows/gridColumn/gridRow/gridArea(String)`. If X2 is
  adopted, un-deprecate them (they're exactly the CSS-parity path:
  `.gridTemplateColumns("repeat(3, 1fr)")`).

- **C18 — FontFace constants collide under wildcard import.** `normal/auto/block` on
  FontFace vs the same names in CSS/CSSUnits — rename or scope.

---

## J. JS DSL (JS / Actions / Events / Async / Runtime / JS* modules)

- **J1 — Keyword policy consistency** (X3): `if_/var_/let_/const_/while_/try_/catch_/
  switch_/case_/default_/else_` but `ret` for return. Pick `return_` or document `ret`
  as the one abbreviation; today it's the only outlier.

- **J2 — Kill the block-closer ceremony.** `for_(...).body(...).endFor()`,
  `while_(...).body(...).endWhile()`, `try_().body(...).catch_("e").body(...).endTry()`,
  `switch_(...).case_(...).then_(...).endSwitch()`, `if_(...).then_(...).end()`.
  The varargs form already exists for if (`if_(cond, stmts...)`) — extend it everywhere:
  `for_("i", 0, n, stmts...)`, `while_(cond, stmts...)`, `tryCatch(stmts, "e", catchStmts)`.
  Closers disappear; nesting reads like the emitted JS.

- **J3 — Shorter core names.** `variable("x")` → add alias `v("x")`; `getElem(id)` →
  `byId(id)` (platform: getElementById); keep `query`/`queryAll` (platform parity).

- **J4 — Rename Java-clash workarounds to platform names where possible.**
  `sliceStr` → `slice` overload disambiguation (or document); `atIndex` → `at`
  (platform has `.at()` now); `indexOfVal` → `indexOf(Val)` overload; `toStringRadix` →
  keep but document. These four exist only because of Java overload clashes — solvable
  with parameter types instead of new names.

- **J5 — Actions `*Expr` twins → Val overloads** (X5). ~20 method pairs deleted.

- **J6 — The Actions vs Js split.** They can't be wildcard-imported together because both
  define `query()`, `queryAll()`, `script()` with different types. Options:
  (a) rename Actions' DOM builder `query()` → `dom(selector)` and its `script()` →
  `page()`/`actions()` so `jweb.Actions` can merge into `jweb.Js` — one import for all
  client-side code; (b) keep the split but document it as permanent. (a) is the
  simplification.

- **J7 — Auto-inject helpers.** `script().withHelpers()` is a sharp edge (actions
  silently break without it). The ScriptBuilder knows which handlers it contains — emit
  the helper functions automatically when any added action needs them; delete
  `withHelpers()`.

- **J8 — `whenVar("x").equals("y")`** overloads `Object.equals` — Java footgun (IDE
  warnings, accidental `boolean` semantics). Rename to `eq()` (matches `Val.eq`),
  same for `notEquals` → `neq`.

- **J9 — Actions UI-widget composites (the biggest parity violation — prune or move to an
  opt-in module).** `alertModal()`, `showModalHtml()`, `confirmDialog()`,
  `statusFeedback()`, `colorSwitch()`, `tabs()`, `renderList()` + `TemplateBuilder`/
  `field()`/`dateField()`/`badge()` (a whole string-based template engine inside the JS
  DSL), `externalService()`/`onSubmitExternal()`, `MessageAction.success()/error()/
  warning()` styled variants, `setTextAndColor()`, `hideOnBackdropClick()`,
  `Events.swipe()` builder, `Runtime.cache/memoize/guard` conventions. Each invents
  jweb-only vocabulary for things plain JS + the core DSL can express. (Framework-
  integration actions like `fetch/ok/fail/show/hide/toggleClass/navigateTo` are fine —
  they're platform verbs.)

- **J10 — Platform-name renames in JS* modules.**
  `JSDate.now()` emits `new Date()` but platform `Date.now()` is a timestamp (the DSL
  puts that under `timestamp()`) — invert to match platform;
  `JSClipboard.copyText` → `writeText`; `JSCrypto.randomValues` → `getRandomValues`;
  `JSWorker.broadcastMessage` → `postMessage` (module has three names for one method);
  `JSStorage.get/set/remove` → add `getItem/setItem/removeItem` aliases (only module
  that shortens platform names); `JSAbort.abortAfterTimeout` → mirror
  `AbortSignal.timeout`; `JSMedia.requestPiP/exitPiP` vs spelled-out siblings — pick one;
  `JSSpeech.speakBuilder` → `speak` returns the builder; JSGeolocation
  constants-as-methods (`PERMISSION_DENIED()`) vs JSRegex static fields — one convention;
  JSIndexedDB `keyOnly/keyLowerBound/keyBound` statics duplicate CursorBuilder's
  platform-named `only/lowerBound/bound` — drop the `key*` set.

- **J11 — Property get/set unification (biggest JS surface reduction).** JSCanvas has
  ~30 `setX(ctx,…)/getX(ctx)` pairs for platform *properties*; same disease in JSMedia
  (12 pairs) and JSWebAnimations. Proposal: `fillStyle(ctx)` reads / `fillStyle(ctx, v)`
  writes — halves the API, restores platform names. Optionally a chainable ctx wrapper:
  `ctx2d("myCanvas").fillRect(10,10,100,50).fillStyle("#f00")`.

- **J12 — Terminal contract violations** (X4): fix JSWorker/JSWebSocket/JSWebRTC
  `toVal()`; normalize the String-returning outliers (JSAnimation's `cancelRaf`,
  `transition`, `animate`, AnimationLoop, MediaSessionBuilder.build) to Val.

- **J13 — One home per API** (X9 applied): JSPromise becomes the single Promise home
  (Async keeps fetch/asyncFunc and delegates); `pushState`/`getQueryParam`/`currentUrl`
  deduped between Events/JSHistory/JSUrl; `spread*`/`pow`/`getFiles`/`arrayBuffer`
  deduped.

- **J14 — `JSHistory.pushState(url, state, title)` reverses the platform's
  `pushState(state, title, url)`.** Anyone who knows the platform passes args wrong.
  Match platform order, or keep url-first but drop the 3-arg form (title is dead per
  spec).

- **J15 — Closure-based debounce/throttle/memoize.** `debounce("timerVar", 300)` forces
  users to invent global variable names. Emit an IIFE closure instead — no name
  parameter, plain JS.

- **J16 — Preset bloat prune** (X10): JSIntl's 14 `formatKilometers…formatGigabytes`
  (keep `formatUnit(v, "kilometer")`); JSCanvas `createPatternRepeat*` (keep
  `createPattern(ctx, img, "repeat")`); JSCrypto algorithm presets (`ecdsaSha256`,
  `hmacSha512`, `rsaOaep2048`… — keep the string-arg base forms); `sleep`=`delay`
  aliases; `JSShare.canShare`=`isSupported`; Runtime.IIFE vs Guard identical member sets.

- **J17 — Signature fixes.** `authorization(token)` (full header value) sits next to
  `bearerToken(token)` (prepends "Bearer ") — easy to mix; GeolocationBuilder.build()
  throws at runtime for the wrong entry point (split the builder types);
  `JSAnimation.transition(elem, prop, from, to, ms)` uses `prop` as both a JS style key
  and inside the CSS shorthand — camelCase breaks the CSS, kebab breaks the style
  assignment (needs camel→kebab conversion); `Events.delegate(parent, event, child)`
  arg grouping; JSRegex constants are mutable `public static` — make them `final`.

- **J18 — Bugs found in passing (fix regardless of syntax decisions).**
  `Async.PromiseExecutorBuilder.setTimeout(int)` is a no-op (body: `return this;`);
  `JSWebSocket.autoReconnect` reconnects without re-attaching any handlers (deaf
  socket); `JSHistory.detectDirection` heuristic always reports "forward";
  JSWebAnimations empty-keyframes builds `.animate([],…)` silently.

- **J19 — Doc examples teach the verbose path.** Async's own javadoc uses
  `.then(callback("response").ret(variable("response").dot("json").invoke()))` although
  `.json()` exists. After the pass, sweep examples to showcase the short forms.

---

## S. State / server events (small)

- **S1 — Surface is clean** (`useState/useComputed/useEffect/useComponent`, `bind/
  bindInput`, `Event` interface). No changes proposed.
- **S2 — `Event.value()/checked()/formData()`** naming matches platform expectations —
  keep.

---

## Suggested top-10 shortlist (highest leverage per unit of work)

1. **X1** bare String = text (delete hijack overloads)
2. **H2+H3** one `(Object...)` form per element, delete redundant overload families
3. **X2/C16/C17** plain-String value overloads everywhere in CSS (and un-deprecate prop)
4. **C1** string-property CSS modules → real Style methods
5. **H4** InlineStyle → lambda + done (delete 300-line shim)
6. **X9/J13** one home per platform API (kills wildcard-import breakage)
7. **X5/J5** Val overloads replace the ~20 `*Expr` twins
8. **J2** varargs bodies replace endFor/endWhile/endTry/endSwitch
9. **H9** one input story, no silent `id=name`
10. **X10/J9/C10** prune the preset/widget layers (or quarantine in one opt-in module)
