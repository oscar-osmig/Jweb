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
 * import static jweb.Layout.*;  // layout primitives
 * import static jweb.Input.*;   // typed input helpers
 * import static jweb.Mongo.*;   // MongoDB access
 * }</pre>
 *
 * <h2>Types</h2>
 * <pre>{@code
 * import jweb.Element;   // what components return
 * import jweb.Template;  // what pages implement
 * import jweb.Style;     // what style helpers return
 * import jweb.CSSValue;  // what color/unit helpers return
 * import jweb.JWeb;      // the app builder
 * import jweb.JWebRoutes;// where you configure routes
 * }</pre>
 *
 * <p>The legacy {@code com.osmig.Jweb.framework.*} imports keep working —
 * every class here extends its legacy counterpart — but new code should use
 * these.</p>
 */
package jweb;
