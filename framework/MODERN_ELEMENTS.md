# Modern HTML Elements Guide

This document provides comprehensive examples for using modern HTML5 elements in the JWeb framework.

---

## Table of Contents

1. [Dialog Element](#dialog-element)
2. [Details/Summary Elements](#detailssummary-elements)
3. [Meter Element](#meter-element)
4. [Progress Element](#progress-element)
5. [Template Element](#template-element)
6. [Slot Element](#slot-element)
7. [Output Element](#output-element)
8. [Data/Time Elements](#datatime-elements)
9. [Text Direction Elements](#text-direction-elements)
10. [Ruby Annotation](#ruby-annotation)

---

## Dialog Element

The `<dialog>` element represents a modal or non-modal dialog box.

### Basic Dialog

```java
import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.DialogHelper.*;

// Define dialog
dialog(attrs().id("confirm-dialog"),
    h2("Confirm Action"),
    p("Are you sure you want to proceed?"),
    div(
        button(attrs().onclick(close("confirm-dialog")), "Cancel"),
        button(attrs()
            .onclick(close("confirm-dialog", "confirmed"))
            .class_("primary"),
            "Confirm")
    )
)

// Button to open dialog
button(attrs().onclick(showModal("confirm-dialog")), "Open Dialog")
```

### Dialog with Return Value

```java
// Dialog
dialog(attrs().id("input-dialog").onClose(e -> {
    String returnValue = getReturnValue("input-dialog");
    // Handle return value
}),
    h3("Enter Value"),
    input(attrs().id("dialog-input").type("text")),
    button(attrs().onclick(close("input-dialog", "submitted")), "Submit")
)
```

### Close on Backdrop Click

```java
dialog(attrs()
    .id("modal")
    .onclick(closeOnBackdropClick("modal")),
    div(class_("dialog-content"),
        h2("Modal Dialog"),
        p("Click outside to close")
    )
)
```

### Dialog Events

```java
dialog(attrs()
    .id("event-dialog")
    .onCancel(e -> log("Dialog cancelled"))
    .onClose(e -> log("Dialog closed")),
    p("Dialog with events")
)
```

---

## Details/Summary Elements

Create collapsible disclosure widgets (accordions).

### Basic Details

```java
import static com.osmig.Jweb.framework.elements.El.*;

details(
    summary("Click to expand"),
    p("Hidden content that can be toggled")
)
```

### Open by Default

```java
details(attrs().open(),
    summary("Already expanded"),
    p("This content is visible by default")
)
```

### Programmatic Control

```java
import static com.osmig.Jweb.framework.elements.DetailsHelper.*;

// Details element
details(attrs().id("faq-1"),
    summary("What is JWeb?"),
    p("JWeb is a pure Java web framework...")
)

// Control buttons
button(attrs().onclick(open("faq-1")), "Expand")
button(attrs().onclick(close("faq-1")), "Collapse")
button(attrs().onclick(toggle("faq-1")), "Toggle")
```

### Exclusive Accordion

```java
// Multiple details with same name = exclusive accordion
details(attrs().id("faq-1").name("faq"),
    summary("Question 1"),
    p("Answer 1")
)

details(attrs().id("faq-2").name("faq"),
    summary("Question 2"),
    p("Answer 2")
)

details(attrs().id("faq-3").name("faq"),
    summary("Question 3"),
    p("Answer 3")
)

// Control all
button(attrs().onclick(closeAll("faq")), "Collapse All")
button(attrs().onclick(openAll("faq")), "Expand All")
```

### Toggle Event

```java
details(attrs()
    .id("tracked")
    .onToggle(e -> {
        boolean isOpen = e.target().getProperty("open");
        log("Details " + (isOpen ? "opened" : "closed"));
    }),
    summary("Track state changes"),
    p("Content")
)
```

---

## Meter Element

Display scalar measurements within a known range.

### Basic Meter

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Simple meter with value/min/max
meter(0.6, 0, 1)

// With attributes
meter(attrs()
    .value(70)
    .min(0)
    .max(100)
    .low(30)
    .high(80)
    .optimum(50))
```

### Styled Meter Examples

```java
// Disk usage
label("Disk Usage:"),
meter(attrs()
    .value(85)
    .max(100)
    .low(60)
    .high(90)
    .optimum(50)),
text(" 85%")

// Battery level
label("Battery:"),
meter(attrs()
    .value(40)
    .max(100)
    .low(20)
    .high(70)
    .optimum(90)),
text(" 40%")
```

---

## Progress Element

Show completion progress of a task.

### Determinate Progress

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Simple progress
progress(70, 100)

// With attributes
progress(attrs()
    .value(45)
    .max(100)
    .id("upload-progress"))

label("Uploading: ", progress(attrs().value(30).max(100)), " 30%")
```

### Indeterminate Progress

```java
// No value = indeterminate (loading spinner)
progressIndeterminate()

// Or with attrs
progress(attrs().max(100).class_("loading"))
```

### Dynamic Progress

```java
// Progress that updates via JavaScript
progress(attrs()
    .id("task-progress")
    .value(0)
    .max(100))

button(attrs().onclick("document.getElementById('task-progress').value += 10"),
    "Increment")
```

---

## Template Element

Define reusable HTML fragments that aren't rendered initially.

### Basic Template

```java
import static com.osmig.Jweb.framework.elements.El.*;

template(attrs().id("card-template"),
    div(class_("card"),
        h3(class_("card-title")),
        p(class_("card-body"))
    )
)
```

### Using Template with JavaScript

```java
// Define template
template(attrs().id("user-card"),
    div(class_("user-card"),
        img(attrs().class_("avatar").src("").alt("")),
        h4(class_("name")),
        p(class_("bio"))
    )
)

// Clone and use via JavaScript
inlineScript("""
    const template = document.getElementById('user-card');
    const clone = template.content.cloneNode(true);
    clone.querySelector('.name').textContent = 'John Doe';
    document.body.appendChild(clone);
""")
```

### Declarative Shadow DOM

```java
div(
    template(attrs().shadowrootmode("open"),
        style("""
            :host { display: block; }
            .content { color: blue; }
        """),
        slot()
    ),
    text("Content goes here")
)
```

---

## Slot Element

Placeholder in web components for user-provided content.

### Named Slots

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Component template
template(attrs().id("custom-card"),
    div(class_("card"),
        div(class_("card-header"),
            slot("header")
        ),
        div(class_("card-body"),
            slot() // default slot
        ),
        div(class_("card-footer"),
            slot("footer")
        )
    )
)
```

---

## Output Element

Represents the result of a calculation or user action.

### Calculator Example

```java
import static com.osmig.Jweb.framework.elements.El.*;

form(attrs().oninput("result.value = parseInt(a.value) + parseInt(b.value)"),
    input(attrs().type("number").id("a").name("a").value("0")),
    text(" + "),
    input(attrs().type("number").id("b").name("b").value("0")),
    text(" = "),
    output(attrs()
        .name("result")
        .set("for", "a b"))
)
```

---

## Data/Time Elements

Provide machine-readable semantic data.

### Time Element

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Basic time
time("Content")

// With datetime attribute
timeWithDatetime("2026-01-21", "January 21, 2026")

// Full datetime
timeWithDatetime("2026-01-21T14:30:00", "Today at 2:30 PM")

// Duration
time(attrs().datetime("PT2H30M"), "2 hours 30 minutes")
```

### Data Element

```java
// Product ID
data("12345", "Premium Widget")

// With attributes
data(attrs()
    .value("SKU-789")
    .class_("product-code"),
    "Product ABC")
```

---

## Text Direction Elements

Handle bidirectional text properly.

### BDI (Bidirectional Isolation)

```java
import static com.osmig.Jweb.framework.elements.El.*;

// Isolate username that might be in different direction
p("User ", bdi(username), " posted this comment")

// List with mixed text directions
ul(
    li(bdi("English Name")),
    li(bdi("اسم عربي")),
    li(bdi("עברית"))
)
```

### BDO (Bidirectional Override)

```java
// Force right-to-left
bdo(attrs().set("dir", "rtl"), "This text flows right to left")

// Force left-to-right
bdo(attrs().set("dir", "ltr"), "This text flows left to right")
```

---

## Ruby Annotation

Provide pronunciation or translation for East Asian typography.

### Basic Ruby

```java
import static com.osmig.Jweb.framework.elements.El.*;

ruby(
    text("漢"),
    rp("("),
    rt("kan"),
    rp(")")
)
```

### Multiple Characters

```java
ruby(
    text("明日"),
    rp("("),
    rt("Ashita"),
    rp(")")
)
```

### Complex Ruby

```java
p(
    text("I study "),
    ruby(
        text("日本語"),
        rp("("),
        rt("Nihongo"),
        rp(")")
    ),
    text(" every day.")
)
```

---

## Complete Example: Feature Showcase Page

```java
package com.example.pages;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;

import static com.osmig.Jweb.framework.elements.El.*;
import static com.osmig.Jweb.framework.elements.DialogHelper.*;
import static com.osmig.Jweb.framework.elements.DetailsHelper.*;

public class ModernElementsDemo implements Template {

    @Override
    public Element render() {
        return div(class_("container"),
            h1("Modern HTML Elements Demo"),

            // Dialog Section
            section(
                h2("Dialog"),
                button(attrs().onclick(showModal("demo-dialog")), "Open Dialog"),
                dialog(attrs().id("demo-dialog"),
                    h3("Modal Dialog"),
                    p("This is a modal dialog with backdrop"),
                    button(attrs().onclick(close("demo-dialog")), "Close")
                )
            ),

            // Accordion Section
            section(
                h2("Accordion"),
                details(attrs().name("demo-accordion").open(),
                    summary("Section 1"),
                    p("Content of section 1")
                ),
                details(attrs().name("demo-accordion"),
                    summary("Section 2"),
                    p("Content of section 2")
                ),
                button(attrs().onclick(closeAll("demo-accordion")), "Collapse All")
            ),

            // Progress Section
            section(
                h2("Progress & Meter"),
                label("Progress: ", progress(65, 100)),
                br(),
                label("Meter: ", meter(0.7, 0, 1))
            ),

            // Data Section
            section(
                h2("Semantic Data"),
                p("Posted on ", timeWithDatetime("2026-01-21", "January 21, 2026")),
                p("Product ", data("SKU-123", "Widget"), " is available")
            )
        );
    }
}
```

---

## CSS Backdrop Styling

For dialog backdrop styling, use the `::backdrop` pseudo-element in CSS:

```java
style("""
    dialog::backdrop {
        background: rgba(0, 0, 0, 0.5);
        backdrop-filter: blur(3px);
    }

    dialog {
        border: none;
        border-radius: 8px;
        padding: 2rem;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
""")
```

---

## Browser Support Notes

- **Dialog**: Widely supported in modern browsers
- **Details/Summary**: Excellent support across all modern browsers
- **Meter/Progress**: Well supported with fallback as plain text
- **Template/Slot**: For web components, requires shadow DOM support
- **Ruby**: Best support in East Asian locale browsers
- **Popover API**: New feature, check browser compatibility

For older browsers, consider feature detection and polyfills where necessary.
