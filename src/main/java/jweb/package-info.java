/**
 * JWeb's short-import surface. One rule to remember: everything you import
 * lives in {@code jweb}.
 *
 * <h2>Static DSL imports</h2>
 * <pre>{@code
 * import static jweb.El.*;      // HTML elements, attributes, conditionals
 * import static jweb.Css.*;     // styles, units, colors, media queries
 * import static jweb.Js.*;      // client scripting + reactive runtime
 * import static jweb.Actions.*; // declarative event actions
 * import static jweb.State.*;   // server-driven state hooks
 * import static jweb.UI.*;      // prebuilt components
 * import static jweb.Mongo.*;   // MongoDB access
 * }</pre>
 *
 * <h2>Types</h2>
 * <pre>{@code
 * import jweb.Element;   // what components return
 * import jweb.Template;  // what pages implement
 * import jweb.Tag;       // what every element factory returns
 * import jweb.Style;     // what style helpers return
 * import jweb.CSSValue;  // what color/unit helpers return
 * import jweb.Action;    // what every JS-emitting helper returns
 * import jweb.Val;       // a JS expression
 * import jweb.Request;   // what handlers receive
 * import jweb.JWeb;      // the app builder
 * import jweb.JWebRoutes;// where you configure routes
 * }</pre>
 *
 * <p>The short names are the real names: every type the framework returns
 * or you implement is declared here (or in {@code jweb.css} / {@code jweb.js} /
 * {@code jweb.three}), so an IDE never needs to auto-import from the legacy
 * package. The legacy {@code com.osmig.Jweb.framework.*} names keep compiling
 * as {@code @Deprecated} aliases, but new code should use these.</p>
 */
package jweb;
