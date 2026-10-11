[← Back to README](./../README.md)

# The 4.0 plan

What is left after 3.0.2, measured on 2026-10-10, with a recommendation on each item and
the order to do them in. Everything here is **breaking**, so it ships together, behind one
tag, with a migration guide in the style of [Migrating to 3.0](../dsl-simplification-3.md).
Nothing in this list is on `main` yet except the additive first step of item 1.

The framework's own sample app is the measuring stick, as in the earlier passes: where its
pages route around a hook, the hook is not the path of least resistance.

## 1. JavaScript identifiers are still strings

The HTML and CSS sides reached "no raw strings" in 3.0.2 through handles (`Cls`, `Id`,
`Keyframes`). The JavaScript side has not. In app code today:

| shape | count | where |
|---|---|---|
| `v("name")` | 192 | 37 in `SandboxScript`, the rest in the JavaScript docs sections, which teach the form |
| `.dot("prop")` | 71 | 51 in `SandboxScript` |
| `.call("fn")` | 59 | 29 in `SandboxScript`, 19 in `JSEvents` |
| `str("…")` | 25 | string literals — these are text, not identifiers, and stay |

**Shipped now (additive):** `jweb.Var` from `local("q")` — a `Val` that declares, assigns
and reads itself, and is a callback parameter by handle (`callback(E, T)`). The docs search
script uses it; [JavaScript DSL](./javascript-dsl.md#locals-are-handles-local) documents it.

**4.0:**
- Migrate `SandboxScript` and the `*Script` classes to `Var` handles; keep `v()` for the
  docs sections that teach it, under an explicit `// raw:` marker, until they are rewritten.
- `.dot("prop")` on a `JS.El` already has typed twins for the DOM (`value()`, `html()`,
  `getData(...)`). What remains is property access on plain objects — JSON a fetch returned,
  the event. Add a `Prop` handle (`static final Prop TITLE = prop("title")`,
  `data.get(TITLE)`) only if the count justifies it; otherwise accept `.dot` on JSON as the
  honest escape hatch and name it so (`raw`-family naming, item 3).
- `.call("fn")`: a `Fn` handle for script-level functions declared with `func(...)` — the
  declaration returns the handle, and calls go through it (`SB_STATUS.call(st)`), so a
  function is named once like a local.
- Then extend `jweb/AppDslHygieneTest` with `v("`, `.dot("` and `.call("` so the gate
  holds.

## 2. The deprecated surface is mostly the real implementation

`@Deprecated` appears on 439 declarations in 163 main files. The important fact for 4.0:
**145 of the framework's classes carry the annotation at class level, and most of them are
not aliases but the implementation**, with the `jweb.*` name a thin subclass
(`jweb.Js extends …framework.js.Actions`, `jweb.Metrics` is the exception, where the
framework name is the alias). The annotation marks "do not spell this name", not "this
will be deleted".

So 4.0's job here is a move, not a delete: the implementation goes under `jweb.*`, the
`com.osmig…` class becomes the thin deprecated subclass or disappears. In that order:

1. Value and handle types that are already real in `jweb` (`Val`, `Func`, `Cls`, `Id`,
   `Ref`, `Var`, `Request`, `Response`…) — nothing to do.
2. The facades (`El`, `Css`, `Js`, `Three`) and the modules they chain: move each
   implementation class under `jweb.el`, `jweb.css`, `jweb.js`, `jweb.three` keeping the
   class name; leave the old FQN as an empty deprecated subclass for one release.
3. Delete the members that are aliases of something that already has a better spelling and
   have no caller in the repo: `class_(String)` (handles), `useComponent` (`live`),
   `Toast.*Js`, `match/cond/otherwise`, `text()` around a String child, `Attributes.style()`
   chains that `style()` as an element argument replaced.
4. Then the "one import" goal: `State<T>` reachable from `jweb.El`-adjacent imports without
   the `jweb.state.State` single-type import (today's documented sharp edge).

`jweb/NoLongImportsTest` and `jweb/JwebSurfaceTest` are the guards; both exist.

## 3. Every escape hatch named `raw`

Today the hatches are `unsafeRaw`, `.raw(`, `CSSValue.of(`, `Selector.of(`, `.prop(String,
String)`, `.set("on…")`, `inlineScript(String)`. One family name — `raw` — makes the hygiene
gate one pattern and the code review one search. Rename with deprecated forwarding
aliases in 3.x, delete the aliases in 4.0.

## 4. `Val` as a statement

The 3.0 pass made the statement builders accept an `Action` as a statement. A `Val` is
also accepted in `does(...)` (via `JS.toStatement`), so the practical gap is narrow: a `Val`
cannot be passed where the type is `Action` (`FetchBuilder.ok(Action)`, `onClick(Action)`),
and the fix today is `actions().does(val)`. Making `Val implements Action` would close it
but every method overloaded on `(Val)` and `(Action)` becomes ambiguous for a `Var`/`El`
argument — the surface has dozens. **Recommendation:** do not; instead add the few missing
`(Val)` overloads where an expression is commonly the whole statement (`ok(Val)`,
`then(Val)`), and keep the two types distinct.

## 5. The component form

PLAN.md's original form was a `Component` base class with `@State` fields and
`onClick(this::increment)`. Half of it exists: `onClick(this::increment)` compiles today
when `increment` takes an `Event`, and a `Template` is already the component unit.
What does not exist is reflection-tracked `@State` fields.

**Recommendation: keep `State<T>` explicit.** `useState` + `live` makes the dependency
between a region and its state visible in the code and testable through `RenderedPage`;
field tracking would need a proxy or bytecode step and would hide which regions re-render.
What 4.0 should add instead is the ergonomic half: `State<T>` as one import (item 2.4),
and a `Component` marker that is a `Template` with a conventional `styles()`/`scripts()`
pair — which is what the CLI's `generate component` already produces.

## 6. Structure, kept honest

The 3.0.2 gate (`AppDslHygieneTest`) and the request-aware page table removed the
structural bypasses; the remaining ones are documented sharp edges, not gaps: middleware
does not run for `@REST` controllers, `/api/v*` is reserved, WebSocket messages skip
guards by design. 4.0 does not change these.

## Order

1. Item 3 (renames with aliases) — can ship in 3.x.
2. Item 1's migration and gate.
3. Item 2's move, facade by facade, each behind the surface tests.
4. Item 2.3 deletions and item 4's overloads.
5. Tag 4.0 with the migration guide; the-gallery and the CLI templates move with it.
