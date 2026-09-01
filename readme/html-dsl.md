[← Back to README](./../README.md)

# HTML DSL

## Imports — one facade

As of the `jweb` short-import surface there is **one facade** for HTML:

```java
import static jweb.El.*;   // every element, attribute helper, typed input,
                           // conditional, popover, SVG shape, responsive image
```

`jweb.El` is the union of the two legacy facades (`El` + `Elements`): every element,
`attrs()` and the attribute shortcuts (`id`, `class_`, `alt`, `placeholder`, `aria`,
`role`, ...), typed inputs (`textInput`, `emailInput`, `checkbox`, `radio`, ...),
conditionals (`when`/`match`/`cond`/`otherwise`/`errorBoundary`), popovers,
`icon`/`appleIcon`, and the core SVG shapes.

## Two rules cover the whole DSL

**1. Every element is `name(Object... attributesAndChildren)`.** Attributes and children mix
freely in any order — `Attr`, `Attributes`, and a bare `style()` builder become attributes,
everything else becomes a child:

```java
div(class_("card"), id("main"),
    h1(text("Title")),
    p(text("Body")))

img(src("/logo.png"), alt("Logo"), loading("lazy"))
```

There are no separate `(Attributes, ...)` or `(InlineStyle, ...)` overloads any more — the
single varargs form always did the same thing, so ~120 duplicate overloads are gone.

**2. A lone String argument is always text.**

```java
a("Home")                    // <a>Home</a>
label("Email:")              // <label>Email:</label>
a(href("/"), "Home")         // <a href="/">Home</a>
```

Previously a single String was silently an attribute for some elements (`a` → href,
`label` → for, `textarea` → name), which is the single biggest trap this pass removed.
Two documented exceptions remain, because they are void elements or content-bearing:
`img(src)`, `img(src, alt)`, and `inlineScript(code)`.

Still separate on purpose:

```java
import static jweb.Input.*;                  // typed input DSL (names clash with El)
import static jweb.el.DialogHelper.*;        // dialog action helpers
import static jweb.el.DetailsHelper.*;       // details action helpers
```

> One deliberate semantic pick in the merge: `data(name, value)` builds a `data-*`
> **attribute** (the `Elements` meaning). Use `data_(value, text)` for the `<data>`
> element.

> The legacy imports (`com.osmig.Jweb.framework.elements.El` / `.Elements`) still
> compile — they are `@Deprecated` aliases of the same methods.

## Two styles, one surface

The same element can be written either way, and both reach every attribute:

```java
// Function style — attributes and children in one call
div(class_("card"), id("main"),
    h1(text("Title")),
    p(text("Body")))

// Builder style — chain from an empty element
div().class_("card").id("main")
     .child(h1().text("Title"))
     .child(p().text("Body"))
```

They produce identical HTML. Function style reads better when the structure is the point;
builder style when the configuration is.

`Tag` and `Attributes` share one definition of the attribute surface — the
`HtmlAttributes` interface, whose defaults are all built on a single `set(name, value)` —
so anything you can set on one you can set on the other, including every event handler.
A chain also keeps its exact type, with no casts:

```java
Tag row = td().colspan(2).class_("num").text("42");
Attributes a = attrs().rel("noopener").tabindex(1).targetBlank();
```

## What `jweb.El` exports

`El` is a pure static facade — each method is a one-line delegate to a category module (the
tables below list the legacy `El` core; `jweb.El` adds everything from `Elements` on top).

| Category | In `El` |
|----------|---------|
| **Attributes** | `attrs()`, `id`, `class_`, `href`, `src`, `type`, `name`, `value`, `disabled()`, `required()`, `attr(n,v)` |
| **Document** | `html`, `head`, `body`, `title`, `meta`, `link`, `css`, `script(src)`, `inlineScript(code)`, `style(css)`, `icon`, `appleIcon` |
| **Semantic** | `header`, `footer`, `nav`, `main`, `section`, `article`, `aside`, `hgroup`, `search`, `address` |
| **Text** | `div`, `span`, `p`, `h1`–`h6`, `strong`, `em`, `a`, `small`, `code`, `pre`, `time`, `wbr`, `br` |
| **Lists** | `ul`, `ol`, `li` |
| **Tables** | `table`, `thead`, `tbody`, `tr`, `th`, `td` |
| **Forms** | `form`, `input`, `textarea`, `select`, `option(value,text)`, `option(valueAndText)`, `label`, `button` |
| **Media** | `img`, `video`, `audio`, `canvas`, `iframe`, `track`, `embed`, `object`, `param`, `map`, `area`, `source`, `srcset` |
| **SVG** | `svg`, `path`, `circle`, `rect`, `line`, `polyline`, `polygon`, `g`, `d`, `viewBox`, `fill`, `stroke`, `strokeWidth` |
| **Modern HTML5** | `dialog`, `details`, `summary`, `meter`, `progress`, `template`, `slot`, `output`, `data_` (the `<data>` element), `bdi`, `bdo`, `ruby`, `rt`, `rp` |
| **Figure** | `figure`, `figcaption` |
| **Definition** | `dl`, `dt`, `dd` |
| **Interactive text** | `abbr`, `dfn`, `cite`, `q`, `blockquote`, `kbd`, `samp`, `var_`, `mark`, `sub`, `sup`, `ins`, `del`, `s` |
| **Picture** | `picture`, `source`, `srcset` (the `media`/`sizes`/`loading` attrs need `PictureElements`) |
| **Form enhancements** | `datalist`, `optgroup`, `fieldset`, `legend` (input helpers need `FormEnhancements`) |
| **Popovers** | `popover`, `popovertarget`, `popovertargetaction` |
| **Conditionals** | `when`, `match`, `cond`, `otherwise`, `errorBoundary` |
| **Misc** | `hr` |
| **Helpers** | `text`, `raw`, `fragment`, `each`, `tag` |

Because `jweb.El` includes all of `Elements`, the old gaps in the legacy `El` core
(`b`, `i`, `u`, `menu`, `tfoot`, `caption`, `colgroup`, `col`, `noscript`, `tryCatch`,
`colorInput`, `dateInput`, `timeInput`, `datetimeInput`, `rangeInput`, `ifElse`,
`classes`-style helpers) are gone — they're all in the one import.

**Still NOT in `jweb.El`** (import the module named in parentheses):

- `media`, `sizes`, `loading` attrs (→ `jweb.el.PictureElements`)
- `monthInput`, `weekInput`, and the submit-button overrides `formaction`/`formmethod`/…
  (→ `jweb.el.FormEnhancements`); typed `Input.text("name")` builders (→ `jweb.Input` —
  the names collide with `El.text`/`El.time` etc.)
- `popoverShowButton`/`popoverHideButton` and the `showPopover`/`hidePopover`/
  `togglePopover` JS helpers (→ `jweb.el.PopoverElements`)
- Naming note: in `jweb.El`, `data("key", "value")` builds the `data-*` **attribute**.
  The `<data>` element is `data_(...)` — pass the value as an attr:
  `data_(value("SKU-123"), "Widget")`.

## Elements — basics

```java
div(class_("container"),
    h1("Welcome"),
    p("Hello, World!")
)

nav(class_("navbar"),
    ul(
        li(a("/", "Home")),
        li(a("/about", "About")),
        li(a("/contact", "Contact"))
    )
)

table(class_("data-table"),
    thead(tr(th("Name"), th("Email"), th("Role"))),
    tbody(
        tr(td("John"), td("john@example.com"), td("Admin")),
        tr(td("Jane"), td("jane@example.com"), td("User"))
    )
)
```

Rules enforced by the VDOM:

- **Void elements** (`img`, `br`, `input`, `hr`, `meta`, `link`, `source`, …) throw
  `IllegalArgumentException` if you pass children.
- Strings become escaped text automatically; use `raw("...")` for trusted HTML.
- Collections passed as children are flattened one level.

## Attributes

`Attr` is a simple `record(name, value)` with static shortcuts. `Attributes` is the fluent
builder returned by `attrs()`. (`Attrs` is `@Deprecated(forRemoval = true)` — don't use it.)

```java
// Shortcuts
div(class_("card"), id("main"), ...)

// Fluent builder
div(attrs()
    .class_("card")
    .id("main")
    .data("user-id", "123")       // data-user-id
    .aria("label", "User card")   // aria-label
    .set("custom-attr", "v"),     // ⚠️ arbitrary attrs use set(), not attr()
    content
)

// Forms
form(attrs().action("/submit").method("POST"),
    label(for_("email"), "Email:"),
    input(attrs().type("email").name("email").placeholder("you@example.com").required()),
    button(type("submit"), "Subscribe")
)

// Conditional classes
div(attrs()
    .class_("btn")
    .classIf("active", isActive)            // adds "active" when true
    .classToggle(isOpen, "open", "closed")  // one or the other
)

// Layout shortcuts (inline flex/grid without a style builder)
// Deprecated: these live on the Style builder, where they compose with other
// properties instead of overwriting the style attribute.
div(attrs().style(s -> s.flexCenter()), ...)
div(attrs().style(s -> s.flexCol().gap(rem(1))), ...)
div(attrs().style(s -> s.flexRow().gap(rem(0.5))), ...)
div(attrs().style(s -> s.flexBetween()), ...)
div(attrs().style(s -> s.grid(3, rem(1))), ...)
```

`Attributes` covers essentially every HTML attribute, grouped: validation (`pattern`, `min`,
`max`, `step`, `minlength`, `maxlength`, `autocomplete`, `inputmode`), global (`tabindex`,
`lang`, `dir`, `contenteditable()`, `draggable`, `inert()`, `popover(type)`,
`popovertarget(id)`), link (`rel`, `download()`, `crossorigin`, `integrity`), media (`srcset`,
`sizes`, `loading`, `controls()`, `autoplay()`, `muted()`, `poster`), script (`async()`,
`defer()`, `nonce`), table (`colspan`, `rowspan`), SVG, microdata, dialog/details (`open()`),
and iframe (`sandbox`, `allow`).

### Event attributes

Two typed forms exist — there is **no** `onclick(String)` string setter:

```java
// 1. Server-side handler (Consumer<Event>) — registers with EventRegistry,
//    requires the client runtime to be wired (see State & Realtime doc)
button(attrs().onClick(e -> counter.update(n -> n + 1)), "Increment")

// 2. JS DSL Action — runs entirely in the browser
import static jweb.Actions.*;
button(attrs().onClick(toggle("panel")), "Toggle")

// DialogHelper/DetailsHelper return Actions too — attach the same way:
button(attrs().onClick(DialogHelper.showModal("confirm-dialog")), "Open")

// For genuinely raw JS strings, use set() — but note this renders a real
// inline attribute, which a nonce CSP (Middlewares.recommended) blocks:
button(attrs().set("onclick", "console.log('hi')"), "Log")
```

Both typed forms are CSP-safe. Inside a page render neither writes an inline
`on<type>=` attribute (a nonce CSP can never allow those): server handlers
render `data-jweb-on<type>`, Actions render `data-jweb-act<type>`, and the
runtime delegates events to them. An Action's JS travels in a nonce-stamped
definitions script — with the page, with its streamed chunk, or inside a
swapped fragment (the runtime executes it on swap). Outside a render context
(bare `toHtml()`, static export) Actions fall back to the classic inline
attribute, which works wherever no CSP is enforced.

Available on `Attributes` for both forms: `onClick`, `onChange`, `onInput`, `onSubmit`,
`onFocus`, `onBlur`, `onKeyDown`, `onKeyUp`, mouse/drag/touch/scroll/animation events, and the
generic `on(type, handler)`.

### Builder shortcuts that replace quoted strings

```java
// Document head
metaCharset()                      // <meta charset="UTF-8">
metaViewport()                     // the standard responsive viewport tag

// SVG line icons (viewBox from ints; lineIcon = no fill, currentColor
// stroke, rounded caps/joins)
svg(attrs().viewBox(0, 0, 24, 24).width(24).height(24).lineIcon(2),
    path(attrs().d("M9 21H5a2...")))

// Also: strokeWidth(int), strokeRound(), width(int), height(int)
```

## Fluent builders: `Input`, `Button`, `Form`

Beyond `input(attrs()...)`, dedicated builders exist:

```java
import jweb.Input;
import com.osmig.Jweb.framework.elements.Button;
import com.osmig.Jweb.framework.elements.Form;   // the small elements/Form builder — no jweb shell
                                                 // (jweb.Form is the richer forms/Form)

Input.email("email").placeholder("you@example.com").required()
Input.password("pw").minLength(8)
Input.range("volume")          // also: text/number/tel/url/search/date/time/color/checkbox/radio/file/hidden

Button.submit("Save")
Button.of("Cancel").formAction("/cancel")

Form.post("/api/users")
    .multipart()
    .add(Input.text("name").required())
    .add(Button.submit("Create"))
```

There is also a much richer `forms/Form` builder (labels, help text, radio groups, selects) and
`forms/FormModel` (POJO → form) — see [Backend](./backend.md#forms).

## Tag instance API

Every element factory returns a `Tag`, which is itself fluent:

```java
div()
    .addClass("card")
    .data("id", "42")
    .child(h2("Title"))
    .children(list.stream().map(ItemView::new).toList())
    .each(users, u -> li(u.name()))          // iterate on the instance
    .when(isAdmin, () -> adminBadge())       // conditional child
    .styled(style().padding(px(16)))         // per-element stylesheet class (jweb-N)
    .hover(style().backgroundColor(hex("#f5f5f5")))
```

`.styled()/.hover()/.focus()/.active()` generate a unique `jweb-N` class plus an inline
`<style>` block next to the element (see CSS DSL doc).

## Modern HTML5 Elements

```java
// Dialog (modal) — helpers return Actions; attach via onClick()
import static jweb.el.DialogHelper.*;

dialog(attrs().id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure?"),
    button(attrs().onClick(close("confirm-dialog")), "Cancel"),
    button(attrs().onClick(close("confirm-dialog", "confirmed")), "Confirm")
)
button(attrs().onClick(showModal("confirm-dialog")), "Open Dialog")

// Details/Summary — name attribute creates an exclusive accordion
details(attrs().name("faq"), summary("Question 1"), p("Answer 1"))
details(attrs().name("faq"), summary("Question 2"), p("Answer 2"))

// Progress and Meter
progress(70, 100)          // determinate
progress()                 // loading state (indeterminate)
meter(0.6, 0, 1)           // scalar measurement

// Machine-readable values
time(datetime("2026-08-08"), "August 8, 2026")
data_(value("SKU-123"), "Product Widget")     // data(n,v) is the data-* attribute
```

Both helper classes return `Action` values (the same type `jweb.Actions` produces), so
they plug straight into `onClick(...)`/`on(type, ...)`.
`DialogHelper`: `showModal`, `show`, `close`, `close(id, returnValue)`, `toggle`,
`closeOnBackdropClick`, `getReturnValue`, `isOpen`.
`DetailsHelper`: `open`, `close`, `toggle`, `isOpen`, `openExclusive`, `closeAll`, `openAll`,
`closeAllBySelector`, `openAllBySelector`.

## Popover API (`PopoverElements` — separate import)

```java
import static jweb.el.PopoverElements.*;

// Attribute factories
div(popover("auto"), id("my-popover"), p("Popover content"))
button(popovertarget("my-popover"), "Toggle")
button(popovertarget("my-popover"), popovertargetaction("show"), "Show")

// Prebuilt elements
div(popover(), id("tips"), p("Closes when clicking outside"))
manualPopover("pinned", p("Stays until explicitly closed"))
button(popovertarget("tips"), "Toggle tips")
button(popovertarget("tips"), popovertargetaction("show"), "Show")
button(popovertarget("tips"), popovertargetaction("hide"), "Hide")

// JS helpers (return strings — attach via set() or inlineScript)
showPopover("tips"); hidePopover("tips"); togglePopover("tips");
```

> The `autoPopover`/`manualPopover`/`popover*Button` composites are deprecated: each saved
> one attribute over the plain form shown above.

## Responsive Images (`PictureElements` — separate import)

```java
import static jweb.el.PictureElements.*;

picture(
    source(srcset("image.avif"), type("image/avif")),
    source(srcset("image.webp"), type("image/webp")),
    img("image.jpg", "Fallback description")     // img comes from El
)

// Exact signatures:
// Every element takes attributes directly, so these need no special helper:
img(src("image.jpg"), alt("Description"), srcset("image@2x.jpg 2x"))
img(src("image.jpg"), alt("Description"), width(640), height(480), loading("lazy"))

// Attribute factories: srcset, media, sizes, type, width, height,
// loading("lazy"|"eager"), decoding, fetchpriority (exact HTML spelling)
```

## Definition Lists / Figures / Interactive Text

```java
// Definition lists (in El)
dl(
    dt("HTML"), dd("HyperText Markup Language"),
    dt("CSS"),  dd("Cascading Style Sheets")
)

// Figures (in El)
figure(class_("code-example"),
    pre(code("const x = 42;")),
    figcaption("Example: Variable declaration")
)

// Interactive/semantic text (in El)
p("The ", abbr("HTML", "HyperText Markup Language"), " specification")
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")
p("Search results for: ", mark("JWeb framework"))
p("H", sub("2"), "O")
p("E = mc", sup("2"))
p(del("old price: $20"), " ", ins("new price: $15"))
blockquote("https://example.com/source", p("Quoted text with a cite URL"))
```

> Overload hazard: `q("Hello")` and `abbr("HTML")` resolve to the varargs form (text
> child). `blockquote` goes the other way — `blockquote("text")` picks the
> `(citeUrl, ...)` overload, so a lone String becomes the cite URL; wrap content in
> `p(...)` as above.

## Form Enhancements (`FormEnhancements` — separate import for input helpers)

```java
import static jweb.el.FormEnhancements.*;

// Datalist for autocomplete (datalist/optgroup/fieldset/legend are also in El)
input(attrs().list("browsers")),
datalist("browsers",
    option("chrome", "Chrome"),        // option is (value, text); option("v") uses v as both
    option("firefox", "Firefox"),
    option("safari", "Safari")
)

select(attrs().name("car"),
    optgroup("Swedish Cars",
        option("volvo", "Volvo"),
        option("saab", "Saab")),
    optgroup("German Cars",
        option("bmw", "BMW"),
        option("audi", "Audi"))
)

fieldset(
    legend("Personal Information"),
    label(attrs().for_("name"), "Name:"),
    input(attrs().type("text").name("name").id("name"))
)

// Typed input helpers (monthInput/weekInput are FormEnhancements-only;
// the rest are also in jweb.El)
colorInput("theme-color", "#3b82f6")
dateInput("birthday")
dateInput("event", "2026-01-01", "2026-12-31")   // with min/max
timeInput("meeting-time")
datetimeInput("appointment")
monthInput("birth-month")
weekInput("work-week")
rangeInput("volume", 0, 100, 50)
rangeInput("opacity", 0, 100, 50, 5)             // with step

// Submit-button overrides: formaction, formmethod, formenctype, formtarget, formnovalidate
```

## Conditional Rendering (in `jweb.El`)

```java
import static jweb.El.*;

// Simple conditional (lazy)
when(isLoggedIn, () -> span("Welcome, " + user.getName()))
when(isLoggedIn, welcomeBanner)          // eager variant

// If/elif/else chain — terminate with otherwise(...) or end()
when(isAdmin)
    .then(adminPanel())
    .elif(isModerator, modPanel())
    .otherwise(loginPrompt())

// Pattern-matching style
match(
    cond(isAdmin, adminPanel()),
    cond(isModerator, modPanel()),
    otherwise(loginPrompt())
)
```

> Two `otherwise` shapes exist: the free function (returns a `CondCase` for `match`) and the
> `Condition` method (returns `Element`). This is fine in practice but confuses auto-import.

## Collection Iteration & Fragments (in `El`)

```java
ul(each(users, user ->
    li(class_("user-item"),
        strong(user.getName()),
        span(" - " + user.getEmail()))
))

fragment(
    h1("Title"),
    p("First paragraph"),
    p("Second paragraph")
)
```

## Error Boundaries

```java
// Free function (Elements)
import static jweb.El.*;
errorBoundary(() -> riskyComponent.render(),
              error -> p("Error: " + error.getMessage()))
tryCatch(() -> riskyComponent.render())   // silent empty fallback

// Fluent class (core)
import com.osmig.Jweb.framework.core.ErrorBoundary;
ErrorBoundary.of(() -> riskyComponent.render())
    .fallback(err -> div(class_("error"), p(err.getMessage())))
    .onError(err -> Log.framework().error("render failed", err));
ErrorBoundary.silent(() -> widget.render());
ErrorBoundary.withMessage(() -> widget.render(), "Something went wrong");
```

## Layout helpers (`framework/layout/Layout`)

A static utility of ~45 prebuilt layout wrappers (distinct from your app's `Layout` template):

```java
import jweb.Layout;

Layout.container(...)      // centered max-width container
Layout.row(...) / Layout.column(...) / Layout.center(...)
Layout.spaceBetween(...) / Layout.cluster(...) / Layout.stack(...)
Layout.grid(3, ...) / Layout.autoGrid(minColWidth, gap, ...)
Layout.sidebar(px(280), side, main)             // (sidebarWidth, sidebar, content)
Layout.card(...) / Layout.divider() / Layout.spacer() / Layout.space(px(24))
Layout.sticky(top, ...) / Layout.scrollable(height, ...) / Layout.aspectRatio("16/9", ...)
Layout.visuallyHidden(...)  // a11y: screen-reader-only content
```

## Raw content & custom tags

```java
text("escaped text")                 // VText
raw("<b>trusted html</b>")           // VRaw — no escaping, use with care
tag("custom-element", attrs().set("prop", "x"), span("child"))

// Full-response raw payloads (from route handlers):
RawContent.json("{\"ok\":true}")     // application/json response
RawContent.html("<h1>hi</h1>")       // text/html response
```
