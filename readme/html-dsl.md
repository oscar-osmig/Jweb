[← Back to README](./../README.md)

# HTML DSL

## Imports — one facade

```java
import static jweb.El.*;   // every element, attribute shortcut, event handler,
                           // conditional, popover, SVG element, state binding
```

One import. `jweb.El` carries every element, `attrs()` and the attribute shortcuts (`id`,
`cls`, `href`, `src`, `placeholder`, `data`, `aria`, `role`, ...), every `on*` handler, the
swap family, `ref`, `bind`, `when`/`classes`/`each`, popovers, `icon`/`appleIcon`, the
dialog and details Actions, the whole SVG element set, and `form(SomeRecord.class)`.

It coexists with the other three DSL imports — `jweb.Css.*`, `jweb.Js.*`, `jweb.Three.*` —
without an ambiguous name between them.

## Two rules cover the whole DSL

**1. Every element is `name(Object... itemsAndChildren)`.** Attributes, handlers, a bare
`style()` builder and children mix freely, in any order:

```java
void save() {}

div(cls("card"), id("main"),
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
`inlineScript(js)` / `style(css)`, whose String is emitted verbatim.

`text("…")` is deprecated — the bare String *is* the escaped text node. It only survives
for the rare spot that needs an `Element` value where no element factory is in reach.

> `data(name, value)` builds a `data-*` **attribute**. The rare `<data>` and `<var>`
> elements are `tag("data", value("SKU-1"), "Widget")` and `tag("var", "x")` — their
> bare names belong to the attribute and to the CSS DSL's `var(...)`.

## Two styles, one surface

```java
// Function style — attributes, handlers and children in one call
div(cls("card"), id("main"),
    h1("Title"),
    p("Body"))

// Builder style — chain from an empty element
div().cls("card").id("main")
     .child(h1().text("Title"))
     .child(p().text("Body"))
```

They produce identical HTML. Function style reads better when the structure is the point;
builder style when the configuration is. `Tag` and `Attributes` share one definition of the
attribute surface (the `HtmlAttributes` interface, built on a single `set(name, value)`),
so anything you can set on one you can set on the other, and a chain keeps its exact type:

```java
Tag row = td().colspan(2).cls("num").text("42");
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
div(cls("card"), id("main"), data("user-id", "123"), aria("label", "User card"), ...)

div(attrs()
    .rel("noopener").tabindex(1)
    .classIf(isActive, "active")            // condition first, like when(...)
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
A trailing underscore marks a Java keyword and nothing else: `class_`, `for_`.

Four attributes have no free static name because an element already owns it, so they live
on `attrs()` only: **`title`**, **`label`**, **`cite`** and **`style`**.

```java
button(attrs().title("Save"), "Save")        // the tooltip
button(title("Save"), "Save")                // NOT the tooltip — nests a <title> element
optgroup(attrs().label("Swedish Cars"), option(value("volvo"), "Volvo"))
blockquote(attrs().cite("https://example.com/source"), p("Quoted"))
div(style().padding(px(8)), "hi")            // the style builder is its own argument
```

`selected()` and `enctype(...)` are free statics, like `required()` and `checked()`.

`title(...)` is the `<title>` **element**. `button(title("x"), ...)` compiles and silently
nests a document title inside the button; the renderer logs a warning when a `<title>`
turns up outside a head or SVG parent.

### Class names

`cls("a b")` is the everyday spelling; `class_` is the same attribute under the
trailing-underscore keyword rule. `classes(...)` joins its parts and drops the ones that
did not match, so a class list is never built with `+` and a ternary:

```java
boolean active = true;

div(cls("card"))

String size = "lg";
div(classes("chip", when(active, "chip-on"), size))     // class="chip chip-on lg"
div(classes("chip", when(false, "chip-on"), size))      // class="chip lg"
div(attrs().cls("chip").classIf(active, "chip-on"))     // the same, on an attrs() chain
```

Since 3.0.2 `cls("card")` returns a `jweb.Cls` and `id("x")` a `jweb.Id`: a handle that is
also a selector and a JavaScript target, so a stylesheet rule and a script name the same
thing the element does. Declare it once and pass it everywhere:

```java
Cls chip = cls("chip");
Cls on = cls("chip-on");
boolean active = true;

div(chip, when(active, on))                              // class="chip chip-on"
div(classes(chip, when(active, on)))                     // the same, joined
div(attrs().cls(chip).classIf(active, on))               // on an attrs() chain
stylesheet().rule(chip.hover(), style().opacity(0.9));   // .chip:hover
dom(chip).addClass("seen");                              // the JS DSL
```

### Inline styles

```java
div(style().padding(SP_4).color(TEXT), "hi")              // bare builder as an argument
div(cls("card"), id("hero"), style().margin(zero), p("content"))

div(attrs().cls("card").style(s -> s.display(flex).gap(rem(1))).id("main"), ...)  // in a chain
```

There is no `attrs().style()` starter and no `.done()`: a style is either its own element
argument or a lambda inside an `attrs()` chain.

### Builder shortcuts that replace quoted strings

```java
metaCharset()                      // <meta charset="UTF-8">
metaViewport()                     // the standard responsive viewport tag
css("/app.css")                    // <link rel="stylesheet" href="/app.css">

// SVG line icons (viewBox from ints)
svg(viewBox(0, 0, 24, 24), attrs().width(24).height(24),
    path(d("M9 21H5a2...")))
```

## SVG

The whole SVG element set is on `jweb.El`:

```
svg  g  defs  symbol  use  path  rect  circle  ellipse  line  polyline  polygon
pattern  filter  mask  clipPath  image  stop
svgText  tspan  textPath
svgLinearGradient  svgRadialGradient
animate  animateTransform  animateMotion
feGaussianBlur  feDropShadow  feColorMatrix
```

Three carry an `svg` prefix because the plain name is taken by something authors reach
for more often under the four wildcard imports: `text(...)` is the text node, and
`linearGradient(...)` / `radialGradient(...)` are CSS values.

```java
svg(viewBox(0, 0, 100, 50),
    defs(svgLinearGradient(id("fade"),
        stop(attrs().set("offset", "0%").set("stop-color", "#6366f1")),
        stop(attrs().set("offset", "100%").set("stop-color", "#ec4899")))),
    rect(attrs().width(100).height(50), fill("url(#fade)")),
    svgText(attrs().x("50").y("30").set("text-anchor", "middle"), "JWeb"))
```

## Forms: a record is the form

There is one form system, `jweb.Form`, and it starts from a record. The record declares
the fields and their rules; the same record renders the form, validates the submission,
and comes back as a typed value.

```java
public record Contact(
    @Form.Required String name,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Multiline(rows = 4) String message) {}
```

```java
record Contact(
    @Form.Required String name,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Multiline(rows = 4) String message) {}

form(Contact.class)
    .id("contact-form")
    .action("/contact/submit")                    // works with JavaScript off
    .swapForm("/contact/submit", "#form-status")  // progressive fragment swap
    .field("email", f -> f.placeholder("you@example.com").autocomplete("email"))
    .submit("Send message")
```

That renders a labelled control per component, the CSRF hidden field taken from the
current request (no token passing), and a submit button.

The Java type picks the control:

| component type | control |
| --- | --- |
| `String` | `<input type="text">` |
| `@Form.Email String` | `<input type="email">` + email validation |
| `@Form.Password String` | `<input type="password">` |
| `@Form.Multiline String` | `<textarea>` |
| `int`, `long`, `Integer`, `Long` | `<input type="number">` |
| `double`, `float`, `BigDecimal` | `<input type="number" step="any">` |
| `boolean`, `Boolean` | `<input type="checkbox">` |
| an enum | `<select>` over its constants |
| `LocalDate` / `LocalTime` / `LocalDateTime` | `date` / `time` / `datetime-local` |
| `UploadedFile` | `<input type="file">`, and the form becomes `multipart/form-data` |

Hints: `@Form.Required`, `@Form.Email`, `@Form.Password`, `@Form.Multiline(rows = 4)`,
`@Form.Label("Your email")`, `@Form.Length(min = 3, max = 40)`.

The token comes from the request being handled, so a page that renders a form takes no
arguments. Pass one explicitly with `.csrf(token)` when the form is built outside a
request.

### Binding and errors

```java
record Contact(
    @Form.Required String name,
    @Form.Required @Form.Email String email,
    @Form.Required @Form.Multiline(rows = 4) String message) {}
class ContactStore { void save(Contact c) {} }
ContactStore store = new ContactStore();

app.post("/contact/submit", req -> {
    Form.Bound<Contact> submitted = Form.bind(Contact.class, req);
    if (!submitted.ok()) {
        return form(Contact.class)
            .action("/contact/submit")
            .errors(submitted)              // messages + the values the user typed
            .submit("Send message");
    }
    store.save(submitted.value());
    return p("Thanks!");
});
```

`Bound` carries `value()` (the record, or `null`), `errors()` (a `ValidationResult` keyed
by component name), `ok()` and `submitted()` (the raw input). `errors(...)` re-renders
every field with its message, `aria-invalid` on the control, and a summary above the form.
`Form.validate(Contact.class, values)` is the same validation without building the record.

`field(name, f -> ...)` only changes presentation — `label`, `placeholder`, `help`,
`type`, `rows`, `accept`, `autocomplete`, `options`. The rules stay on the record, so the
browser and the server cannot disagree about them.

### Styling

The form emits stable class names: `jweb-form`, `jweb-field`, `jweb-label`,
`jweb-control`, `jweb-help`, `jweb-error`, `jweb-errors`, `jweb-submit`. `Form.styles()`
is a ready-made stylesheet for them — put it in the head once and override what you like.

## Tag instance API

Every element factory returns a `Tag`, which is itself fluent:

```java
record Item(String name) {}
class ItemView implements Template {
    ItemView(Item item) {}
    public Element render() { return li(); }
}
List<Item> list = List.of(new Item("Widget"));
List<Item> users = list;
boolean isAdmin = true;
Tag adminBadge() { return span("Admin"); }

div()
    .addClass("card")
    .data("id", "42")
    .child(h2("Title"))
    .children(list.stream().map(ItemView::new).toList())
    .each(users, u -> li(u.name()))          // iterate on the instance
    .when(isAdmin, () -> adminBadge())       // conditional child
```

For per-element pseudo-classes, put them on the style instead —
`div(style().padding(px(16)).hover(style().backgroundColor(hex("#f5f5f5"))))` generates a
content-hashed class and puts the rule in the page's stylesheet (see CSS DSL doc). The old
`Tag.styled()/.hover()/.focus()/.active()`, which emitted a `<style>` block next to the
element under a counter-based `jweb-N` class, are gone in 3.0.

## Templates are elements

A `Template` is an `Element`, so components drop straight into a tree — no `.render()`:

```java
class Head implements Template {
    Head(String title) {}
    public Element render() { return tag("head"); }
}
class Nav implements Template { public Element render() { return nav(); } }
class Footer implements Template { public Element render() { return footer(); } }
String title = "Page";
Element content = div();

html(new Head(title), body(new Nav(), main(content), new Footer()))
```

## Modern HTML5 Elements

```java
// Dialog (modal) — the openDialog/closeDialog family on El returns Actions
dialog(id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure?"),
    button(onClick(closeDialog("confirm-dialog")), "Cancel"),
    button(onClick(closeDialog("confirm-dialog", "confirmed")), "Confirm")
)
button(onClick(openDialog("confirm-dialog")), "Open Dialog")

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

`<dialog>`: `openDialog(id)` (modal), `closeDialog(id)`, `closeDialog(id, returnValue)`,
`toggleDialog(id)`. `<details>`: `openDetails(id)`, `closeDetails(id)`,
`toggleDetails(id)`. All seven are Actions on `jweb.El` — the name says `Dialog`/`Details`
because `jweb.Js` already owns `show`, `hide`, `toggle` and `showModal` for plain elements.

## Popover API

```java
div(popover("auto"), id("tips"), p("Closes when clicking outside"))
div(popover("manual"), id("pinned"), p("Stays until explicitly closed"))
button(popovertarget("tips"), "Toggle tips")
button(popovertarget("tips"), popovertargetaction("show"), "Show")

// Actions, for when a button isn't the trigger — a selective import: the
// module's own popover(String) would otherwise collide with jweb.El's
import static jweb.el.PopoverElements.showPopover;
import static jweb.el.PopoverElements.hidePopover;
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

figure(cls("code-example"),
    pre(code("const x = 42;")),
    figcaption("Example: Variable declaration")
)

p("The ", abbr(attr("title", "HyperText Markup Language"), "HTML"), " specification")
p("Press ", kbd("Ctrl"), "+", kbd("C"), " to copy.")
p("Search results for: ", mark("JWeb framework"))
p("H", sub("2"), "O")
p("E = mc", sup("2"))
p(del("old price: $20"), " ", ins("new price: $15"))
blockquote(attrs().cite("https://example.com/source"), p("Quoted text with a cite URL"))
```

## Form markup outside a record

When the fields are not a record — a search box, a filter bar, a datalist — the form
elements are elements like any other.

```java
input(attrs().list("browsers")),
datalist(id("browsers"),
    option("Chrome"),                  // text — the browser uses it as the value too
    option("Firefox"),
    option("Safari")
)

select(name("car"),
    optgroup(attrs().label("Swedish Cars"),
        option(value("volvo"), "Volvo"),
        option(value("saab"), "Saab")),
    optgroup(attrs().label("German Cars"),
        option(value("bmw"), "BMW"),
        option(value("audi"), "Audi"))
)

fieldset(
    legend("Personal Information"),
    label(for_("name"), "Name:"),
    input(type("text"), name("name"), id("name"))
)

// Every other input is the element with its attributes — there is no second spelling
input(type("color"), name("theme-color"), id("theme-color"), value("#3b82f6"))
input(type("date"), name("event"), id("event"), attrs().min("2026-01-01").max("2026-12-31"))
input(type("range"), name("volume"), id("volume"), attrs().min(0).max(100).value(50))
```

## Conditional Rendering

`when(condition, element)` renders the element or nothing. `when(condition, ifTrue,
ifFalse)` is the same call one argument longer — so the predicate is never written twice,
negated. Branches take an `Element`, a `Supplier` lambda (only the taken branch is built),
or a String (text):

```java
class User { String getName() { return "Ada"; } }
boolean isLoggedIn = true;
User user = new User();
int unread = 3;
Element userMenu() { return div(); }
Element loginButton() { return button("Log in"); }

when(isLoggedIn, () -> span("Welcome, " + user.getName()))   // one branch
when(isLoggedIn, userMenu(), loginButton())                  // two
when(unread > 0, unread + " new", "All caught up")           // Strings are text
```

When a branch is long enough that the argument list stops reading, the chain says the same
thing down the page. A chain without `otherwise(...)` is still an element:

```java
boolean active = true;
String name = "java";

when(active)
    .then(span(cls("chip chip-on"), name))
    .otherwise(a(href("/tag/" + name), cls("chip"), name))
```

Several branches are what Java's `switch` expression is for:

```java
enum Role { ADMIN, MODERATOR, GUEST }
Role role = Role.GUEST;
Element adminPanel() { return div("Admin"); }
Element modPanel() { return div("Moderator"); }
Element guestPanel() { return div("Guest"); }

Element panel = switch (role) {
    case ADMIN -> adminPanel();
    case MODERATOR -> modPanel();
    default -> guestPanel();
};
```

`null` children render nothing, so a conditional branch never needs a placeholder.
`match(cond(...), otherwise(...))`, the `.elif(...)` chain and `Tag.ifElse` are gone in
3.0 — see [Migrating to 3.0](./../dsl-simplification-3.md).

## Collection Iteration & Fragments

```java
class ListUser {
    String getName() { return "Ada"; }
    String getEmail() { return "ada@example.com"; }
}
List<ListUser> users = List.of(new ListUser());

ul(each(users, user ->
    li(cls("user-item"),
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
class RiskyComponent { Element render() { return div(); } }
RiskyComponent riskyComponent = new RiskyComponent();

errorBoundary(() -> riskyComponent.render(),
              error -> p("Error: " + error.getMessage()))
tryCatch(() -> riskyComponent.render())   // silent empty fallback

import jweb.ErrorBoundary;
ErrorBoundary.of((Supplier<Element>) () -> riskyComponent.render())
    .fallback(err -> div(cls("error"), p(err.getMessage())))
    .onError(err -> System.err.println("render failed: " + err.getMessage()));
```

## Layout mixins (`jweb.Css`)

The old `jweb.Layout` — 45 element-returning wrappers nothing used — is gone in 3.0.
Layout is a style, not an element, so it composes with everything else a style does:

```java
div(row(SP_4), a, b)                     // flex, centred, gapped
div(stack(SP_4).maxWidth(px(500)), …)
div(center().flex(1), …)
nav(cluster(SP_2).justifyContent(spaceBetween), …)
div(container(px(1200)).padding(SP_8, GUTTER), …)
div(autoGrid(px(240), SP_4), cards)      // as many columns as fit
span(truncate(2), longText)
span(srOnly(), "Skip to content")
img(src("/hero.jpg"), cover())
```

See [the CSS DSL](./css-dsl.md#layout-mixins) for the full list.

## Raw content & custom tags

```java
raw("<b>trusted html</b>")           // no escaping, use with care
                                     // (a bare String is already escaped text;
                                     //  text("…") is deprecated)
tag("custom-element", attrs().set("prop", "x"), span("child"))

// Full-response raw payloads (from route handlers):
Response.ok().contentType("application/json").body("{\"ok\":true}")   // application/json response
Response.html("<h1>hi</h1>")                                          // text/html response
```
