/**
 * JWeb Framework - A type-safe, builder-pattern web framework for Java.
 *
 * <h2>Overview</h2>
 * <p>JWeb provides fluent DSLs for HTML, CSS, and JavaScript generation, eliminating the need
 * to write raw markup or scripts. All APIs use builder patterns with method chaining.</p>
 *
 * <h2>Quick Start</h2>
 * <pre>
 * import static com.osmig.Jweb.framework.elements.Elements.*;
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 * import static com.osmig.Jweb.framework.js.JS.*;
 *
 * // Create a styled button with event handler
 * Tag button = button(
 *     attrs()
 *         .class_("btn", "btn-primary")
 *         .onClick(e -&gt; System.out.println("Clicked!")),
 *     "Click Me"
 * ).style(style()
 *     .padding(px(10), px(20))
 *     .backgroundColor(blue)
 *     .color(white)
 *     .borderRadius(px(4))
 * );
 * </pre>
 *
 * <h2>Naming Conventions</h2>
 *
 * <h3>Java Reserved Words</h3>
 * <p>When a method name would conflict with a Java reserved word, an underscore suffix is added:</p>
 * <ul>
 *   <li>{@code class_()} - for CSS class attribute (Java keyword: class)</li>
 *   <li>{@code for_()} - for label's for attribute (Java keyword: for)</li>
 *   <li>{@code var_()} - for JavaScript var declaration (Java keyword: var)</li>
 *   <li>{@code default_()} - for default keyword (Java keyword: default)</li>
 *   <li>{@code new_()} - for new keyword (Java keyword: new)</li>
 *   <li>{@code if_()} - for if statement (Java keyword: if)</li>
 *   <li>{@code else_()} - for else statement (Java keyword: else)</li>
 *   <li>{@code while_()} - for while loop (Java keyword: while)</li>
 *   <li>{@code try_()} - for try block (Java keyword: try)</li>
 *   <li>{@code catch_()} - for catch block (Java keyword: catch)</li>
 *   <li>{@code switch_()} - for switch statement (Java keyword: switch)</li>
 *   <li>{@code case_()} - for case clause (Java keyword: case)</li>
 *   <li>{@code return_/ret()} - for return statement (Java keyword: return)</li>
 *   <li>{@code null_()} - for null value (Java keyword: null)</li>
 *   <li>{@code this_()} - for this reference (Java keyword: this)</li>
 * </ul>
 *
 * <p>CSS keywords that conflict with Java:</p>
 * <ul>
 *   <li>{@code static_} - for position: static (Java keyword: static)</li>
 *   <li>{@code double_} - for border-style: double (Java keyword: double)</li>
 * </ul>
 *
 * <h3>CamelCase Methods</h3>
 * <p>All multi-word methods use camelCase:</p>
 * <ul>
 *   <li>HTML: {@code onClick()}, {@code onSubmit()}, {@code onKeyDown()}</li>
 *   <li>CSS: {@code backgroundColor()}, {@code fontSize()}, {@code gridTemplateColumns()}</li>
 *   <li>JS: {@code forEach()}, {@code addClass()}, {@code setAttribute()}</li>
 * </ul>
 *
 * <h3>Unsafe Methods</h3>
 * <p>Methods that bypass type safety are prefixed with "unsafe":</p>
 * <ul>
 *   <li>{@code unsafeHtml()} - add raw HTML without escaping</li>
 *   <li>{@code unsafeRaw()} - add raw JavaScript without validation</li>
 *   <li>{@code unsafeProp()} - set CSS property with raw string value</li>
 * </ul>
 *
 * <h2>Module Structure</h2>
 *
 * <h3>Elements (HTML DSL)</h3>
 * <ul>
 *   <li>{@link com.osmig.Jweb.framework.elements.Elements} - HTML element factories</li>
 *   <li>{@link com.osmig.Jweb.framework.elements.SVGElements} - SVG element factories</li>
 *   <li>{@link com.osmig.Jweb.framework.elements.Tag} - HTML tag builder</li>
 *   <li>{@link com.osmig.Jweb.framework.attributes.Attributes} - Attribute builder</li>
 * </ul>
 *
 * <h3>Styles (CSS DSL)</h3>
 * <ul>
 *   <li>{@link com.osmig.Jweb.framework.styles.CSS} - CSS utilities and rule builder</li>
 *   <li>{@link com.osmig.Jweb.framework.styles.Style} - CSS property builder</li>
 *   <li>{@link com.osmig.Jweb.framework.styles.CSSUnits} - Unit factories (px, rem, percent, etc.)</li>
 *   <li>{@link com.osmig.Jweb.framework.styles.CSSColors} - Color constants and functions</li>
 *   <li>{@link com.osmig.Jweb.framework.styles.CSSGrid} - Grid template functions</li>
 * </ul>
 *
 * <h3>JavaScript (JS DSL)</h3>
 * <ul>
 *   <li>{@link com.osmig.Jweb.framework.js.JS} - JavaScript builder</li>
 *   <li>{@link com.osmig.Jweb.framework.js.JS.Script} - Top-level script builder</li>
 *   <li>{@link com.osmig.Jweb.framework.js.JS.Func} - Function builder</li>
 *   <li>{@link com.osmig.Jweb.framework.js.JS.Val} - Value/expression builder</li>
 *   <li>{@link com.osmig.Jweb.framework.js.JS.El} - DOM element accessor</li>
 * </ul>
 *
 * <h2>Static Imports</h2>
 * <p>For the best developer experience, use static imports:</p>
 * <pre>
 * // HTML
 * import static com.osmig.Jweb.framework.elements.Elements.*;
 * import static com.osmig.Jweb.framework.elements.SVGElements.*;
 *
 * // CSS
 * import static com.osmig.Jweb.framework.styles.CSS.*;
 * import static com.osmig.Jweb.framework.styles.CSSUnits.*;
 * import static com.osmig.Jweb.framework.styles.CSSColors.*;
 * import static com.osmig.Jweb.framework.styles.CSSGrid.*;
 *
 * // JavaScript
 * import static com.osmig.Jweb.framework.js.JS.*;
 * </pre>
 *
 * <h2>Event Handling</h2>
 * <p>Event handlers use Java lambda expressions for type safety:</p>
 * <pre>
 * button("Submit").onClick(event -&gt; {
 *     // Handle click in Java
 *     System.out.println("Button clicked!");
 * })
 *
 * // Or with attributes builder
 * input(attrs()
 *     .type("text")
 *     .onChange(e -&gt; validateInput(e.getValue()))
 * )
 * </pre>
 *
 * <h2>CSS Grid Example</h2>
 * <pre>
 * import static com.osmig.Jweb.framework.styles.CSSGrid.*;
 *
 * // Responsive grid layout
 * style()
 *     .display(grid)
 *     .gridTemplateColumns(repeat(autoFill(), minmax(px(250), fr(1))))
 *     .gap(rem(1.5))
 * </pre>
 *
 * <h2>JavaScript DSL Example</h2>
 * <pre>
 * // Create a function with loops and DOM manipulation
 * Func updateList = func("updateList", "items")
 *     .let_("container", getElem("list"))
 *     .call("container.innerHTML", "")
 *     .forOf("item", variable("items"))
 *         .body(
 *             getElem("list").appendChild(
 *                 call("document.createElement", "li")
 *             )
 *         )
 *     .endFor();
 * </pre>
 *
 * @see com.osmig.Jweb.framework.elements.Elements
 * @see com.osmig.Jweb.framework.styles.CSS
 * @see com.osmig.Jweb.framework.js.JS
 */
package com.osmig.Jweb.framework;
