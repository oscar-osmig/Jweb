[← Back to README](./../README.md)

# HTML DSL

## Imports — one facade

```java
import static jweb.El.*;   // every element, attribute shortcut, event handler, typed
                           // input, conditional, popover, SVG shape, state binding
```

`jweb.El` is the union of the legacy `El` + `Elements` facades: every element, `attrs()`
and the attribute shortcuts (`id`, `class_`, `href`, `src`, `placeholder`, `data`, `aria`,
`role`, ...), every `on*` handler, the swap family, `ref`, `bind`, typed inputs
(`textInput`, `emailInput`, `checkbox`, `radio`, ...), `when`, popovers, `icon`/`appleIcon`,
and the core SVG shapes.

It coexists with the other three DSL imports — `jweb.Css.*`, `jweb.Js.*`, `jweb.Three.*` —
without an ambiguous name between them.

## Two rules cover the whole DSL

**1. Every element is `name(Object... itemsAndChildren)`.** Attributes, handlers, a bare
`style()` builder and children mix freely, in any order:

```java
div(class_("card"), id("main"),
    h1("Title"),
    p("Body"))

button(id("save"), onClick(e -> save()), style().padding(px(8)), "Save")

img(src("/logo.png"), alt("Logo"), loading("lazy"))
```

**2. A String argument is always text.** Wherever it appears, whatever the element:

```java
a("Home")                    // <a>Home</a>
a(href("/"), "Home")         // <a href="/">Home</a>
label(for_("email"), "Email:")
option(value("us"), "United States")
a("Hello ", strong("world")) // text, never a link to "Hello "
```

No element reads its first String as an attribute any more (`a(href, text)`,
`label(forId, text)`, `option(value, text)`, `blockquote(cite, ...)`, `datalist(id, ...)`,
`optgroup(label, ...)` and `abbr(text, title)` are gone — see
[Migrating to 3.0](./../dsl-simplification-3.md)). The exceptions are void elements, which
cannot hold text, so their Strings are their most common attributes: `img(src)`,
`img(src, alt)`, `meta(name, content)`, `input(type, name)`; and the code-bearing
`inlineScript(js)` / `style(css)`, whose String is emitted verbatim. `text("…")` still
exists when you want to say it explicitly.

Still separate on purpose:

```java
import static jweb.Input.*;                  // typed input DSL (names clash with El)
import static jweb.el.DialogHelper.*;        // dialog Actions
import static jweb.el.DetailsHelper.*;       // details Actions
```

> `data(name, value)` builds a `data-*` **attribute**. The rare `<data>` and `<var>`
> elements are `tag("data", value("SKU-1"), "Widget")` and `tag("var", "x")` — their
> bare names belong to the attribute and to the CSS DSL's `var(...)`.

## Two styles, one surface

```java
// Function style — attributes, handlers and children in one call
div(class_("card"), id("main"),
    h1("Title"),
    p("Body"))

// Builder style — chain from an empty element
div().class_("card").id("main")
     .child(h1().text("Title"))
     .child(p().text("Body"))
```

They produce identical HTML. Function style reads better when the structure is the point;
builder style when the configuration is. `Tag` and `Attributes` share one definition of the
attribute surface (the `HtmlAttributes` interface, built on a single `set(name, value)`),
so anything you can set on one you can set on the other, and a chain keeps its exact type:

```java
Tag row = td().colspan(2).class_("num").text("42");
Attributes a = attrs().rel("noopener").tabindex(1).targetBlank();
```

## Handlers are arguments

Every event handler `Attributes` accepts is also a plain element argument, in both
flavors, so `attrs()` is never needed just to attach one:

```java
// Server-side handler (Consumer<Event>) — runs in Java over the WebSocket
button(onClick(e -> counter.update(n -> n + 1)), "Increment")

// Client-side Action — runs in the browser, no round trip
import static jweb.Js.*;
button(onClick(toggle("panel")), "Toggle")
button(onClick(showModal("confirm-dialog")), "Open")     // DialogHelper returns Actions
button(onClick(inputRef.focus()), "Focus")               // so does Ref
button(onClick(Toast.success("Saved!")), "Save")         // and Toast

// Any event type
div(on("pointerdown", e -> start(e)), ...)
```

Available for both forms: `onClick`, `onChange`, `onInput`, `onSubmit`, `onFocus`,
`onBlur`, `onKeyDown`, `onKeyUp`, `onMouseEnter`, `onMouseLeave`, `onDoubleClick`; the
Consumer form also covers mouse/drag/touch/scroll/animation/clipboard events, and
`on(type, handler)` takes anything.

Every form is CSP-safe. Inside a page render no handler writes an inline `on<type>=`
attribute (a nonce CSP can never allow those): server handlers render
`data-jweb-on<type>`, Actions render `data-jweb-act<type>`, and the serializer rewrites
even raw `attrs().set("on<type>", js)` strings to the same delegation at render time. Handler
JS travels in a nonce-stamped definitions script — with the page, its streamed chunk, or
inside a swapped fragment. Outside a render context (bare `toHtml()`, static export)
everything falls back to classic inline attributes; `attrs().inlineHandlers()` forces that
per element for content that ships without the runtime.

## Server-driven UI as arguments

The swap family, refs and state bindings are arguments too — the whole
"fragment over the wire" story without `attrs()`:

```java
button(swap("/products/list?page=2", "#products"), swapPush("/products?page=2"), "Next")
form(id("contact-form"), action("/contact/submit"), method("post"),   // no-JS fallback
     swapForm("/contact/submit", "#form-status"),                      // progressive swap
     ...)

Ref search = Ref.create();
input(ref(search), type("text"))

State<Integer> clicks = useState(0);
p("Clicks: ", span(bind(clicks), clicks.get()))    // patched live on every change
```

## Attributes

`Attr` is a `record(name, value)` with the common shortcuts; `Attributes` is the fluent
builder returned by `attrs()`, for the long tail and for chaining several unusual ones:

```java
div(class_("card"), id("main"), data("user-id", "123"), aria("label", "User card"), ...)

div(attrs()
    .rel("noopener").tabindex(1)
    .classIf("active", isActive)            // adds "active" when true
    .classToggle(isOpen, "open", "closed")  // one or the other
    .set("custom-attr", "v"),               // any attribute
    content)
```

`Attributes` covers essentially every HTML attribute: validation (`pattern`, `min`,
`max`, `step`, `minlength`, `maxlength`, `autocomplete`, `inputmode`), global (`tabindex`,
`lang`, `dir`, `title`, `contenteditable()`, `draggable`, `inert()`, `popover(type)`,
`popovertarget(id)`), link (`rel`, `download()`, `crossorigin`, `integrity`), media
(`srcset`, `sizes`, `loading`, `controls()`, `autoplay()`, `muted()`, `poster`), script
(`async()`, `defer()`, `nonce`), table (`colspan`, `rowspan`), SVG, microdata,
dialog/details (`open()`), iframe (`sandbox`, `allow`).

Attribute names use exact HTML spelling (`fetchpriority`, `popovertarget`, `minlength`).
A trailing underscore marks a Java keyword and nothing else: `class_`, `for_`. The `title`
and `style` attributes have no underscore shortcut — `attrs().title(...)`, and a bare
`style()` builder argument.

### Inline styles

```java
div(style().padding(SP_4).color(TEXT), "hi")              // bare builder as an argument
div(class_("card"), id("hero"), style().margin(zero), p("content"))

div(attrs().class_("card").style(s -> s.display(flex).gap(rem(1))), ...)   // inside attrs()
```

### Builder shortcuts that replace quoted strings

```java
metaCharset()                      // <meta charset="UTF-8">
metaViewport()                     // the standard responsive viewport tag
css("/app.css")                    // <link rel="stylesheet" href="/app.css">

// SVG line icons (viewBox from ints)
svg(viewBox(0, 0, 24, 24), attrs().width(24).height(24),
    path(d("M9 21H5a2...")))
```

## Fluent builders: `Input`, `Button`, `Form`

Beyond `input(type("email"), name("email"), ...)`, dedicated builders exist:

```java
import jweb.Input;
import com.osmig.Jweb.framework.elements.Button;
import com.osmig.Jweb.framework.elements.Form;   // the small elements/Form builder
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

There is also a much richer `forms/Form` builder (labels, help text, radio groups, selects)
and `forms/FormModel` (POJO → form) — see [Backend](./backend.md#forms).

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

## Templates are elements

A `Template` is an `Element`, so components drop straight into a tree — no `.render()`:

```java
html(new Head(title), body(new Nav(), main(content), new Footer()))
```

## Modern HTML5 Elements

```java
// Dialog (modal) — helpers return Actions
import static jweb.el.DialogHelper.*;

dialog(id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure?"),
    button(onClick(close("confirm-dialog")), "Cancel"),
    button(onClick(close("confirm-dialog", "confirmed")), "Confirm")
)
button(onClick(showModal("confirm-dialog")), "Open Dialog")

// Details/Summary — name attribute creates an exclusive accordion
details(name("faq"), summary("Question 1"), p("Answer 1"))
details(name("faq"), summary("Question 2"), p("Answer 2"))

// Progress and Meter
progress(70, 100)          // determinate
progress()                 // loading state (indeterminate)
meter(0.6, 0, 1)           // scalar measurement

// Machine-readable values
time(datetime("2026-08-08"), "August 8, 2026")
tag("data", value("SKU-123"), "Product Widget")
```

`DialogHelper`: `showModal`, `show`, `close`, `close(id, returnValue)`, `toggle`,
`closeOnBackdropClick`, `getReturnValue`, `isOpen`.
`DetailsHelper`: `open`, `close`, `toggle`, `isOpen`, `openExclusive`, `closeAll`, `openAll`,
`closeAllBySelector`, `openAllBySelector`.

## Popover API

```java
div(popover("auto"), id("tips"), p("Closes when clicking outside"))
div(popover("manual"), id("pinned"), p("Stays until explicitly closed"))
button(popovertarget("tips"), "Toggle tips")
button(popovertarget("tips"), popovertargetaction("show"), "Show")

// Actions, for when a button isn't the trigger
import static jweb.el.PopoverElements.*;
div(onMouseEnter(showPopover("tips")), onMouseLeave(hidePopover("tips")), "Hover me")
```

## Responsive Images

```java
picture(
    source(srcset("image.avif"), type("image/avif")),
    source(srcset("image.webp"), type("image/webp")),
    img("image.jpg", "Fallback description")
)

img(src("image.jpg"), alt("Description"), srcset("image@2x.jpg 2x"))
img(src("image.jpg"), alt("Description"), attrs().width(640).height(480).loading("lazy"))
```

The `media`/`sizes` attribute factories live in `jweb.el.PictureElements`.

## Definition Lists / Figures / Interactive Text

```java
dl(
    dt("HTML"), dd("HyperText Markup Language"),
    dt("CSS"),  dd("Cascading Style Sheets")
)

figure(class_("code-example"),
    pre(code("const x = 42;")),
    figcaption("Example: Variable declaration")
)

p("The ", abbr(attr("title", "HyperText Markup Language"), "HTML"), " specification")
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")
p("Search results for: ", mark("JWeb framework"))
p("H", sub("2"), "O")
p("E = mc", sup("2"))
p(del("old price: $20"), " ", ins("new price: $15"))
blockquote(attr("cite", "https://example.com/source"), p("Quoted text with a cite URL"))
```

## Form Enhancements

```java
input(attrs().list("browsers")),
datalist(id("browsers"),
    option("Chrome"),                  // text — the browser uses it as the value too
    option("Firefox"),
    option("Safari")
)

select(name("car"),
    optgroup(attr("label", "Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")),
    optgroup(attr("label", "German Cars"),
        option(value("bmw"), "BMW"),
        option(value("audi"), "Audi"))
)

fieldset(
    legend("Personal Information"),
    label(for_("name"), "Name:"),
    input(type("text"), name("name"), id("name"))
)

// Typed input helpers (the input's id defaults to its name, so label(for_(name), ...) pairs)
colorInput("theme-color", "#3b82f6")
dateInput("birthday")
dateInput("event", "2026-01-01", "2026-12-31")   // with min/max
timeInput("meeting-time")
rangeInput("volume", 0, 100, 50)
// monthInput/weekInput and the submit-button overrides formaction/formmethod/... are in
// jweb.el.FormEnhancements
```

## Conditional Rendering

One shape. `when(condition, element)` renders the element or nothing; the Supplier form
only builds it when needed — the same `when` the Three DSL uses:

```java
when(isLoggedIn, () -> span("Welcome, " + user.getName()))
when(isLoggedIn, welcomeBanner)

// Two branches: Java's ternary
isLoggedIn ? userMenu() : loginButton()

// Several: a switch expression
switch (role) {
    case ADMIN -> adminPanel();
    case MODERATOR -> modPanel();
    default -> guestPanel();
}
```

`null` children render nothing, so a conditional branch never needs a placeholder. The
`when(cond).then(...).elif(...).otherwise(...)` chain and `match(cond(...), otherwise(...))`
are deprecated — Java already has both.

## Collection Iteration & Fragments

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
errorBoundary(() -> riskyComponent.render(),
              error -> p("Error: " + error.getMessage()))
tryCatch(() -> riskyComponent.render())   // silent empty fallback

import com.osmig.Jweb.framework.core.ErrorBoundary;
ErrorBoundary.of(() -> riskyComponent.render())
    .fallback(err -> div(class_("error"), p(err.getMessage())))
    .onError(err -> Log.framework().error("render failed", err));
```

## Layout helpers (`jweb.Layout`)

A static utility of ~45 prebuilt layout wrappers. Most apps also have their own `Layout`
template, so import this one qualified:

```java
jweb.Layout.container(...)      // centered max-width container
jweb.Layout.row(...) / column(...) / center(...)
jweb.Layout.spaceBetween(...) / cluster(...) / stack(...)
jweb.Layout.grid(3, ...) / autoGrid(minColWidth, gap, ...)
jweb.Layout.sidebar(px(280), side, main)
jweb.Layout.card(...) / divider() / spacer() / space(px(24))
jweb.Layout.sticky(top, ...) / scrollable(height, ...) / aspectRatio("16/9", ...)
jweb.Layout.visuallyHidden(...)  // a11y: screen-reader-only content
```

## Raw content & custom tags

```java
text("escaped text")                 // VText — the explicit form of a bare String
raw("<b>trusted html</b>")           // VRaw — no escaping, use with care
tag("custom-element", attrs().set("prop", "x"), span("child"))

// Full-response raw payloads (from route handlers):
RawContent.json("{\"ok\":true}")     // application/json response
RawContent.html("<h1>hi</h1>")       // text/html response
```
