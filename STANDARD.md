# JWeb Development Standards

A guide to building maintainable, readable, and consistent applications with JWeb.

---

## Table of Contents

1. [File Length](#1-file-length)
2. [Code Readability](#2-code-readability)
3. [Separation of Concerns](#3-separation-of-concerns)
4. [Avoid Raw HTML, CSS, and JavaScript](#4-avoid-raw-html-css-and-javascript)
5. [Follow the Java DSL](#5-follow-the-java-dsl)
6. [Keep It Simple](#6-keep-it-simple)

---

## 1. File Length

Keep files under **100 lines**.

Going a few lines over is acceptable, but minimizing file length helps maintain readability and makes the codebase easier to navigate over time.

If a file grows too large, consider splitting it into smaller, focused components.

---

## 2. Code Readability

Write coherent, self-explanatory code.

Your code should be understandable by you and anyone else who reads it:

- Use meaningful variable and method names
- Maintain consistent formatting
- Structure code logically
- Avoid unnecessary complexity

---

## 3. Separation of Concerns

Organize your code by responsibility:

| Type | Purpose |
|------|---------|
| **Pages** | Handle routing and page structure |
| **Components** | Reusable UI pieces |
| **APIs** | Data handling and business logic |
| **Scripts** | Client-side behavior |
| **Themes** | Styling constants and design tokens |

Each file should have a single, clear purpose.

---

## 4. Avoid Raw HTML, CSS, and JavaScript

Always prefer JWeb's Java DSL over raw markup:

- Use `Elements.*` for HTML
- Use `CSS.*` and `Style` for styling
- Use `JS.*` for JavaScript

When something is too complex to accomplish with JWeb's current capabilities, using `safeRaw()` or `unsafeRaw()` is acceptable. However, minimize raw usage as it breaks the core conviction of JWeb:

> **Pure Java, full-stack development.**

---

## 5. Follow the Java DSL

Embrace JWeb's type-safe DSLs:

### HTML

```java
div(class_("card"),
    h2("Title"),
    p("Content")
)
```

### CSS

```java
attrs().style(s -> s
    .display(flex)
    .padding(rem(1))
    .backgroundColor(white))
```

### JavaScript

```java
script()
    .const_("btn", getElem("submit"))
    .add(func("handleClick").log(str("clicked")))
    .build()
```

---

## 6. Keep It Simple

Do not over-complicate things.

Choose the straightforward solution. If a feature can be built simply, build it simply. Avoid premature optimization and unnecessary abstractions.

---

*Following these standards ensures a consistent, maintainable codebase that stays true to JWeb's philosophy.*
